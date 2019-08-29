package crawler.core.cache

import com.redis._

object CachingClients {

  def withRedisClient[A](host: String, port: Int)(body: RedisClient => A) = {
    val client = new RedisClient(host, port)
    body(client)
  }

  def withMockRedisClient[A](body: RedisClient => A) = {
    val clientMock = None
  }

}
