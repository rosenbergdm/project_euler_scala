package scalit.util

import markup.Line
abstract class MarkupFilter extends
  (Stream[Line] => Stream[Line]) {
    def externalFilter(command: String,
                       lines: Stream[Line]): Stream[Line] = {
      val p = Runtime.getRuntime().exec(command)

      val procWriter = new java.io.PrintWriter(p.getOutputStream())

      lines foreach procWriter.println

      procWriter.close()
      p.waitFor()
      util.conversions.linesFromMarkupInput(p.getInputStream())
    }

}


class tee extends MarkupFilter {
  def apply(lines: Stream[Line]): Stream[Line] = {
    externalFilter("tee out",lines)
  }
}

class simplesubst extends MarkupFilter {
  def apply(lines: Stream[Line]): Stream[Line] = {
    import markup.TextLine
    lines map {
      case TextLine(cont) =>
        TextLine(cont.replace(" " + "LaTeX "," \\LaTeX "))
      case unchanged => unchanged
    }
  }
}


import markup.Block
abstract class BlockFilter extends
  (Stream[Block] => Stream[Block]) {
}


class stats extends BlockFilter {
  def apply(blocks: Stream[Block]): Stream[Block] = {
    val (doclines,codelines) = collectStats(blocks)

    import markup.{TextLine,NewLine}
    val content =
      Stream.cons(NewLine,
      Stream.cons(TextLine("Documentation lines: " +
                  doclines +
                  ", Code lines: " +
                  codelines),
      Stream.cons(NewLine,Stream.empty)))
    Stream.concat(blocks,Stream.cons(
      markup.DocuBlock(-1,-1,content),Stream.empty))
  }


  def collectStats(bs: Stream[Block]): (Int,Int) = {
    def collectStats0(str: Stream[Block],
                doclines: Int,
                codelines: Int): (Int,Int) = str match {
       case Stream.empty => (doclines,codelines)
       case Stream.cons(first,rest) => first match {

         case markup.CodeBlock(_,_,lines,_) =>
           val codels = (lines foldLeft 0) {
             (acc: Int, l: markup.Line) => l match {
               case markup.NewLine => acc + 1
               case _ => acc
             }
           }
           collectStats0(rest,doclines,codelines + codels)

         case markup.DocuBlock(_,_,lines) =>
           val doculs = (lines foldLeft 0) {
             (acc: Int, l: markup.Line) => l match {
               case markup.NewLine => acc + 1
               case _ => acc
             }
           }
           collectStats0(rest,doclines + doculs, codelines)
       }
    }
    collectStats0(bs,0,0)
  }
}


