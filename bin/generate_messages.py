import requests
from bs4 import BeautifulSoup
import sys
import re

session = requests.Session()

cookie_string = 'authentication=95f33aca87239c2179260855dcb4ff289127a76e5f728833cd1b4c42149f95fc; govuk-prototype-kit-2f617070=s%3AojjeECHZCvj9t9aPDfkQWjgb9kRgpK7m.7Rt9JcmgfCi9JFYQMBKwE3UlXr%2FGr4JaejV7XejmVMc'

headers = {
    'Cookie': cookie_string
}

url = sys.argv[1]

response = requests.get(url)

html = ''

with requests.Session() as session:
    response = session.get(url, headers=headers)

    if response.status_code == 200:
        html = BeautifulSoup(response.content, 'html.parser')
    else:
        print(f"Failed to retrieve the webpage, status code: {response.status_code}")

target_div = html.select_one('main#main-content > div > div')
primary_element = target_div.find('form') or target_div
target_div = primary_element

tag_counters = {}
elements = []

tags_of_interest = ['h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'p', 'li']

for element in target_div.find_all(tags_of_interest):
    tag = 'h' if element.name.startswith('h') else element.name
    tag_counters[tag] = tag_counters.get(tag, 0) + 1
    tag_id = f"{tag}{tag_counters[tag]}"
    content = re.sub(r'\s+', ' ', element.get_text(" ", strip=True))
    elements.append((tag_id, content))

messages = "\n".join([f"{tag}={content}" for tag, content in elements])

print(messages)
