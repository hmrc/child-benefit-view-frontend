package models

import base.BaseSpec
import org.mockito.Mockito.when
import play.api.i18n.Messages

class WithMessageSpec extends BaseSpec {
  val unitTestKey           = "unit.test.key"
  val unitTestValue         = "value for unit test withMessage"
  implicit val mockMessages = mock[Messages]
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
        Foo.message mustBe unitTestValue
      }
    }
  }
}
