
package org.simulation.cloudsim

import java.util
import java.util.List

//import com.typesafe.scalalogging.Logger
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.network.{CloudletExecutionTask, CloudletReceiveTask, CloudletSendTask, NetworkCloudlet}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.network.switches.EdgeSwitch
import org.cloudbus.cloudsim.provisioners.{PeProvisionerSimple, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull
import org.cloudbus.cloudsim.vms.network.NetworkVm
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.simulation.cloudsim.config.PullConfig
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._


class Network_Simulator {
  val config_obj = new PullConfig()
  private val logback = LoggerFactory.getLogger("SIM-NetworkAppSimulator")
//  val logback = Logger("NetworkAppSimulator")
  private val NUMBER_OF_HOSTS = config_obj.getSetting("NW.NUMBER_OF_HOSTS").toInt
  private val HOST_MIPS = config_obj.getSetting("NW.HOST_MIPS").toInt
  private val HOST_PES = config_obj.getSetting("NW.HOST_PES").toInt
  private val HOST_RAM = config_obj.getSetting("NW.HOST_RAM").toInt // host memory (Megabyte)
  private val HOST_STORAGE = config_obj.getSetting("NW.HOST_STORAGE").toInt // host storage
  private val HOST_BW = config_obj.getSetting("NW.HOST_BW").toInt
  private val CLOUDLET_EXECUTION_TASK_LENGTH = config_obj.getSetting("NW.CLOUDLET_EXECUTION_TASK_LENGTH").toInt
  private val CLOUDLET_FILE_SIZE = config_obj.getSetting("NW.CLOUDLET_FILE_SIZE").toInt
  private val CLOUDLET_OUTPUT_SIZE = config_obj.getSetting("NW.CLOUDLET_OUTPUT_SIZE").toInt
  private val PACKET_DATA_LENGTH_IN_BYTES = config_obj.getSetting("NW.PACKET_DATA_LENGTH_IN_BYTES").toInt
  private val NUMBER_OF_PACKETS_TO_SEND = config_obj.getSetting("NW.NUMBER_OF_PACKETS_TO_SEND").toInt
  private val TASK_RAM = config_obj.getSetting("NW.TASK_RAM").toInt
  private val NUMBER_OF_CLOUDLETS=config_obj.getSetting("NW.NUMBER_OF_CLOUDLETS").toInt
  private val NUMBER_OF_EDGE_SWITCHES = NUMBER_OF_HOSTS / 4
  private val NUMBER_OF_VM = config_obj.getSetting("NW.NUMBER_OF_VM").toInt

  private var simulation:CloudSim=null

  private var vmList:List[NetworkVm] = null
  private var cloudletList:List[NetworkCloudlet] = null
  private var datacenter:NetworkDatacenter = null
  private var anotherdatacenter:NetworkDatacenter = null



  private var broker:DatacenterBrokerSimple = null

  /**
    * Starts the execution of the simulator
    *
    *
    */
  /*  def main(args: Array[String]): Unit = {
         startSimulation()
    }*/

  def startSimulation() {
//    import org.cloudsimplus.util.Log
//    Log.setLevel(ch.qos.logback.classic.Level.INFO)
    logback.info("Starting " + getClass.getSimpleName)
    simulation = new CloudSim()
    datacenter = createDatacenter
    anotherdatacenter = createDatacenter
    broker = new DatacenterBrokerSimple(simulation)
    vmList = createAndSubmitVMs(broker)
    cloudletList = createNetworkCloudlets
    broker.submitCloudletList(cloudletList)
    simulation.start
    showSimulationResults()
  }

  private def showSimulationResults(): Unit = {
    val newList = broker.getCloudletFinishedList
    new CloudletsTableBuilder(newList).build()
    logback.info("Statistics for datacenter1")
    for (host <- datacenter.getHostList[NetworkHost]) {
      logback.info("Host "+host.getId+" data transferred: "+host.getTotalDataTransferBytes+" bytes")
    }
    println()
    println("Statistics for datacenter2")
    for (host <- anotherdatacenter.getHostList[NetworkHost]) {
      logback.info("Host "+host.getId+" data transferred: "+host.getTotalDataTransferBytes+" bytes")
    }
    println()
    println("Network Simulation finished!")
  }

  /**
    * This method creates a datacenter ,datacenter toplogy is established in another method
    * createNetwork called inside this method
    *
    * @return the Datacenter
    */
  private def createDatacenter = {
    val hostList = new util.ArrayList[Host]
    var i = 0
    while ( i < NUMBER_OF_HOSTS) {
      val host = createHost
      hostList.add(host)
      i+=1;
    }
    val newDatacenter = new NetworkDatacenter(simulation, hostList, new VmAllocationPolicySimple)
    newDatacenter.setSchedulingInterval(5)
    createNetwork(newDatacenter)
    newDatacenter
  }

  /**
    * Creates a network host based on the paramaters,important to note here that VMscheduler is spaceshared scheduler
    * since for network simulator a VM can't be time shared since receive and send task depend on the target host machine and VM to
    * have one to one matching (i.e. VM migration is not supported in network cloudsim plus)
    * @return
    */
  private def createHost = {
    val peList = createPEs(HOST_PES, HOST_MIPS)
    new NetworkHost(HOST_RAM, HOST_BW, HOST_STORAGE, peList).setRamProvisioner(new ResourceProvisionerSimple).setBwProvisioner(new ResourceProvisionerSimple).setVmScheduler(new VmSchedulerSpaceShared)
  }

  /**
    * Creates a PE which will be assigned to a host.
    * @param numberOfPEs
    * @param mips
    * @return
    */
  def createPEs(numberOfPEs: Int, mips: Long) = {
    val peList = new util.ArrayList[Pe]
    var i = 0
    while (i < numberOfPEs) {
      peList.add(new PeSimple(mips, new PeProvisionerSimple))
      i += 1
    }
    peList
  }

  /**
    * Creates internal Datacenter network.Here we are creating a TOR topology
    *  Each host/physical machine is connected to edgeswitch
    * * @param datacenter Datacenter where the network will be created
    */
  private def createNetwork(datacenter: NetworkDatacenter): Unit = {
    logback.info("Create network topology")
    val edgeSwitches = new Array[EdgeSwitch](NUMBER_OF_EDGE_SWITCHES)
    var i = 0
    while (i < edgeSwitches.length) {
      edgeSwitches(i) = new EdgeSwitch(simulation, datacenter)
      //edgeSwitches[i].setSwitchingDelay(200);
      datacenter.addSwitch(edgeSwitches(i))
      i += 1
    }
    import scala.collection.JavaConversions._
    for (host <- datacenter.getHostList[NetworkHost]) {
      val switchNum = getSwitchIndex(host, edgeSwitches(0).getPorts)
      edgeSwitches(switchNum).connectHost(host)
    }
    /*AggregateSwitch ag= new AggregateSwitch(simulation, datacenter);
            ag.setPorts(2);
            ag.getDownlinkSwitches();
            ag.setSwitchingDelay(1000);
            datacenter.addSwitch(ag);
            RootSwitch rs= new RootSwitch(simulation,datacenter);
            rs.setSwitchingDelay(10000);
            datacenter.addSwitch(rs);*/
  }

  /**
    * Creates a list of virtual machines in a Datacenter for a given broker and
    * submit the list to the broker.
    *
    * @param broker The broker that will own the created VMs
    * @return the list of created VMs
    */
  private def createAndSubmitVMs(broker: DatacenterBroker) = {
    val list = new util.ArrayList[NetworkVm]
    var i = 0
    while (i < NUMBER_OF_VM) {
      val vm = createVm(i)
      list.add(vm)
      i += 1
    }
    broker.submitVmList(list)
    list
  }

  /**
    * Here we are creating a VM ,VM created such that as per the architecture,a single host can support 4 VMS
    * @param id
    * @return a VM
    */
  def createVm(id: Int) = {
    val vm = new NetworkVm(id, HOST_MIPS, HOST_PES / 4)
    vm.setRam(HOST_RAM / 4).setBw(HOST_BW / 4).setSize(HOST_STORAGE / 4).setCloudletScheduler(new CloudletSchedulerTimeShared)
    vm
  }

  /**
    * Creates a list of  NetworkCloudlet that together represents the
    * distributed processes of a given diffusing computation based application.
    * A Single netwok cloudlet sends messages to multiple other cloudlets
    * which are waiting for the message to start with the executing tasks.
    *
    * @return the list of create NetworkCloudlets
    */
  private def createNetworkCloudlets = {
    val numberOfCloudlets = NUMBER_OF_CLOUDLETS
    val networkCloudletList = new util.ArrayList[NetworkCloudlet](numberOfCloudlets)
    var i = 0
    while ( i < numberOfCloudlets) {
      val div = i % NUMBER_OF_VM
      networkCloudletList.add(createNetworkCloudlet(vmList.get(div)))
      i += 1; i - 1
    }
    var temp = 0
    var j = 0
    while ( j < networkCloudletList.size) {
      if (j % 6 == 0) {
        temp = j
        addExecutionTask(networkCloudletList.get(j))
        var k = 1
        while (k < 6) {
          if ((j + k) < networkCloudletList.size) addSendTask(networkCloudletList.get(j), networkCloudletList.get(j + k))
          k += 1
        }
      }
      else {
        addReceiveTask(networkCloudletList.get(j), networkCloudletList.get(temp))
        addExecutionTask(networkCloudletList.get(j))
      }
      j += 1
    }
    networkCloudletList
  }

  /**
    * Since we are dealing with NetworkCloudlet we have to assign a VM to cloudlet so that underlying host can receieve
    * or send messages ,in this method we assign a newly created cloudlet to input VM
    *
    * @param vm the VM that will run the created  NetworkCloudlet
    * @return
    */
  private def createNetworkCloudlet(vm: NetworkVm) = {
    val netCloudlet = new NetworkCloudlet(4000 , HOST_PES / 4)
    netCloudlet.setMemory(TASK_RAM).setFileSize(CLOUDLET_FILE_SIZE).setOutputSize(CLOUDLET_OUTPUT_SIZE).setUtilizationModel(new UtilizationModelFull)
    netCloudlet.setVm(vm)
    netCloudlet
  }

  /**
    * Adds an execution task to the list of tasks of the given
    * CLOUDLET_EXECUTION_TASK_LENGTH is the attribute which will have impact on cloudlets total running time
    * NetworkCloudlet
    *
    * @param cloudlet the  NetworkCloudlet the task will belong to
    */
  private def addExecutionTask(cloudlet: NetworkCloudlet): Unit = {
    val task = new CloudletExecutionTask(cloudlet.getTasks.size, CLOUDLET_EXECUTION_TASK_LENGTH)
    task.setMemory(TASK_RAM)
    cloudlet.addTask(task)
  }

  /**
    * Adds a send task to the list of tasks of the given NetworkCloudlet.
    *
    * @param sourceCloudlet the NetworkCloudlet from which packets will be sent
    * @param destinationCloudlet the destination NetworkCloudlet to send packets to
    */
  private def addSendTask(sourceCloudlet: NetworkCloudlet, destinationCloudlet: NetworkCloudlet): Unit = {
    val task = new CloudletSendTask(sourceCloudlet.getTasks.size)
    task.setMemory(TASK_RAM)
    sourceCloudlet.addTask(task)
    var i = 0
    while ( i < NUMBER_OF_PACKETS_TO_SEND) {
      task.addPacket(destinationCloudlet, PACKET_DATA_LENGTH_IN_BYTES)
      i += 1
    }
  }

  /**
    * Adds a receive task to the list of tasks of the given
    * NetworkCloudlet.
    *
    * @param cloudlet the  NetworkCloudlet the task will belong to
    * @param sourceCloudlet the  NetworkCloudlet expected to receive packets from
    */
  private def addReceiveTask(cloudlet: NetworkCloudlet, sourceCloudlet: NetworkCloudlet): Unit = {
    val task = new CloudletReceiveTask(cloudlet.getTasks.size, sourceCloudlet.getVm)
    task.setMemory(TASK_RAM)
    task.setExpectedPacketsToReceive(NUMBER_OF_PACKETS_TO_SEND)
    cloudlet.addTask(task)
  }

  /**
    * Gets the index of a switch where a Host will be connected,
    * considering the number of ports the switches have.
    * Ensures that each set of N Hosts is connected to the same switch
    * (where N is defined as the number of switch's ports).
    * Since the host ID is long but the switch array index is int,
    * the module operation is used safely convert a long to int
    * For instance, if the id is 2147483648 (higher than the max int value 2147483647),
    * it will be returned 0. For 2147483649 it will be 1 and so on.
    *
    * @param host        the Host to get the index of the switch to connect to
    * @param switchPorts the number of ports (N) the switches where the Host will be connected have
    * @return the index of the switch to connect the host
    */
  def getSwitchIndex(host: NetworkHost, switchPorts: Int): Int = Math.round(host.getId % Integer.MAX_VALUE) / switchPorts
}
