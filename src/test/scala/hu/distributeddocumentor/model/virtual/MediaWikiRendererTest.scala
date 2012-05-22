package hu.distributeddocumentor.model.virtual

import org.junit.runner.RunWith
import org.scalatest
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import hu.distributeddocumentor.model.virtual.MediaWikiRenderer.render

@RunWith(classOf[JUnitRunner])
class MediaWikiRendererTest extends FunSuite {

  test("Rendering simple headings") {
    
    assert(render(Heading(1, "Test")) === "= Test =\n\n")
    assert(render(Heading(2, "Test")) === "== Test ==\n\n")
    assert(render(Heading(3, "Test")) === "=== Test ===\n\n")
    assert(render(Heading(4, "Test")) === "==== Test ====\n\n")
    assert(render(Heading(5, "Test")) === "===== Test =====\n\n")
    assert(render(Heading(6, "Test")) === "====== Test ======\n\n")
  }

  test("Rendering paragraph with single text item") {
    
    assert(render(Paragraph(List(Text("hello world")))) === "hello world\n\n")
  }
  
  test("Rendering paragraph with line breaks") {
    
    assert(render(Paragraph(List(Text("hello world\n"), Text("second line")))) === "hello world\nsecond line\n\n")
  }
  
  test("Rendering paragraph with bold and italic members") {
    
    assert(render(Paragraph(List(
            Text("hello "), 
            Italic("unit testing"),
            Text(" "),
            Bold("world"),
            Text("!!!")
          ))) === "hello ''unit testing'' '''world'''!!!\n\n")
  }
  
  test("Rendering indented multi-line paragraph") {
    
    val para = Paragraph(List(Text("hello world\n"), Text("second line")))
    
    val result1 = render(IndentedParagraph(1, para))
    val result2 = render(IndentedParagraph(3, para))
    
    assert(result1 === "; hello world\n; second line\n\n")
    assert(result2 === ";;; hello world\n;;; second line\n\n")
  }
  
  test("Rendering bulleted list") {
    
    val paras = List(List(Paragraph(List(Text("hello world")))),
                     List(Paragraph(List(Text("second line")))))

    assert(render(BulletList(1, paras)) === "* hello world\n* second line\n\n")
    assert(render(BulletList(2, paras)) === "** hello world\n** second line\n\n")
    assert(render(BulletList(3, paras)) === "*** hello world\n*** second line\n\n")
    assert(render(BulletList(4, paras)) === "**** hello world\n**** second line\n\n")
  }
  
  test("Rendering enumerated list") {
    
    val paras = List(List(Paragraph(List(Text("hello world")))),
                     List(Paragraph(List(Text("second line")))))

    assert(render(EnumeratedList(1, paras)) === "# hello world\n# second line\n\n")
    assert(render(EnumeratedList(2, paras)) === "## hello world\n## second line\n\n")
    assert(render(EnumeratedList(3, paras)) === "### hello world\n### second line\n\n")
    assert(render(EnumeratedList(4, paras)) === "#### hello world\n#### second line\n\n")
  }
  
  test("Rendering java source code") {
    
    val code = "public static int func() {\n\treturn 0;\n}"
    
    assert(render(SourceCode("java", code)) === "<pre class='brush: java'>\npublic static int func() {\n\treturn 0;\n}\n</pre>\n\n")
  }
  
  test("Rendering images and links") {
    
    assert(render(Image("img01")) === "[[Image:img01]]")
    assert(render(InternalLink("OtherPage", "something else")) === "[OtherPage something else]")
    assert(render(ExternalLink(java.net.URI.create("http://www.google.hu"), "Google")) === "[http://www.google.hu Google]")
  }
}
