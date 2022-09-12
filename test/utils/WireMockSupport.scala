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

package utils

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

trait WireMockSupport extends SpecBase with BeforeAndAfterEach with BeforeAndAfterAll {

  implicit val defaultTimeout = FiniteDuration(100, TimeUnit.SECONDS)

  val wiremockPort = 11111

  private val stubHost = "localhost"

  protected val wiremockBaseUrl: String = s"http://localhost:$wiremockPort"
  private val wireMockServer = new WireMockServer(wireMockConfig().port(wiremockPort))

  override protected def beforeAll() = {
    super.beforeAll()
    wireMockServer.stop()
    wireMockServer.start()
    WireMock.configureFor(stubHost, wiremockPort)
  }

  override protected def afterAll() = {
    wireMockServer.stop()
    super.afterAll()
  }

  override protected def beforeEach() = {
    super.beforeEach()
    WireMock.reset()
  }
}