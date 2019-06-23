package org.simulation.cloudsim.loadBalancer

import org.cloudbus.cloudsim.brokers.DatacenterBrokerHeuristic
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.distributions.ContinuousDistribution
import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.heuristics.CloudletToVmMappingHeuristic
import org.cloudsimplus.heuristics.CloudletToVmMappingSimulatedAnnealing
import org.cloudsimplus.heuristics.CloudletToVmMappingSolution
import java.util.{Collections, Comparator, List}
import java.util


class LB_Heuristic(val simulation: CloudSim) extends DatacenterBrokerHeuristic(simulation) {
  val SA_INITIAL_TEMPERATURE = 1.0
  val SA_COLD_TEMPERATURE = 0.0001
  val SA_COOLING_RATE = 0.003
  val SA_NUMBER_OF_NEIGHBORHOOD_SEARCHES = 50

  val heuristic = new CloudletToVmMappingSimulatedAnnealing(SA_INITIAL_TEMPERATURE, new UniformDistr(0, 1))
  heuristic.setColdTemperature(SA_COLD_TEMPERATURE)
  heuristic.setCoolingRate(SA_COOLING_RATE)
  heuristic.setNeighborhoodSearchesByIteration(SA_NUMBER_OF_NEIGHBORHOOD_SEARCHES)
  setHeuristic(heuristic)

  override def defaultVmMapper(cloudlet: Cloudlet): Vm = {
    if (cloudlet.isBindToVm) {
      val vm = cloudlet.getVm
      if (this == vm.getBroker && vm.isCreated) return vm
      return Vm.NULL
    }
    getVmFromCreatedList(getNextVm(cloudlet))
  }

  /**
    * Gets the index of next VM in the broker's created VM list.
    * If not VM was selected yet, selects the first one,
    * otherwise, cyclically selects the next VM.
    *
    * @return the index of the next VM to bind a cloudlet to
    */

  private def getNextVm(cloudlet: Cloudlet): Int = {
    if (getVmExecList.isEmpty) return -1
    val vmIndex = getVmExecList.indexOf(getLastSelectedVm)
    return (vmIndex + 1) % getVmExecList.size
  }
}
