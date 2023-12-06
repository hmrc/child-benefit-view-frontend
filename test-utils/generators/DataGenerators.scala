package generators

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Gen.{alphaChar, alphaNumStr, alphaStr, choose, numStr, stringOf}

trait DataGenerators {
  private val MAX_ID_LENGTH = 20
  val generateId: Gen[String] = alphaNumStr.map(_.take(MAX_ID_LENGTH))

  private val NINO_NUMERIC_LENGTH = 6
  val generateNino: Gen[String] = for {
    pre1    <- stringOf(alphaChar)
    pre2    <- stringOf(alphaChar)
    numeric <- numStr.suchThat(_.length >= NINO_NUMERIC_LENGTH)
    suffix  <- stringOf(alphaChar)
  } yield s"$pre1$pre2${numeric.take(NINO_NUMERIC_LENGTH)}$suffix"
  val generateReferenceNumber: Gen[String] = for {
    pre1    <- stringOf(alphaChar)
    pre2    <- stringOf(alphaChar)
    numeric <- numStr.suchThat(_.length >= NINO_NUMERIC_LENGTH)
  } yield s"$pre1$pre2${numeric.take(NINO_NUMERIC_LENGTH)}"

  private val MAX_NAME_LENGTH = 30
  val generateName: Gen[String] = alphaStr.map(_.take(MAX_NAME_LENGTH))

  private val SORT_CODE_LENGTH = 6
  val generateSortCode: Gen[String] = numStr.suchThat(_.length >= SORT_CODE_LENGTH).map(_.take(SORT_CODE_LENGTH))

  private val ACCOUNT_NUMBER_CODE_LENGTH = 8
  val generateAccountNumber: Gen[String] =
    numStr.suchThat(_.length >= ACCOUNT_NUMBER_CODE_LENGTH).map(_.take(ACCOUNT_NUMBER_CODE_LENGTH))

  private val MIN_BUILDING_SOCIETY_NUMBER_CODE_LENGTH = 1
  private val MAX_BUILDING_SOCIETY_NUMBER_CODE_LENGTH = 18
  val generateBuildingSocietyNumber: Gen[String] =
    alphaNumStr
      .suchThat(_.length >= MIN_BUILDING_SOCIETY_NUMBER_CODE_LENGTH)
      .map(_.take(MAX_BUILDING_SOCIETY_NUMBER_CODE_LENGTH))

  private val MIN_ADDRESS_LINE_LENGTH = 15
  private val MAX_ADDRESS_LINE_LENGTH = 30
  val generateAddressLine: Gen[String] =
    alphaStr.suchThat(_.length >= MIN_ADDRESS_LINE_LENGTH).map(_.take(MAX_ADDRESS_LINE_LENGTH))

  val generatePostCode: Gen[String] =
    for {
      a1 <- stringOf(alphaChar)
      a2 <- stringOf(alphaChar)
      n1 <- choose[Int](1, 99)
      a3 <- stringOf(alphaChar)
      n2 <- choose[Int](1, 9)
      n3 <- choose[Int](1, 9)
    } yield s"$a1$a2$n1 $a3$n2$n3"
}
