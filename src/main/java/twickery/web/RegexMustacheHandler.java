package twickery.web;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Function;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMustacheHandler implements Function<HttpServletRequest, Matcher>, Handler<Matcher> {

  private Pattern pattern;
  private Mustache mustache;
  private Scoper<Matcher> makeScope;
  private static MustacheFactory mj = new DefaultMustacheFactory("templates");

  public RegexMustacheHandler(String pattern, String template) throws ServletException {
    this.pattern = Pattern.compile(pattern);
    try {
      mustache = mj.compile(template);
    } catch (MustacheException e) {
      throw new ServletException(e);
    }
  }

  public RegexMustacheHandler(String pattern, String template, Scoper<Matcher> makeScope) throws ServletException {
    this(pattern, template);
    this.makeScope = makeScope;
  }

  public Matcher apply(HttpServletRequest httpServletRequest) {
    Matcher matcher = pattern.matcher(httpServletRequest.getPathInfo());
    if (matcher.matches()) {
      return matcher;
    }
    return null;
  }

  public void handle(HttpServletRequest request, HttpServletResponse response, Matcher matcher) throws ServletException {
    try {
      mustache.execute(response.getWriter(), makeScope == null ? new Object() : makeScope.newScope(request, matcher));
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
