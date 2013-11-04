%global rasdir %{_sharedstatedir}/rasdaman
Name:           rasdaman
Version:        8.3.1
Release:        3%{?dist}
Summary:        rasdaman - Raster Data Manager

Group:          Applications/Databases
License:        GPLv3
URL:            http://rasdaman.org
Source0:        %{name}-%{version}.tar.gz
Source1:        rasdaman.init.in
%if 0%{?mandriva_version}  
BuildRoot:      %{_tmppath}/%{name}-%{version}  
%else
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
%endif

BuildRequires: bison
BuildRequires: libtiff-devel
BuildRequires: hdf-devel
BuildRequires: libjpeg-devel
BuildRequires: readline-devel
BuildRequires: zlib-devel
BuildRequires: libpng-devel
BuildRequires: netpbm-devel
BuildRequires: openssl-devel
BuildRequires: flex
BuildRequires: postgresql-devel
BuildRequires: doxygen
BuildRequires: netcdf-devel
BuildRequires: java-1.6.0-openjdk-devel

Requires(pre): /usr/sbin/useradd
Requires(pre): shadow-utils
Requires(post): chkconfig
Requires: postgresql-server

Provides: rasserver

%description
Rasdaman ("raster data manager"), see http://www.rasdaman.org, extends standard relational
database systems with the ability to store and retrieve multi-dimensional raster data
(arrays) of unlimited size through an SQL-style query language. On such sensor, image,
or statistics data appearing, e.g., in earth, space, and life science applications
rasdaman allows to quickly set up array-intensive services which are distinguished by
their flexibility, speed, and scalability.

The petascope component of rasdaman provides service interfaces based
on the  OGC  WCS,  WCPS,  WCS-T, and  WPS. For several of these, rasdaman
will be reference implementation.

Rasdaman embeds itself smoothly into  PostgreSQL; a  GDAL rasdaman driver is available,
and likewise a MapServer integration (beta). A PostGIS query language integration
is under work.

%package devel
Summary:        rasdaman headers
Group:          Development/Libraries
Requires:       %{name}%{?_isa} = %{version}-%{release}

%description devel
Files needed for rasdaman development.

Rasdaman ("raster data manager"), see http://www.rasdaman.org, extends standard relational
database systems with the ability to store and retrieve multi-dimensional raster data
(arrays) of unlimited size through an  SQL-style query language. On such sensor, image,
or statistics data appearing, e.g., in earth, space, and life science applications
rasdaman allows to quickly set up array-intensive services which are distinguished by
their flexibility, speed, and scalability. 

The petascope component of rasdaman provides service interfaces based
on the  OGC  WCS,  WCPS,  WCS-T, and  WPS. For several of these, rasdaman
will be reference implementation.

Rasdaman embeds itself smoothly into PostgreSQL; a GDAL rasdaman driver is available,
and likewise a  MapServer integration (beta). A  PostGIS query language integration
is under work, see our planning.

%package docs
Summary:        Documentation for rasdaman
Group:          Applications/Databases
Requires:       %{name} = %{version}-%{release}
BuildArch:      noarch

%description docs
The rasdaman-docs package includes documentation for rasdaman in html format.

%package examples
Summary:        Documentation for rasdaman
Group:          Applications/Databases
Requires:       %{name} = %{version}-%{release}
BuildArch:      noarch

%description examples
The rasdaman-examples package includes examples for rasdaman.

%package petascope
Summary:        Petascope is an add-in to the rasdaman
Group:          Applications/Databases
Requires:       %{name} = %{version}-%{release}
Requires:       tomcat6
BuildArch:      noarch

%description petascope
Petascope is an add-in to the rasdaman raster server providing making it a geo raster data with open, interoperable OGC standards-based interfaces.

%package rasdaview
Summary:        WxWidgets based GUI client for rasdaman
Group:          Graphics
Requires:       %{name}%{?_isa} = %{version}-%{release}

%description rasdaview
The rasdaman-rasdaview package installs GUI client for rasdaman. It is based on WxWidgets.

