package org.simulation.cloudsim


//import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory


object Main extends App {

//  import org.cloudsimplus.util.Log
//  Log.setLevel(ch.qos.logback.classic.Level.INFO);
  val logback = LoggerFactory.getLogger("CloudSim+")
  logback.info("   ---    Wecome to cloudsim+ simulator    ---    ")
  logback.info("Press 1 to start Load balancing simulator")
  logback.info("Press 2 to start Network simulator")

  try {
    val ip = scala.io.StdIn.readInt()
    if (ip == 1) {
      logback.info("You chose SIMULATION " + ip)
      val sim = new LoadBalancer_Simulator
      sim.startSimulation()
    }
    else if (ip == 2) {
      logback.info("You chose SIMULATION " + ip)
      val sim = new Network_Simulator
      sim.startSimulation()

    }
    else {
      logback.info("SIMULATION " + ip + " doesn't exists!")
      logback.info("Please run again...")
    }
  }
  catch {
    case e: Exception =>
      logback.info("Wrong Input.... Please runagain with correct input")
  }
}
