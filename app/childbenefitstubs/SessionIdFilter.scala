/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs

/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

import akka.stream.Materializer
import play.api.http.Status
import play.api.mvc.Results._
import play.api.mvc.{Filter, RequestHeader}
import play.api.{Logger, mvc}
import uk.gov.hmrc.http.SessionKeys

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * Adds "sessionId" entry to the session.
 */
class SessionIdFilter @Inject() (implicit val mat: Materializer,
                                 ex: ExecutionContext)
  extends Filter {

  private def requiresSession(rh: RequestHeader): Boolean =
    SessionIdFilter.requiresSession(rh)

  override def apply(
      f: RequestHeader => Future[mvc.Result]
  )(rh: RequestHeader): Future[mvc.Result] = {

    lazy val result = f(rh)
    if (requiresSession(rh)) {
      val sid = s"session-${UUID.randomUUID()}"
      logger.info(
        s"There was missing sessionId. Adding new one and redirecting to the same endpoint [newSid:$sid]"
      )
      Future.successful(
        Redirect(rh.uri, Status.SEE_OTHER)
          .addingToSession(SessionKeys.sessionId -> sid)(rh)
      )
    } else {
      result
    }
  }

  val logger: Logger = Logger(this.getClass)
}

object SessionIdFilter {
  def requiresSession(rh: RequestHeader): Boolean = {
    val hasSessionId = rh.session.get(SessionKeys.sessionId).isDefined
    !hasSessionId && rh.path.startsWith("/child-benefit-data-stubs")
  }
}
