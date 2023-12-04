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
import config.FeatureAllowlistFilter._
import play.api.Configuration
import play.api.mvc.Results.{Forbidden, Redirect}
import play.api.mvc.{Call, RequestHeader, Result}

import scala.concurrent.Future

class FeatureAllowlistFilter @Inject() (config: Configuration) {
  def apply(f: RequestHeader => Future[Result])(feature: String, rh: RequestHeader): Future[Result] =
    if (config.get[Boolean](s"feature-flags.$feature.allowlist.enabled")) {
      rh.headers
        .get(trueClient)
        .fold(noHeaderAction(feature, config))(ip =>
          if (allowlist(feature, config).contains(ip))
            f(rh)
          else if (rh.uri == destination(feature, config).url)
            Future.successful(Forbidden)
          else
            Future.successful(Redirect(destination(feature, config)))
        )
    } else {
      f(rh)
    }
}

object FeatureAllowlistFilter {
  val trueClient = "True-Client-IP"

  val noHeaderAction: (String, Configuration) => Future[Result] =
    (feature: String, config: Configuration) => Future.successful(Redirect(destination(feature, config)))

  val destination: (String, Configuration) => Call = (feature: String, config: Configuration) => {
    val path = config.get[String](s"feature-flags.$feature.allowlist.destination")
    Call("GET", path)
  }

  val allowlist: (String, Configuration) => Seq[String] = (feature: String, config: Configuration) =>
    config
      .get[String](s"feature-flags.$feature.allowlist.ips")
      .split(",")
      .toIndexedSeq
      .map(_.trim)
      .filter(_.nonEmpty)
}
