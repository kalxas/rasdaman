import pandoc
import os

# This script is used to generate README.md (markdown format used by GitHub) to README.rst (reStructred formate used by PYPI)
# When changing in README.md, run this script to have new README.rst.

# Install pandoc first (e.g: sudo yum install pandoc)
# https://coderwall.com/p/qawuyq/use-markdown-readme-s-in-python-modules
pandoc.core.PANDOC_PATH = '/usr/bin/pandoc'

doc = pandoc.Document()
doc.markdown = open('README.md').read()
f = open('README.rst','w')
f.write(doc.rst)
f.close()

