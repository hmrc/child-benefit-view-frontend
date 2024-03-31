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

package controllers.testonly

import com.google.inject.Inject
import play.api.cache.AsyncCacheApi
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HICBCCacheTestController @Inject() (cache: AsyncCacheApi, val controllerComponents: ControllerComponents)(implicit
    val ec:                                      ExecutionContext
) extends BaseController {
  def clearCacheItem: Action[AnyContent] =
    Action.async {
      cache.remove("ignore-hicbic.toggle")
      Future.successful(Ok("ignore-hicbic.toggle cache item removed"))
    }

}
