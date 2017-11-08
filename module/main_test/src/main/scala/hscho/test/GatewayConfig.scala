package hscho.test

import com.typesafe.config.{Config, ConfigFactory}

/**
  * 2017. 6. 7. - Created by Kwon, Yeong Eon
  */
object GatewayConfig {
  lazy val config: Config = ConfigFactory.load()
  //lazy val config: Config = ConfigFactory.parseFile(new File(getClass.getClassLoader.getResource("gateway.conf").getFile))
  //lazy val config: Config = ConfigFactory.parseFile(new File(System.getProperty("config.file")))

  lazy val hostname: String = config.getString("akka.remote.netty.tcp.hostname")

  lazy val port: Int = config.getInt("akka.remote.netty.tcp.port")
}
