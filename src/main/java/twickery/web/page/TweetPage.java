package twickery.web.page;

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
  private final String tweetId;
  private final String sourceId;

  public TweetPage(Twitter twitter, String tweetId, String sourceId) {
    this.twitter = twitter;
    this.tweetId = tweetId;
    this.sourceId = sourceId;
  }

  Object tweet() throws TwitterException {
    return new StatusCode(twitter.showStatus(parseLong(tweetId)));
  }

  String source_user() {
    return sourceId;
  }
}
