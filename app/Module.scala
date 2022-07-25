/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

import java.time.{Clock, LocalDateTime, ZoneId, ZoneOffset}

import com.google.inject.{AbstractModule, Provides}
import javax.inject.Singleton

class Module() extends AbstractModule {

  @Provides
  @Singleton
  def clock: Clock = {
      def frozenTimeString: String = "2027-11-02T16:33:51.880"
    val fixedInstant = LocalDateTime.parse(frozenTimeString).toInstant(ZoneOffset.UTC)
    Clock.fixed(fixedInstant, ZoneId.systemDefault)
  }

}

