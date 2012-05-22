package hu.distributeddocumentor.model.virtual

import org.apache.commons.lang3.StringUtils.removeEnd

trait WikiRenderer {
    def render(item: WikiItem): String
    
    def render(contents: Seq[WikiItem]): String = 
      contents map render mkString("")
    
    def renderTight(contents: Seq[WikiItem]): String = 
      contents map render map (ps => removeEnd(ps, "\n\n")) mkString("\n")
}
