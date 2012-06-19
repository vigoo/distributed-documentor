package hu.distributeddocumentor.model.virtual.builders

import hu.distributeddocumentor.model.Page
import hu.distributeddocumentor.model.TOCNode
import hu.distributeddocumentor.model.Snippet
import hu.distributeddocumentor.model.SnippetCollection
import hu.distributeddocumentor.model.virtual.VirtualHierarchyBuilder
import hu.distributeddocumentor.model.virtual.WikiItem
import hu.distributeddocumentor.model.virtual.WikiRenderer
import hu.distributeddocumentor.model.virtual.MediaWikiRenderer
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

object VirtualHierarchyBuilderBase {    
  private val log: Logger = LoggerFactory.getLogger(classOf[VirtualHierarchyBuilderBase].getName)
}

abstract class VirtualHierarchyBuilderBase(val markupLanguage: String) extends VirtualHierarchyBuilder {   
  
  private val renderer: WikiRenderer = 
    markupLanguage match {
      case "MediaWiki" => MediaWikiRenderer
      case _ => null
    }
  
  private val emptySnippets = new SnippetCollection {
    def getSnippets() = asJavaCollection(List[Snippet]())
    def getSnippet(id: String) = null
    def addSnippet(snippet: Snippet) : Unit = {}
    def removeSnippet(id: String) = {}
  }
  
  def build(): TOCNode
  
  protected def generateId() : String = UUID.randomUUID().toString()
  
  protected def virtualPage(id: String, markup: String): Page = {
            
    val page = new Page(id, emptySnippets)        
    page.setMarkupLanguage(markupLanguage)
    page.setMarkup(markup)
   
    return page
  }  
    
  protected def virtualPage(markup: String): Page = 
    virtualPage(generateId(), markup)
  
  protected def virtualPage(id: String, contents: List[WikiItem]): Page =
    virtualPage(id, render(contents))
  
  protected def virtualPage(contents: List[WikiItem]): Page =
    virtualPage(render(contents))  
  
  protected def tocNode(title: String, page: Page, children: List[TOCNode]): TOCNode = {
    
    val node = new TOCNode(title, page)
    children.foreach {
      child => node.addToEnd(child)
    }
    
    return node
  }    
  
  private def render(contents: Seq[WikiItem]): String = {
    val result = renderer.render(contents)    
    VirtualHierarchyBuilderBase.log.debug(result)    
    return result
  }
  
  private def render(item: WikiItem): String = {
    val result = renderer.render(item)
    VirtualHierarchyBuilderBase.log.debug(result)    
    return result
  }
}
