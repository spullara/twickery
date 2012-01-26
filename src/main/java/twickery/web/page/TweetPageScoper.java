package twickery.web.page;

import twickery.web.Scoper;
import twickery.web.Twickery;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;

public class TweetPageScoper implements Scoper<Matcher> {
  public Object newScope(HttpServletRequest httpServletRequest, final Matcher matcher) {
    return new TweetPage(Twickery.twitter(), matcher);
  }
}
