package twickery.web.handler;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twickery.web.Handler;
import twickery.web.Twickery;

public class FacebookConnectHandler implements Handler<Matcher> {
  public void handle(HttpServletRequest request, HttpServletResponse response, Matcher matcher) throws ServletException {
    try {
      boolean twitter = false;
      final Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          if (cookie.getName().equals("twitter")) {
            String decode = Twickery.decode(cookie.getValue());
            if (decode != null) {
              twitter = true;
            }
          }
        }
      }
      if (twitter) {
        String url = URLEncoder.encode("http://www.twickery.com/facebook/oauth", "utf-8");
        response.sendRedirect(
                "https://www.facebook.com/dialog/oauth?client_id=171266056289639&scope=publish_actions,offline_access&redirect_uri=" + url);
      } else {
        response.sendRedirect("http://www.twickery.com/twitter/connect");
      }
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