%package rasgeo
Summary:        rasgeo is an add-in for GDAL-based image file import
Group:          Applications/Databases
Requires:       %{name}%{?_isa} = %{version}-%{release} gdal

%description rasgeo
The rasgeo package is an add-in for GDAL-based image file import. It uses GDAL.

%package raswct
Summary:        Rasdaman Web Client Toolkit based on JavaScript
Group:          Applications/Databases
Requires:       %{name} = %{version}-%{release}
BuildArch:      noarch

%description raswct
raswct is a Web Client Toolkit based on JavaScript. The main purpose of this toolkit is to 
allow developers to create user interfaces for displaying data from a raster database.

%prep
%setup -q

%build
sed -i 's#    JAVA_DIR = petascope raswct petascope/src/main/db secore#    JAVA_DIR = petascope raswct petascope/src/main/db#' applications/Makefile.am

autoreconf -fi

CC="gcc -L%{_libdir}/hdf -I/usr/include/netpbm -fpermissive -g -O2" CXX="g++ -L%{_libdir}/hdf -I/usr/include/gdal -I/usr/include/netpbm -fpermissive -g -O2" \
	./configure \
		--prefix=/usr \
		--docdir=%{_docdir}/rasdaman \
		--libdir=%{_libdir} \
		--localstatedir=%{_localstatedir} \
		--sysconfdir=%{_sysconfdir}/rasdaman \
		--with-logdir=%{_localstatedir}/log/rasdaman \
	  --with-hdf4 \
	  --with-netcdf \
		--with-pic \
		--with-docs \
    --with-wardir=%{_sharedstatedir}/tomcat6/webapps
sed -i 's/^metadata_user=.\+/metadata_user=tomcat6/' applications/petascope/src/main/resources/petascope.properties
sed -i 's/^metadata_pass=.\+/metadata_pass=/' applications/petascope/src/main/resources/petascope.properties
sed -i 's#@confdir@#%{_sysconfdir}/rasdaman#' applications/petascope/src/main/webapp/WEB-INF/web.xml.in

make %{?_smp_mflags} DESTDIR=%{buildroot}

%install
rm -rf %{buildroot}

mkdir -p %{buildroot}%{_sharedstatedir}/tomcat6/webapps/secoredb
make install DESTDIR=%{buildroot}

# install SYSV init stuff
mkdir -p %{buildroot}%{_initddir}
sed 's/^RASVERSION=.*$/RASVERSION=%{version}/' < %{SOURCE1} > %{_sourcedir}/rasdaman.init
install -m 755 %{_sourcedir}/rasdaman.init %{buildroot}%{_initddir}/rasdaman

# Remove unpackaged files
rm -f %{buildroot}%{_bindir}/rasmgr.conf
rm -f %{buildroot}%{_bindir}/create_db.sh
rm -f %{buildroot}%{_bindir}/start_rasdaman.sh
rm -f %{buildroot}%{_bindir}/stop_rasdaman.sh
rm -f %{buildroot}%{_bindir}/directql

# Create home for our user
install -d -m 700 %{buildroot}%{rasdir}
cp -a %{buildroot}%{_datadir}/rasdaman/examples/rasdl/basictypes.dl %{buildroot}%{rasdir}

# Move includes from topdir to subdir
mkdir %{buildroot}%{_includedir}/rasdaman
mv %{buildroot}%{_includedir}/basictypes.hh %{buildroot}%{_includedir}/rasdaman
mv %{buildroot}%{_includedir}/bool.h %{buildroot}%{_includedir}/rasdaman
mv %{buildroot}%{_includedir}/clientcomm %{buildroot}%{_includedir}/rasdaman
mv %{buildroot}%{_includedir}/commline %{buildroot}%{_includedir}/rasdaman
mv %{buildroot}%{_includedir}/conversion %{buildroot}%{_includedir}/rasdaman
mv %{buildroot}%{_includedir}/globals.hh %{buildroot}%{_includedir}/rasdaman
mv %{buildroot}%{_includedir}/rasdaman.hh %{buildroot}%{_includedir}/rasdaman
mv %{buildroot}%{_includedir}/stdexcept.h %{buildroot}%{_includedir}/rasdaman
mv %{buildroot}%{_includedir}/debug %{buildroot}%{_includedir}/rasdaman

