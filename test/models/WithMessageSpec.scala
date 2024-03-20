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

package models

import base.BaseSpec
import org.mockito.Mockito.when
import play.api.i18n.Messages

class WithMessageSpec extends BaseSpec {
  val unitTestKey                     = "unit.test.key"
  val unitTestValue                   = "value for unit test withMessage"
  implicit val mockMessages: Messages = mock[Messages]
  when(mockMessages(unitTestKey)).thenReturn(unitTestValue)

  object Foo extends WithMessage("bar", m => m(unitTestKey))

  "WithMessage" - {
    ".toString" - {
      "must return the correct name" in {
        Foo.toString mustBe "bar"
      }
    }
    ".message" - {
      "must return the correct message" in {
        Foo.message() mustBe unitTestValue
      }
    }
  }
}
