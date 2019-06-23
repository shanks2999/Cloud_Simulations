package org.simulation.cloudsim.utils

/**
  * A Utility Object which facilitates easy conversion between Java and Scala collections
  */

object Converter {

  def toJava[T](s: List[T]): java.util.List[T] = {
    val j = new java.util.ArrayList[T]
    s.map(j.add(_))
    j
  }

  def toScala[T](j: java.util.List[T]): List[T] = {
    var s = scala.collection.mutable.ListBuffer[T]()
    for (r <- 0 until j.size) s += j.get(r)
    s.toList
  }
}
