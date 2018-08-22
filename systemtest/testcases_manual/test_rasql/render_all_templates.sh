#!/bin/bash

readonly TEMPLATES_DIR="templates"
readonly QUERIES_DIR="queries"
readonly RASQTE="rasqte.py"

for f in "$TEMPLATES_DIR"/*.j2; do
    python $RASQTE -d "$QUERIES_DIR" -t "$f"
    echo
done
