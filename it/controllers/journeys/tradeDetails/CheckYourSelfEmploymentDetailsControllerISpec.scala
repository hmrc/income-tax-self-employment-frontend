package controllers.journeys.tradeDetails

import base.IntegrationBaseSpec
import controllers.standard.routes._
import helpers.{AuthStub, SelfEmploymentApiStub, WiremockSpec}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.http.HeaderNames
import play.api.http.Status.{NOT_FOUND, OK, SEE_OTHER}
import play.api.libs.json.Json

class CheckYourSelfEmploymentDetailsControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  private val url: String = routes.CheckYourSelfEmploymentDetailsController.onPageLoad(taxYear, businessId).url

  case class SummaryListRow(key: String, expectedValue: String)

  def assertSummaryListRow(doc: Document, row: SummaryListRow): Assertion = {
    val rowElement  = doc.select(s".govuk-summary-list__key:contains(${row.key})").first()
    val actualValue = rowElement.nextElementSibling().select(".govuk-summary-list__value").text()
    actualValue mustBe row.expectedValue
  }

  "GET /:taxYear/:businessId/check-your-self-employment-details" when {

    "the user is authorised" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        SelfEmploymentApiStub.getBusiness(nino, businessId, mtditid)(OK, Json.toJson(businessData))
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.status mustBe OK
        result.header(HeaderNames.LOCATION) mustBe None

        val doc = Jsoup.parse(result.body)
        doc.title() must include("Check your self-employment details")

        val summaryList = doc.select(".govuk-summary-list")
        summaryList.size() must be > 0

        val expectedRows = Seq(
          SummaryListRow("What name did you use for your self-employment", "Trade one"),
          SummaryListRow("What did you do for your self-employment", "self-employment"),
          SummaryListRow("When did your self-employment start", "6 April 2023")
        )

        expectedRows.foreach(assertSummaryListRow(doc, _))
      }
    }

    "the user is an agent" must {
      "return OK with the correct view for agent" in {
        AuthStub.agentAuthorised()
        SelfEmploymentApiStub.getBusiness(nino, businessId, mtditid)(OK, Json.toJson(businessData))
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        result.status mustBe OK
        val doc = Jsoup.parse(result.body)
        doc.title() must include("Check your client’s self-employment details")

        val summaryList = doc.select(".govuk-summary-list")
        summaryList.size() must be > 0

        val expectedRows = Seq(
          SummaryListRow("What name did your client use for their self-employment", "Trade one"),
          SummaryListRow("What did your client do for their self-employment", "self-employment"),
          SummaryListRow("When did your client’s self-employment start", "6 April 2023")
        )

        expectedRows.foreach(assertSummaryListRow(doc, _))
      }
    }

    "the API returns a not found error" must {
      "redirect to journey recovery" in {
        AuthStub.authorised()
        SelfEmploymentApiStub.getBusiness(nino, businessId, mtditid)(NOT_FOUND)
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION).value mustBe JourneyRecoveryController.onPageLoad().url
      }
    }

    "the user is unauthorised" must {
      "redirect to login page" in {
        AuthStub.unauthorisedOtherEnrolment()
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION).value must include("/gg-sign-in")
      }
    }
  }
}
