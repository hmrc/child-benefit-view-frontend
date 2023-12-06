package utils.helpers

import models.ftnae.{FtnaeChildInfo, FtnaeResponse}
import models.requests.DataRequest
import pages.ftnae.{FtnaeResponseUserAnswer, WhichYoungPersonPage}
import utils.helpers.StringHelper.toFtnaeChildNameTitleCase

case class YoungPersonTitleHelper[A](request: DataRequest[A]) {
  private def childFromConcatenatedChildNamesList(): Option[FtnaeChildInfo] = {
    request.userAnswers.get(FtnaeResponseUserAnswer).map(createMatchList(_)) match {
      case Some(item) => item
      case None       => None
    }
  }
  private def createMatchList(userAnswer: FtnaeResponse) = {
    val childNamesMatchListWithIndex: List[(String, Int)] =
      userAnswer.children.map(toFtnaeChildNameTitleCase(_)).zipWithIndex

    val youngPersonNameAsPerPage = request.userAnswers.get(WhichYoungPersonPage)

    childNamesMatchListWithIndex
      .find(childName => childName._1 == youngPersonNameAsPerPage.getOrElse(""))
      .map(item => userAnswer.children(item._2))
  }

  /**
    * Match on user from UI page, if found, then return the name that matches.
    */
  def firstNameFromConcatenatedChildNames(): Option[String] = {
    childFromConcatenatedChildNamesList().map(_.name.value)
  }
}
