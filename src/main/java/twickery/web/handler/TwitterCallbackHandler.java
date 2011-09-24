package twickery.web.handler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;
import com.google.common.io.CharStreams;

import redis.clients.jedis.Jedis;
import twickery.web.Handler;
import twickery.web.Twickery;

import twitter4j.Twitter;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterCallbackHandler implements Handler<Matcher> {
  private static final int _20_YEARS = 365 * 24 * 3600 * 20;

  public void handle(HttpServletRequest request, HttpServletResponse response, Matcher matcher) throws ServletException {
    final Twitter twitter = Twickery.twitter();
    RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
    String verifier = request.getParameter("oauth_verifier");
    try {
      if (verifier == null || requestToken == null) {
        response.sendRedirect("http://www.twickery.com/");
        return;
      }
      final AccessToken token = twitter.getOAuthAccessToken(requestToken, verifier);
      request.getSession().removeAttribute("requestToken");
      final User user = twitter.verifyCredentials();
      final long userId = user.getId();
      Twickery.redis(new Function<Jedis, Void>() {
        public Void apply(Jedis jedis) {
          String uid = String.valueOf(userId);
          jedis.sadd("twitter:uids", uid);
          jedis.hset("twitter:uid:" + uid, "oauth_token", token.getToken());
          jedis.hset("twitter:uid:" + uid, "oauth_token_secret", token.getTokenSecret());
          return null;
        }
      });
      String encode = Twickery.hash(userId);
      Cookie twitterCookie = new Cookie("twitter", userId + ":" + encode);
      twitterCookie.setMaxAge(_20_YEARS);
      twitterCookie.setPath("/");
      response.addCookie(twitterCookie);
      response.sendRedirect("http://www.twickery.com/facebook/connect");
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

}
