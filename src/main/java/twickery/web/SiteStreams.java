package twickery.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.common.base.Function;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.MappingJsonFactory;
import redis.clients.jedis.Jedis;

import twitter4j.DirectMessage;
import twitter4j.SiteStreamsListener;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.auth.AccessToken;

import static java.net.URLEncoder.encode;
import static twickery.web.Twickery.redis;

/**
 * Listen to site streams from twitter for our users
 */
public class SiteStreams implements ServletContextListener {
  private static final String FAVORITE_API = "https://graph.facebook.com/me/twickery:favorite";
  private static final String FOLLOW_API = "https://graph.facebook.com/me/twickery:follow";

  // We don't really need these yet
  private static final String TWEET_API = "https://graph.facebook.com/me/twickery:tweet";
  private static final String RETWEET_API = "https://graph.facebook.com/me/twickery:retweet";

  private static final String TWEET = "tweet";

  private JsonFactory jf = new MappingJsonFactory();
  private TwitterStream twitterStream;

  public void contextInitialized(ServletContextEvent servletContextEvent) {
    System.out.println("Connecting to Site Stream");
    TwitterStreamFactory tsf = new TwitterStreamFactory();
    twitterStream = tsf.getInstance();
    twitterStream.addListener(new SiteStreamsListener() {
      public void onStatus(long l, Status status) {
        System.out.print("onStatus" + " ");
        System.out.println(l + ": " + status);
      }

      public void onDeletionNotice(long l, StatusDeletionNotice statusDeletionNotice) {
        System.out.print("onDeletionNotice" + " ");
        System.out.println(l + ": " + statusDeletionNotice);
      }

      public void onFriendList(long l, long[] longs) {
        System.out.print("onFriendList" + " ");
        System.out.println(l + ": " + Arrays.toString(longs));
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
                  post(FAVORITE_API, form(access_token, TWEET, tweet(status)));
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

      public void onUnfavorite(long l, User user, User user1, Status status) {
        System.out.print("onUnfavorite" + " ");
        System.out.println(l + ": " + user + " " + user1 + " " + status);
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
                  post(FOLLOW_API, form(access_token, TWEET, user(target)));
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

      public void onUnfollow(long l, User user, User user1) {
        System.out.print("onUnfollow" + " ");
        System.out.println(l + ": " + user + " " + user1);
      }

      public void onDirectMessage(long l, DirectMessage directMessage) {
        System.out.print("onDirectMessage" + " ");
        System.out.println(l + ": " + directMessage);
      }

      public void onDeletionNotice(long l, long l1, long l2) {
        System.out.print("onDeletionNotice" + " ");
        System.out.println(l + ": " + l1 + ", " + l2);
      }

      public void onUserListMemberAddition(long l, User user, User user1, UserList userList) {
        System.out.print("onUserListMemberAddition" + " ");
        System.out.println(l + ": " + user + " " + user1 + " " + userList);
      }

      public void onUserListMemberDeletion(long l, User user, User user1, UserList userList) {
        System.out.print("onUserListMemberDeletion" + " ");
        System.out.println(l + ": " + user + " " + user1 + " " + userList);
      }

      public void onUserListSubscription(long l, User user, User user1, UserList userList) {
        System.out.print("onUserListSubscription" + " ");
        System.out.println(l + ": " + user + " " + user1 + " " + userList);
      }

      public void onUserListUnsubscription(long l, User user, User user1, UserList userList) {
        System.out.print("onUserListUnsubscription" + " ");
        System.out.println(l + ": " + user + " " + user1 + " " + userList);
      }

      public void onUserListCreation(long l, User user, UserList userList) {
        System.out.print("onUserListCreation" + " ");
        System.out.println(l + ": " + user + " " + userList);
      }

      public void onUserListUpdate(long l, User user, UserList userList) {
        System.out.print("onUserListUpdate" + " ");
        System.out.println(l + ": " + user + " " + userList);
      }

      public void onUserListDeletion(long l, User user, UserList userList) {
        System.out.print("onUserListDeletion" + " ");
        System.out.println(l + ": " + user + " " + userList);
      }

      public void onUserProfileUpdate(long l, User user) {
        System.out.print("onUserProfileUpdate" + " ");
        System.out.println(l + ": " + user);
      }

      public void onBlock(long l, User user, User user1) {
        System.out.print("onBlock" + " ");
        System.out.println(l + ": " + user + " " + user1);
      }

      public void onUnblock(long l, User user, User user1) {
        System.out.print("onUnblock" + " ");
        System.out.println(l + ": " + user + " " + user1);
      }

      public void onException(Exception e) {
        e.printStackTrace();
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
    twitterStream.setOAuthAccessToken(new AccessToken(auth[0], auth[1]));
    System.out.println("Users: " + uids);
    long[] array = new long[uids.size()];
    Iterator<String> i = uids.iterator();
    for (int j = 0; j < array.length; j++) {
      array[j] = Long.parseLong(i.next());
    }
    twitterStream.site(false, array);
  }

  private String tweet(Status status) throws UnsupportedEncodingException {
    return encode("http://www.twickery.com/tweet/" + status.getId(), "utf-8");
  }

  private String user(User user) throws UnsupportedEncodingException {
    return encode("http://www.twickery.com/user/" + user.getId(), "utf-8");
  }

  private String form(String access_token, String entityType, String entityUrl) throws UnsupportedEncodingException {
    return "access_token=" +
                            URLEncoder.encode(access_token,
                                    "utf-8") + "&" + entityType + "=" + entityUrl;
  }

  private void post(String api, String form) throws IOException {
    URL url = new URL(api);
    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
    urlc.setDoOutput(true);
    OutputStream outputStream = urlc.getOutputStream();
    outputStream.write(form.getBytes("utf-8"));
    outputStream.flush();
    JsonNode jsonNode = jf.createJsonParser(urlc.getInputStream()).readValueAsTree();
    System.out.println(jsonNode);
  }

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    twitterStream.shutdown();
  }
}
