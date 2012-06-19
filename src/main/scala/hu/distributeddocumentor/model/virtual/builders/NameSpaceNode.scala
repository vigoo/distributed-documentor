package hu.distributeddocumentor.model.virtual.builders

import scala.collection.mutable.MutableList

class NameSpaceNode(val name: String) {
  
  val children = new MutableList[NameSpaceNode]()
}
