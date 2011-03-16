package scalit.util

object conversions {
  import java.io.{BufferedReader,FileReader,InputStreamReader}
  import scala.util.parsing.input.StreamReader

  import markup.{Line,MarkupGenerator}

  def linesFromLiterateFile(filename: String): Stream[Line] = {
    val input = StreamReader(
                  new BufferedReader(
                  new FileReader(filename)))
    (new MarkupGenerator(input,filename)).lines
  }

  def linesFromLiterateInput(in: java.io.InputStream): Stream[Line] = {
    val input = StreamReader(new InputStreamReader(in))
    (new MarkupGenerator(input,"")).lines
  }

  import markup.MarkupParser
  def linesFromMarkupFile(filename: String): Stream[Line] = {
    val input = StreamReader(
                  new BufferedReader(
                  new FileReader(filename)))

    (new MarkupParser(input)).lines
  }

  def linesFromMarkupInput(in: java.io.InputStream): Stream[Line] = {
    val input = StreamReader(new InputStreamReader(in))
    (new MarkupParser(input)).lines
  }



  import markup.{BlockBuilder,Block}
  def blocksFromLiterateFile(filename: String): Stream[Block] =
    BlockBuilder(linesFromLiterateFile(filename)).blocks

  def blocksFromLiterateInput(in: java.io.InputStream): Stream[Block] =
    BlockBuilder(linesFromLiterateInput(in)).blocks

  def blocksFromMarkupFile(filename: String): Stream[Block] =
    BlockBuilder(linesFromMarkupFile(filename)).blocks

  def blocksFromMarkupInput(in: java.io.InputStream): Stream[Block] =
    BlockBuilder(linesFromMarkupInput(in)).blocks

  import markup.{CodeBlock,DocuBlock}
  def codeblocks(blocks: Stream[Block]): Stream[CodeBlock] =
    (blocks filter {
      case c: CodeBlock => true
      case d: DocuBlock => false
    }).asInstanceOf[Stream[CodeBlock]]
}

