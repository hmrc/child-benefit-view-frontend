package utils

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.i18n.Messages.MessageSource

import scala.io.Source

class MessagesSpec extends AnyFreeSpec with Matchers {

  private val MatchSingleQuoteOnly = """\w+'{1}\w+""".r
  private val MatchBacktickQuoteOnly = """`+""".r

  private val englishMessages = parseMessages("conf/messages.en").filterNot(message => message._1.startsWith("pdf"))
  private val welshMessages = parseMessages("conf/messages.cy")

  "All message files" - {
    "have the same set of keys" in {
      withClue(describeMismatch(englishMessages.keySet, welshMessages.keySet)) {
        welshMessages.keySet mustBe englishMessages.keySet
      }
    }

    "have a non-empty message for each key" in {
      assertNonEmpty("English", englishMessages)
      assertNonEmpty("Welsh", welshMessages)
    }

    "have no unescaped single quotes in value" in {
      assertCorrectUseOfQuotes("English", englishMessages)
      assertCorrectUseOfQuotes("Welsh", welshMessages)
    }

  }

  private def parseMessages(filename: String): Map[String, String] =
    Messages.parse(
      new MessageSource {
        override def read: String = Source.fromFile(filename).mkString
      },
      filename
    ) match {
      case Right(messages) => messages
      case Left(e)         => throw e
    }

  private def assertNonEmpty(label: String, messages: Map[String, String]): Unit =
    messages.foreach { case (key: String, value: String) =>
      withClue(
        s"In $label, there is an empty value for the key:[$key][$value]"
      ) {
        value.trim.isEmpty mustBe false
      }
    }

  private def assertCorrectUseOfQuotes(label: String, messages: Map[String, String]): Unit =
    messages.foreach { case (key: String, value: String) =>
      withClue(
        s"In $label, there is an unescaped or invalid quote:[$key][$value]"
      ) {
        MatchSingleQuoteOnly.findFirstIn(value).isDefined mustBe false
        MatchBacktickQuoteOnly.findFirstIn(value).isDefined mustBe false
      }
    }

  private def listMissingMessageKeys(header: String, missingKeys: Set[String]) = {
    val displayLine = "\n" + ("@" * 42) + "\n"
    missingKeys.toList.sorted.mkString(header + displayLine, "\n", displayLine)
  }

  private def describeMismatch(englishKeySet: Set[String], welshKeySet: Set[String]) = {

    if (englishKeySet.size > welshKeySet.size)
      listMissingMessageKeys(
        "The following message keys are missing from the Welsh Set:",
        englishKeySet -- welshKeySet
      )
    else
      listMissingMessageKeys(
        "The following message keys are missing from the English Set:",
        welshKeySet -- englishKeySet
      )
  }
}