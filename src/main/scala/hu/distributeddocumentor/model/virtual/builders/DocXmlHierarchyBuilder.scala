package hu.distributeddocumentor.model.virtual.builders

import java.io.File
import scala.collection.mutable.MutableList
import scala.xml._
import hu.distributeddocumentor.model.virtual._
import hu.distributeddocumentor.model.virtual.{Text => Txt}

class DocXmlHierarchyBuilder(val xmlFile: File, val title: String, markupLanguage: String) 
  extends VirtualHierarchyBuilderBase(markupLanguage) {

  val data = XML.loadFile(xmlFile)
  val rootId = generateId()
  
  val flatNamespaces = 
    (
      for (member <- data \ "members" \ "member";
           name = member.attribute("name").getOrElse("").toString;
           if name.startsWith("T:");
           idx = name.lastIndexOf('.');
           if idx > -1
      )        
        yield name.drop(2).take(idx - 2)
    ).distinct
  
  val namespaces = generateNamespaceHierarchy(flatNamespaces)
  
  val rootPage = virtualPage(
    rootId, 
    List(
      Heading(1, (data \ "assembly" \ "name").text),
      Paragraph(List(
          Txt("This is the root of the assembly's reference manual. The manual "+
              "is structured by "), Italic("namespaces"), 
          Txt(" containing "), Italic("classes"), 
          Txt(". There is one documentation page for each class, describing "+
              "all the fields, properties, events and methods of the class.")
        )),
     toBullets(flatNamespaces)
     ))
  
  override def build() = tocNode(title, rootPage, toNodes(flatNamespaces))
  
  private def classesIn(ns: String) = {    
    val prefix = "T:"+ns;
    
    for (member <- data \ "members" \ "member";
         name = member.attribute("name").getOrElse("").toString;         
         idx = name.lastIndexOf('.');
         if idx > -1;
         if name.take(idx) == prefix)
      yield name.drop(idx+1)
  }
  
  private def toNodes(nss: Seq[String]) = List()
  
  private def toBullets(nss: Seq[String]): BulletList =
    BulletList(1,
               nss.map(ns => 
                List(Paragraph(List(Txt(ns))),                     
                     toClassBullets(classesIn(ns)))))
                     

  private def toClassBullets(cls: Seq[String]): BulletList =
    BulletList(2,
              cls.map(cs => 
                List(Paragraph(List(Txt(cs))))))
          
  private def generateNamespaceHierarchy(nss: Seq[String]): Seq[NameSpaceNode] = {
    
    val result = new MutableList[NameSpaceNode]
    nss.foreach(ns => {
        
        val parts = ns.split('.')
        if (parts.size > 1) {
          
          addNameSpaceNode(result, parts)
        } else {
          result += new NameSpaceNode(ns)
        }
    }) 
  
    return result
  }
  
  private def addNameSpaceNode(root: MutableList[NameSpaceNode], items: Seq[String]) {
    
    items.headOption match {
        case Some(f) => {
            
          root.find(item => item.name == f) match {
            case Some(ns) => addNameSpaceNode(ns.children, items.tail)
            case None =>
              val ns = new NameSpaceNode(f)
              addNameSpaceNode(ns.children, items.tail)
              root += ns
          }                  
        }  
      
        case None =>
    }
  }
}
