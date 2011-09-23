package twickery.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;

import redis.clients.jedis.JedisPool;
import twickery.web.handler.CallbackHandler;
import twickery.web.handler.ConnectHandler;
import twickery.web.page.TweetPageScoper;

import twitter4j.TwitterFactory;

import static java.lang.Integer.parseInt;

/**
 * Routes all requests
 */
public class Router extends HttpServlet {

  private void add(RegexMustacheHandler index) {
    handlerMap.put(index, index);
  }

  private Map<Function<HttpServletRequest, Matcher>, Handler<Matcher>> handlerMap =
          new LinkedHashMap<Function<HttpServletRequest, Matcher>, Handler<Matcher>>();

  @Override
  public void init() throws ServletException {
    System.setProperty("mustache.debug", "true");

    add(new RegexMustacheHandler("/", "index.html"));
    add(new RegexMustacheHandler("/tweet/([0-9]+)", "tweet.html", new TweetPageScoper()));
    handlerMap.put(new SimpleMatcher("/twitter/oauth"), new CallbackHandler());
    handlerMap.put(new SimpleMatcher("/twitter/connect"), new ConnectHandler());
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    for (Map.Entry<Function<HttpServletRequest, Matcher>, Handler<Matcher>> handlerEntry : handlerMap.entrySet()) {
      Matcher matcher = handlerEntry.getKey().apply(req);
      if (matcher != null) {
        handlerEntry.getValue().handle(req, resp, matcher);
        return;
      }
    }
    resp.setStatus(404);
  }

}
