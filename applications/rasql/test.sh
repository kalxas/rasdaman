#!/bin/bash

NO_COLLS=$(seq 1 $1);

for i in $NO_COLLS
do
    ./rasql -q "select avg_cells(rgb) from rgb" --out string&
done
