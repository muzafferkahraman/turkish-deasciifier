package turkish

import cats.effect.{ExitCode, IO, IOApp, Resource}
import io.circe.parser._
import scala.io.{Source, StdIn}

object DeasciifierApp extends IOApp {

  private def loadPatternFile(fileName: String): Resource[IO, Source] = Resource.fromAutoCloseable(
    IO(Source.fromResource(fileName))
  )

  private def parsePatterns(source: Source): IO[PatternTable] = {
    val json = source.mkString

    def isPatternTableValid(table: Map[String, _]): Boolean = table.keySet.forall(_.length == 1)

    IO.fromEither(decode[Map[String, Map[String, Int]]](json)).flatMap { table =>
      if (isPatternTableValid(table)) IO.pure(table.keySet.map(k => k.charAt(0) -> table(k)).toMap)
      else IO.raiseError(new RuntimeException("Pattern file contains one or more invalid keys"))
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val patternFile = args.headOption.getOrElse("defaultTurkishPatternTable.json")
    for {
      patterns  <- loadPatternFile(patternFile).use(parsePatterns)
      inputText <- IO(StdIn.readLine())
      _         <- IO(println(Deasciifier(patterns).convertToTurkish(inputText)))
    } yield ExitCode.Success
  }
}
