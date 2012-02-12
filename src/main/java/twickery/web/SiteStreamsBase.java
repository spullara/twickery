package twickery.web;

import twitter4j.DirectMessage;
import twitter4j.SiteStreamsListener;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;

import java.util.Arrays;

public abstract class SiteStreamsBase implements SiteStreamsListener {

  public void onDeletionNotice(long l, StatusDeletionNotice statusDeletionNotice) {
    System.out.print("onDeletionNotice" + " ");
    System.out.println(l + ": " + statusDeletionNotice);
  }

  public void onFriendList(long l, long[] longs) {
    System.out.print("onFriendList" + " ");
    System.out.println(l + ": " + Arrays.toString(longs));
  }

  public void onUnfavorite(long l, User user, User user1, Status status) {
    System.out.print("onUnfavorite" + " ");
    System.out.println(l + ": " + user + " " + user1 + " " + status);
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
}
