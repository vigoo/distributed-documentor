package hu.distributeddocumentor.model.virtual

import java.net.URI

abstract class WikiItem 
abstract class Inline extends WikiItem

case class Heading(level: Int, title: String) extends WikiItem
case class Paragraph(inlines: Seq[Inline]) extends WikiItem
case class IndentedParagraph(level: Int, para: Paragraph) extends WikiItem
case class BulletList(level: Int, items: Seq[Seq[WikiItem]]) extends WikiItem
case class EnumeratedList(level: Int, items: Seq[Seq[WikiItem]]) extends WikiItem
case class SourceCode(lang: String, code: String) extends WikiItem

case class Text(text: String) extends Inline
case class Bold(text: String) extends Inline
case class Italic(text: String) extends Inline
case class BoldItalic(text: String) extends Inline
case class Image(name: String) extends Inline
case class InternalLink(id: String, text: String) extends Inline
case class ExternalLink(url: URI, text: String) extends Inline
