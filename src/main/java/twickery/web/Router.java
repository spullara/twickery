package twickery.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sampullara.mustache.Mustache;
import com.sampullara.mustache.MustacheBuilder;
import com.sampullara.mustache.MustacheException;
import com.sampullara.mustache.MustacheJava;
import com.sampullara.mustache.Scope;

/**
 * Routes all requests
 */
public class Router extends HttpServlet {

  private Mustache index;

  @Override
  public void init() throws ServletException {
    MustacheJava mj = new MustacheBuilder("templates");
    try {
      index = mj.parseFile("index.html");
    } catch (MustacheException e) {
      throw new ServletException(e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      index.execute(resp.getWriter(), new Scope());
    } catch (MustacheException e) {
      throw new ServletException(e);
    }
  }
}
