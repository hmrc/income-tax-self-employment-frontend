#!/bin/bash

ROUTES_FILE="conf/app.routes"
last_section=$(awk '/#########/{x="";next}{x=x"\n"$0}END{print x}' "$ROUTES_FILE")

JOURNEY_TYPE=""
PARENT_PAGE_NAME=""

# Simulating associative array using prefix and suffix manipulation
page_names=()

while read -r line; do
  if [[ $line =~ controllers\.journeys\.([a-zA-Z]+)\.([a-zA-Z]+)\.([a-zA-Z]+)Controller ]]; then

    # Extract journey type and parent page name from the first match
    if [ -z "$JOURNEY_TYPE" ] && [ -z "$PARENT_PAGE_NAME" ]; then
      JOURNEY_TYPE=${BASH_REMATCH[1]}
      PARENT_PAGE_NAME=${BASH_REMATCH[2]}
    fi

    # Extract and store the unique page name
    page_name=${BASH_REMATCH[3]}
    if [[ ! " ${page_names[@]} " =~ " ${page_name} " ]]; then
        page_names+=("$page_name")
    fi
  fi
done <<< "$last_section"

create_target() {
  local page_name="$1"

  local target_controller_path="app/controllers/journeys/${JOURNEY_TYPE}/${PARENT_PAGE_NAME}/${page_name}Controller.scala"
  local target_form_path="app/forms/${JOURNEY_TYPE}/${PARENT_PAGE_NAME}/${page_name}FormProvider.scala"
  local target_page_path="app/pages/${JOURNEY_TYPE}/${PARENT_PAGE_NAME}/${page_name}Page.scala"
  local target_view_path="app/views/journeys/${JOURNEY_TYPE}/${PARENT_PAGE_NAME}/${page_name}View.scala.html"

  for target_path in "$target_controller_path" "$target_form_path" "$target_page_path" "$target_view_path"; do
    local dir_path=$(dirname "$target_path")
    mkdir -p "$dir_path"
    if [ ! -f "$target_path" ]; then
      touch "$target_path"
      echo "Created: $target_path"
    else
      echo "Skipped (already exists): $target_path"
    fi
  done
}

for page_name in "${page_names[@]}"; do
  create_target "$page_name"
done