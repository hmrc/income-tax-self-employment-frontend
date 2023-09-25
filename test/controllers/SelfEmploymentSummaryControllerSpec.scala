package controllers

import base.SpecBase
import connectors.SelfEmploymentConnector
import connectors.builders.BusinessDataBuilder.aGetBusinessResponse
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.SelfEmploymentSummaryView
import viewmodels.govuk.SummaryListFluency
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentSummaryControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar{

  val tradingNames: Seq[String] = Seq("Trade one", "Trade two")
  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "SelfEmploymentSummary Controller" - {

    "onPageLoad" - {

        "must return OK and the correct view when there are no self-employments" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.SelfEmploymentSummaryController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[SelfEmploymentSummaryView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(Seq.empty)(request, messages(application)).toString
          }
        }


    "must return OK and the correct view for a GET when self-employment data exist" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        when(mockConnector.getBusinesses("AA0134089", "1345566")) thenReturn Future(aGetBusinessResponse)

        val request = FakeRequest(GET, routes.SelfEmploymentSummaryController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelfEmploymentSummaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(tradingNames)(request, messages(application)).toString
      }
    }

    }
  }
}
