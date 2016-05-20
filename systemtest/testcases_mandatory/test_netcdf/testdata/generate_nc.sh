#!/bin/bash

for filename in *.cdl; do
  new_filename=$(echo "$filename" | sed 's/cdl/nc/g')
  echo "converting $filename to $new_filename"
  ncgen -o "$new_filename" "$filename"
done

echo done.
