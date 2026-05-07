# Project: income-tax-self-employment-frontend

Play Framework / Scala 3.3.7 frontend for HMRC Self Employment.

## Running Tests

### Unit tests
```
sbt test
```
Run a specific test:
```
sbt "testOnly *SomeSpec"
```

### Integration tests
Integration tests live in `it/test/` and are run via the `it` sbt subproject:
```
sbt it/test
```
Run a specific integration test:
```
sbt "it/Test/testOnly connectors.SelfEmploymentConnectorISpec"
```
Compile-only check:
```
sbt it/Test/compile
```

### FD limits
The full test suite can hit macOS file descriptor limits ("Too many open files"). Mitigations:
- Close IDE/Metals before running
- Run in smaller batches
- Use `sbt 'set Test/fork := true' test`

## Integration Test Patterns

- Base trait: `base.IntegrationBaseSpec` (extends `PlaySpec`, `GuiceOneServerPerSuite`, mixes in `DefaultBodyWritables`)
- WireMock support: `helpers.WiremockSpec` for stubbing downstream HTTP calls
- Auth stubs: `helpers.AuthStub.authorised()` / `.agentAuthorised()` / `.unauthorisedOtherEnrolment()`
- Answers API stubs: `helpers.AnswersApiStub` for stubbing journey answers endpoints
- MongoDB: `DbHelper` object inside `IntegrationBaseSpec` provides `insertEmpty()`, `insertOne()`, `insertMany()`, `insertUserAnswers()`, `getJson()`, `get()`, `teardown`
- WS client POST with form data requires explicit type: `.post(Map[String, Seq[String]]("key" -> Seq("value")))`

## Scala 3 Migration Notes

### Mockito
- `mockito-scala` upgraded to 2.2.1 (Scala 3 support)
- `scalamock` removed; all mock traits rewritten to use Mockito
- Import `org.mockito.IdiomaticMockito` or `org.scalatestplus.mockito.MockitoSugar` for mocking DSL
- Import `org.mockito.ArgumentMatchers.{any, eq => eqTo}` for argument matchers

### Matchers
- `convertToAnyShouldWrapper` does not work in Scala 3
- Use `import org.scalatest.matchers.should.Matchers.shouldBe` or mix in `Matchers`

### Type inference
- `Map("k" -> Seq("v"))` infers `Map[K, Seq[String]]` with unconstrained `K` in Scala 3 — use explicit `Map[String, Seq[String]](...)`
- Play JSON validation errors may be returned in a different order than Scala 2 — compare as `Set` not `Seq` when order doesn't matter
- EitherT calls may need explicit type params `[Future, ServiceError]` to avoid ambiguous givens

### MongoDB
- `org.mongodb.scala.SingleObservableFuture` import needed for `.toFuture()` on single observables
- Lambda type annotations: `{ x: Type =>` must be `{ (x: Type) =>`
