package testconfig

object TestConfig {
  def apply(): Map[String, Any] = Map()

  def featureFlags(
      dummyFlag:    Boolean = true,
      changeOfBank: Boolean = true,
      newClaim:     Boolean = true,
      ftnae:        Boolean = true,
      addChild:     Boolean = true,
      hicbc:        Boolean = true
  ): Map[String, Boolean] =
    Map(
      ("dummy-flag", dummyFlag),
      ("change-of-bank", changeOfBank),
      ("new-claim", newClaim),
      ("ftnae", ftnae),
      ("add-child", addChild),
      ("hicbc", hicbc)
    )

  implicit class TestConfigExtensions(config: Map[String, Any]) {
    def withFeatureFlags(featureFlags: Map[String, Boolean]): Map[String, Any] = {
      val featureFlagMap = ("feature-flags", featureFlags.map(f => (f._1, f._2.toString)))
      config + featureFlagMap
    }
  }
}
