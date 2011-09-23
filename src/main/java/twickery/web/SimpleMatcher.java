package twickery.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Function;

/**
* Created by IntelliJ IDEA.
* User: spullara
* Date: 9/23/11
* Time: 2:53 PM
* To change this template use File | Settings | File Templates.
*/
public class SimpleMatcher implements Function<HttpServletRequest, Matcher> {
  private String string;
  public SimpleMatcher(String string) {
    this.string = string;
  }

  public Matcher apply(HttpServletRequest httpServletRequest) {
    Matcher matcher = Pattern.compile(string).matcher(
            httpServletRequest.getPathInfo());
    return matcher.matches() ? matcher : null;
  }
}
