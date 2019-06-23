package org.simulation.cloudsim.loadBalancer

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.vms.Vm
import org.simulation.cloudsim.utils.{EXEC, Helper}

/**
  * Implements Load Balancing via Round Robin/FIFO
  * This Class overrides the defaultVmMapper
  * Assign the cloudlet/task to VM based the next available VM
  */
class LB_RoundRobin(val simulation: CloudSim) extends DatacenterBrokerSimple(simulation) {

  override def defaultVmMapper(cloudlet: Cloudlet): Vm = {
    if (cloudlet.isBindToVm) {
      val vm = cloudlet.getVm
      if (this == vm.getBroker && vm.isCreated) return vm
      return Vm.NULL
    }
    return getVmFromCreatedList(VmViaRoundRobin());
  }

  /**
    * This method fetches the next VM to assign it to a cloudlet
    */
  private def VmViaRoundRobin(): Int = {
    if (getVmExecList.isEmpty) return -1
    val vmList = getVmExecList
    if(EXEC.index >= vmList.size())
      EXEC.index = 0
    var vm = EXEC.index
    EXEC.index += 1
    vm
  }
}
