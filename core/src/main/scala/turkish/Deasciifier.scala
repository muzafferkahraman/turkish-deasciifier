package turkish

import scala.annotation.tailrec

object Deasciifier {

  private val ContextSize = 10

  private val TurkishToggleAccentTable: Map[Char, Char] = Map(
    'c' -> 'ç',
    'C' -> 'Ç',
    'g' -> 'ğ',
    'G' -> 'Ğ',
    'o' -> 'ö',
    'O' -> 'Ö',
    'u' -> 'ü',
    'U' -> 'Ü',
    'i' -> 'ı',
    'I' -> 'İ',
    's' -> 'ş',
    'S' -> 'Ş',
    'ç' -> 'c',
    'Ç' -> 'C',
    'ğ' -> 'g',
    'Ğ' -> 'G',
    'ö' -> 'o',
    'Ö' -> 'O',
    'ü' -> 'u',
    'Ü' -> 'U',
    'ı' -> 'i',
    'İ' -> 'I',
    'ş' -> 's',
    'Ş' -> 'S'
  )

  private val TurkishAsciifyTable: Map[Char, Char] = Map(
    'ç' -> 'c',
    'Ç' -> 'C',
    'ğ' -> 'g',
    'Ğ' -> 'G',
    'ö' -> 'o',
    'Ö' -> 'O',
    'ı' -> 'i',
    'İ' -> 'I',
    'ş' -> 's',
    'Ş' -> 'S'
  )

  private val AsciiUppercaseLetters: Seq[Char] = Seq(
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
    'U', 'V', 'W', 'X', 'Y', 'Z'
  )

  private val TurkishDowncaseAsciifyTable: Map[Char, Char] =
    AsciiUppercaseLetters.map(c => (c, c.toLower)).toMap ++
      AsciiUppercaseLetters.map(c => (c.toLower, c.toLower)).toMap ++
      Map(
        'ç' -> 'c',
        'Ç' -> 'c',
        'ğ' -> 'g',
        'Ğ' -> 'g',
        'ö' -> 'o',
        'Ö' -> 'o',
        'ı' -> 'i',
        'İ' -> 'i',
        'ş' -> 's',
        'Ş' -> 's',
        'ü' -> 'u',
        'Ü' -> 'u'
      )
  private val TurkishUpcaseAsciifyTable: Map[Char, Char] =
    AsciiUppercaseLetters.map(c => (c, c.toLower)).toMap ++
      AsciiUppercaseLetters.map(c => (c.toLower, c.toLower)).toMap ++
      Map(
        'ç' -> 'C',
        'Ç' -> 'C',
        'ğ' -> 'G',
        'Ğ' -> 'G',
        'ö' -> 'O',
        'Ö' -> 'O',
        'ı' -> 'I',
        'İ' -> 'i',
        'ş' -> 'S',
        'Ş' -> 'S',
        'ü' -> 'U',
        'Ü' -> 'U'
      )

  def apply(patternTable: PatternTable): Deasciifier = new Deasciifier(patternTable)

}

final class Deasciifier(patternTable: PatternTable) {

  import Deasciifier._

  private lazy val initialPattern = replaceWith(ContextSize, " ".repeat(2 * ContextSize + 1), 'X')

  private def getContext(index: Int, text: String): String = {
    @tailrec
    def rightLoop(patternIndex: Int, textIndex: Int, pattern: String): String =
      if (patternIndex < pattern.length && textIndex < text.length) {
        val currentChar = text.charAt(textIndex)

        TurkishDowncaseAsciifyTable.get(currentChar) match {
          case None => pattern.substring(0, patternIndex + 1)
          case Some(targetChar) => rightLoop(patternIndex + 1, textIndex + 1, replaceWith(patternIndex, pattern, targetChar))
        }
      } else pattern.substring(0, patternIndex)

    val pattern = rightLoop(ContextSize + 1, index + 1, initialPattern)

    @tailrec
    def leftLoop(patternIndex: Int, textIndex: Int, pattern: String): String =
      if (patternIndex >= 0 && textIndex >= 0) {
        val currentChar = text.charAt(textIndex)
        TurkishUpcaseAsciifyTable.get(currentChar) match {
          case None => pattern
          case Some(targetChar) => leftLoop(patternIndex - 1, textIndex - 1, replaceWith(patternIndex, pattern, targetChar))
        }
      } else pattern

    leftLoop(ContextSize - 1, index - 1, pattern)

  }

  private def matchPattern(decisionList: Map[String, Int], index: Int, text: String): Boolean = {
    val context = getContext(index, text)

    @tailrec
    def loop(start: Int, accRank: Int): Int = {
      @tailrec
      def innerLoop(end: Int, innerRank: Int): Int =
        if (end > context.length) innerRank
        else {
          val candidate = context.substring(start, end)
          val maybeRank = decisionList.get(candidate)
          innerLoop(
            end + 1,
            maybeRank.fold(innerRank) { newRank =>
              if (Math.abs(newRank) < Math.abs(accRank)) newRank
              else innerRank
            }
          )
        }

      if (start > ContextSize) accRank
      else loop(start + 1, innerLoop(ContextSize + 1, accRank))
    }

    loop(0, decisionList.size * 2) > 0
  }

  private def needsCorrection(index: Int, text: String): Boolean = {
    val inputChar = text.charAt(index)
    val onlyAscii = TurkishAsciifyTable.getOrElse(inputChar, inputChar)
    val maybePattern = patternTable.get(onlyAscii.toLower)
    val matches = maybePattern.fold(false)(pattern => matchPattern(pattern, index, text))
    matches && (onlyAscii == 'I' ^ inputChar == onlyAscii)
  }

  private def toggleAccent(index: Int, text: String): String = {
    val target = text.charAt(index)
    replaceWith(index, text, TurkishToggleAccentTable.getOrElse(target, target))
  }

  private def replaceWith(index: Int, text: String, char: Char): String =
    text.toCharArray.updated(index, char).mkString

  def convertToTurkish(inputText: String): String = {
    (0 until inputText.length).foldLeft(inputText) {
      case (accText, index) => if (needsCorrection(index, accText)) toggleAccent(index, accText) else accText
    }
  }

}