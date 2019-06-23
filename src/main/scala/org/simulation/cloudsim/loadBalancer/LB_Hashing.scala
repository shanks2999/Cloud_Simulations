package org.simulation.cloudsim.loadBalancer

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.vms.Vm
 import org.simulation.cloudsim.utils.Helper

/**
  * Implements Load Balancing via Hashing
  * This Class overrides the defaultVmMapper
  * Assign the cloudlet/task to VM based on the hash of the cloudlet
  */
class LB_Hashing(val simulation: CloudSim) extends DatacenterBrokerSimple(simulation) {

//  def this(simulation: CloudSim) {
//    this(simulation)
//  }


  override def defaultVmMapper(cloudlet: Cloudlet): Vm = {
    if (cloudlet.isBindToVm) {
      val vm = cloudlet.getVm
      if (this == vm.getBroker && vm.isCreated) return vm
      return Vm.NULL
    }
    vmViaHash(cloudlet)
  }

  /**
    * This method computes the hash
    * maps the hash to an index -> vm, then returns it.
    */
  private def vmViaHash(cloudlet: Cloudlet): Vm = {
    if (getVmExecList.isEmpty) return Vm.NULL
    val vmList = getVmExecList
    val hash = cloudlet.hashCode() + 31
    val index = Math.abs(hash) % vmList.size()
    vmList.get(index)
  }
}
