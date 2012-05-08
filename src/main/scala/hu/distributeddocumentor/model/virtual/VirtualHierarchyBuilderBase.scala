package hu.distributeddocumentor.model.virtual

import hu.distributeddocumentor.model.Page
import hu.distributeddocumentor.model.TOCNode
import hu.distributeddocumentor.model.Snippet
import hu.distributeddocumentor.model.SnippetCollection
import java.util.UUID
import scala.collection.JavaConversions._

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
  
  def build() = new TOCNode("Test", virtualPage("'''Test page'''"))
  
  protected def virtualPage(id: String, markup: String): Page = {
            
    val page = new Page(id, emptySnippets)        
    page.setMarkupLanguage(markupLanguage)
    page.setMarkup(markup)
   
    return page
  }
    
  protected def virtualPage(markup: String): Page = 
    virtualPage(UUID.randomUUID().toString(), markup)
  
  protected def virtualPage(id: String, contents: List[WikiItem]): Page =
    virtualPage(id, render(contents)) // TODO
  
  protected def virtualPage(contents: List[WikiItem]): Page =
    virtualPage(render(contents)) // TODO
  
  private def render(contents: List[WikiItem]): String = 
    contents map render mkString("")
  
  private def render(item: WikiItem): String = renderer.render(item)
}
