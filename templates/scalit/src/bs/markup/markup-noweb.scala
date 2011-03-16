package scalit.markup

abstract class Line

case class TextLine(content: String) extends Line {
  override def toString = "@text " + content
}

case object NewLine extends Line {
  override def toString = "@nl"
}

case object Quote extends Line {
  override def toString = "@quote"
}
case object EndQuote extends Line {
  override def toString = "@endquote"
}

case class Code(number: Int) extends Line {
  override def toString = "@begin code " + number
}
case class EndCode(number: Int) extends Line {
  override def toString = "@end code " + number
}

case class Definition(chunkname: String) extends Line {
  override def toString = "@defn " + chunkname
}

case class Use(chunkname: String) extends Line {
  override def toString = "@use " + chunkname
}

case class Doc(number: Int) extends Line {
  override def toString = "@begin docs " + number
}
case class EndDoc(number: Int) extends Line {
  override def toString = "@end docs " + number
}

case class File(filename: String) extends Line {
  override def toString = "@file " + filename
}
case object LastLine extends Line


import scala.util.parsing.input.StreamReader
class MarkupParser(in: StreamReader) {
  def nextToken(input: StreamReader): (Line,StreamReader) = {
    def readLine(inreader: StreamReader,
             acc: List[Char]): (List[Char],StreamReader) =
      if( inreader.atEnd || inreader.first == '\n' )
        (inreader.first :: acc.reverse, inreader.rest )
      else readLine(inreader.rest, inreader.first :: acc )


    in.first match {
      case '@' => {
        val (line,rest) = readLine(input.rest,Nil)
        val directive = (line.tail mkString "" split ' ').toList match {
                          case "file" :: sth => File(sth mkString " ")

                          case "begin" :: "docs" :: number :: Nil =>
                            Doc(Integer.parseInt(number))
                          case "begin" :: "code" :: number :: Nil =>
                            Code(Integer.parseInt(number))

                          case "end" :: "docs" :: number :: Nil =>
                            EndDoc(Integer.parseInt(number))
                          case "end" :: "code" :: number :: Nil =>
                            EndCode(Integer.parseInt(number))

                          case "text" :: content =>
                            TextLine(line drop 6 mkString "")
                          case "nl" :: Nil =>
                            NewLine

                          case "defn" :: chunkname => Definition(chunkname mkString " ")

                          case "use" :: chunkname => Use(chunkname mkString " ")

                          case "quote" :: Nil => Quote
                          case "endquote" :: Nil => EndQuote

                          case "" :: Nil => LastLine
                          case unrecognized => {
                            System.err.println("Unrecognized directive: " +
                                         unrecognized.head.size)
                            println(input.pos)
                            LastLine
                          }
                        }

        (directive,rest)
      }
      case _ =>
        System.err.println("Found a line not beginning" +
                   " with @, but with " +
                   input.first)
        exit(1)
    }
  }

  def lines: Stream[Line] = {
    def lines0(input: StreamReader): Stream[Line] =
    nextToken(input) match {
      case (LastLine,rest) => Stream.empty
      case (line,rest) => Stream.cons(line, lines0(rest))
    }
    lines0(in)
  }

}


object MarkupReader {
  import java.io.{FileReader,BufferedReader,Reader}
  import java.io.InputStreamReader

  def usage =
    println("Usage: scala markup.MarkupReader [infile]")

  def main(args: Array[String]) = {
    val input: Reader = args.length match {
      case 0 => new InputStreamReader(System.in)
      case 1 => new BufferedReader(new FileReader(args(0)))
      case _ => usage; exit
    }
    val markupReader = new MarkupParser(StreamReader(input))
    markupReader.lines foreach {
      line => println(line)
    }
  }
}


