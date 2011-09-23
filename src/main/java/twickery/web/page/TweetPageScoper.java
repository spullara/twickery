package twickery.web.page;

import java.util.regex.Matcher;
import javax.servlet.http.HttpServletRequest;

import com.sampullara.mustache.Scope;
import twickery.web.Scoper;
import twickery.web.Twickery;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

/**
* Created by IntelliJ IDEA.
* User: spullara
* Date: 9/23/11
* Time: 2:55 PM
* To change this template use File | Settings | File Templates.
*/
public class TweetPageScoper implements Scoper<Matcher> {
  public Scope newScope(HttpServletRequest httpServletRequest, final Matcher matcher) {
    return new Scope(new TweetPage(Twickery.twitter(), matcher));
  }
}
