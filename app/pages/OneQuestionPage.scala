/*
 * Copyright 2023 HM Revenue & Customs
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

package pages

import models.Mode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import play.api.libs.json.JsPath
import play.api.mvc.{Call, Result}
import play.api.mvc.Results.Redirect

import scala.concurrent.Future

trait OneQuestionPage[A] extends QuestionPage[A] {

  override def path(businessId: Option[BusinessId] = None): JsPath =
    businessId match {
      case Some(id) => JsPath \ id.value \ toString
      case None     => JsPath \ toString
    }

  protected def nextPage(mode: Mode)(implicit userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call = ???

  def redirectNextPage(mode: Mode)(implicit userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Future[Result] =
    Future.successful(Redirect(nextPage(mode)))

}
