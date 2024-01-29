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

package config

import base.BaseAppSpec
import org.mockito.Mockito.when
import play.api.mvc.Results.{Forbidden, Ok, Redirect}
import play.api.mvc.{Headers, RequestHeader, Result}

import scala.concurrent.Future

class FeatureAllowlistFilterSpec extends BaseAppSpec {
  val rh: RequestHeader = mock[RequestHeader]

  val expectedResult: Result                          = Ok("Unit test block result")
  val unitTestBlock:  RequestHeader => Future[Result] = (_: RequestHeader) => Future successful expectedResult

  val allFeaturesTestCases = Table("featureName", "change-of-bank", "ftnae", "add-child", "hicbc")

  val featureFlagKey                   = (featureName: String) => s"feature-flags.$featureName.enabled"
  val featureAllowListEnabledKey       = (featureName: String) => s"feature-flags.$featureName.allowlist.enabled"
  val featureAllowListDestinationKey   = (featureName: String) => s"feature-flags.$featureName.allowlist.destination"
  val featureAllowListDestinationValue = (featureName: String) => s"https://$featureName.unit-test.com"
  val featureAllowListIPsKey           = (featureName: String) => s"feature-flags.$featureName.allowlist.ips"

  "FeatureAllowlistFilter" - {
    forAll(allFeaturesTestCases) { featureName =>
      s"GIVEN the feature $featureName is disabled" - {
        "THEN the provided block runs regardless" in {
          val application = applicationBuilder(Map(featureFlagKey(featureName) -> false)).build()
          val sut         = application.injector.instanceOf[FeatureAllowlistFilter]

          whenReady(sut(unitTestBlock)(featureName, rh)) { result =>
            result mustBe expectedResult
          }
        }
      }

      s"GIVEN the feature $featureName is enabled" - {
        val expectedIP    = "123.456.78.90"
        val alternativeIP = "09.87.654.321"
        "AND the feature's allowlist is enabled" - {
          "AND the request is not provided with a True-Client-IP" - {
            "THEN a Redirect to the feature's destination is returned" in {
              when(rh.headers)
                .thenReturn(Headers())
              val application = applicationBuilder(
                Map(
                  featureAllowListEnabledKey(featureName)     -> true,
                  featureAllowListDestinationKey(featureName) -> featureAllowListDestinationValue(featureName)
                )
              ).build()
              val sut = application.injector.instanceOf[FeatureAllowlistFilter]

              whenReady(sut(unitTestBlock)(featureName, rh)) { result =>
                result mustBe Redirect(featureAllowListDestinationValue(featureName))
              }
            }
          }
          "AND the request is provided with a True-Client-IP" - {
            "AND the provided IP is listed on the allowlist" - {
              "THEN the provided block runs and returns it's result" in {
                when(rh.headers)
                  .thenReturn(Headers((FeatureAllowlistFilter.trueClient, expectedIP)))
                val application = applicationBuilder(
                  Map(
                    featureFlagKey(featureName)             -> true,
                    featureAllowListEnabledKey(featureName) -> true,
                    featureAllowListIPsKey(featureName)     -> List(expectedIP, alternativeIP).mkString(", ")
                  )
                ).build()
                val sut = application.injector.instanceOf[FeatureAllowlistFilter]

                whenReady(sut(unitTestBlock)(featureName, rh)) { result =>
                  result mustBe expectedResult
                }
              }
            }
            "AND the provided IP is not listed on the allowlist" - {
              "AND the request's uri matches the features destination" - {
                "THEN the a Forbidden result is returned to avoid recursive request cycles" in {
                  when(rh.headers)
                    .thenReturn(Headers((FeatureAllowlistFilter.trueClient, expectedIP)))
                  when(rh.uri)
                    .thenReturn(featureAllowListDestinationValue(featureName))
                  val application = applicationBuilder(
                    Map(
                      featureFlagKey(featureName)                 -> true,
                      featureAllowListEnabledKey(featureName)     -> true,
                      featureAllowListDestinationKey(featureName) -> featureAllowListDestinationValue(featureName),
                      featureAllowListIPsKey(featureName)         -> alternativeIP
                    )
                  ).build()
                  val sut = application.injector.instanceOf[FeatureAllowlistFilter]

                  whenReady(sut(unitTestBlock)(featureName, rh)) { result =>
                    result mustBe Forbidden
                  }
                }
              }
              "AND the request's uri does not match the feature's destination" - {
                "THEN a Redirect to the feature's destination is returned" in {
                  when(rh.headers)
                    .thenReturn(Headers((FeatureAllowlistFilter.trueClient, expectedIP)))
                  when(rh.uri)
                    .thenReturn("")
                  val application = applicationBuilder(
                    Map(
                      featureFlagKey(featureName)                 -> true,
                      featureAllowListEnabledKey(featureName)     -> true,
                      featureAllowListDestinationKey(featureName) -> featureAllowListDestinationValue(featureName),
                      featureAllowListIPsKey(featureName)         -> alternativeIP
                    )
                  ).build()
                  val sut = application.injector.instanceOf[FeatureAllowlistFilter]

                  whenReady(sut(unitTestBlock)(featureName, rh)) { result =>
                    result mustBe Redirect(featureAllowListDestinationValue(featureName))
                  }
                }
              }
            }
          }
        }
        "AND the feature's allowlist is disabled" - {
          "THEN the provided block runs regardless" in {
            val application = applicationBuilder(
              Map(
                featureFlagKey(featureName)             -> true,
                featureAllowListEnabledKey(featureName) -> false
              )
            ).build()
            val sut = application.injector.instanceOf[FeatureAllowlistFilter]

            whenReady(sut(unitTestBlock)(featureName, rh)) { result =>
              result mustBe expectedResult
            }
          }
        }
      }
    }
  }
}
