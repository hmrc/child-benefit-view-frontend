/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs

import childbenefitstubs.models.financialdetails._
import childbenefitstubs.models.individuals.IndividualDetails
import childbenefitstubs.models.relationshipdetails.Relationships
import play.api.http.ContentTypes
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class DESDataController @Inject() (config: Config,
                                   http:   HttpClient,
                                   mcc:    MessagesControllerComponents)
  extends FrontendController(mcc)
  with HeaderValidations
  with ParameterValidations
  with SyntaxExtensions {
  def financialDetails(identifier: String): Action[AnyContent] =
    Action.async { implicit request =>
      val validations =
        validateNINO(identifier) ++
          validateEnvironment(request) ++
          validateCorrelationId(request)

      validations.toFuture {
        case ((nino, environment), correlationId) =>
          val dataProvider = config.dataProviders.by(environment)
          val url = s"${dataProvider.host}/financial-details/$nino"
          val headers = List(
            "Authorization" -> s"Basic ${dataProvider.token}",
            "CorrelationId" -> correlationId.toString
          )

          http.GET[FinancialDetails](url     = url, headers = headers).map { fd =>
            Ok(Json.toJson(fd))
              .as(ContentTypes.JSON)
              .withHeaders(headers: _*)
          }
      }
    }

  def relationshipDetails(idNumber: String): Action[AnyContent] =
    Action.async { implicit request =>
      val validations =
        validateNINOOrTRN(idNumber) ++
          validateEnvironment(request) ++
          validateCorrelationId(request) ++
          validateOriginatorId(request, Set("DA2_CHB_UNATTENDED"))

      validations.toFuture {
        case (((ninoOrTrn, environment), correlationId), originatorId) =>
          val dataProvider = config.dataProviders.by(environment)
          val id = ninoOrTrn.fold(_.value, _.value)
          val url = s"${dataProvider.host}/individuals/relationship/$id"
          val headers = List(
            "Authorization" -> s"Basic ${dataProvider.token}",
            "CorrelationId" -> correlationId.toString,
            "OriginatorId" -> originatorId
          )

          http.GET[Relationships](url     = url, headers = headers).map { rs =>
            Ok(Json.toJson(rs))
              .as(ContentTypes.JSON)
              .withHeaders(headers: _*)
          }
      }
    }

  def individualDetails(idNumber:     String,
                        resolveMerge: String): Action[AnyContent] =
    Action.async { implicit request =>
      val validations =
        validateNINOOrTRN(idNumber) ++
          validateYesOrNo("resolveMerge", resolveMerge) ++
          validateEnvironment(request) ++
          validateCorrelationId(request) ++
          validateOriginatorId(
            request,
            Set("DA2_CHB_UNATTENDED", "DA2_CHB", "DA2_CHB_DWP")
          )

      validations.toFuture {
        case ((((ninoOrTrn, _), environment), correlationId), originatorId) =>
          val additionalValidations =
            validateUserId(request, originatorId) ++
              validateAudience(request, originatorId, Set("DWP")) ++
              validateAlignment(
                request,
                originatorId,
                Set("hmrc-aligned", "hmrc-unaligned")
              )

          additionalValidations.toFuture {
            case ((userId, audience), alignment) =>
              val dataProvider = config.dataProviders.by(environment)
              val id = ninoOrTrn.fold(_.value, _.value)
              val url =
                s"${dataProvider.host}/individuals/details/$id/$resolveMerge"

              val headers =
                List(
                  "Authorization" -> s"Basic ${dataProvider.token}",
                  "CorrelationId" -> correlationId.toString,
                  "OriginatorId" -> originatorId
                ) ++
                  userId
                  .map(uid => List("UserId" -> uid.value))
                  .getOrElse(List.empty) ++
                  audience
                  .map(a => List("Audience" -> a))
                  .getOrElse(List.empty) ++
                  alignment
                  .map(a => List("Alignment" -> a))
                  .getOrElse(List.empty)

              http.GET[IndividualDetails](url     = url, headers = headers).map {
                id =>
                  Ok(Json.toJson(id))
                    .as(ContentTypes.JSON)
                    .withHeaders(headers: _*)
              }
          }
      }
    }
}
