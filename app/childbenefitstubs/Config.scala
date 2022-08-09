/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs

import childbenefitstubs.Config.DataProviders
import childbenefitstubs.models.Environment
import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class Config @Inject() (configuration: Configuration) {
  val dataProviders: DataProviders =
    DataProviders.from(configuration.get[Configuration]("dataProviders"))
}

object Config {
  final case class DataProvider(host: String, token: String)

  object DataProvider {
    def from(configuration: Configuration): DataProvider =
      DataProvider(
        configuration.get[String]("host"),
        configuration.get[String]("token")
      )
  }

  final case class DataProviders(ist0Provider:  DataProvider,
                                 cloneProvider: DataProvider,
                                 liveProvider:  DataProvider) {
    def by(environment: Environment): DataProvider =
      environment match {
        case Environment.Ist0  => ist0Provider
        case Environment.Clone => cloneProvider
        case Environment.Live  => liveProvider
      }
  }

  object DataProviders {
    def from(configuration: Configuration): DataProviders =
      DataProviders(
        DataProvider.from(configuration.get[Configuration]("ist0")),
        DataProvider.from(configuration.get[Configuration]("clone")),
        DataProvider.from(configuration.get[Configuration]("live"))
      )
  }
}
