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

package forms.behaviours

import org.scalacheck.Gen
import play.api.data.{Form, FormError}

trait StringFieldBehaviours extends FieldBehaviours {

  def fieldWithMaxLength(
      form:                     Form[?],
      fieldName:                String,
      maxLength:                Int,
      lengthError:              FormError,
      maxLengthStringGenerator: Int => Gen[String] = stringsLongerThan
  ): Unit = {

    s"not bind strings longer than $maxLength characters" in {

      forAll(maxLengthStringGenerator(maxLength) -> "longString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors must contain only lengthError
      }
    }
  }

  def fieldWithMinLength(
      form:                     Form[?],
      fieldName:                String,
      minLength:                Int,
      lengthError:              FormError,
      minLengthStringGenerator: Int => Gen[String] = stringsShorterThan
  ): Unit = {

    s"not bind strings shorter than $minLength characters" in {

      forAll(minLengthStringGenerator(minLength) -> "longString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors must contain only lengthError
      }
    }
  }
}
