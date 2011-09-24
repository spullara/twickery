package twickery.web.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamSource;

import com.google.common.base.Function;
import com.google.common.io.CharStreams;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import redis.clients.jedis.Jedis;
import twickery.web.Handler;
import twickery.web.Twickery;

import twitter4j.Twitter;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class FacebookCallbackHandler implements Handler<Matcher> {
  private static final int _20_YEARS = 365 * 24 * 3600 * 20;
  private static JsonFactory jf = new MappingJsonFactory();
  private static Properties fbprops = new Properties();
  static {
    try {
      fbprops.load(FacebookCallbackHandler.class.getResourceAsStream("/facebook.properties"));
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }
  public void handle(HttpServletRequest request, HttpServletResponse response, Matcher matcher) throws ServletException {
    try {
      String twitterId = null;
      for (Cookie cookie : request.getCookies()) {
        if (cookie.getName().equals("twitter")) {
          twitterId = Twickery.decode(cookie.getValue());
        }
      }

      String client_id = fbprops.getProperty("client_id");
      String client_secret = fbprops.getProperty("client_secret");
      String redirect_uri = URLEncoder.encode("http://www.twickery.com/facebook/oauth", "utf-8");
      String code = URLEncoder.encode(request.getParameter("code"), "utf-8");
      URL url = new URL("https://graph.facebook.com/oauth/access_token" +
              "?client_id=" + client_id +
              "&client_secret=" + client_secret +
              "&redirect_uri=" + redirect_uri +
              "&code=" + code);
      String result = CharStreams.toString(
              new BufferedReader(new InputStreamReader(url.openStream(), "utf-8")));
      String access_token = null;
      for (String pairString : result.split("&")) {
        String[] pair = pairString.split("=");
        if (pair[0].equals("access_token")) {
          access_token = pair[1];
        }
      }
      url = new URL("https://graph.facebook.com/me?access_token=" + URLEncoder.encode(access_token, "utf-8"));
      JsonNode jsonNode = jf.createJsonParser(url).readValueAsTree();
      final String userId = jsonNode.get("id").getValueAsText();
      final String finalTwitterId = twitterId;
      final String finalAccess_token = access_token;
      Twickery.redis(new Function<Jedis, Void>() {
        public Void apply(Jedis jedis) {
          jedis.hset("facebook:uid:" + userId, "twitter", finalTwitterId);
          jedis.hset("facebook:uid:" + userId, "access_token", finalAccess_token);
          jedis.hset("twitter:uid:" + finalTwitterId, "facebook", userId);
          return null;
        }
      });
      String encode = Twickery.hash(Long.parseLong(userId));
      Cookie facebookCookie = new Cookie("facebook", userId + ":" + encode);
      facebookCookie.setMaxAge(_20_YEARS);
      response.addCookie(facebookCookie);
      response.sendRedirect("http://www.twickery.com/");
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
