import java.util

import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.scalatest.FunSuite
import org.simulation.cloudsim.Network_Simulator
import org.simulation.cloudsim.config.PullConfig

class TestConfig extends FunSuite {
  test("Config.test") {
    assert(new PullConfig().getSetting("test.a").toInt >= 0)
  }

  test("PE Creation Test") {
    assert(new Network_Simulator().createPEs(2,4000).size()== 2)
  }

  test("Network Host ID Port Test") {
    val peList = new util.ArrayList[Pe]
    peList.add(new PeSimple(1000, new PeProvisionerSimple))
    var nn:NetworkHost= new NetworkHost(512,100,100,peList)
    nn.setId(4)
    assert(new Network_Simulator().getSwitchIndex(nn,4)== 1)
  }

  test("VM creation Test") {
    assert(new Network_Simulator().createVm(4).getMips== new PullConfig().getSetting("NW.HOST_MIPS").toInt)
  }

  test("Random Test") {
    val num = org.simulation.cloudsim.utils.Helper.getRandom(1,10)
    assert(num >= 1 && num<=10)
  }

  test("Dynamic Cloudlet generation Test") {
    val listCloudlet = org.simulation.cloudsim.utils.Helper.createDynamicCloudlets(10,2)
    assert(listCloudlet.size() == 10 && listCloudlet.get(0).getLength==9000)
  }
}