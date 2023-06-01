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

package views.templates

import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.Request
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.sca.services.WrapperService
import views.html.components.{AdditionalScript, HeadBlock}

import javax.inject.Inject

trait LayoutProvider {
  //noinspection ScalaStyle
  def apply(
             pageTitle: String,
             showBackLink: Boolean = true,
             timeout: Boolean = true,
             showSignOut: Boolean = false,
             scripts: Option[Html] = None,
             stylesheets: Option[Html] = None
           )(contentBlock: Html)(
             implicit request: Request[_],
             messages: Messages
           ): HtmlFormat.Appendable
}


class OldLayoutProvider @Inject()(layout: views.html.templates.Layout) extends LayoutProvider {


  //noinspection ScalaStyle
  override def apply(pageTitle: String, showBackLink: Boolean, timeout: Boolean, showSignOut: Boolean,
                     scripts: Option[Html], stylesheets: Option[Html])(contentBlock: Html)
                    (implicit request: Request[_], messages: Messages): HtmlFormat.Appendable = {
    layout(pageTitle, showBackLink, timeout, showSignOut, scripts, stylesheets)(contentBlock)
  }
}


class NewLayoutProvider @Inject()(wrapperService: WrapperService,
                                  additionalScript: AdditionalScript, headBlock: HeadBlock) extends LayoutProvider with Logging {


  //noinspection ScalaStyle
  override def apply(pageTitle: String, showBackLink: Boolean, timeout: Boolean, showSignOut: Boolean,
                     scripts: Option[Html], stylesheets: Option[Html])(contentBlock: Html)
                    (implicit request: Request[_], messages: Messages): HtmlFormat.Appendable = {
    wrapperService.layout(
      disableSessionExpired = !timeout,
      content = contentBlock,
      pageTitle = Some(pageTitle),
      showBackLinkJS = showBackLink,
      scripts = scripts.toSeq :+ additionalScript(),
      styleSheets = stylesheets.toSeq :+ headBlock(),
      fullWidth = false
    )(messages, HeaderCarrierConverter.fromRequest(request), request)
  }
}

