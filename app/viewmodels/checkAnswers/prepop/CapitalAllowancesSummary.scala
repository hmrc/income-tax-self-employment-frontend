package viewmodels.checkAnswers.prepop

import cats.implicits.catsSyntaxOptionId
import models.journeys.capitalallowances.CapitalAllowancesPrepopAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table}
import utils.MoneyUtils
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow}

object CapitalAllowancesSummary extends MoneyUtils {

  private def headRow(implicit messages: Messages): Option[Seq[HeadCell]] =
    Seq(
      HeadCell(HtmlContent(messages("capitalAllowances.title"))),
      HeadCell(HtmlContent(messages("site.amount")), classes = "govuk-!-text-align-right")
    ).some

  def buildCapitalAllowancesTable(answers: CapitalAllowancesPrepopAnswers)(implicit messages: Messages): Table = {
    val rows = Seq(
      answers.annualInvestment.map(buildTableAmountRow("capitalAllowances.annualInvestmentAllowance", _)),
    ).flatten
    buildTable(headRow, rows)
  }
}
