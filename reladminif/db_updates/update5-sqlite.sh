#!/bin/sh

DIRECTQL="$CONF_DIR/../bin/rasserver"
SCRIPT_DIR="${SCRIPT_DIR:-.}"

if ! type python3 > /dev/null 2>&1; then
    error "python3 not found, please install it first."
fi

update_sdoms="$SCRIPT_DIR/update_sdoms.py"

python3 "$update_sdoms" "$DIRECTQL" "$DBCONN"