# Move rview pieces from bin
mkdir -p %{buildroot}%{_libdir}/rasdaview/bin
mv %{buildroot}%{_bindir}/labels.txt %{buildroot}%{_libdir}/rasdaview/bin
mv %{buildroot}%{_bindir}/rview %{buildroot}%{_libdir}/rasdaview/bin/rasdaview.bin
mv %{buildroot}%{_bindir}/../.rviewrc %{buildroot}%{_libdir}/rasdaview
cp -a %{buildroot}%{_datadir}/rasdaman/errtxts* %{buildroot}%{_libdir}/rasdaview/bin

echo "#!/bin/bash" > %{buildroot}%{_bindir}/rasdaview
echo "cd %{_libdir}/rasdaview/bin" >> %{buildroot}%{_bindir}/rasdaview
echo "exec %{_libdir}/rasdaview/bin/rasdaview.bin" >> %{buildroot}%{_bindir}/rasdaview

chmod +x %{buildroot}%{_bindir}/rasdaview

%clean
rm -rf %{buildroot}

%pre
# Add the "rasdaman" user
getent group rasdaman >/dev/null || groupadd -r rasdaman
getent passwd rasdaman >/dev/null || \
    useradd -r -g rasdaman -d %{rasdir} -s /sbin/nologin -c "Rasdaman" rasdaman
exit 0

%preun
# If not upgrading
if [ $1 = 0 ] ; then
	/sbin/service rasdaman stop >/dev/null 2>&1
	chkconfig --del rasdaman
fi

%post
chkconfig --add rasdaman

%postun
# If upgrading
if [ $1 -ge 1 ] ; then
	/sbin/service rasdaman condrestart >/dev/null 2>&1 || :
fi

%files
%defattr(-,root,root,-)
%{_bindir}/rasdaman_insertdemo.sh
%{_bindir}/insertppm
%{_bindir}/rascontrol
%{_bindir}/rasdl
%{_bindir}/rasmgr
%{_bindir}/raspasswd
%{_bindir}/rasql
%{_bindir}/rasserver
%{_bindir}/update_db.sh
%config(noreplace) %verify(not md5 mtime size) %{_sysconfdir}/rasdaman/rasmgr.conf
%{_localstatedir}/log/rasdaman/empty
%{_datadir}/rasdaman/errtxts*
%{_datadir}/rasdaman/db_updates
%attr(700,rasdaman,rasdaman) %dir %{rasdir}
%attr(644,rasdaman,rasdaman) %config(noreplace) %{rasdir}/basictypes.dl
%{_initddir}/rasdaman

%files devel
%defattr(-,root,root,-)
%{_includedir}/rasdaman
%{_includedir}/raslib
%{_includedir}/rasodmg
%{_includedir}/config.h
%{_libdir}/libcatalogmgr.a
%{_libdir}/libclientcomm.a
%{_libdir}/libcommline.a
%{_libdir}/libconversion.a
%{_libdir}/libhttpserver.a
%{_libdir}/libindexmgr.a
%{_libdir}/libmddmgr.a
%{_libdir}/libnetwork.a
%{_libdir}/libqlparser.a
%{_libdir}/libraslib.a
%{_libdir}/librasodmg.a
%{_libdir}/libreladminif.a
%{_libdir}/librelblobif.a
%{_libdir}/librelcatalogif.a
%{_libdir}/librelindexif.a
%{_libdir}/librelmddif.a
%{_libdir}/librelstorageif.a
%{_libdir}/libservercomm.a
%{_libdir}/libstoragemgr.a
%{_libdir}/libtilemgr.a

%files docs
%defattr(-,root,root,-)
%{_datadir}/rasdaman/doc

