import models.common.UserType

package object forms {
  val invalidError: String = "error.invalid"
  val lessThanZeroError: String = "error.lessThanZero"
  val nonNumericError: String   = "error.nonNumeric"
  val noDecimalsError: String   = "error.nonDecimal"

  final case class FormStandardErrors(prefix: String) {
    def requiredError(userType: UserType) = s"$prefix.error.required.$userType"
    val amountRequiredError               = s"$prefix.error.required.amount"
    val overMaxError                      = s"$prefix.error.overMax"
  }

}
