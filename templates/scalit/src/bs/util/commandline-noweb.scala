package scalit.util
import scalit.markup._

object LiterateSettings {
    def getArgs(args: List[String], settings: Map[String,List[String]],
                lines: List[Stream[Line]]) :
        (Map[String,List[String]],List[Stream[Line]]) = args match {

  case "-m" :: markupfile :: xs => {
    val mlines = conversions.linesFromMarkupFile(markupfile)
    getArgs(xs,settings,mlines :: lines)
  }

  case "-li" :: Nil => {
    val llines = conversions.linesFromLiterateInput(System.in)
    (settings, lines.reverse ::: List(llines))
  }
  case "-mi" :: Nil => {
    val mlines = conversions.linesFromMarkupInput(System.in)
    (settings,lines.reverse ::: List(mlines))
  }

  case opt :: arg :: xs =>
    if( opt(0) == '-' )
      getArgs(xs,settings +
              (opt -> (arg :: settings.getOrElse(opt,Nil))),
             lines)
    else {
      val llines = conversions.linesFromLiterateFile(opt)
      getArgs(arg :: xs, settings, llines :: lines)
    }

  case litfile :: xs => {
    val llines = conversions.linesFromLiterateFile(litfile)
    getArgs(xs,settings,llines :: lines)
  }
  case Nil => (settings,lines.reverse)
  }

}

class LiterateSettings(val settings: Map[String,List[String]],
                       ls: List[Stream[Line]]) {
    def this(p: (Map[String,List[String]],List[Stream[Line]])) =
      this(p._1, p._2)

    def this(args: Array[String]) =
      this(LiterateSettings.getArgs(args.toList,Map(),Nil))


   lazy val output: java.io.PrintStream =
     settings get "-o" match {
       case None => System.out
       case Some(List(file)) => new java.io.PrintStream(
         new java.io.FileOutputStream(file)
       )
     }

    import scalit.util.{MarkupFilter,BlockFilter}
    val markupFilters: List[MarkupFilter] =
      settings get ("-lfilter") match {
        case None => Nil
        case Some(xs) => {

  xs map {
    name =>
      try {
        val filterClass = Class.forName(name)
        filterClass.newInstance.asInstanceOf[MarkupFilter]
      } catch {
        case ex =>
          Console.err.println("Could not load filter " + name)
          System.exit(1)
          new util.tee
      }
     }
    }
  }


  lazy val blockFilters: List[BlockFilter] =
    settings get "-bfilter" match {
      case None => Nil
      case Some(xs) => xs map {
        name =>
          try {
            val filterClass = Class.forName(name)
            filterClass.newInstance.asInstanceOf[BlockFilter]
          } catch {
            case e =>
              Console.err.println("Could not load" +
              " block filter " + name)
              System.exit(1)
              new util.stats
          }
      }
    }

    lazy val lines: List[Stream[Line]] = ls map {
      markupStream: Stream[Line] => markupStream
        (markupFilters foldLeft markupStream) {
          (acc: Stream[Line], f: MarkupFilter) => f(acc)
        }
    }

    val blocks: List[(Stream[markup.Block],String)] = lines map {
      l => {
        val bb = BlockBuilder(l)
        val filteredBlocks: Stream[Block] =
          (blockFilters foldLeft bb.blocks) {
            (acc: Stream[Block],f: BlockFilter) => f(acc)
          }
        (filteredBlocks,bb.filename)
      }
    }

  import scalit.tangle.emptyChunkCollection
  lazy val chunkCollections = blocks map {
    case (bs,name) =>
    emptyChunkCollection(name) addBlocks conversions.codeblocks(bs)
  }
}

