package twickery.web;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.MappingJsonFactory;
import redis.clients.jedis.Jedis;
import redispatterns.queue.RedisQueue;
import twitter4j.Status;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.PropertyConfiguration;

import javax.annotation.Nullable;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.net.URLEncoder.encode;
import static twickery.web.Twickery.redis;

/**
 * Listen to site streams from twitter for our users
 */
public class SiteStreams implements ServletContextListener {
  private static final String FAVORITE_ACTION = "https://graph.facebook.com/me/twickery:favorite";
  private static final String FOLLOW_ACTION = "https://graph.facebook.com/me/twickery:follow";

  // We don't really need these yet
  private static final String TWEET_ACTION = "https://graph.facebook.com/me/twickery:tweet";
  private static final String RETWEET_ACTION = "https://graph.facebook.com/me/twickery:retweet";

  private static final String TWEET_OBJECT = "tweet";
  private static final String USER_OBJECT = "user";

  private static JsonFactory jf = new MappingJsonFactory();
  private static TwitterStream twitterStream;
  private static ExecutorService es = Executors.newCachedThreadPool();
  private static RedisQueue queue;

  public static void restart() {
    System.out.println("Restarting connection to site stream");
    boolean started = false;
    do {
      stop();
      try {
        start();
        started = true;
      } catch (Exception e) {
        e.printStackTrace();
        try {
          Thread.sleep(10000);
        } catch (InterruptedException ie) {
          // ignore
        }
      }
    } while (!started);
  }

  public void contextInitialized(ServletContextEvent servletContextEvent) {
    start();
  }

  private static void start() {
    System.out.println("Connecting to Site Stream");
    Properties props = new Properties();
    try {
      props.load(SiteStreams.class.getClassLoader().getResourceAsStream("twitter4j.properties"));
    } catch (IOException e) {
      throw new AssertionError("Couldn't load twitter4j.properties");
    }
    props.setProperty(PropertyConfiguration.SITE_STREAM_BASE_URL,
        "https://sitestream.twitter.com/2b/");
    TwitterStreamFactory tsf = new TwitterStreamFactory(new PropertyConfiguration(props));
    twitterStream = tsf.getInstance();
    twitterStream.addListener(new SiteStreamsBase() {
      public void onStatus(long l, final Status status) {
        System.out.print("onStatus" + " ");
        System.out.println(l + ": " + status);
        final long source = status.getUser().getId();
        final Status retweetedStatus = status.getRetweetedStatus();
        if (retweetedStatus != null) {
          Twickery.redis(new Function<Jedis, String>() {
            public String apply(Jedis jedis) {
              try {
                String facebookId = jedis.hget("twitter:uid:" + source, "facebook");
                if (facebookId != null) {
                  String access_token = jedis.hget("facebook:uid:" + facebookId, "access_token");
                  if (access_token != null) {
                    post(RETWEET_ACTION, form(access_token, TWEET_OBJECT, tweet(retweetedStatus)));
                  }
                }
                return null;
              } catch (Exception e) {
                e.printStackTrace();
                return null;
              }
            }
          });
        }
      }

      public void onFavorite(long l, final User source, User target, final Status status) {
        System.out.print("onFavorite" + " ");
        System.out.println(l + ": " + source + " " + target + " " + status);
        Twickery.redis(new Function<Jedis, String>() {
          public String apply(Jedis jedis) {
            try {
              String facebookId = jedis.hget("twitter:uid:" + source.getId(), "facebook");
              if (facebookId != null) {
                String access_token = jedis.hget("facebook:uid:" + facebookId, "access_token");
                if (access_token != null) {
                  post(FAVORITE_ACTION, form(access_token, TWEET_OBJECT, tweet(status)));
                }
              }
              return null;
            } catch (Exception e) {
              e.printStackTrace();
              return null;
            }
          }
        });
      }

      public void onFollow(long l, final User source, final User target) {
        System.out.print("onFollow" + " ");
        System.out.println(l + ": " + source + " " + target);
        Twickery.redis(new Function<Jedis, String>() {
          public String apply(Jedis jedis) {
            try {
              String facebookId = jedis.hget("twitter:uid:" + source.getId(), "facebook");
              if (facebookId != null) {
                String access_token = jedis.hget("facebook:uid:" + facebookId, "access_token");
                if (access_token != null) {
                  post(FOLLOW_ACTION, form(access_token, USER_OBJECT, user(target)));
                }
              }
              return null;
            } catch (Exception e) {
              e.printStackTrace();
              return null;
            }
          }
        });
      }

    });
    final String[] auth = new String[2];
    Set<String> uids = redis(new Function<Jedis, Set<String>>() {
      public Set<String> apply(Jedis jedis) {
        auth[0] = jedis.hget("twitter:uid:378853703", "oauth_token");
        auth[1] = jedis.hget("twitter:uid:378853703", "oauth_token_secret");
        return jedis.smembers("twitter:uids");
      }
    });
    queue = new RedisQueue(Twickery.redisPool(), "twickery_queue");
    es.submit(new Runnable() {
      @Override
      public void run() {
        while (true) {
          try {
            queue.pop(new Predicate<byte[]>() {
              @Override
              public boolean apply(@Nullable byte[] bytes) {
                try {
                  String message = new String(bytes, Charsets.UTF_8);
                  int index = message.indexOf("?");
                  String api = message.substring(0, index);
                  String form = message.substring(index + 1);
                  URL url = new URL(api);
                  HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                  urlc.setDoOutput(true);
                  OutputStream outputStream = urlc.getOutputStream();
                  outputStream.write(form.getBytes("utf-8"));
                  outputStream.flush();
                  JsonNode jsonNode = jf.createJsonParser(urlc.getInputStream()).readValueAsTree();
                  System.out.println(jsonNode);
                  return true;
                } catch (IOException e) {
                  e.printStackTrace();
                  return false;
                }
              }            
            });
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    });
    twitterStream.setOAuthAccessToken(new AccessToken(auth[0], auth[1]));
    System.out.println("Users: " + uids);
    long[] array = new long[uids.size()];
    Iterator<String> i = uids.iterator();
    for (int j = 0; j < array.length; j++) {
      array[j] = Long.parseLong(i.next());
    }
    twitterStream.site(false, array);
  }

  private static void post(String api, String form) throws IOException {
    String message = api + "?" + form;
    System.out.println(message);
    queue.push(message.getBytes(Charsets.UTF_8));
  }

  private static String tweet(Status status) throws UnsupportedEncodingException {
    return encode("http://www.twickery.com/tweet/" + status.getId(), "utf-8");
  }

  private static String user(User user) throws UnsupportedEncodingException {
    return encode("http://www.twickery.com/user/" + user.getScreenName(), "utf-8");
  }

  private static String form(String access_token, String entityType, String entityUrl) throws UnsupportedEncodingException {
    return "access_token=" +
        URLEncoder.encode(access_token,
            "utf-8") + "&" + entityType + "=" + entityUrl;
  }

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    stop();
  }

  private static void stop() {
    twitterStream.shutdown();
  }
}
