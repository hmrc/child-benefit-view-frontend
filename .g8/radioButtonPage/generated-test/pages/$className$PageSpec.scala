package utils.pages

import models.$className$
import utils.pages.behaviours.PageBehaviours

class $className$Spec extends PageBehaviours {

  "$className$Page" - {

    beRetrievable[$className$]($className$Page)

    beSettable[$className$]($className$Page)

    beRemovable[$className$]($className$Page)
  }
}
