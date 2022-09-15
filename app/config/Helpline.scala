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

package config

import play.api.{ConfigLoader, Configuration}

import java.time.{DayOfWeek, LocalTime}

final case class Helpline(
    telephone:          String,
    welshTelephone:     String,
    textPhone:          String,
    outsideUKTelephone: String,
    workingFromDay:     DayOfWeek,
    workingFromTime:    LocalTime,
    workingToDay:       DayOfWeek,
    workingToTime:      LocalTime
)

object Helpline {
  implicit lazy val dayOfWeekConfigLoader: ConfigLoader[DayOfWeek] = ConfigLoader.intLoader.map(DayOfWeek.of)
  implicit lazy val localTimeConfigLoader: ConfigLoader[LocalTime] = ConfigLoader.stringLoader.map(LocalTime.parse)

  implicit lazy val configLoader: ConfigLoader[Helpline] = ConfigLoader { config => prefix =>
    val helpline           = Configuration(config).get[Configuration](prefix)
    val telephone          = helpline.get[String]("telephone")
    val welshTelephone     = helpline.get[String]("welsh-telephone")
    val textPhone          = helpline.get[String]("text-phone")
    val outsideUKTelephone = helpline.get[String]("outside-uk-telephone")
    val workingFromDay     = helpline.get[DayOfWeek]("working-from-day")
    val workingFromTime    = helpline.get[LocalTime]("working-from-time")
    val workingToDay       = helpline.get[DayOfWeek]("working-to-day")
    val workingToTime      = helpline.get[LocalTime]("working-to-time")

    Helpline(
      telephone,
      welshTelephone,
      textPhone,
      outsideUKTelephone,
      workingFromDay,
      workingFromTime,
      workingToDay,
      workingToTime
    )
  }
}