class MarkupGenerator(in: StreamReader, filename: String) {
  def lines: Stream[Line] =
    Stream.cons(File(filename),
    Stream.cons(Doc(0),documentation(in,0)))
     def documentation(inp: StreamReader,
                  docnumber: Int): Stream[Line] = {
       def docAcc(input: StreamReader,
               acc: List[Char]): Stream[Line] =
          input.first match {
            case '[' => input.rest.first match {
              case '[' =>
                val (content,continue) = quote(input.rest.rest)
                acc match {
                  case Nil =>
                    Stream.concat(
                      Stream.cons(Quote,content),
                      Stream.cons(EndQuote,
                                docAcc(continue,Nil)))
                  case _   =>
                    Stream.concat(
                      Stream.cons(TextLine(acc.reverse mkString ""),
                      Stream.cons(Quote,
                               content)),
                      Stream.cons(EndQuote,
                               docAcc(continue,Nil)))
                }
           case _ => docAcc(input.rest, input.first :: acc)
         }

          case '@' => acc match {
            case Nil =>
              if( input.rest.first == '\n' ||
                         input.rest.first == ' ' )
                Stream.cons(EndDoc(docnumber),
                Stream.cons(Doc(docnumber + 1),
                         documentation(input.rest.rest,docnumber + 1)))
              else
                docAcc(input.rest, List('@'))
            case _ => docAcc(input.rest, '@' :: acc)
          }

          case '<' => input.rest.first match {
            case '<' => acc match {
              case x :: xs =>
                   error("Unescaped << in doc mode")
              case Nil =>
                  val (chunkName,continue) = chunkDef(input.rest.rest)
                  Stream.cons(EndDoc(docnumber),
                  Stream.cons(Code(docnumber + 1),
                    code(continue,chunkName,docnumber+1)))
            }
            case _ => docAcc(input.rest, '<' :: acc)
          }

          case c =>
            if( c == '\n' ) {
              Stream.cons(TextLine(acc.reverse mkString ""),
              Stream.cons(NewLine,docAcc(input.rest,Nil)))
            } else {
              if( !input.atEnd ) docAcc(input.rest,input.first :: acc)
              else acc match {
                case Nil => Stream.cons(EndDoc(docnumber),
                                      Stream.empty)
                case _ =>
                  Stream.cons(TextLine(acc.reverse mkString ""),
                  Stream.cons(NewLine,
                  Stream.cons(EndDoc(docnumber),Stream.empty)))
              }
            }
          }


       docAcc(inp,Nil)
     }

  def quote(inp: StreamReader): (Stream[Line], StreamReader) = {
    def quoteAcc(input: StreamReader, acc: List[Char]):
       (Stream[Line], StreamReader) =
   input.first match {
     case ']' => input.rest.first match {
       case ']' => input.rest.rest.first match {
         case ']' => quoteAcc(input.rest,']' :: acc)
         case _ => acc match {
           case Nil => (Stream.empty,input.rest.rest)
           case _ => (Stream.cons(TextLine(acc.reverse mkString ""),
                                  Stream.empty),
                      input.rest.rest)
         }
       }
       case _ => quoteAcc(input.rest,']' :: acc)
     }
     case '\n' => acc match {
       case Nil => val (more,contreader) = quoteAcc(input.rest,Nil)
                   (Stream.cons(NewLine,more),contreader)
       case _ => val (more,contreader) = quoteAcc(input.rest,Nil)
                 (Stream.cons(TextLine(acc.reverse mkString ""),
                 Stream.cons(NewLine, more)),contreader)
     }
     case c => quoteAcc(input.rest, c :: acc)
   }
         quoteAcc(inp,Nil)
  }

