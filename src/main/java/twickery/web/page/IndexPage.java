package twickery.web.page;

import twickery.web.code.UserCode;

import twitter4j.TwitterException;
import twitter4j.User;

public class IndexPage {

  private User user;

  public IndexPage(User user) {
    this.user = user;
  }

  Object connected() throws TwitterException {
    if (user == null) return null;
    return new UserCode(user);
  }
}
