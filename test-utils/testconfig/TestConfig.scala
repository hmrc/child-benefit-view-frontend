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

package testconfig

object TestConfig {
  def apply(): Map[String, Any] = Map()

  def featureFlags(
      dummyFlag:    Boolean = true,
      changeOfBank: Boolean = true,
      ftnae:        Boolean = true,
      addChild:     Boolean = true,
      hicbc:        Boolean = true
  ): Map[String, Boolean] =
    Map(
      ("dummy-flag", dummyFlag),
      ("change-of-bank", changeOfBank),
      ("ftnae", ftnae),
      ("add-child", addChild),
      ("hicbc", hicbc)
    )

  implicit class TestConfigExtensions(config: Map[String, Any]) {
    def withFeatureFlags(featureFlags: Map[String, Boolean]): Map[String, Any] = {
      val featureFlagMap = ("feature-flags", featureFlags.map(f => (s"${f._1}.enabled", f._2.toString)))
      config + featureFlagMap
    }
  }
}
