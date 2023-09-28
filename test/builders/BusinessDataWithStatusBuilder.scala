package builders

import builders.BusinessDataBuilder.{aBusinessData1, aBusinessData2}
import models.requests.BusinessDataWithStatus

object BusinessDataWithStatusBuilder {

  val aCompletedBusinessDataWithStatus = BusinessDataWithStatus(aBusinessData1, true)

  val aNonCompletedBusinessDataWithStatus2 = BusinessDataWithStatus(aBusinessData2, false)

}
