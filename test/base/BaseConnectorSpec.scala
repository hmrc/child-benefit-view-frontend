package base

import generators.ModelGenerators
import org.scalamock.scalatest.proxy.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

trait BaseConnectorSpec
  extends AnyFreeSpec
  with Matchers
  with MockFactory
  with ScalaCheckPropertyChecks
  with ModelGenerators
    with ScalaFutures
{}
