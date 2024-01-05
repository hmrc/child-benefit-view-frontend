/*
 * Copyright 2024 HM Revenue & Customs
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

package generators.modelgenerators

import generators.DataGenerators
import models.common.{ChildReferenceNumber, FirstForename, Surname}
import models.ftnae._
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.alphaStr

import java.time.LocalDate

trait FTNAEGenerators extends DataGenerators with CommonGenerators {
  implicit lazy val arbitraryChildDetails: Arbitrary[ChildDetails] =
    Arbitrary {
      for {
        courseDuration   <- arbitrary[CourseDuration]
        crn              <- arbitrary[ChildReferenceNumber]
        dateOfBirth      <- arbitrary[LocalDate]
        whichYoungPerson <- generateName
        ftnaeQA <-
          Gen.containerOf[List, FtnaeQuestionAndAnswer](arbitrary[FtnaeQuestionAndAnswer]) suchThat (x => x.nonEmpty)

      } yield ChildDetails(courseDuration, crn, dateOfBirth, whichYoungPerson, ftnaeQA)
    }
  implicit lazy val arbitraryCourseDuration: Arbitrary[CourseDuration] =
    Arbitrary {
      Gen.oneOf(CourseDuration.OneYear, CourseDuration.TwoYear)
    }
  implicit lazy val arbitraryFtnaeChildInfo: Arbitrary[FtnaeChildInfo] =
    Arbitrary {
      for {
        crn          <- arbitrary[ChildReferenceNumber]
        firstName    <- arbitrary[FirstForename]
        midName      <- arbitrary[SecondForename]
        lastName     <- arbitrary[Surname]
        dateOfBirth  <- arbitrary[LocalDate]
        claimEndDate <- arbitrary[LocalDate]
      } yield FtnaeChildInfo(crn, firstName, Some(midName), lastName, dateOfBirth, claimEndDate)
    }
  implicit lazy val arbitraryFtnaeClaimantInfo: Arbitrary[FtnaeClaimantInfo] =
    Arbitrary {
      for {
        firstname <- arbitrary[FirstForename]
        surname   <- arbitrary[Surname]
      } yield FtnaeClaimantInfo(firstname, surname)
    }
  implicit lazy val arbitraryFtnaeQuestionAndAnswer: Arbitrary[FtnaeQuestionAndAnswer] =
    Arbitrary {
      for {
        question <- alphaStr
        answer   <- alphaStr
      } yield FtnaeQuestionAndAnswer(question, answer)
    }
  implicit lazy val arbitraryFtnaeResponse: Arbitrary[FtnaeResponse] =
    Arbitrary {
      for {
        claimant <- arbitrary[FtnaeClaimantInfo]
        children <- Gen.containerOf[List, FtnaeChildInfo](arbitrary[FtnaeChildInfo]) suchThat (x => x.nonEmpty)
      } yield FtnaeResponse(claimant, children)
    }
  implicit lazy val arbitraryHowManyYears: Arbitrary[HowManyYears] =
    Arbitrary {
      Gen.oneOf(HowManyYears.values)
    }
  implicit lazy val arbitrarySecondForename: Arbitrary[SecondForename] =
    Arbitrary {
      for {
        name <- generateName
      } yield SecondForename(name)
    }
}
