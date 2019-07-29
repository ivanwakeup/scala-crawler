package utils

import java.util.Properties

trait ConfigSupport {

   final val CRAWL_BYTES_TOPIC = "crawl_bytes"

}

object ConfigSupport {
   def config2JavaProps(conf: Config): Properties = {

   }
}
