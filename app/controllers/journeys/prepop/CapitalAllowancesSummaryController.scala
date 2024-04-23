
package controllers.journeys.prepop

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction, SubmittedDataRetrievalActionProvider}
import models.common.{BusinessId, TaxYear}
import models.journeys.Journey.{AdjustmentsPrepop, CapitalAllowancesPrepop}
import models.journeys.adjustments.AdjustmentsPrepopAnswers
import models.journeys.capitalallowances.CapitalAllowancesPrepopAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.checkAnswers.prepop.AdjustmentsSummary.buildAdjustmentsTable
import viewmodels.checkAnswers.prepop.CapitalAllowancesSummary.buildCapitalAllowancesTable
import views.html.journeys.prepop.CapitalAllowancesSummaryView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CapitalAllowancesSummaryController @Inject()(override val messagesApi: MessagesApi,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    getJourneyAnswers: SubmittedDataRetrievalActionProvider,
                                                    requireData: DataRequiredAction,
                                                    view: CapitalAllowancesSummaryView)(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] =
    (identify andThen getData andThen getJourneyAnswers[CapitalAllowancesPrepopAnswers](req =>
      req.mkJourneyNinoContext(taxYear, businessId, CapitalAllowancesPrepop)) andThen requireData) { implicit request =>
      val answers      = CapitalAllowancesPrepopAnswers.getFromRequest(request, businessId)
      val answersTable = buildCapitalAllowancesTable(answers)
      Ok(view(request.userType, taxYear, businessId, answersTable))
    }
}
