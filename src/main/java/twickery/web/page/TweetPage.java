package twickery.web.page;

import java.util.regex.Matcher;

import twickery.web.code.StatusCode;

import twitter4j.Twitter;
import twitter4j.TwitterException;

import static java.lang.Long.parseLong;

/**
* Created by IntelliJ IDEA.
* User: spullara
* Date: 9/23/11
* Time: 2:50 PM
* To change this template use File | Settings | File Templates.
*/
public class TweetPage {
  private final Twitter twitter;
  private final Matcher matcher;

  public TweetPage(Twitter twitter, Matcher matcher) {
    this.twitter = twitter;
    this.matcher = matcher;
  }

  Object tweet() throws TwitterException {
    return new StatusCode(twitter.showStatus(parseLong(matcher.group(1))));
  }
}
