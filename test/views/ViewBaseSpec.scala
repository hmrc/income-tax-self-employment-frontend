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
import models.common.{BusinessId, TaxYear}
import models.requests.DataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.twirl.api.Html
import views.helper.{JsoupHelper, writeHtmlToTempFile}

trait ViewBaseSpec extends PlaySpec with BeforeAndAfterAll with GuiceOneAppPerSuite with JsoupHelper {

  implicit val fakeRequest: DataRequest[AnyContent] = fakeDataRequest(emptyUserAnswers)

  override def beforeAll(): Unit =
    super.beforeAll()

  override def afterAll(): Unit =
    super.afterAll()

  implicit def messages: Messages = SpecBase.messages(app)
  val taxYear: TaxYear            = TaxYear(2025)
  val businessId: BusinessId      = BusinessId("XAIS123456789012")

  def debugDoc(html: Html): Document = {
    writeHtmlToTempFile(html)
    Jsoup.parse(html.body)
  }

}
