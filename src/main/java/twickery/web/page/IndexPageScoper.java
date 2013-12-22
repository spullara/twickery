package twickery.web.page;

import com.google.common.base.Function;
import redis.clients.jedis.Jedis;
import twickery.web.Twickery;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;

public class IndexPageScoper extends BasePageScoper {
  public Object newScope(HttpServletRequest httpServletRequest, final Matcher matcher) {
    final String twitterId = getTwitterId(httpServletRequest);
    if (twitterId == null) {
      return new IndexPage(null);
    } else {
      final User user = Twickery.redis(new Function<Jedis, User>() {
        public User apply(Jedis jedis) {
          final Twitter t = Twickery.twitter();
          final String oauth_token = jedis.hget("twitter:uid:" + twitterId, "oauth_token");
          final String oauth_token_secret = jedis.hget("twitter:uid:" + twitterId,
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
      return new IndexPage(user);
    }
  }

}
