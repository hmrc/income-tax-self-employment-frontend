package navigation

import base.SpecBase
import controllers.journeys.industrysectors.routes
import controllers.standard
import models.NormalMode
import models.journeys.industrySectors.IndustrySectorsDb
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.Page
import pages.industrysectors.{FarmerOrMarketGardenerPage, LiteraryOrCreativeWorksPage}

class IndustrySectorsNavigatorSpec extends SpecBase {

  val navigator = new IndustrySectorsNavigator

  case object UnknownPage extends Page

  "IndustrySectorsNavigator" - {
    "navigating to the next page" - {
      "in NormalMode" - {
        val mode = NormalMode

        val industrySectorsDb: IndustrySectorsDb = IndustrySectorsDb(
          isFarmerOrMarketGardener = Some(true),
          hasProfitFromCreativeWorks = Some(true),
          isAllSelfEmploymentAbroad = Some(true)
        )

        "page does not exist" - {
          "navigate to the JourneyRecoveryController" in {
            val expectedResult = standard.routes.JourneyRecoveryController.onPageLoad()

            navigator.nextPage(UnknownPage, mode, industrySectorsDb, taxYear, businessId) shouldBe expectedResult
          }
        }

        "navigate to LiteraryOrCreativeWorksPage from FarmerOrMarketGardenerPage when option 'Yes' is selected" in {
          val expectedResult = routes.LiteraryOrCreativeWorksController.onPageLoad(taxYear, businessId, NormalMode)

          navigator.nextPage(FarmerOrMarketGardenerPage, mode, industrySectorsDb, taxYear, businessId) shouldBe expectedResult
        }

        "navigate to LiteraryOrCreativeWorksPage from FarmerOrMarketGardenerPage when option 'No' is selected" in {
          val expectedResult = routes.LiteraryOrCreativeWorksController.onPageLoad(taxYear, businessId, NormalMode)
          val vd             = industrySectorsDb.copy(isFarmerOrMarketGardener = Some(false))

          navigator.nextPage(FarmerOrMarketGardenerPage, mode, vd, taxYear, businessId) shouldBe expectedResult
        }

        "navigate to SelfEmploymentAbroadCYAPage from LiteraryOrCreativeWorksPage" in {
          val expectedResult = routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear, businessId)
          val vd             = industrySectorsDb.copy(isFarmerOrMarketGardener = Some(false))

          navigator.nextPage(LiteraryOrCreativeWorksPage, mode, vd, taxYear, businessId) shouldBe expectedResult
        }
      }
    }
  }
}
