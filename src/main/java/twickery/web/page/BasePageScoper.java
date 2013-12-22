package twickery.web.page;

import com.google.common.base.Function;
import redis.clients.jedis.Jedis;
import twickery.web.Scoper;
import twickery.web.Twickery;
import twitter4j.Twitter;
import twitter4j.auth.AccessToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;

public abstract class BasePageScoper implements Scoper<Matcher> {

  protected Twitter getTwitter(final String twitterId) {
    return Twickery.redis(new Function<Jedis, Twitter>() {
          public Twitter apply(Jedis jedis) {
            final Twitter t = Twickery.twitter();
            final String oauth_token = jedis.hget("twitter:uid:" + twitterId, "oauth_token");
            final String oauth_token_secret = jedis.hget("twitter:uid:" + twitterId,
                    "oauth_token_secret");
            if (oauth_token == null || oauth_token_secret == null) {
              return null;
            }
            t.setOAuthAccessToken(new AccessToken(oauth_token, oauth_token_secret));
            return t;
          }
    });
  }

  protected String getTwitterId(HttpServletRequest httpServletRequest) {
    String twitterId = null;
    final Cookie[] cookies = httpServletRequest.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("twitter")) {
          twitterId = Twickery.decode(cookie.getValue());
        }
      }
    }
    return twitterId;
  }
}
