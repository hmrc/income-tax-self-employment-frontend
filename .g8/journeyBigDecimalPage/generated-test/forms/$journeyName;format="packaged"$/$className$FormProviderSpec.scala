package forms.$journeyName$

import forms.behaviours.BigDecimalFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends BigDecimalFieldBehaviours {

  val formProvider = new $className$FormProvider()

  ".value" - {

    val fieldName = "value"
    val minimum   = BigDecimal("$minimum$")
    val maximum   = BigDecimal("$maximum$")

    val users = Seq(individual, agent)

    users.foreach { user =>
      val form = formProvider(user)

      s"when user is an \$user, form should " - {

        val validDataGenerator = bigDecimalsInRangeWithCommas(minimum, maximum)

        behave like fieldThatBindsValidData(
          form,
          fieldName,
          validDataGenerator
        )

        behave like bigDecimalField(
          form,
          fieldName,
          nonNumericError = FormError(fieldName, s"$className;format="decap"$.error.nonNumeric.\$user")
        )

        behave like bigDecimalFieldWithMinimum(
          form,
          fieldName,
          minimum = minimum,
          expectedError = FormError(fieldName, s"$className;format="decap"$.error.lessThanZero.\$user", Seq(minimum))
        )

        behave like bigDecimalFieldWithMaximum(
          form,
          fieldName,
          maximum,
          expectedError = FormError(fieldName, s"$className;format="decap"$.error.overMax.\$user", Seq(maximum))
        )

        behave like mandatoryField(
          form,
          fieldName,
          requiredError = FormError(fieldName, s"$className;format="decap"$.error.required.\$user")
        )
      }
    }
  }
}
