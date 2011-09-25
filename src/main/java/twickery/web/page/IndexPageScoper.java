package twickery.web.page;

import java.util.regex.Matcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Function;

import com.sampullara.mustache.Scope;
import redis.clients.jedis.Jedis;
import twickery.web.Scoper;
import twickery.web.Twickery;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class IndexPageScoper implements Scoper<Matcher> {
  public Scope newScope(HttpServletRequest httpServletRequest, final Matcher matcher) {
    String twitter = null;
    final Cookie[] cookies = httpServletRequest.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("twitter")) {
          twitter = Twickery.decode(cookie.getValue());
        }
      }
    }
    if (twitter == null) {
      return new Scope(new IndexPage(null));
    } else {
      final String finalTwitter = twitter;
      final User user = Twickery.redis(new Function<Jedis, User>() {
        public User apply(Jedis jedis) {
          final Twitter t = Twickery.twitter();
          final String oauth_token = jedis.hget("twitter:uid:" + finalTwitter, "oauth_token");
          final String oauth_token_secret = jedis.hget("twitter:uid:" + finalTwitter,
                  "oauth_token_secret");
          if (oauth_token == null || oauth_token_secret == null) {
            return null;
          }
          t.setOAuthAccessToken(new AccessToken(oauth_token, oauth_token_secret));
          try {
            return t.verifyCredentials();
          } catch (TwitterException e) {
            return null;
          }
        }
      });
      return new Scope(new IndexPage(user));
    }
  }
}
