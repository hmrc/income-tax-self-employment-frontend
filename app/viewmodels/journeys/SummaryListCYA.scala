package viewmodels.journeys

import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

object SummaryListCYA {

  def summaryList(rows: List[SummaryListRow]): SummaryList = SummaryList(
    rows = rows,
    classes = "govuk-!-margin-bottom-7"
  )

}
