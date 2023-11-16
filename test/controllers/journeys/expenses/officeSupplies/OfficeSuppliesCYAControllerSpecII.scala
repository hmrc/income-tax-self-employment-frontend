package controllers.journeys.expenses.officeSupplies

import base.CYAControllerBaseSpec
import models.common.{UserType, onwardRoute}
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Request
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.expenses.officeSupplies.OfficeSuppliesAmountSummary
import views.html.journeys.expenses.officeSupplies.OfficeSuppliesCYAView

class OfficeSuppliesCYAControllerSpecII extends CYAControllerBaseSpec("OfficeSuppliesCYAController") {

  private val userAnswerData = Json
    .parse(s"""
         |{
         |  "$stubbedBusinessId": {
         |    "officeSupplies": "yesAllowable",
         |    "officeSuppliesAmount": 200.00
         |  }
         |}
         |""".stripMargin)
    .as[JsObject]

  override val userAnswers: UserAnswers = UserAnswers(userAnswersId, userAnswerData)

  override lazy val onPageLoadRoute: String = routes.OfficeSuppliesCYAController.onPageLoad(taxYear, stubbedBusinessId).url

  override protected val summaryStylingClass = "govuk-!-margin-bottom-7"

  override val bindings: List[Binding[_]] = List(bind[ExpensesNavigator].to(new FakeExpensesNavigator(onwardRoute)))

  override def expectedSummaryRows(authUserType: UserType)(implicit messages: Messages): List[Option[SummaryListRow]] =
    List(OfficeSuppliesAmountSummary.row(userAnswers, taxYear, stubbedBusinessId, authUserType.toString))

  override def expectedView(scenario: TestScenario, summaryList: SummaryList, nextRoute: String)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {

    val view = application.injector.instanceOf[OfficeSuppliesCYAView]
    view(scenario.userType.toString, summaryList, taxYear, nextRoute).toString()
  }

}
