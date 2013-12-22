package twickery.web.page;

import twitter4j.Twitter;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;

public class UserPageScoper extends BasePageScoper {
  public Object newScope(HttpServletRequest httpServletRequest, final Matcher matcher) {
    String twitterId = matcher.group(1);
    String userId = matcher.group(2);
    Twitter twitter = getTwitter(twitterId);
    return new UserPage(twitter, userId, twitterId);
  }
}
