SRC = src
BIN = bin
LIB = lib
CP = cp -r
RM = rm -rf
MKDIR = mkdir
INSTALL_DIR = $(DESTDIR)$(datadir)/rasdaman/wcst_import
PYTHON_SCRIPT = $(INSTALL_DIR)/wcst_import.py
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
