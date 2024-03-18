To generage messages from prototype follow:

1. Go to the prototype page you want to scrape
2. Get Cookie value from Development tools
3. set env variable: `export COOKIE=<value copied>`
4. Run generate_messages.py and pass the prototype URL, e.g.: `python generate_messages.py https://income-tax-submission.herokuapp.com/v06/current/self-employment/capital-allowances/pools/any-single-asset-pools-amount`
