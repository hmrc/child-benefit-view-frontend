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

package utils.logging


import org.slf4j.MDC
import play.api.Logger
import uk.gov.hmrc.http.{ HeaderCarrier, RequestId }

class RequestLogger(clazz: Class[_]) {
  private val underlying = Logger(clazz)

  private val requestIdKey = "x-request-id"

  def trace(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIdinMDC(underlying.trace(msg))
  def debug(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIdinMDC(underlying.debug(msg))
  def info(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIdinMDC(underlying.info(msg))
  def warn(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIdinMDC(underlying.warn(msg))
  def error(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIdinMDC(underlying.error(msg))

  def withRequestIdinMDC(f: => Unit)(implicit hc: HeaderCarrier): Unit = {
    val requestId = hc.requestId.getOrElse(RequestId("Undefined"))
    MDC.put(requestIdKey, requestId.value)
    f
    MDC.remove(requestIdKey)
  }

}