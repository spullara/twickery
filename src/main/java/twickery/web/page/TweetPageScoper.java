package twickery.web.page;

import twitter4j.Twitter;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;

public class TweetPageScoper extends BasePageScoper {
  public Object newScope(HttpServletRequest httpServletRequest, final Matcher matcher) {
    String sourceId = matcher.group(1);
    Twitter twitter = getTwitter(sourceId);
    return new TweetPage(twitter, matcher.group(2), sourceId);
  }
}
