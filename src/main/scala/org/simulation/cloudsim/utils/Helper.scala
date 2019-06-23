package org.simulation.cloudsim.utils

import java.util
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.distributions.{ContinuousDistribution, UniformDistr}
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}

/**
  * A Utility Object which provides common methods used by simulations
  */
object Helper {

  /**
    * Method which created cloudlets/tasks dynamically.
    * @param1 : Number of cloudlets to be created
    * @param2 : The seed for the random generator
    */
  def createDynamicCloudlets(cloudlet_count:Int, seed:Int)= {
    val CLOUDLET_LENGTHS: Array[Long] = Array(2000, 4000, 10000, 16000, 20000, 30000, 40000)
//    val CLOUDLET_MIPS: Array[Long] = Array(10000, 20000, 30000, 40000, 20000, 30000, 40000)
    val list = new util.ArrayList[Cloudlet]
    //        final UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(0.5);
    val utilizationModel = new UtilizationModelFull
    var rand_mips  = new UniformDistr(1, 10, seed)
    var rand_pes  = new UniformDistr(1, 8, seed)
    var rand_size  = new UniformDistr(0, CLOUDLET_LENGTHS.length, seed)

    for(i <- 1 to cloudlet_count){
//      list.add( new CloudletSimple(getRandom(1,10)*1000, getRandom(1,8), utilizationModel)
//        .setSizes(CLOUDLET_LENGTHS(getRandom(0,CLOUDLET_LENGTHS.length-1))))
      list.add( new CloudletSimple(rand_mips.sample().toInt*1000, rand_pes.sample().toInt, utilizationModel)
        .setSizes(CLOUDLET_LENGTHS(rand_size.sample().toInt)))
    }
    list
  }

  /**
    * Method which created Hosts for the DC.
    * @param1 : Number of Hosts to be creates
    * @param2 : Core count for each host
    */
  def createHosts(host_count:Int, host_pes:Int):util.ArrayList[Host] = {
    val hostList = new util.ArrayList[Host](host_count)
    for(i <- 1 to host_count){
      val peList = new util.ArrayList[Pe](host_pes)
      //List of Host's CPUs (Processing Elements, PEs)
      for(j <- 1 to host_pes){
        peList.add(new PeSimple(1000))
      }
      val ram = 2048 //in Megabytes
      val bw = 10000 //in Megabits/s
      val storage = 1000000
      hostList.add(new HostSimple(ram, bw, storage, peList)
        .setRamProvisioner(new ResourceProvisionerSimple)
        .setBwProvisioner(new ResourceProvisionerSimple)
        .setVmScheduler(new VmSchedulerTimeShared))
      //                .setVmScheduler(new VmSchedulerTimeShared());
    }
  hostList
  }

  /**
    * Method which created VM for hosts
    * @param1 : Number of VM's to be created
    * @param2 : Core requirement for each VM
    */
  def createVms(vm_count:Int, vm_pes:Int) = {
    val list = new util.ArrayList[Vm](vm_count)
    for(i <- 1 to vm_count){ //Uses a CloudletSchedulerTimeShared by default to schedule Cloudlets
      val vm = new VmSimple(1000, vm_pes)
      vm.setRam(512).setBw(1000).setSize(10000).setCloudletScheduler(new CloudletSchedulerSpaceShared)
      //                    .setCloudletScheduler(new CloudletSchedulerTimeShared());
      list.add(vm)
    }
    list
  }

  /**
    * Method which gets a random number between a specific range(both inclusive)
    * @param1 : Minimum value
    * @param2 : Maximum value
    */
  def getRandom(min:Int, max:Int):Int ={
    val rnd = new scala.util.Random
    return min + rnd.nextInt( (max - min) + 1 )
  }
}

/**
  * Object used for storing previous value (S/M to static variable in JAVA)
  * Utilized to store previous VM's index for RoundRobin implementation
  */
object EXEC {
  var index = 0
}
