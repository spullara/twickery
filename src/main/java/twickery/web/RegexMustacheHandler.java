package twickery.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;

import com.sampullara.mustache.Mustache;
import com.sampullara.mustache.MustacheBuilder;
import com.sampullara.mustache.MustacheException;
import com.sampullara.mustache.MustacheJava;
import com.sampullara.mustache.Scope;

/**
* Created by IntelliJ IDEA.
* User: spullara
* Date: 9/23/11
* Time: 1:35 PM
* To change this template use File | Settings | File Templates.
*/
public class RegexMustacheHandler implements Function<HttpServletRequest, Matcher>, Router.Handler<Matcher> {

  private Pattern pattern;
  private Mustache mustache;
  private Router.Scoper<Matcher> makeScope;
  private static MustacheJava mj = new MustacheBuilder("templates");

  public RegexMustacheHandler(String pattern, String template) throws ServletException {
    this.pattern = Pattern.compile(pattern);
    try {
      mustache = mj.parseFile(template);
    } catch (MustacheException e) {
      throw new ServletException(e);
    }
  }

  public RegexMustacheHandler(String pattern, String template, Router.Scoper<Matcher> makeScope) throws ServletException {
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
      mustache.execute(response.getWriter(), makeScope == null ? new Scope() : makeScope.newScope(request, matcher));
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
