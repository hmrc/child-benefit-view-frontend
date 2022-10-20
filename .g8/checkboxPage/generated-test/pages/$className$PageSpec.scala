package utils.pages

import models.$className$
import utils.pages.behaviours.PageBehaviours

class $className$PageSpec extends PageBehaviours {

  "$className$Page" - {

    beRetrievable[Set[$className$]]($className$Page)

    beSettable[Set[$className$]]($className$Page)

    beRemovable[Set[$className$]]($className$Page)
  }
}
