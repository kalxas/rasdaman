# *
# * This file is part of rasdaman community.
# *
# * Rasdaman community is free software: you can redistribute it and/or modify
# * it under the terms of the GNU General Public License as published by
# * the Free Software Foundation, either version 3 of the License, or
# * (at your option) any later version.
# *
# * Rasdaman community is distributed in the hope that it will be useful,
# * but WITHOUT ANY WARRANTY; without even the implied warranty of
# * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# * See the GNU  General Public License for more details.
# *
# * You should have received a copy of the GNU  General Public License
# * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
# *
# * Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
# *
# * For more information please see <http://www.rasdaman.org>
# * or contact Peter Baumann via <baumann@rasdaman.com>.
# *

SRC = src
BIN = bin
LIB = lib
CP = cp -r
RM = rm -rf
MKDIR = mkdir
INSTALL_DIR = $(DESTDIR)$(datadir)/rasdaman/wcst_import
PYTHON_SCRIPT = $(datadir)/rasdaman/wcst_import/wcst_import.py
BIN_DIR = $(DESTDIR)$(bindir)

all:
	sed -e 's|WCST_IMPORT_PY_SCRIPT|$(PYTHON_SCRIPT)|g' wcst_import.sh.in > wcst_import.sh
	chmod +x wcst_import.sh
	@echo "Build Process sucessfully. You can now install the application by running $ make install"

install:
	$(MKDIR) -p $(INSTALL_DIR)
	$(CP) * $(INSTALL_DIR)
	$(CP) wcst_import.sh $(BIN_DIR)

uninstall:
	@$(RM) $(INSTALL_DIR)

clean:
	@$(RM) wcst_import.sh

EXTRA_DIST = *
