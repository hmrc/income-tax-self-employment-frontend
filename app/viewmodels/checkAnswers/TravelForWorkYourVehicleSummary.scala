package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.TravelForWorkYourVehiclePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TravelForWorkYourVehicleSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TravelForWorkYourVehiclePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "travelForWorkYourVehicle.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.TravelForWorkYourVehicleController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("travelForWorkYourVehicle.change.hidden"))
          )
        )
    }
}
