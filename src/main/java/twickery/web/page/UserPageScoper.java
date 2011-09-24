package twickery.web.page;

import java.util.regex.Matcher;
import javax.servlet.http.HttpServletRequest;

import com.sampullara.mustache.Scope;
import twickery.web.Scoper;
import twickery.web.Twickery;

public class UserPageScoper implements Scoper<Matcher> {
  public Scope newScope(HttpServletRequest httpServletRequest, final Matcher matcher) {
    return new Scope(new UserPage(Twickery.twitter(), matcher));
  }
}
