package scalit.tangle

class CoTangle(sourceFiles: List[LiterateProgramSourceFile],
             destination: Option[String]) {
    import scala.tools.nsc.{Global,Settings,reporters}
    import reporters.ConsoleReporter

    val settings = new Settings()
    destination match {
      case Some(dd) => {
        settings.outdir.tryToSet(List("-d",dd))
      }
      case None => ()
    }
    val reporter =
      new ConsoleReporter(settings,null,
                          new java.io.PrintWriter(System.err))
    val compiler = new Global(settings,reporter)

    def compile: Global#Run = {
      val r = new compiler.Run

      r.compileSources(sourceFiles)
      if( compiler.globalPhase.name != "terminal" ) {
        System.err.println("Compilation failed")
        System.exit(2)
      }

      r
    }

}


import scala.tools.nsc.util.{BatchSourceFile,Position,LinePosition}
class LiterateProgramSourceFile(chunks: ChunkCollection)
  extends BatchSourceFile(chunks.filename,
                          chunks.serialize("*").toArray) {
    val lines2orig = new scala.collection.mutable.HashMap[Int,Int]()

    import scalit.markup.StringRefs._
    lazy val codeblocks: Stream[RealString] =
      chunks.expandedStream("*")

    def findOrigLine(ol: Int): Int =
      if( lines2orig contains ol ) lines2orig(ol)
      else {
        def find0(offset: Int,
            search: Stream[RealString]): Int = search match {
          case Stream.empty => error("Could not find line for " + ol)
          case Stream.cons(first,rest) =>
            first match {
              case RealString(cont,from,to) => {
                val diff = to - from
                if( ol >= offset && ol <= offset + diff ) {
                  val res = from + (ol - offset)
                  lines2orig += (ol -> res)
                  res
                } else
                  find0(offset + diff,rest)
              }
            }
        }

        find0(0,codeblocks)
      }

    import scala.tools.nsc.util.{SourceFile,CharArrayReader}
    lazy val origSourceFile = {
      val f = new java.io.File(chunks.filename)
      val inf = new java.io.BufferedReader(
        new java.io.FileReader(f))
      val arr = new Array[Char](f.length().asInstanceOf[Int])
      inf.read(arr,0,f.length().asInstanceOf[Int])
      new BatchSourceFile(chunks.filename,arr)
    }

    override def positionInUltimateSource(position: Position) = {
      val line = position.line match {
        case None => 0
        case Some(l) => l
      }
      val col = position.column match {
        case None => 0
        case Some(c) => c
      }
      val literateLine = findOrigLine(line)
      LineColPosition(origSourceFile,literateLine,col)
    }

}


import scala.tools.nsc.util.SourceFile
case class LineColPosition(source0: SourceFile, line0: Int,
                    column0: Int) extends Position {
  override def offset = None
  override def column: Option[Int] = Some(column0)
  override def line: Option[Int]   = Some(line0)
  override def source = Some(source0)
}


object LitComp {
  def main(args: Array[String]): Unit = {
    import scalit.util.LiterateSettings

    val ls = new LiterateSettings(args)

    val sourceFiles = ls.chunkCollections map {
      cc => new LiterateProgramSourceFile(cc)
    }

    val destinationDir: Option[String] =
      ls.settings get "-d" match {
        case Some(x :: xs) => Some(x)
        case _ => None
      }

    val cotangle = new CoTangle(sourceFiles,
                              destinationDir)

    cotangle.compile
  }
}


object LiterateCompilerSupport {
  def getLiterateSourceFile(filename: String): BatchSourceFile = {
    import scalit.util.conversions
    val cbs = conversions.codeblocks(conversions.blocksFromLiterateFile(filename))
    val chunks = emptyChunkCollection(filename) addBlocks cbs
    new LiterateProgramSourceFile(chunks)
  }
}

