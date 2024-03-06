package views.journeys.capitalallowances.zeroEmissionGoodsVehicle

import base.SpecBase._
import forms.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimFormProviderSpec._
import models.{CheckMode, NormalMode}
import models.common.UserType._
import org.jsoup.nodes.Element
import play.twirl.api.Html
import views.ViewBaseSpec
import views.html.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimView

class ZegvHowMuchDoYouWantToClaimViewSpec extends ViewBaseSpec {

  "view" should {
    def view     = application.injector.instanceOf[ZegvHowMuchDoYouWantToClaimView]
    val form     = individualFormProvider.bind(Map("howMuchDoYouWantToClaim" -> "fullCost"))
    val fullCost = 1000

    "select the full cost" in {
      val html = view(form, NormalMode, Individual, taxYear, businessId, BigDecimal(fullCost))
      new Page(html) {
        assert(fullCostRadio.hasAttr("checked"))
        assert(fullCostLabel.text() === "The full cost (£1,000)")
        assert(!lowerAmountRadio.hasAttr("checked"))
      }
    }

    "render specific text for an individual" in {
      val html = view(form, NormalMode, Individual, taxYear, businessId, BigDecimal(fullCost))
      new Page(html) {
        assert(doc.toString.contains("You "))
        assert(!doc.toString.contains("Your client"))
      }
    }
    "render specific text for an Agent" in {
      val html = view(form, NormalMode, Agent, taxYear, businessId, BigDecimal(fullCost))
      new Page(html) {
        assert(!doc.toString.contains("You "))
        assert(doc.toString.contains("Your client"))
      }
    }

    "render specific text when CheckMode" in {
      val html = view(form, CheckMode, Agent, taxYear, businessId, BigDecimal(fullCost))
      new Page(html) {
        assert(formElement.attr("action").contains("change-capital-allowances"))
      }
    }
  }

  abstract class Page(html: Html) {
    val doc = debugDoc(html)

    def fullCostRadio: Element    = doc.select("#fullCost").first()
    def fullCostLabel: Element    = fullCostRadio.parents().first().select("label[for=fullCost]").first()
    def lowerAmountRadio: Element = doc.select("#lowerAmount").first()
    def formElement: Element      = doc.select("form").first()
  }

}
