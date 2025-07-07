#!/usr/bin/env bash
  # delete-by-attr.sh
  # Usage: ./delete-by-attr.sh INPUT_XML ATTRIBUTE_NAME ATTRIBUTE_VALUE [OUTPUT_XML]
  # If OUTPUT_XML is omitted, changes are applied in-place (overwrites INPUT_XML).
delete_by_attribute() {
  set -eo pipefail

  INPUT="$1"
  ATTR="$2"
  VAL="$3"
  OUTPUT="${4:-$INPUT}"

  if [[ -z "$INPUT" || -z "$ATTR" || -z "$VAL" ]]; then
  echo "Usage: $0 INPUT_XML ATTRIBUTE_NAME ATTRIBUTE_VALUE [OUTPUT_XML]"
  exit 1
  fi

  # Construct XPath to match any element having the attribute name=VALUE.
  XPATH="//*[@$ATTR='$VAL']"

  if [[ "$OUTPUT" == "$INPUT" ]]; then
  xmlstarlet ed -L -P -d "$XPATH" "$INPUT"
  else
  xmlstarlet ed -P -d "$XPATH" "$INPUT" > "$OUTPUT"
  fi
}


file_content=$(<docker/standalone/openldap/ldap-utils/deprecated_attrs.txt)
IFS=$'\n' read -r -d '' -a deprecated <<< "$file_content"

# Iterate over each line
for item in "${deprecated[@]}"; do
    echo "Remove attribute $item from xml"
    delete_by_attribute "./store/src/main/resources/conf/rights/adminconsole-ui.xml" "n" "$item"
    delete_by_attribute "./store/src/main/resources/conf/rights/rights.xml" "n" "$item"
    delete_by_attribute "./store/src/main/resources/conf/rights/rights-adminconsole.xml" "n" "$item"
    delete_by_attribute "./store/src/main/resources/conf/rights/rights-adminconsole-domainadmin.xml" "n" "$item"
    delete_by_attribute "./store/src/main/resources/conf/rights/rights-domainadmin.xml" "n" "$item"
    delete_by_attribute "./store/src/main/resources/conf/rights/rights-roles.xml" "n" "$item"
    delete_by_attribute "./store/src/main/resources/conf/rights/user-rights.xml" "n" "$item"
done