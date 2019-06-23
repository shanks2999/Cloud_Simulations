package org.simulation.cloudsim.loadBalancer

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.vms.Vm
import org.simulation.cloudsim.utils.Helper

/**
  * Implements Load Balancing via Random Allocation
  * This Class overrides the defaultVmMapper
  * Assign the cloudlet/task to VM based on a function which arbitrary chooses VM
  */
class LB_Random(val simulation: CloudSim) extends DatacenterBrokerSimple(simulation) {

  override def defaultVmMapper(cloudlet: Cloudlet): Vm = {
    if (cloudlet.isBindToVm) {
      val vm = cloudlet.getVm
      if (this == vm.getBroker && vm.isCreated) return vm
      return Vm.NULL
    }
    getRandomVM
  }

  /**
    * This method gets a random vm and gives it a task
    */
  private def getRandomVM: Vm = {
    if (getVmExecList.isEmpty) return Vm.NULL
    val random = Helper.getRandom(0, getVmExecList().size()-1)
    getVmExecList.get(random)
  }
}
