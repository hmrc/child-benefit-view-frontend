package utils.pages

import utils.pages.behaviours.PageBehaviours

class $className$PageSpec extends PageBehaviours {

  "$className$Page" - {

    beRetrievable[Int]($className$Page)

    beSettable[Int]($className$Page)

    beRemovable[Int]($className$Page)
  }
}
