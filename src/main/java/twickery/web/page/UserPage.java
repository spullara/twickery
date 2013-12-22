package twickery.web.page;

import twickery.web.code.UserCode;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class UserPage {
  private final Twitter twitter;
  private final String userId;
  private final String sourceId;

  public UserPage(Twitter twitter, String userId, String sourceId) {
    this.twitter = twitter;
    this.userId = userId;
    this.sourceId = sourceId;
  }

  Object user() throws TwitterException {
    return new UserCode(twitter.showUser(userId));
  }

  String source_user() {
    return sourceId;
  }
}
