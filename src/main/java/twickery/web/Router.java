package twickery.web;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;

import com.sampullara.mustache.Scope;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import twickery.web.code.StatusCode;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

/**
 * Routes all requests
 */
public class Router extends HttpServlet {

  interface Handler<T> {
    void handle(HttpServletRequest request, HttpServletResponse response, T t) throws ServletException;
  }

  interface Scoper<T> {
    Scope newScope(HttpServletRequest request, T t);
  }

  private void add(RegexMustacheHandler index) {
    handlerMap.put(index, index);
  }

  private Map<Function<HttpServletRequest, Matcher>, Handler<Matcher>> handlerMap =
          new LinkedHashMap<Function<HttpServletRequest, Matcher>, Handler<Matcher>>();

  @Override
  public void init() throws ServletException {
    final JedisPool pool;
    try {
      MappingJsonFactory jf = new MappingJsonFactory();
      JsonParser parser = jf.createJsonParser(new File("/home/dotcloud/environment.json"));
      JsonNode env = parser.readValueAsTree();
      GenericObjectPool.Config config = new GenericObjectPool.Config();
      String host = env.get("DOTCLOUD_DATA_REDIS_HOST").getTextValue();
      int port = parseInt(env.get("DOTCLOUD_DATA_REDIS_PORT").getTextValue());
      String password = env.get("DOTCLOUD_DATA_REDIS_PASSWORD").getTextValue();
      pool = new JedisPool(config, host, port, 60, password);
    } catch (Exception e) {
      throw new ServletException(e);
    }
    System.setProperty("mustache.debug", "true");
    final TwitterFactory tf = new TwitterFactory();
    add(new RegexMustacheHandler("/", "index.html"));
    add(new RegexMustacheHandler("/tweet/([0-9]+)", "tweet.html", new Scoper<Matcher>() {
      public Scope newScope(HttpServletRequest httpServletRequest, final Matcher matcher) {
        final Twitter twitter = tf.getInstance();
        return new Scope(new Object() {
          Object tweet() throws TwitterException {
            return new StatusCode(twitter.showStatus(parseLong(matcher.group(1))));
          }
        });
      }
    }));
    handlerMap.put(new Function<HttpServletRequest, Matcher>() {
              public Matcher apply(HttpServletRequest httpServletRequest) {
                Matcher matcher = Pattern.compile("/twitter/oauth").matcher(
                        httpServletRequest.getPathInfo());
                return matcher.matches() ? matcher : null;
              }
            }, new Handler<Matcher>() {
      public void handle(HttpServletRequest request, HttpServletResponse response, Matcher matcher) throws ServletException {
        final Twitter twitter = tf.getInstance();
        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
        String verifier = request.getParameter("oauth_verifier");
        try {
          AccessToken token = twitter.getOAuthAccessToken(requestToken, verifier);
          request.getSession().removeAttribute("requestToken");
          User user = twitter.verifyCredentials();
          Jedis jedis = pool.getResource();
          try {
            String uid = String.valueOf(user.getId());
            jedis.sadd("twitter:uids", uid);
            jedis.hset("twitter:uid:" + uid, "oauth_token", token.getToken());
            jedis.hset("twitter:uid:" + uid, "oauth_token_secret", token.getTokenSecret());
          } catch (Exception e) {
            pool.returnBrokenResource(jedis);
            jedis = null;
            throw e;
          } finally {
            if (jedis != null) {
              pool.returnResource(jedis);
            }
          }
          response.sendRedirect("http://www.twickery.com/");
        } catch (Exception e) {
          throw new ServletException(e);
        }
      }
    }
    );
    handlerMap.put(new Function<HttpServletRequest, Matcher>() {
              public Matcher apply(HttpServletRequest httpServletRequest) {
                Matcher matcher = Pattern.compile("/twitter/connect").matcher(
                        httpServletRequest.getPathInfo());
                return matcher.matches() ? matcher : null;
              }
            }, new Handler<Matcher>() {
      public void handle(HttpServletRequest request, HttpServletResponse response, Matcher matcher) throws ServletException {
        final Twitter twitter = tf.getInstance();
        RequestToken requestToken;
        try {
          requestToken = twitter.getOAuthRequestToken("http://www.twickery.com/twitter/oauth");
          request.getSession().setAttribute("requestToken", requestToken);
          response.sendRedirect(requestToken.getAuthenticationURL());
        } catch (Exception e) {
          throw new ServletException(e);
        }
      }
    }
    );
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    for (Map.Entry<Function<HttpServletRequest, Matcher>, Handler<Matcher>> handlerEntry : handlerMap.entrySet()) {
      Matcher matcher = handlerEntry.getKey().apply(req);
      if (matcher != null) {
        handlerEntry.getValue().handle(req, resp, matcher);
        return;
      }
    }
    resp.setStatus(404);
  }

}
