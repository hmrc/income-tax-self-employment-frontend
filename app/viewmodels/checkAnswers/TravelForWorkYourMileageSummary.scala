package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.TravelForWorkYourMileagePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TravelForWorkYourMileageSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TravelForWorkYourMileagePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "travelForWorkYourMileage.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.TravelForWorkYourMileageController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("travelForWorkYourMileage.change.hidden"))
          )
        )
    }
}
