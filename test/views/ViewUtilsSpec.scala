package views

import base.BaseSpec
import org.mockito.MockitoSugar.when
import play.api.data.Form
import play.api.i18n.{Lang, Messages}

import java.time.LocalDate

class ViewUtilsSpec extends BaseSpec {
  implicit val messages: Messages = mock[Messages]
  val errorPrefixKey = "error.browser.title.prefix"
  val titleKey = "title.key"
  val sectionKey = "section.key"
  val serviceNameKey = "service.name"
  val govUkKey = "site.govuk"

  val expectedErrorPrefix = "Error:"
  val expectedTitle = "Unit Test Page Title"
  val expectedSection = "Unit Test Section"
  val expectedServiceName = "Service Name"
  val expectedGovUk = "Gov UK"

  when(messages.lang).thenReturn(Lang.defaultLang)
  when(messages(errorPrefixKey)).thenReturn(expectedErrorPrefix)
  when(messages(titleKey)).thenReturn(expectedTitle)
  when(messages(sectionKey)).thenReturn(expectedSection)
  when(messages(serviceNameKey)).thenReturn(expectedServiceName)
  when(messages(govUkKey)).thenReturn(expectedGovUk)

  val formWithoutErrors: Form[_] = mock[Form[_]]
  when(formWithoutErrors.hasErrors).thenReturn(false)
  when(formWithoutErrors.hasGlobalErrors).thenReturn(false)
  val formWithLocalErrors: Form[_] = mock[Form[_]]
  when(formWithLocalErrors.hasErrors).thenReturn(true)
  when(formWithLocalErrors.hasGlobalErrors).thenReturn(false)
  val formWithGlobalErrors: Form[_] = mock[Form[_]]
  when(formWithGlobalErrors.hasErrors).thenReturn(false)
  when(formWithGlobalErrors.hasGlobalErrors).thenReturn(true)
  val formWithAllErrors: Form[_] = mock[Form[_]]
  when(formWithAllErrors.hasErrors).thenReturn(true)
  when(formWithAllErrors.hasGlobalErrors).thenReturn(true)


  "View Utils Spec" - {
    "title" - {
      val testNameForForm = (includeErrorPrefix: Boolean, includeSection: Boolean) =>
        s"For a form ${withOrWithout(includeErrorPrefix)} an Error Prefix and ${withOrWithout(includeSection)} a Section"
      val titleTestCases = Table(
        ("includeErrorPrefix", "includeSection", "form"),
        (false, false, formWithoutErrors),
        (false, true,  formWithoutErrors),
        (true,  false, formWithLocalErrors),
        (true,  true,  formWithLocalErrors)
      )

      forAll(titleTestCases) { (includeErrorPrefix, includeSection, form) =>
        val testName = testNameForForm(includeErrorPrefix, includeSection)
        val section  = if(includeSection) Some(sectionKey) else None
        val result = ViewUtils.title(form, titleKey, section)
        testTitle(testName, result, includeErrorPrefix, includeSection)
      }
    }

    "titleNoForm" - {
      val testNameForNoForm = (includeSection: Boolean) =>
        s"For without a form and ${withOrWithout(includeSection)} a Section"
      val titleNoFormTestCases = Table(
        "includeSection",
        false,
        true,
      )

      forAll(titleNoFormTestCases) { includeSection =>
        val testName = testNameForNoForm(includeSection)
        val section = if (includeSection) Some(sectionKey) else None
        val result = ViewUtils.titleNoForm(titleKey, section)
        testTitle(testName, result, errorPrefix = false, section = includeSection)
      }
    }

    def testTitle(testName: String, result: String, errorPrefix: Boolean, section: Boolean): Unit = {
      testName - {
        if (errorPrefix) {
          "must begin with the expected error prefix" in {
            result.startsWith(expectedErrorPrefix) mustBe true
          }
        } else {
          "must not begin with the expected error prefix" in {
            result.startsWith(expectedErrorPrefix) mustBe false
          }
        }

        "must contain the expected title" in {
          result.contains(expectedTitle) mustBe true
        }

        if (section) {
          "must contain the expected error prefix" in {
            result.contains(expectedSection) mustBe true
          }
        } else {
          "must not contain the expected error prefix" in {
            result.contains(expectedSection) mustBe false
          }
        }

        "must contain the expected service name" in {
          result.contains(expectedServiceName) mustBe true
        }

        "must contain the expected Gov Uk phrase" in {
          result.contains(expectedGovUk) mustBe true
        }

        "must be constructed in the expected order" in {
          val expectedResult =
            s"${if (errorPrefix) s"$expectedErrorPrefix " else ""}" +
              s"$expectedTitle - ${if (section) s"$expectedSection - " else ""}" +
              s"$expectedServiceName - $expectedGovUk"

          result mustBe expectedResult
        }
      }
    }

    "errorPrefix" - {
      val errorPrefixTestCases = Table(
        ("expectedName",              "formDescription",         "form",               "expectedResult"),
        ("an empty string",           "no errors",     formWithoutErrors,    ""),
        ("the expected error prefix", "local errors",  formWithLocalErrors,  s"$expectedErrorPrefix "),
        ("the expected error prefix", "global errors", formWithGlobalErrors, s"$expectedErrorPrefix "),
        ("the expected error prefix", "all errors",    formWithAllErrors,    s"$expectedErrorPrefix ")
      )

      forAll(errorPrefixTestCases) { (name, description, form, result) =>
      s"GIVEN a form with $description THEN $name should be returned" in {
          ViewUtils.errorPrefix(form) mustBe result
        }
      }
    }

    "formatDate" - {
      "must return a date in the format d MMMM yyyy" in {
        val testDate = LocalDate.now()
        val result = ViewUtils.formatDate(testDate)

        val expectedDate = testDate.getDayOfMonth
        val expectedMonth = testDate.getMonth
        val expectedYear = testDate.getYear
        result.toUpperCase mustBe s"$expectedDate $expectedMonth $expectedYear"
      }
    }

    "formatMoney" - {
      "must default to GBP (£) when no currency is provided" in {
        val result = ViewUtils.formatMoney(1)

        result.startsWith("£") mustBe true
      }

      "must place the currency symbol at the start of the formatted result" - {
        val currencySymbolTestCases = Table("Currency", "£", "$", "€")

        forAll(currencySymbolTestCases) { currency =>
          s"Currency Symbol: $currency" in {
            val result = ViewUtils.formatMoney(1, currency)

            result.startsWith(currency) mustBe true
          }
        }
      }

      val twoScaleTestCases = Table(
        ("testName", "testValue", "expectedResult"),
        ("No decimals",          "1",          "1.00"),
        ("One decimal place",    "2.3",        "2.30"),
        ("Two decimal places",   "4.56",       "4.56"),
        ("Three decimal places", "7.891",      "7.89"),
        ("Many decimal places",  "1.23456789", "1.23")
      )

      "must always return a two scale decimal" - {
        forAll(twoScaleTestCases) { (testName, testValue, expectedResult) =>
          s"$testName: $testValue" in {
            val result = ViewUtils.formatMoney(BigDecimal(testValue))

            result mustBe s"£$expectedResult"
          }
        }
      }
    }
  }
}
