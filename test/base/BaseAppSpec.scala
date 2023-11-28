package base

import controllers.actions._
import models.UserAnswers
import org.jsoup.Jsoup
import org.scalactic.source.Position
import org.scalatest.Assertion
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

trait BaseAppSpec extends BaseSpec with WireMockSupport {
  protected def applicationBuilder(
      config: Map[String, Any] = Map(),
      userAnswers: Option[UserAnswers] = None
    ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(defaultWireMockConfig ++ config)
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[CBDataRetrievalAction].toInstance(new FakeCobDataRetrievalAction(userAnswers)),
        //        bind[VerifyBarNotLockedAction].toInstance(FakeVerifyBarNotLockedAction(verify = true)),
        //        bind[VerifyHICBCAction].toInstance(FakeVerifyHICBCAction(verify = true)),
        //        bind[ChildBenefitEntitlementConnector].to[DefaultChildBenefitEntitlementConnector]
      )

  protected def applicationBuilderWithVerificationActions(
     config: Map[String, Any] = Map(),
     userAnswers: Option[UserAnswers] = None,
     verifyBarNotLockedAction: VerifyBarNotLockedAction = FakeVerifyBarNotLockedAction(verify = true),
     verifyHICBCAction: VerifyHICBCAction = FakeVerifyHICBCAction(verify = true)
   ): GuiceApplicationBuilder = {
    applicationBuilder(config, userAnswers)
      .overrides(
        bind[VerifyBarNotLockedAction].toInstance(verifyBarNotLockedAction),
        bind[VerifyHICBCAction].toInstance(verifyHICBCAction)
      )
  }

  protected def assertSameHtmlAfter(
                                     transformation: String => String
                                   )(left: String, right: String)(implicit position: Position): Assertion = {
    val leftHtml = Jsoup.parse(transformation(left))
    val rightHtml = Jsoup.parse(transformation(right))
    leftHtml.html() mustBe rightHtml.html()
  }

}
