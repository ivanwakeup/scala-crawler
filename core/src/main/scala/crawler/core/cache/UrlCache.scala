package crawler.core.cache

import com.redis._

object UrlCache {

  def withRedisClient[A](host: String, port: Int)(body: RedisClient => A) = {
    val client = new RedisClient(host, port)
    body(client)
  }

}
