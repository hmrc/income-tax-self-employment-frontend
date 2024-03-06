/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views

import base.SpecBase
import base.SpecBase._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.Html
import views.helper.writeHtmlToTempFile
import views.html.journeys.capitalallowances.zeroEmissionGoodsVehicle.ZegvHowMuchDoYouWantToClaimView

trait ViewBaseSpec extends PlaySpec with BeforeAndAfterAll {
  implicit val fakeRequest = fakeDataRequest(emptyUserAnswers)

  var application: Application              = _
  var view: ZegvHowMuchDoYouWantToClaimView = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    application = new GuiceApplicationBuilder().build()
    view = application.injector.instanceOf[ZegvHowMuchDoYouWantToClaimView]
  }

  override def afterAll(): Unit = {
    super.afterAll()
    application.stop()
  }

  implicit def messages: Messages = SpecBase.messages(application)

  def debugDoc(html: Html): Document = {
    writeHtmlToTempFile(html)
    Jsoup.parse(html.body)
  }
}
