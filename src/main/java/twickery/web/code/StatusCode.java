package twickery.web.code;

import twitter4j.Status;

/**
* Created by IntelliJ IDEA.
* User: spullara
* Date: 9/23/11
* Time: 1:57 PM
* To change this template use File | Settings | File Templates.
*/
public class StatusCode {
  private final Status status;

  public StatusCode(Status status) {
    this.status = status;
  }

  long id() {
    return status.getId();
  }

  String title() {
    return status.getUser().getName() + "'s Tweet";
  }

  String name() {
    return status.getUser().getName();
  }

  String screen_name() {
    return status.getUser().getScreenName();
  }

  String image() {
    return status.getUser().getProfileImageURL().toString().replace("normal", "reasonably_small");
  }

  String text() {
    return status.getText().replace("\n", " ");
  }
}
