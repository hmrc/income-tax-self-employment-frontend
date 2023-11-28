package controllers.journeys.expenses.tailoring

import base.SpecBase.{stubBusinessId, taxYear}
import controllers.base.CYAOnPageLoadControllerSpec
import controllers.journeys.expenses.tailoring
import models.common.{BusinessId, TaxYear, UserType}
import org.scalatest.prop.TableFor2
import pages.expenses.tailoring.ExpensesTailoringCYAPage
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.JsObject
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.journeys.SummaryListCYA
import views.html.journeys.expenses.tailoring.ExpensesTailoringCYAView

class ExpensesTailoringCYAControllerSpec extends CYAOnPageLoadControllerSpec {
  def onPageLoad: (TaxYear, BusinessId) => Call = tailoring.routes.ExpensesTailoringCYAController.onPageLoad

  def onPageLoadCases: TableFor2[JsObject, OnPageLoadView] = Table(
    ("userAnswersData", "expectedViews"),
    (JsObject.empty, createExpectedView(taxYear, stubBusinessId, UserType.Individual, SummaryListCYA.summaryList(Nil)))
  )

  def createExpectedView(taxYear: TaxYear, businessId: BusinessId, userType: UserType, summaryList: SummaryList): OnPageLoadView = {
    (msg: Messages, application: Application, request: Request[_]) =>
      val view = application.injector.instanceOf[ExpensesTailoringCYAView]
      view(
        ExpensesTailoringCYAPage.pageName,
        taxYear,
        businessId,
        summaryList,
        userType,
        tailoring.routes.ExpensesTailoringCYAController.onSubmit(taxYear, businessId)
      )(request, msg).toString()
  }
}
