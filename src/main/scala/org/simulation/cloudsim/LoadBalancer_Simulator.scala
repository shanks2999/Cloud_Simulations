package org.simulation.cloudsim

import java.util

import org.cloudsimplus.util.Log

//import com.typesafe.scalalogging.Logger
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.simulation.cloudsim.config.PullConfig
import org.simulation.cloudsim.loadBalancer.{LB_Hashing, LB_Heuristic, LB_Random, LB_RoundRobin}
import org.simulation.cloudsim.utils.{Converter, Helper}
import org.slf4j.LoggerFactory


/**
  * This class simulates the working of different types of LB techniques.
  * Compares them based on the way it handles dynamic load (which is changes based on SEED value from the config files)
  * Execution time is compared to measure the optimal LB
  */
class LoadBalancer_Simulator {
//  private val config_LB = new PullConfig()
//  private val HOSTS :Int = config_LB.getSetting("LOAD_BALANCER.HOSTS").toInt
//  private val HOST_PES :Int = config_LB.getSetting("LOAD_BALANCER.HOST_PES").toInt
//  private val VMS :Int = config_LB.getSetting("LOAD_BALANCER.VMS").toInt
//  private val VM_PES :Int = config_LB.getSetting("LOAD_BALANCER.VM_PES").toInt
//  private val CLOUDLETS :Int = config_LB.getSetting("LOAD_BALANCER.CLOUDLETS").toInt
  private var HOST_LIST: util.List[Host] = null
  private var VM_LIST: util.List[Vm] = null
  private var CLOUDLET_LIST: util.List[Cloudlet] = null
  private val logback = LoggerFactory.getLogger("SIM-LoadBalancer")
//  private val logback = Logger("SIM-LoadBalancer")
  private var time_lb_hashing = 0.0
  private var time_lb_random = 0.0
  private var time_lb_minMin = 0.0
  private var time_lb_maxMin = 0.0
  private var time_lb_roundRobin = 0.0

  /**
    * An initializer method which:
    * pulls from config
    * create HOSTS from config params
    * create VM from config params
    * create dynamic tasks
    */
  def initialize={
    logback.info("   ---    Initializing LB Simulator    ---    ")
    logback.info("Fetching params from config")
    val config_LB = new PullConfig()
    val HOSTS :Int = config_LB.getSetting("LOAD_BALANCER.HOSTS").toInt
    val HOST_PES :Int = config_LB.getSetting("LOAD_BALANCER.HOST_PES").toInt
    val VMS :Int = config_LB.getSetting("LOAD_BALANCER.VMS").toInt
    val VM_PES :Int = config_LB.getSetting("LOAD_BALANCER.VM_PES").toInt
    val CLOUDLETS :Int = config_LB.getSetting("LOAD_BALANCER.CLOUDLETS").toInt
    val SEED :Int = config_LB.getSetting("LOAD_BALANCER.SEED").toInt

    logback.info("Creating Hosts for VM allocation")
    HOST_LIST = Helper.createHosts(HOSTS, HOST_PES)
    logback.info("Creating VM's for executing Tasks")
    VM_LIST = Helper.createVms(VMS, VM_PES)
    logback.info("Creating dynamic tasks in order to test LB")
    CLOUDLET_LIST = Helper.createDynamicCloudlets(CLOUDLETS,SEED)
  }


  /**
    * This method clone the dynamic tasks creates to be fed into different simulations
    * This is needed as each simulation changes the task's encapsulated fields after execution (one of the limitations of cloudsim)
    */
  def clone_cloudlets= {
    val utilizationModel = new UtilizationModelFull
    val list = new util.ArrayList[Cloudlet]
    for(i <- 1 to CLOUDLET_LIST.size()-1) {
      list.add( new CloudletSimple(CLOUDLET_LIST.get(i).getLength, CLOUDLET_LIST.get(i).getNumberOfPes.toInt, utilizationModel)
        .setSizes(CLOUDLET_LIST.get(i).getOutputSize))
    }
    list
  }


  /**
    * The driving method which calls/runs different simulation and clocks them.
    *
    */
  def startSimulation(): Unit = {
//    import org.cloudsimplus.util.Log
//    Log.setLevel(ch.qos.logback.classic.Level.INFO)
    initialize
    val sim_HASHING = runSimulation_HASHING()

    val sim_RANDOM = runSimulation_RANDOM()
    val sim_MIN_MIN = runSimulation_MINMIN()
    val sim_MAX_MIN = runSimulation_MAXMIN()
    val sim_RR = runSimulation_RR()
//    val sim_HEURISTIC = runSimulation_HEURISTIC()

    logback.info("Collecting results.....")
    logback.info("Load Balancer \t|\t Time taken ")
    logback.info("HASHING    \t|\t " + time_lb_hashing)
    logback.info("RANDOM     \t|\t " + time_lb_random)
    logback.info("MIN-MIN    \t|\t " + time_lb_minMin)
    logback.info("MAX-MIN    \t|\t " + time_lb_maxMin)
    logback.info("ROND ROBIN \t|\t " + time_lb_roundRobin)

    logback.info("")


//    /*Enables just some level of log messages.
//           Make sure to import org.cloudsimplus.util.Log;*/
//    //Log.setLevel(ch.qos.logback.classic.Level.WARN);
//
  }


