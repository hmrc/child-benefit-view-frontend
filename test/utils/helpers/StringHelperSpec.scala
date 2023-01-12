/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils.helpers

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._

class StringHelperSpec extends AnyFreeSpec with Matchers {
  "toTitleCase" - {
    val delimitedTestCases = Table(
      ("caseName", "initial", "delimiters", "exceptedWords", "expected"),
      ("whitespace", "test value", List(" "), List(), "Test Value"),
      ("hyphen", "test-value", List("-"), List(), "Test-Value"),
      ("apostrophe", "test'value", List("'"), List(), "Test'Value"),
      ("multiple delimiters", "test-va lu'e", List(" ", "-", "'"), List(), "Test-Va Lu'E"),
      ("excepted words ignored", "test and value", List(" "), List("and"), "Test and Value")
    )
    "For a given initial value and set of delimiters: return the expected value" - {
      forAll(delimitedTestCases) {
        (caseName: String, initial: String, delimiters: List[String], exceptedWords: List[String], expected: String) =>
          s"$caseName: initial: $initial - delimiters: $delimiters -> expected: $expected" in {
            val result = StringHelper.toTitleCase(initial, delimiters, exceptedWords)
            result mustEqual expected
          }
      }
    }

    val defaultTestCases = Table(
      ("caseName", "initial", "expected"),
      ("whitespace", "test value", "Test Value"),
      ("hyphen", "test-value", "Test-Value"),
      ("apostrophe", "test'value", "Test'Value"),
      ("multiple delimiters", "test-va lu'e", "Test-Va Lu'E"),
      ("excepted words ignored", "test and value", "Test and Value")
    )
    "When given no delimiters, default are used as expected" - {
      forAll(defaultTestCases) { (caseName: String, initial: String, expected: String) =>
        s"$caseName: initial: $initial" in {
          val result = StringHelper.toTitleCase(initial)
          result mustEqual expected
        }
      }
    }
  }
}
