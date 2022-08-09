/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

import play.api.http.DefaultHttpErrorHandler

class AppErrorHandler extends DefaultHttpErrorHandler(sourceMapper = None)
