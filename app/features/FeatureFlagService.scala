/*
 * Copyright 2022 HM Revenue & Customs
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

package features

import features.FeatureFlagService.enumerableKeyConfigLoader
import models.Enumerable
import play.api.{ConfigLoader, Configuration}

import javax.inject.{Inject, Singleton}

@Singleton
class FeatureFlagService @Inject() (configuration: Configuration) {
  private val featureFlags: Map[FeatureFlag, Boolean] = configuration.get[Map[FeatureFlag, Boolean]]("feature-flags")

  def isEnabled(featureFlag: FeatureFlag): Boolean = featureFlags.getOrElse(featureFlag, false)
}

object FeatureFlagService {
  implicit def enumerableKeyConfigLoader[K, V](implicit
      enumerable:  Enumerable[K],
      valueLoader: ConfigLoader[V]
  ): ConfigLoader[Map[K, V]] =
    implicitly[ConfigLoader[Map[String, V]]].map {
      _.map {
        case (stringKey, value) =>
          val key = enumerable
            .withName(stringKey)
            .getOrElse(throw new NoSuchElementException(s"Invalid item '$stringKey'"))

          key -> value
      }
    }
}
