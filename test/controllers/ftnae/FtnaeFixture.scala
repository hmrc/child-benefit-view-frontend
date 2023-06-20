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

package controllers.ftnae

import java.time.{DayOfWeek, LocalDate, Month}

trait FtnaeFixture {
  val thisYear1stOfSeptember            = LocalDate.of(LocalDate.now().getYear, Month.SEPTEMBER, 1)
  val sixteenBy1stOfSeptemberThisYear   = thisYear1stOfSeptember.minusYears(16)
  val seventeenBy1stOfSeptemberThisYear = thisYear1stOfSeptember.minusYears(17)

  protected def getFirstMondayOfSeptemberThisYear(): LocalDate = {
    val firstDayOfSeptemberThisYear = LocalDate.of(LocalDate.now().getYear, Month.SEPTEMBER, 1)
    val daysOfMonth                 = (0 to 7).to(LazyList)
    val nthDayForFirstMonday: Int = daysOfMonth.dropWhile(daysToAdd =>
      firstDayOfSeptemberThisYear.plusDays(daysToAdd).getDayOfWeek != DayOfWeek.MONDAY
    )(0)
    firstDayOfSeptemberThisYear.plusDays(nthDayForFirstMonday)
  }
}
