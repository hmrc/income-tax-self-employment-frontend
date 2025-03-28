package viewmodels.checkAnswers.$journeyName;format="normalize,lower"$.$packageName$

import controllers.journeys.$journeyName;format="normalize,lower"$.$packageName$.routes
import models.CheckMode
import models.database.UserAnswers
import pages.$journeyName;format="normalize,lower"$.$packageName$.$className$Page
import play.api.i18n.Messages
import models.common.{BusinessId, TaxYear}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object $className$Summary  {

  def row(taxYear:TaxYear, businessId: BusinessId, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get($className$Page).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "$className;format="decap"$.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.$className$Controller.onPageLoad(taxYear, businessId, CheckMode).url)
              .withVisuallyHiddenText(messages("$className;format="decap"$.change.hidden"))
          )
        )
    }
}
