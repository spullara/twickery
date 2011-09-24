package twickery.web.page;

import java.util.regex.Matcher;

import twickery.web.code.StatusCode;
import twickery.web.code.UserCode;

import twitter4j.Twitter;
import twitter4j.TwitterException;

import static java.lang.Long.parseLong;

public class UserPage {
  private final Twitter twitter;
  private final Matcher matcher;

  public UserPage(Twitter twitter, Matcher matcher) {
    this.twitter = twitter;
    this.matcher = matcher;
  }

  Object user() throws TwitterException {
    return new UserCode(twitter.showUser(matcher.group(1)));
  }
}
