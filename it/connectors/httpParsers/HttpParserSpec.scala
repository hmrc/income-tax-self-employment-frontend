package connectors.httpParsers

class HttpParserSpec extends HttpParserBehaviours {
  
  "FakeParser"  - {
      behave like logHttpResponse()
      behave like handleSingleError()
      behave like handleMultpleError()
      behave like returnParsingErrors()
    }
}
