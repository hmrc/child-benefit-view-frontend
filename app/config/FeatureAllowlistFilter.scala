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

package config

import com.google.inject.Inject
import play.api.Configuration
import play.api.mvc.Results.{Forbidden, Redirect}
import play.api.mvc.{Call, RequestHeader, Result}

import scala.concurrent.Future

class FeatureAllowlistFilter @Inject() (config: Configuration) {

  val trueClient = "True-Client-IP"

  val allowlist: String => Seq[String] = (feature: String) =>
    config
      .get[String](s"feature-flags.$feature.allowlist.ips")
      .split(",")
      .toIndexedSeq
      .map(_.trim)
      .filter(_.nonEmpty)

  val destination: String => Call = (feature: String) => {
    val path = config.get[String](s"feature-flags.$feature.allowlist.destination")
    Call("GET", path)
  }

  val noHeaderAction: String => Future[Result] = (feature: String) => Future.successful(Redirect(destination(feature)))

  def apply(f: RequestHeader => Future[Result])(feature: String, rh: RequestHeader): Future[Result] =
    if (config.get[Boolean](s"feature-flags.$feature.allowlist.enabled")) {
      rh.headers
        .get(trueClient)
        .fold(noHeaderAction(feature))(ip =>
          if (allowlist(feature).contains(ip))
            f(rh)
          else if (rh.uri == destination(feature).url)
            Future.successful(Forbidden)
          else
            Future.successful(Redirect(destination(feature)))
        )
    } else {
      f(rh)
    }
}
