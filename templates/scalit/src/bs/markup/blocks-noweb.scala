package scalit.markup

object StringRefs {
  sealed abstract class StringRef
  case class RealString(content: String,
                  from: Int,
                  to: Int) extends StringRef
  case class QuotedString(content: String) extends StringRef
  case class BlockRef(referenced: CodeBlock) extends StringRef

  implicit def realString2string(rs: RealString) = rs.content
}


sealed abstract class Block(blocknumber: Int, linenumber: Int,
                            content: Stream[Line]) {
   import StringRefs._

   def stringRefForm(codeBlocks: Map[String,CodeBlock]): Stream[StringRef]

}

case class CodeBlock(blocknumber: Int, linenumber: Int,
     content: Stream[Line], blockname: String)
       extends Block(blocknumber,linenumber,content) {
  import StringRefs._
  override def stringRefForm(
    codeBlocks: Map[String,CodeBlock]): Stream[StringRef] = {

  def cbAcc(ls: Stream[Line], acc: String,
        begin: Int, off: Int): Stream[StringRef] =
ls match {
  case Stream.cons(first,rest) => first match {
    case NewLine => cbAcc(rest, acc + "\n", begin, off + 1)
    case TextLine(content) =>
      cbAcc(rest, acc + content, begin, off)
    case Use(usename) => {
      val cb = codeBlocks get usename match {
        case Some(codeBlock) => codeBlock
        case None =>
          System.err.println("Did not find block " +
                         usename)
          exit(1)
      }
      Stream.cons(RealString(acc,begin,off),
      Stream.cons(BlockRef(cb),cbAcc(rest,"",off,off)))
    }
    case other => error("Unexpected line: " + other)
  }

  case Stream.empty => acc match {
    case "" => Stream.empty
    case s  => Stream.cons(RealString(s,begin,off),Stream.empty)
  }
}
      
  cbAcc(content,"",linenumber,linenumber)
  }
}

case class DocuBlock(blocknumber: Int, linenumber: Int,
content: Stream[Line]) extends
  Block(blocknumber,linenumber,content) {
  import StringRefs._
  override def stringRefForm(codeBlocks: Map[String,CodeBlock]):
    Stream[StringRef] = {
      srContent
    }
    lazy val srContent: Stream[StringRef] = {
      def srcAcc(ls: Stream[Line], acc: String): Stream[StringRef] =
        ls match {
          case Stream.empty =>
            Stream.cons(RealString(acc,-1,-1),
            Stream.empty)
          case Stream.cons(first,rest) => first match {
            case NewLine => srcAcc(rest,acc + "\n")
            case TextLine(content) => srcAcc(rest, acc + content)

  case Quote => {
    val (quoted,continue) = quote(rest,"")
    Stream.cons(RealString(acc,-1,-1),
    Stream.cons(quoted,srcAcc(continue,"")))
  }
  case other => error("Unexpected line in doc: " + other)
      }
    }

     def quote(ls: Stream[Line],
           acc: String): (QuotedString,Stream[Line]) =
       ls match {
         case Stream.empty => (QuotedString(acc),Stream.empty)
         case Stream.cons(first,rest) => first match {
           case NewLine => quote(rest, acc + "\n")
           case TextLine(content) => quote(rest, acc + content)
           case EndQuote => (QuotedString(acc),rest)
           case other => error("Unexpected inside quote: " + other)
         }
       }

      
     srcAcc(content,"")
  }

}


case class BlockBuilder(lines: Stream[Line]) {
  def blocks: Stream[Block] = lines match {
    case Stream.cons(_,beg @ Stream.cons(Doc(0),_)) => {
      selectNext(beg,0)
    }
    case _ => error("Unexpected beginnig: " + lines.take(2).toList)
  }

  def filename: String = lines.head match {
    case File(fname) => fname
    case other => error("Unexpected first line: " + other)
  }

    def readUpToTag(ls: Stream[Line],
                    acc: Stream[Line],
                    linenumber: Int,
                    endTag: Line):
      (Stream[Line],Stream[Line],Int) = ls match {
        case Stream.empty =>
          error("Expected end tag but found end of stream")
        case Stream.cons(first,rest) =>
          if( first == endTag )
            (acc.reverse,rest,linenumber)
          else first match {
            case NewLine =>
              readUpToTag(rest,
                Stream.cons(first,acc),
                linenumber + 1,endTag)
            case other =>
              readUpToTag(rest,
                Stream.cons(first,acc),
                linenumber,endTag)
          }
    }

    def selectNext(ls: Stream[Line],
                   linenumber: Int): Stream[Block] =
      ls match {
        case Stream.empty => Stream.empty
        case Stream.cons(first,rest) => first match {
          case Doc(n) => documentation(rest,n,linenumber)
          case Code(n) => code(rest,n,linenumber)
          case other => error("Expected begin code or begin doc" +
                              "but found " + other)
        }
      }

    def documentation(ls: Stream[Line],
                      blocknumber: Int,
                      linenumber: Int): Stream[Block] =
      {

  ls match {
    case Stream.empty => error("Unexpected empty doc block")
    case s @ Stream.cons(first,rest) => {
      val (blockLines,cont,nextline) =
      readUpToTag(s,Stream.empty,linenumber,EndDoc(blocknumber))
      Stream.cons(
      DocuBlock(blocknumber,
                linenumber,
                blockLines),
       selectNext(cont,nextline))
     }
   }
  }

    def code(ls: Stream[Line],
             blocknumber: Int,
             linenumber: Int): Stream[Block] = {

        val Stream.cons(defline,Stream.cons(nline,cont)) = ls
        val chunkname = defline match {
          case Definition(name) => name
          case other => error("Expected definition but got " + other)
        }
        val cont2 = nline match {
          case NewLine => cont
          case _ => Stream.cons(nline,cont)
        }
        val linenumber2 = linenumber + 1

      ls match {
        case Stream.empty => error("Unexpected empty code block")
        case Stream.cons(first,rest) =>
          val (lines,continue,lnumber) =
            readUpToTag(cont2,Stream.empty,
                        linenumber2,EndCode(blocknumber))

        Stream.cons(
          CodeBlock(blocknumber,
                    linenumber2,
                    lines,
                    chunkname),selectNext(continue,lnumber))
      }
    }

}


object Blocks {
  def usage: Unit = {
    System.err.println("Usage: scala markup.Blocks [infile]\n")
  }

  def main(args: Array[String]) = {
    import util.conversions._

    val blocks = args.length match {
      case 0 => blocksFromLiterateInput(System.in)
      case 1 => blocksFromLiterateFile(args(0))
      case _ => usage; exit
    }

    blocks foreach {
      b => println(b)
    }
  }
}

