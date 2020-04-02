package CodingChallenge
import org.clulab.odin._
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.corenlp.CoreNLPProcessor

import scala.collection.mutable.Map 
import scala.io.Source
import utils._

import java.io.File 
import java.io.PrintWriter 

object CodingChallenge extends App {

  if (args.length == 0) {
    println("Required argument missing: <File name>")
  }

  val currentDirectory = new java.io.File(".").getCanonicalPath
  val text = Source.fromFile(args(0)).mkString

  // read rules from general-rules.yml file in resources
  val source = Source.fromFile("src/main/resources/grammar/master.yml")
  val rules = source.mkString
  source.close()

  // creates an extractor engine using the rules and the default actions
  val extractor = ExtractorEngine(rules)

  // annotate the sentences
  val proc = new CluProcessor
  val doc = proc.annotate(text)

  // for (sentence <- doc.sentences) {
  //     sentence.entities.foreach(entities => println(s"Named entities: ${entities.mkString(" ")}"))
  //     sentence.lemmas.foreach(lemmas => println(s"Lemmas: ${lemmas.mkString(" ")}"))
  // }

  // extract mentions from annotated document
  val mentions = extractor.extractFrom(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))

  // display the mentions
  // displayMentions(mentions, doc)

  // Write relations to event relations to file
  // println("The following event relations where found:")
  val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
  val output_writer = new PrintWriter(new File("ouput.tsv"))
  output_writer.write("Experiencer\tTrigger\tStimulus\n")

  for ((sentence, i) <- doc.sentences.zipWithIndex) {

    // Gather Mentions
    val sortedMentions = mentionsBySentence(i).sortBy(_.label)
    val (events, entities) = sortedMentions.partition(_ matches "Event")

    // Get values for all events for the sentence
    for (event <- events) {

      // Store values in a map for easy access
      val values:Map[String, String] = Map()

      event match{
        case em: EventMention =>
          // println(s"\ttrigger: ${em.trigger.text}")
          values += ("trigger" -> em.trigger.text)
          em.arguments foreach {
            case (argname, ms) =>
              ms foreach { v =>
                // println(s"\t$argname: ${v.text}")
                values += (argname -> v.text)
              }
            }
      }

      // Write to file in tsv format
      // println(s"${values("experiencer")}\t${values("trigger")}\t${values("stimulus")}\n")
      output_writer.write(s"${values("experiencer")}\t${values("trigger")}\t${values("stimulus")}\n")
      output_writer.flush()
    }
  }

  // Cleanup
  output_writer.flush()
  output_writer.close()

}


