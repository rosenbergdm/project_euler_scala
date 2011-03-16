package scalit.tangle
import markup._

case class CodeChunk(bn: Int, ln: Int,
           cont: Stream[Line],bname: String,
           next: Option[CodeChunk]) extends
CodeBlock(bn,ln,cont,bname) {
  
  import StringRefs._

  def append(that: CodeBlock): CodeChunk = next match {
    case None =>
      CodeChunk(this.blocknumber,
                this.linenumber,
                this.content,
                this.blockname,
                Some(CodeChunk(that.blocknumber,
                    that.linenumber,
                    that.content,
                    that.blockname,None)))
    case Some(next) =>
      CodeChunk(this.blocknumber,
                this.linenumber,
                this.content,
                this.blockname,
                Some(next append that))
  }

  override def stringRefForm(codeChunks: Map[String,CodeBlock]):
    Stream[StringRef] = next match {
      case None => super.stringRefForm(codeChunks)
      case Some(el) => Stream.concat(
        super.stringRefForm(codeChunks),
        el.stringRefForm(codeChunks))
    }
}


import scala.collection.immutable.{Map,HashMap}
case class ChunkCollection(cm: Map[String,CodeChunk],
                         filename: String) {

  import StringRefs._

  def serialize(chunkname: String): String =
    cm get chunkname match {
      case None => error("Did not find chunk " + chunkname)
      case Some(el) => flatten(el.stringRefForm(cm))
    }

  def addBlock(that: CodeBlock): ChunkCollection =
    cm get that.blockname match {
      case None => ChunkCollection(cm +
        (that.blockname ->
         CodeChunk(that.blocknumber,
                  that.linenumber,
                  that.content,
                  that.blockname,
                  None)),filename)
      case Some(el) => ChunkCollection(cm +
        (that.blockname ->
         el.append(that)),filename)
    }

  def addBlocks(those: Stream[CodeBlock]): ChunkCollection =
    (those foldLeft this) {
      (acc: ChunkCollection, n: CodeBlock) =>
        acc.addBlock(n)
    }

  def expandRefs(str: Stream[StringRef]): Stream[RealString] =
    str match {
      case Stream.empty => Stream.empty
      case Stream.cons(first,rest) =>
        first match {
          case r @ RealString(_,_,_) =>
            Stream.cons(r,expandRefs(rest))
          case BlockRef(ref) =>
            Stream.concat(
              expandRefs(cm(ref.blockname).stringRefForm(cm)),
              expandRefs(rest))
          case other => error("Unexpected string ref: " + other)
        }
    }

  def expandedStream(chunkname: String): Stream[RealString] =
    cm get chunkname match {
      case None => error("Did not find chunk " + chunkname)
      case Some(el) => expandRefs(el.stringRefForm(cm))
    }


  private def flatten(str: Stream[StringRef]): String = {
    val sb = new StringBuffer
    expandRefs(str) foreach {
      case RealString(content,_,_) => sb append content
    }
    sb.toString
  }
}

case class emptyChunkCollection(fn: String)
     extends ChunkCollection(Map(),fn)


object Tangle {
  def main(args: Array[String]) = {
    import util.LiterateSettings

    val ls = new LiterateSettings(args)

    val chunksToTake = ls.settings get "-r" match {
      case None => Nil
      case Some(cs) => cs.reverse
    }

    val out = ls.output

    chunksToTake match {
      case Nil =>
        ls.chunkCollections foreach {
          cc => out.println(cc.serialize("*"))
        }

      case cs =>
        cs foreach {
          chunk =>
            ls.chunkCollections foreach {
              cc =>
                try {
                  out.println(cc.serialize(chunk))
                } catch {
                  case e => ()
                }
            }
        }
    }
  }
}

