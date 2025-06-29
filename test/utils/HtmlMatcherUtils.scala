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

package utils

object HtmlMatcherUtils {

  val pattern: String = """nonce="[^"]+""""

  val removeCsrfAndNonce: String => String = removeNonce andThen removeCsrfToken andThen replaceMenuRight

  val removeNonceAndMenuRight: String => String = removeNonce andThen replaceMenuRight

  def removeNonce(html: String): String =
    html.replaceAll(pattern, "")

  def removeCsrfToken(html: String): String =
    html.replaceAll(".*csrfToken.*", "")

  // Adding play.filters.enabled += "uk.gov.hmrc.sca.filters.WrapperDataFilter" to application.conf
  // has the unexpected side effect of incrementing the signout menu items index by 1
  def replaceMenuRight(html: String): String =
    html.replaceAll("menu.right.3", "menu.right.4")
}
