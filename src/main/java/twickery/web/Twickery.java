package twickery.web;

import com.google.common.base.Function;
import com.google.common.io.CharStreams;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;
import sun.misc.BASE64Encoder;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Singleton representing the application
 */
public class Twickery {

  private static JedisPool pool;

  private static TwitterFactory twitterFactory;
  public static String salt;

  public static Twitter twitter() {
    return twitterFactory.getInstance();
  }

  public static JedisPool redisPool() {
    return pool;
  }

  public static <T> T redis(Function<Jedis, T> callable) {
    Jedis jedis = pool.getResource();
    try {
      return callable.apply(jedis);
    } catch (JedisException e) {
      pool.returnBrokenResource(jedis);
      jedis = null;
      throw e;
    } finally {
      if (jedis != null) {
        pool.returnResource(jedis);
      }
    }
  }

  public static String hash(long userId) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] digest = md.digest((userId + ":" + salt).getBytes("utf-8"));
    BASE64Encoder b64e = new BASE64Encoder();
    return b64e.encode(digest);
  }

  public static String decode(String value) {
    String[] values = value.split(":");
    try {
      return chechHash(values[0], values[1]) ? values[0] : null;
    } catch (Exception e) {
      return null;
    }
  }

  public static boolean chechHash(String data, String hash) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] digest = md.digest((data + ":" + salt).getBytes("utf-8"));
    BASE64Encoder b64e = new BASE64Encoder();
    return b64e.encode(digest).equals(hash);
  }

  public static void main(String[] args) throws Exception {
    try {
      GenericObjectPool.Config config = new GenericObjectPool.Config();
      config.testWhileIdle = true;
      config.minEvictableIdleTimeMillis = 60000;
      config.timeBetweenEvictionRunsMillis = 30000;
      config.numTestsPerEvictionRun = -1;
      pool = new JedisPool(config, "localhost", 6379);
      twitterFactory = new TwitterFactory();
      salt = CharStreams.readLines(
              new InputStreamReader(Twickery.class.getResourceAsStream("/salt.txt"),
                      "utf-8")).get(0);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    SiteStreams.restart();
    Server server = new Server(8080);
    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(Router.class, "/*");
    handler.setHandler(new SessionHandler(new HashSessionManager()));
    SiteStreams siteStreams = new SiteStreams();
    siteStreams.contextInitialized(null);
    server.setHandler(handler);
    server.start();
  }
}
