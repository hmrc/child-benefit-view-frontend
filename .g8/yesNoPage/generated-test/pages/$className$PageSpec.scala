package utils.pages

import utils.pages.behaviours.PageBehaviours

class $className$PageSpec extends PageBehaviours {

  "$className$Page" - {

    beRetrievable[Boolean]($className$Page)

    beSettable[Boolean]($className$Page)

    beRemovable[Boolean]($className$Page)
  }
}
