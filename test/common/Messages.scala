package common

import base.SpecBase
import play.api.i18n.Messages

object Messages {
  implicit val messages: Messages = SpecBase.messagesStubbed
}