  /**
    * This method simulates a RANDOM load balancer
    * Calls the inherited broker class "LB_Random" which assign VM's randomly
    */
  def runSimulation_RANDOM()={
    logback.info("Begin Simulating Random Load Balancer")
    val simulation = new CloudSim
    val datacenter =   new DatacenterSimple(simulation, HOST_LIST, new VmAllocationPolicySimple)
    val broker = new LB_Random(simulation)
    broker.submitVmList(VM_LIST)
    broker.submitCloudletList(clone_cloudlets)
    simulation.start
    time_lb_random = simulation.clock()
    logback.info("Simulation completed...")
    simulation
  }

  /**
    * This method simulates a ROUND-ROBIN load balancer
    * Calls the inherited broker class "LB_RoundRobin" which assign VM's to the next one available
    */
  def runSimulation_RR()={
    logback.info("Begin Simulating Round Robin Balancer")
    val simulation = new CloudSim
    val datacenter =   new DatacenterSimple(simulation, HOST_LIST, new VmAllocationPolicySimple)
    val broker = new LB_RoundRobin(simulation);
    broker.submitVmList(VM_LIST)
    broker.submitCloudletList(clone_cloudlets)
    simulation.start
    time_lb_roundRobin = simulation.clock()
    logback.info("Simulation completed...")
    simulation
  }

  /**
    * This method simulates a MIN-MIN load balancer
    * It sorts in the incoming cloudlets ascending based on task completion
    * Then calls the same inherited broker class "LB_RoundRobin"  which makes sure that shorter tasks come in queue first
    */
  def runSimulation_MINMIN()={
    logback.info("Begin Simulating MIN-MIN Load Balancer")
    val simulation = new CloudSim
    val datacenter =   new DatacenterSimple(simulation, HOST_LIST, new VmAllocationPolicySimple);
    val broker = new LB_RoundRobin(simulation);
    val minFirst = Converter.toScala(clone_cloudlets).sortBy(_.getLength)
    broker.submitVmList(VM_LIST)
    broker.submitCloudletList(Converter.toJava(minFirst))
    simulation.start
    time_lb_minMin = simulation.clock()
    logback.info("Simulation completed...")
    simulation
  }

  /**
    * This method simulates a MAX-MIN load balancer
    * It sorts in the incoming cloudlets descending based on task completion
    * Then calls the same inherited broker class "LB_RoundRobin"  which makes sure that longer tasks come in queue first
    */
  def runSimulation_MAXMIN()={
    logback.info("Begin Simulating MAX-MIN Load Balancer")
    val simulation = new CloudSim
    val datacenter =   new DatacenterSimple(simulation, HOST_LIST, new VmAllocationPolicySimple);
    val broker = new LB_RoundRobin(simulation);
    val maxFirst = Converter.toScala(clone_cloudlets).sortBy(-_.getLength)
    broker.submitVmList(VM_LIST)
    broker.submitCloudletList(Converter.toJava(maxFirst))
    simulation.start
    time_lb_maxMin = simulation.clock()
    logback.info("Simulation completed...")
    simulation
  }

  /**
    * This method simulates a HASHING load balancer
    * Calls the inherited broker class "LB_Hashing" which assign VM's uniquely based on the hash of the task
    */
  def runSimulation_HASHING()={
    logback.info("Begin Simulating HASHING Load Balancer")
    val simulation = new CloudSim
    val datacenter =   new DatacenterSimple(simulation, HOST_LIST, new VmAllocationPolicySimple)
    val broker = new LB_Hashing(simulation)
    broker.submitVmList(VM_LIST)
    broker.submitCloudletList(clone_cloudlets)
    simulation.start
    time_lb_hashing = simulation.clock()
    logback.info("Simulation completed...")
    simulation
  }


  /**
    * This method simulates a HEURISTIC load balancer
    * Calls the inherited broker class "LB_Heuristic" which assign VM's via Simulated annealing
    */
  def runSimulation_HEURISTIC()={
    val simulation = new CloudSim
    val datacenter =   new DatacenterSimple(simulation, HOST_LIST, new VmAllocationPolicySimple)
    val broker = new LB_Heuristic(simulation)
    broker.submitVmList(VM_LIST)
    broker.submitCloudletList(clone_cloudlets)
    simulation.start
    simulation
  }
}
