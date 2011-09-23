package twickery.web;

import java.io.File;
import java.util.concurrent.Callable;
import javax.servlet.ServletException;

import com.google.common.base.Function;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import static java.lang.Integer.parseInt;

/**
 * Singleton representing the application
 */
public class Twickery {

  private static JedisPool pool;

  static {
    try {
      MappingJsonFactory jf = new MappingJsonFactory();
      JsonParser parser = jf.createJsonParser(new File("/home/dotcloud/environment.json"));
      JsonNode env = parser.readValueAsTree();
      GenericObjectPool.Config config = new GenericObjectPool.Config();
      String host = env.get("DOTCLOUD_DATA_REDIS_HOST").getTextValue();
      int port = parseInt(env.get("DOTCLOUD_DATA_REDIS_PORT").getTextValue());
      String password = env.get("DOTCLOUD_DATA_REDIS_PASSWORD").getTextValue();
      pool = new JedisPool(config, host, port, 60, password);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private static TwitterFactory twitterFactory;

  static {
    twitterFactory = new TwitterFactory();
  }

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
}
