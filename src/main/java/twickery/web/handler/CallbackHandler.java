package twickery.web.handler;

import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;
import twickery.web.Handler;
import twickery.web.Twickery;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
* Created by IntelliJ IDEA.
* User: spullara
* Date: 9/23/11
* Time: 2:55 PM
* To change this template use File | Settings | File Templates.
*/
public class CallbackHandler implements Handler<Matcher> {
  public void handle(HttpServletRequest request, HttpServletResponse response, Matcher matcher) throws ServletException {
    final Twitter twitter = Twickery.twitter();
    RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
    String verifier = request.getParameter("oauth_verifier");
    try {
      final AccessToken token = twitter.getOAuthAccessToken(requestToken, verifier);
      request.getSession().removeAttribute("requestToken");
      final User user = twitter.verifyCredentials();
      Twickery.redis(new Function<Jedis, Void>() {
        public Void apply(Jedis jedis) {
          String uid = String.valueOf(user.getId());
          jedis.sadd("twitter:uids", uid);
          jedis.hset("twitter:uid:" + uid, "oauth_token", token.getToken());
          jedis.hset("twitter:uid:" + uid, "oauth_token_secret", token.getTokenSecret());
          return null;
        }
      });
      response.sendRedirect("http://www.twickery.com/");
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
