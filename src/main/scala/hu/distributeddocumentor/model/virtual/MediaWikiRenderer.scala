package hu.distributeddocumentor.model.virtual

import org.apache.commons.lang3.StringUtils._

object MediaWikiRenderer extends WikiRenderer {  
 def render(item: WikiItem): String =  
    item match {
      case Heading(level, title) => {
          val eqs = repeat('=', level)
          "%s %s %s\n\n".format(eqs, title, eqs)
      }
      
      case Paragraph(inlines) => (inlines map render mkString("")) + "\n\n"
        
      case IndentedParagraph(level, para) => 
        prefixPerLine(level, ';', removeEnd(render(para), "\n\n").split('\n') toList)        
      
      case BulletList(level, items) =>
        prefixPerLine(level, '*', items map renderTight)
      
      case EnumeratedList(level, items) =>
        prefixPerLine(level, '#', items map renderTight)
        
      case SourceCode(lang, code) =>
        "<pre class='brush: %s'>\n%s\n</pre>\n\n".format(lang, code)
        
      case Text(text) => text        
      case Bold(text) => "'''%s'''".format(text)        
      case Italic(text) => "''%s''".format(text)
      case BoldItalic(text) => "''''%s''''".format(text)
        
      case Image(name) => "[[Image:%s]]".format(name)
      case InternalLink(id, text) => "[%s %s]".format(id, text)
      case ExternalLink(url, text) => "[%s %s]".format(url.toString, text)
        
      case _ => item.toString
    }    
  
  private def prefixPerLine(level: Int, ch: Char, lines: Seq[String]): String = {          
    val prefix = repeat(ch, level) + " "
    (lines map (line => prefix + line) mkString("\n")) + "\n\n"
  }                        
}
