package crawler.core.cache

import com.redis._
import crawler.core.conf.ConfigSupport

object CachingClients extends ConfigSupport {

  private val redisHost = redisConfg.getString("host")
  private val redisPort = redisConfg.getInt("port")

  lazy val redisClientPool = new RedisClientPool(redisHost, redisPort)

}
