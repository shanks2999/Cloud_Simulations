package org.simulation.cloudsim.config


import com.typesafe.config._

/**
  * This is configuration reader class
  */
class PullConfig(config: Config) {
  config.checkValid(ConfigFactory.defaultReference(), "test", "LOAD_BALANCER")
  def this() {
    this(ConfigFactory.load("config.conf"))
  }

  def getSetting(path: String):String = {
    config.getString(path)
  }
}


