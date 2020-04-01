package CodingChallenge
import org.clulab.odin._
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.corenlp.CoreNLPProcessor

import scala.io.Source
import utils._

object CodingChallenge extends App {

  if (args.length == 0) {
    println("Required argument missing: <File name>")
  }

  val currentDirectory = new java.io.File(".").getCanonicalPath
  val text = Source.fromFile(args(0)).mkString

  // read rules from general-rules.yml file in resources
  val source = Source.fromFile("src/main/grammar/example.yml")
  val rules = source.mkString
  source.close()

  // creates an extractor engine using the rules and the default actions
  val extractor = ExtractorEngine(rules)

  // annotate the sentences
  val proc = new CluProcessor()
  val doc = proc.annotate(text)

  // extract mentions from annotated document
  val mentions = extractor.extractFrom(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))

  // display the mentions
  displayMentions(mentions, doc)

}


