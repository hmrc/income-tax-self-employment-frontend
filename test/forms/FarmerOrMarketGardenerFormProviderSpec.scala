//package forms
//
//import forms.behaviours.BooleanFieldBehaviours
//import models.common.UserType
//import models.common.UserType.{Agent, Individual}
//import pages.QuestionPage
//import play.api.data.{Form, FormError}
//
//class FarmerOrMarketGardenerFormProviderSpec extends BooleanFieldBehaviours {
//
//  val requiredKey = "farmerOrMarketGardener.error.required"
//  val invalidKey  = "error.boolean"
//
////  val form = new FarmerOrMarketGardenerFormProvider(userType: UserType)()
//  override def getFormProvider(userType: UserType): Form[Boolean] = new FarmerOrMarketGardenerFormProvider()(userType)
//
//  ".value" - {
//
//    val fieldName = "value"
//
//    behave like booleanField(
//      form,
//      fieldName,
//      invalidError = FormError(fieldName, invalidKey)
//    )
//
//    behave like mandatoryField(
//      form,
//      fieldName,
//      requiredError = FormError(fieldName, requiredKey)
//    )
//  }
//}
