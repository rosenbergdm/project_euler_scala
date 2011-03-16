/*
 * Initializes a literate program source file and a line number for
 * testing purposes.
 */
import util.conversions._
import tangle._
val blocks = blocksFromLiterateFile("tangle/compilesupport.nw")
val chunks = emptyChunkCollection addBlocks codeblocks(blocks)
val sf = new LiterateProgramSourceFile(chunks)
import scala.tools.nsc.util.LinePosition
val lp = LinePosition(sf,7)
val ct = new CoTangle(chunks)
