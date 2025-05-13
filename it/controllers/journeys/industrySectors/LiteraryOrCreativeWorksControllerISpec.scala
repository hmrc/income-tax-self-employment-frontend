package controllers.journeys.industrySectors

import base.IntegrationBaseSpec
import controllers.journeys.industrysectors.routes
import helpers.{AnswersApiStub, AuthStub, WiremockSpec}
import models.NormalMode
import models.common.Journey.IndustrySectors
import models.common.JourneyAnswersContext
import models.journeys.industrySectors.IndustrySectorsDb
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.Helpers._

class LiteraryOrCreativeWorksControllerISpec extends WiremockSpec with IntegrationBaseSpec {

  val url: String                        = routes.LiteraryOrCreativeWorksController.onPageLoad(taxYear, businessId, NormalMode).url
  val submitUrl: String                  = routes.LiteraryOrCreativeWorksController.onSubmit(taxYear, businessId, NormalMode).url
  val testContext: JourneyAnswersContext = JourneyAnswersContext(taxYear, nino, businessId, mtditid, IndustrySectors)

  val testIndustrySectors: IndustrySectorsDb = IndustrySectorsDb(
    isFarmerOrMarketGardener = Some(true),
    hasProfitFromCreativeWorks = Some(true)
  )

  "GET /:taxYear/:businessId/industry-sectors/literary-or-creative-works" when {
    "the user is an agent" must {
      "return OK with the correct view" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getAnswers(testContext)(NOT_FOUND)
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }

      "return OK and pre-populate the field when the user has data" in {
        AuthStub.agentAuthorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testIndustrySectors)))
        DbHelper.insertEmpty()

        val result = await(buildClient(url, isAgent = true).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
        Jsoup.parse(result.body).select("input[id=value][checked]").isEmpty mustBe false
      }
    }

    "the user is an individual" must {
      "return OK with the correct view" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(NOT_FOUND)
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
      }

      "return OK and pre-populate the field when the user has data" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testIndustrySectors)))
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION) mustBe None
        result.status mustBe OK
        Jsoup.parse(result.body).select("input[id=value][checked]").isEmpty mustBe false
      }
    }

    "the user is unauthorised" must {
      "redirect to the login page" in {
        AuthStub.unauthorisedOtherEnrolment()
        DbHelper.insertEmpty()

        val result = await(buildClient(url).get())

        result.header(HeaderNames.LOCATION).exists(_.contains("gg-sign-in")) mustBe true
        result.status mustBe SEE_OTHER
      }
    }
  }

  "POST /:taxYear/:businessId/industry-sectors/literary-or-creative-works" when {
    "the user selects 'Yes'" must {
      "redirect to the next page" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testIndustrySectors.copy(hasProfitFromCreativeWorks = None))))
        AnswersApiStub.replaceAnswers(testContext, Json.toJson(testIndustrySectors))(OK)
        DbHelper.insertEmpty()

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq("true"))))

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear, businessId).url)
      }
    }

    "the user selects 'No'" must {
      "redirect to the next page" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(OK, Some(Json.toJson(testIndustrySectors.copy(hasProfitFromCreativeWorks = None))))
        AnswersApiStub.replaceAnswers(testContext, Json.toJson(testIndustrySectors.copy(hasProfitFromCreativeWorks = Some(false))))(OK)
        DbHelper.insertEmpty()

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq("false"))))

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear, businessId).url)
      }
    }

    "the user submits without selecting an option" must {
      "return BAD REQUEST" in {
        AuthStub.authorised()
        AnswersApiStub.getAnswers(testContext)(NOT_FOUND)
        DbHelper.insertEmpty()

        val result = await(buildClient(submitUrl).post(Map("value" -> Seq(""))))

        result.status mustBe BAD_REQUEST
      }
    }
  }
}
