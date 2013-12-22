package twickery.web.handler;

import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twickery.web.Handler;
import twickery.web.Twickery;

import twitter4j.Twitter;
import twitter4j.auth.RequestToken;

public class TwitterConnectHandler implements Handler<Matcher> {
  public void handle(HttpServletRequest request, HttpServletResponse response, Matcher matcher) throws ServletException {
    Twitter twitter = Twickery.twitter();
    RequestToken requestToken;
    try {
      requestToken = twitter.getOAuthRequestToken("http://twickery.com/twitter/oauth");
      request.getSession().setAttribute("requestToken", requestToken);
      response.sendRedirect(requestToken.getAuthenticationURL());
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
