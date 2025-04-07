#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app$journeyName;format="cap"$.routes"

echo "" >> ../conf/app$journeyName;format="cap"$.routes
echo "GET        /:taxYear/:businessId/$journeyName;format="normalize,lower"$/$className;format="decap"$                        controllers.journeys.$journeyName;format="normalize,lower"$.$packageName$.$className$Controller.onPageLoad(taxYear:TaxYear, businessId: BusinessId, mode: Mode = NormalMode)" >> ../conf/app$journeyName;format="cap"$.routes
echo "POST       /:taxYear/:businessId/$journeyName;format="normalize,lower"$/$className;format="decap"$                        controllers.journeys.$journeyName;format="normalize,lower"$.$packageName$.$className$Controller.onSubmit(taxYear:TaxYear, businessId: BusinessId, mode: Mode = NormalMode)" >> ../conf/app$journeyName;format="cap"$.routes

echo "GET        /:taxYear/:businessId/change$className$                  controllers.journeys.$journeyName;format="normalize,lower"$.$packageName$.$className$Controller.onPageLoad(taxYear:TaxYear, businessId: BusinessId, mode: Mode = CheckMode)" >> ../conf/app$journeyName;format="cap"$.routes
echo "POST       /:taxYear/:businessId/change$className$                  controllers.journeys.$journeyName;format="normalize,lower"$.$packageName$.$className$Controller.onSubmit(taxYear:TaxYear, businessId: BusinessId, mode: Mode = CheckMode)" >> ../conf/app$journeyName;format="cap"$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.checkYourAnswersLabel = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.error.required = Select yes if $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Migration $className;format="snake"$ completed"
