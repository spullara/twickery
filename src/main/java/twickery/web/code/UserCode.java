package twickery.web.code;

import twitter4j.User;

public class UserCode {
  private User user;

  public UserCode(User user) {
    this.user = user;
  }

  String name() {
    return user.getName();
  }

  String screen_name() {
    return user.getScreenName();
  }

  String image() {
    return user.getProfileImageURL().toString();
  }

  String description() {
    return user.getDescription().replace("\n", " ");
  }
}
