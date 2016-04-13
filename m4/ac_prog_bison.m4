dnl
dnl Check for bison
dnl AC_PROG_BISON([MIN_VERSION=2.0])
dnl
AC_DEFUN([AC_PROG_BISON], [

AC_CHECK_PROG(have_prog_bison, [bison], [yes],[no])

#Do not use *.h extension for parser header file but *.hh
bison_use_parser_h_extension=false

if test "$have_prog_bison" = "yes" ; then
#Verify automake version
#Upto version 1.11 parser headers for yy files are with h extension, from 1.12 it is hh
        automake_version=`automake --version | head -n 1 | cut '-d ' -f 4`
        AC_DEFINE_UNQUOTED([AUTOMAKE_VERSION], [$automake_version],
                           [Defines automake version])

        if test "$automake_version" \< "1.12" ; then
            #Use *.h extension for parser header file
            bison_use_parser_h_extension=true
            echo "Automake version < 1.12"
            AC_DEFINE([BISON_USE_PARSER_H_EXTENSION], [1],
                      [Use *.h extension for parser header file])
        fi
fi

AM_CONDITIONAL([BISON_USE_PARSER_H_EXTENSION], [test x$bison_use_parser_h_extension = xtrue])
])