%files examples
%defattr(-,root,root,-)
%{_datadir}/rasdaman/examples

%files petascope
%defattr(-,root,root,-)
%{_datadir}/rasdaman/petascope/*
%{_sharedstatedir}/tomcat6/webapps/petascope.war
%{_sharedstatedir}/tomcat6/webapps/def.war
%config(noreplace) %verify(not md5 mtime size) %{_sysconfdir}/rasdaman/petascope.properties
%config(noreplace) %verify(not md5 mtime size) %{_sysconfdir}/rasdaman/log4j.properties
%{_bindir}/petascope_insertdemo.sh
%{_bindir}/update_petascopedb.sh

%files rasdaview
%defattr(-,root,root,-)
%{_bindir}/rasdaview
%{_libdir}/rasdaview

%files rasgeo
%defattr(-,root,root,-)
%{_bindir}/rasimport
%{_bindir}/raserase
%{_bindir}/fillpyramid
%{_bindir}/initpyramid
%{_bindir}/add_wms_service.sh
%{_bindir}/drop_wms.sh
%{_bindir}/fill_pyramid.sh
%{_bindir}/init_wms.sh

%files raswct
%defattr(-,root,root,-)
%{_datadir}/rasdaman/raswct

%changelog

* Mon Nov 12  2012 Dimitar Misev <misev@rasdaman.com> - 8.3.1-3

- Add WMS import tools to the rasgeo package

* Mon Nov 05  2012 Dimitar Misev <misev@rasdaman.com> - 8.3.1-2

- Use macros for common directories
- Set documentation, examples, raswct, petascope packages to noarch build
- Use concurrency when running make
- Fix user/group creation, and don't delete the user on package uninstall
- Add tomcat as dependency for petascope
- Petascope is run by tomcat so use this user to connect to postgres
- Add {?_isa} to sub-packages

* Mon Jul 26  2012 Konstantin Kozlov <mackoel@gmail.com> - 8.3.1-1

- Added gdal to Requires

* Wed Jul 11  2012 Dimitar Misev <misev@rasdaman.com> - 8.3.1-1

- Moved petascope settings files to /etc/rasdaman
- Split update_db.sh into update_petascopedb.sh and petascope_insertdemo.sh

* Fri Jun 29  2012 Dimitar Misev <misev@rasdaman.com> - 8.3.1-0

- insertdemo.sh renamed to rasdaman_insertdemo.sh in trunk

* Sun Feb 26  2012 Dimitar Misev <misev@rasdaman.com> - 8.3.0-2

- Rename the init script from rasmgr to rasdaman

* Sun Jan 29  2012 Dimitar Misev <misev@rasdaman.com> - 8.3.0-1

- Move rasview to rasdaview
- Add raswct

* Sun Jan 22  2012 Dimitar Misev <misev@rasdaman.com> - 8.3.0-0

- New rasdaman version
- Move petascope install to a deploy target

* Sat Dec 17  2011 Dimitar Misev <misev@rasdaman.com> - 8.2.1-4

- Move petascope to applications directory
- Fixed the all target in petascope's Makefile
- Remove compression

* Fri Dec 09  2011 Konstantin Kozlov <kozlov@spbcas.ru> - 8.2.1-3

- Merged with upstream
- Add rasgeo

* Thu Nov 02  2011 Konstantin Kozlov <kozlov@spbcas.ru> - 8.2.1-2

- Merged with upstream.
- Added rview, petascope packages.

* Fri Oct 21  2011 Dimitar Misev <d.misev@jacobs-university.de> - 8.2.1-1

- Support for rview

* Thu Jul 30  2011 Konstantin Kozlov <kozlov@spbcas.ru> - 8.2.1-0

- Merge with upstream.

* Thu Jul 12  2011 Konstantin Kozlov <kozlov@spbcas.ru> - 8.0.0-1

- Docs and examples packages. Fix for SL6

* Thu Feb 17  2011 Konstantin Kozlov <kozlov@spbcas.ru> - 8.0.0-0

- Initial spec
