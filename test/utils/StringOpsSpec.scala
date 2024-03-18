package utils

import org.scalatest.wordspec.AnyWordSpecLike
import utils.StringOps._

class StringOpsSpec extends AnyWordSpecLike {

  "lowerFirstLetter" should {
    "return empty" in {
      assert(lowerFirstLetter("") === "")
    }

    "return lower cased one ltter string" in {
      assert(lowerFirstLetter("A") === "a")
    }

    "return lower cased first letter for longer strings" in {
      assert(lowerFirstLetter("CamelCase") === "camelCase")
    }
  }
}
