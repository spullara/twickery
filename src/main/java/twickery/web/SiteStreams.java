package twickery.web;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.transform;
import static twickery.web.Twickery.redis;

/**
 * Listen to site streams from twitter for our users
 */
public class SiteStreams implements ServletContextListener {

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
        System.out.println(l + ": " + longs);
      }

      public void onFavorite(long l, User user, User user1, Status status) {
        System.out.print("onFavorite" + " ");
        System.out.println(l + ": " + user + " " + user1 + " " + status);
      }

      public void onUnfavorite(long l, User user, User user1, Status status) {
        System.out.print("onUnfavorite" + " ");
        System.out.println(l + ": " + user + " " + user1 + " " + status);
      }

      public void onFollow(long l, User user, User user1) {
        System.out.print("onFollow" + " ");
        System.out.println(l + ": " + user + " " + user1);
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

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    twitterStream.shutdown();
  }
}