  def code(inp: StreamReader, chunkname: String, codenumber: Int):
    Stream[Line] = {
          def isNewCodeChunk(input: StreamReader): Boolean =
            input.first match {
              case '>' => input.rest.first match {
                case '>' => input.rest.rest.first match {
                  case '=' => true
                  case _ => false
                }
                case _ => isNewCodeChunk(input.rest)
              }
              case c =>
                if( c == '\n' )
                  false
                else isNewCodeChunk(input.rest)
          }

          def isNewUseDirective(input: StreamReader): Boolean =
          input.first match {
            case '>' => input.rest.first match {
              case '>' => input.rest.rest.first match {
                case '=' => false
                case _   => true
              }
              case _ => isNewUseDirective(input.rest)
            }
            case c =>
              if( c == '\n' ) false
              else isNewUseDirective(input.rest)
          }

    def codeAcc(input: StreamReader, acc: List[Char]):
      Stream[Line] = input.first match {
        case '<' => input.rest.first match {
          case '<' =>
            acc match {
              case Nil =>
              if( isNewCodeChunk(input.rest.rest) ) {
                val (chunkName,continue) =
                  chunkDef(input.rest.rest)
                Stream.cons(EndCode(codenumber),
                Stream.cons(Code(codenumber + 1),
                  code(continue,
                  chunkName,
                  codenumber + 1)))
              } else if( isNewUseDirective(input.rest) ) {
                val (usename,cont) = use(input.rest.rest)
                Stream.cons(Use(usename),
                    codeAcc(cont,Nil))
              } else {
                codeAcc(input.rest,'<' :: acc)
              }
              case _ =>
              if( isNewUseDirective(input.rest) ) {
                val (usename,cont) = use(input.rest.rest)
                Stream.cons(TextLine(acc.reverse mkString ""),
                Stream.cons(Use(usename),
                  codeAcc(cont, Nil)))
              } else {
                codeAcc(input.rest, '<' :: acc)
              }
            }
            case _ => codeAcc(input.rest, '<' :: acc)
          }

  case '@' =>
    if( input.rest.first == ' ' ||
       input.rest.first == '\n' )
      acc match {
        case Nil => Stream.cons(EndCode(codenumber),
                    Stream.cons(Doc(codenumber + 1),
                    documentation(input.rest.rest,
                                  codenumber + 1)))
        case _ => codeAcc(input.rest, '@' :: acc)
      }
    else codeAcc(input.rest, '@' :: acc)

  case c =>
    if( c == '\n' ) {
      val tl = TextLine(acc.reverse mkString "")
      Stream.cons(tl,
      Stream.cons(NewLine,codeAcc(input.rest,Nil)))

    } else {
      if( input.atEnd )
        acc match {
          case Nil => Stream.cons(EndCode(codenumber),
                                  Stream.empty)
          case _ =>
            Stream.cons(TextLine(acc.reverse mkString ""),
            Stream.cons(NewLine,
            Stream.cons(EndCode(codenumber),Stream.empty)))
        }
      else if( c == '\t' )
        codeAcc(input.rest,tab ::: acc )
      else
        codeAcc(input.rest,c :: acc )
    }
  }
      
  Stream.cons(Definition(chunkname),
  Stream.cons(NewLine,
          codeAcc(inp.rest,Nil)))
  }


  def chunkDef(inp: StreamReader): (String, StreamReader) = {
    def chunkAcc(input: StreamReader, acc: List[Char]):
    (String, StreamReader) =
    input.first match {
      case '>' => input.rest.first match {
        case '>' => input.rest.rest.first match {
        case '=' => ((acc.reverse mkString ""),input.rest.rest.rest)
        case _ => System.err.println("Unescaped"); exit
      }
      case _ => chunkAcc(input.rest, '>' :: acc)
    }
    case c => chunkAcc(input.rest, c :: acc)
    }

    chunkAcc(inp, Nil)
  }

    def use(inp: StreamReader): (String, StreamReader) = {
      def useAcc(input: StreamReader, acc: List[Char]):
            (String,StreamReader) = input.first match {
        case '>' => input.rest.first match {
          case '>' => (acc.reverse mkString "",input.rest.rest)
          case _ => useAcc(input.rest, '>' :: acc)
        }
        case c => useAcc(input.rest, c :: acc)
      }

      useAcc(inp,Nil)
    }



    val tab = (1 to 8 map { x => ' ' }).toList

}


object Markup {

  def usage: Unit = {
    System.err.println("Usage: scala markup.Markup [infile]\n")
  }

  def main(args: Array[String]) = {
    import util.LiterateSettings

    val settings = new LiterateSettings(args)

    val listlines: List[Stream[Line]] = settings.lines
    listlines foreach {
      linestream => linestream foreach println
    }
  }
}

