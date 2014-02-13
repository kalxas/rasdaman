-- ~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=
-- This file is part of rasdaman community.
--
-- Rasdaman community is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- Rasdaman community is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
--
-- Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
-- rasdaman GmbH.
--
-- For more information please see <http://www.rasdaman.org>
-- or contact Peter Baumann via <baumann@rasdaman.com>.
-- ~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=

-- ####################################################################
-- |                 EPSG CRSs with northings first                   |
-- ####################################################################

-----------------------------------------------------------------------
-- File to create a table which specifies the EPSG CRS codes defining the
-- northing coordinate before the eastings.
-----------------------------------------------------------------------

CREATE TABLE ps9_north_first_crs (
    axis_abbrev  text,
    axis_dir     text,
    crs_code     numeric PRIMARY KEY,
    crs_type     text,
    crs_name     text
);

-- epsg_v8_3=# SELECT coord_axis_abbreviation AS "axis bbrv",
--                    coord_axis_orientation  AS "axis direction",
--                    coord_ref_sys_code      AS "crs code",
--                    coord_ref_sys_kind      AS "crs type",
--                    coord_ref_sys_name      AS "crs name"
--             FROM epsg_coordinatereferencesystem, epsg_coordinateaxis
--             WHERE epsg_coordinateaxis.coord_sys_code = epsg_coordinatereferencesystem.coord_sys_code
--               AND coord_axis_abbreviation     IN ('Lat','J','n','N','N(Y)','y','Y')
--               AND coord_axis_orientation  NOT IN ('east','west')
--               AND coord_ref_sys_kind <> 'engineering'
--               AND coord_axis_order   =  1
--             ORDER BY "crs code";
-- > psql -f %query% -F $'\t' -P t -P format=unaligned  epsg_v8_3
COPY ps9_north_first_crs (axis_abbrev, axis_dir, crs_code, crs_type, crs_name) FROM STDIN;
N	north	2036	projected	NAD83(CSRS98) / New Brunswick Stereo
Y	north	2085	projected	NAD27 / Cuba Norte
Y	north	2086	projected	NAD27 / Cuba Sur
N	north	2105	projected	NZGD2000 / Mount Eden 2000
N	north	2106	projected	NZGD2000 / Bay of Plenty 2000
N	north	2107	projected	NZGD2000 / Poverty Bay 2000
N	north	2108	projected	NZGD2000 / Hawkes Bay 2000
N	north	2109	projected	NZGD2000 / Taranaki 2000
N	north	2110	projected	NZGD2000 / Tuhirangi 2000
N	north	2111	projected	NZGD2000 / Wanganui 2000
N	north	2112	projected	NZGD2000 / Wairarapa 2000
N	north	2113	projected	NZGD2000 / Wellington 2000
N	north	2114	projected	NZGD2000 / Collingwood 2000
N	north	2115	projected	NZGD2000 / Nelson 2000
N	north	2116	projected	NZGD2000 / Karamea 2000
N	north	2117	projected	NZGD2000 / Buller 2000
N	north	2118	projected	NZGD2000 / Grey 2000
N	north	2119	projected	NZGD2000 / Amuri 2000
N	north	2120	projected	NZGD2000 / Marlborough 2000
N	north	2121	projected	NZGD2000 / Hokitika 2000
N	north	2122	projected	NZGD2000 / Okarito 2000
N	north	2123	projected	NZGD2000 / Jacksons Bay 2000
N	north	2124	projected	NZGD2000 / Mount Pleasant 2000
N	north	2125	projected	NZGD2000 / Gawler 2000
N	north	2126	projected	NZGD2000 / Timaru 2000
N	north	2127	projected	NZGD2000 / Lindis Peak 2000
N	north	2128	projected	NZGD2000 / Mount Nicholas 2000
N	north	2129	projected	NZGD2000 / Mount York 2000
N	north	2130	projected	NZGD2000 / Observation Point 2000
N	north	2131	projected	NZGD2000 / North Taieri 2000
N	north	2132	projected	NZGD2000 / Bluff 2000
N	north	2193	projected	NZGD2000 / New Zealand Transverse Mercator 2000
N	north	2200	projected	ATS77 / New Brunswick Stereographic (ATS77)
Y	north	2218	projected	Scoresbysund 1952 / Greenland zone 5 east
Y	north	2221	projected	Scoresbysund 1952 / Greenland zone 6 east
Y	north	2296	projected	Ammassalik 1958 / Greenland zone 7 east
N	north	2297	projected	Qornoq 1927 / Greenland zone 1 east
N	north	2298	projected	Qornoq 1927 / Greenland zone 2 east
Y	north	2299	projected	Qornoq 1927 / Greenland zone 2 west
N	north	2300	projected	Qornoq 1927 / Greenland zone 3 east
Y	north	2301	projected	Qornoq 1927 / Greenland zone 3 west
N	north	2302	projected	Qornoq 1927 / Greenland zone 4 east
Y	north	2303	projected	Qornoq 1927 / Greenland zone 4 west
Y	north	2304	projected	Qornoq 1927 / Greenland zone 5 west
Y	north	2305	projected	Qornoq 1927 / Greenland zone 6 west
Y	north	2306	projected	Qornoq 1927 / Greenland zone 7 west
Y	north	2307	projected	Qornoq 1927 / Greenland zone 8 east
N	north	2326	projected	Hong Kong 1980 Grid System
N	north	2953	projected	NAD83(CSRS) / New Brunswick Stereographic
N	north	3006	projected	SWEREF99 TM
N	north	3007	projected	SWEREF99 12 00
N	north	3008	projected	SWEREF99 13 30
N	north	3009	projected	SWEREF99 15 00
N	north	3010	projected	SWEREF99 16 30
N	north	3011	projected	SWEREF99 18 00
N	north	3012	projected	SWEREF99 14 15
N	north	3013	projected	SWEREF99 15 45
N	north	3014	projected	SWEREF99 17 15
N	north	3015	projected	SWEREF99 18 45
N	north	3016	projected	SWEREF99 20 15
N	north	3017	projected	SWEREF99 21 45
N	north	3018	projected	SWEREF99 23 15
N	north	3034	projected	ETRS89 / LCC Europe
Y	north	3035	projected	ETRS89 / LAEA Europe
N	north	3038	projected	ETRS89 / TM26
N	north	3039	projected	ETRS89 / TM27
N	north	3040	projected	ETRS89 / UTM zone 28N (N-E)
N	north	3041	projected	ETRS89 / UTM zone 29N (N-E)
N	north	3042	projected	ETRS89 / UTM zone 30N (N-E)
N	north	3043	projected	ETRS89 / UTM zone 31N (N-E)
N	north	3044	projected	ETRS89 / UTM zone 32N (N-E)
N	north	3045	projected	ETRS89 / UTM zone 33N (N-E)
N	north	3046	projected	ETRS89 / UTM zone 34N (N-E)
N	north	3047	projected	ETRS89 / UTM zone 35N (N-E)
N	north	3048	projected	ETRS89 / UTM zone 36N (N-E)
N	north	3049	projected	ETRS89 / UTM zone 37N (N-E)
N	north	3050	projected	ETRS89 / TM38
N	north	3051	projected	ETRS89 / TM39
N	north	3114	projected	MAGNA-SIRGAS / Colombia Far West zone
N	north	3115	projected	MAGNA-SIRGAS / Colombia West zone
N	north	3116	projected	MAGNA-SIRGAS / Colombia Bogota zone
N	north	3117	projected	MAGNA-SIRGAS / Colombia East Central zone
N	north	3118	projected	MAGNA-SIRGAS / Colombia East zone
N	north	3126	projected	ETRS89 / ETRS-GK19FIN
N	north	3127	projected	ETRS89 / ETRS-GK20FIN
N	north	3128	projected	ETRS89 / ETRS-GK21FIN
N	north	3129	projected	ETRS89 / ETRS-GK22FIN
N	north	3130	projected	ETRS89 / ETRS-GK23FIN
N	north	3131	projected	ETRS89 / ETRS-GK24FIN
N	north	3132	projected	ETRS89 / ETRS-GK25FIN
N	north	3133	projected	ETRS89 / ETRS-GK26FIN
N	north	3134	projected	ETRS89 / ETRS-GK27FIN
N	north	3135	projected	ETRS89 / ETRS-GK28FIN
N	north	3136	projected	ETRS89 / ETRS-GK29FIN
N	north	3137	projected	ETRS89 / ETRS-GK30FIN
N	north	3138	projected	ETRS89 / ETRS-GK31FIN
Y	north	3144	projected	FD54 / Faroe Lambert
Y	north	3145	projected	ETRS89 / Faroe Lambert
Y	north	3173	projected	fk89 / Faroe Lambert FK89
N	north	3366	projected	Hong Kong 1963 Grid System
N	north	3407	projected	Hong Kong 1963 Grid System
N	north	3414	projected	SVY21 / Singapore TM
N	north	3764	projected	NZGD2000 / Chatham Island Circuit 2000
N	north	3788	projected	NZGD2000 / Auckland Islands TM 2000
N	north	3789	projected	NZGD2000 / Campbell Island TM 2000
N	north	3790	projected	NZGD2000 / Antipodes Islands TM 2000
N	north	3791	projected	NZGD2000 / Raoul Island TM 2000
N	north	3793	projected	NZGD2000 / Chatham Islands TM 2000
Y	north	3795	projected	NAD27 / Cuba Norte
Y	north	3796	projected	NAD27 / Cuba Sur
Lat	north	3819	geographic 2D	HD1909
Lat	north	3821	geographic 2D	TWD67
Lat	north	3823	geographic 3D	TWD97
Lat	north	3824	geographic 2D	TWD97
N	north	3851	projected	NZGD2000 / NZCS2000
N	north	3852	projected	RSRGD2000 / DGLC2000
N	north	3873	projected	ETRS89 / GK19FIN
N	north	3874	projected	ETRS89 / GK20FIN
N	north	3875	projected	ETRS89 / GK21FIN
N	north	3876	projected	ETRS89 / GK22FIN
N	north	3877	projected	ETRS89 / GK23FIN
N	north	3878	projected	ETRS89 / GK24FIN
N	north	3879	projected	ETRS89 / GK25FIN
N	north	3880	projected	ETRS89 / GK26FIN
N	north	3881	projected	ETRS89 / GK27FIN
N	north	3882	projected	ETRS89 / GK28FIN
N	north	3883	projected	ETRS89 / GK29FIN
N	north	3884	projected	ETRS89 / GK30FIN
N	north	3885	projected	ETRS89 / GK31FIN
Lat	north	3888	geographic 3D	IGRS
Lat	north	3889	geographic 2D	IGRS
Lat	north	3906	geographic 2D	MGI 1901
Lat	north	4001	geographic 2D	Unknown datum based upon the Airy 1830 ellipsoid
Lat	north	4002	geographic 2D	Unknown datum based upon the Airy Modified 1849 ellipsoid
Lat	north	4003	geographic 2D	Unknown datum based upon the Australian National Spheroid
Lat	north	4004	geographic 2D	Unknown datum based upon the Bessel 1841 ellipsoid
Lat	north	4005	geographic 2D	Unknown datum based upon the Bessel Modified ellipsoid
Lat	north	4006	geographic 2D	Unknown datum based upon the Bessel Namibia ellipsoid
Lat	north	4007	geographic 2D	Unknown datum based upon the Clarke 1858 ellipsoid
Lat	north	4008	geographic 2D	Unknown datum based upon the Clarke 1866 ellipsoid
Lat	north	4009	geographic 2D	Unknown datum based upon the Clarke 1866 Michigan ellipsoid
Lat	north	4010	geographic 2D	Unknown datum based upon the Clarke 1880 (Benoit) ellipsoid
Lat	north	4011	geographic 2D	Unknown datum based upon the Clarke 1880 (IGN) ellipsoid
Lat	north	4012	geographic 2D	Unknown datum based upon the Clarke 1880 (RGS) ellipsoid
Lat	north	4013	geographic 2D	Unknown datum based upon the Clarke 1880 (Arc) ellipsoid
Lat	north	4014	geographic 2D	Unknown datum based upon the Clarke 1880 (SGA 1922) ellipsoid
Lat	north	4015	geographic 2D	Unknown datum based upon the Everest 1830 (1937 Adjustment) ellipsoid
Lat	north	4016	geographic 2D	Unknown datum based upon the Everest 1830 (1967 Definition) ellipsoid
Lat	north	4017	geographic 3D	MOLDREF99
Lat	north	4018	geographic 2D	Unknown datum based upon the Everest 1830 Modified ellipsoid
Lat	north	4019	geographic 2D	Unknown datum based upon the GRS 1980 ellipsoid
Lat	north	4020	geographic 2D	Unknown datum based upon the Helmert 1906 ellipsoid
Lat	north	4021	geographic 2D	Unknown datum based upon the Indonesian National Spheroid
Lat	north	4022	geographic 2D	Unknown datum based upon the International 1924 ellipsoid
Lat	north	4023	geographic 2D	MOLDREF99
Lat	north	4024	geographic 2D	Unknown datum based upon the Krassowsky 1940 ellipsoid
Lat	north	4025	geographic 2D	Unknown datum based upon the NWL 9D ellipsoid
Lat	north	4027	geographic 2D	Unknown datum based upon the Plessis 1817 ellipsoid
Lat	north	4028	geographic 2D	Unknown datum based upon the Struve 1860 ellipsoid
Lat	north	4029	geographic 2D	Unknown datum based upon the War Office ellipsoid
Lat	north	4030	geographic 2D	Unknown datum based upon the WGS 84 ellipsoid
Lat	north	4031	geographic 2D	Unknown datum based upon the GEM 10C ellipsoid
Lat	north	4032	geographic 2D	Unknown datum based upon the OSU86F ellipsoid
Lat	north	4033	geographic 2D	Unknown datum based upon the OSU91A ellipsoid
Lat	north	4034	geographic 2D	Unknown datum based upon the Clarke 1880 ellipsoid
Lat	north	4035	geographic 2D	Unknown datum based upon the Authalic Sphere
Lat	north	4036	geographic 2D	Unknown datum based upon the GRS 1967 ellipsoid
N	north	4037	projected	WGS 84 / TMzn35N
N	north	4038	projected	WGS 84 / TMzn36N
Lat	north	4040	geographic 3D	RGRDC 2005
Lat	north	4041	geographic 2D	Unknown datum based upon the Average Terrestrial System 1977 ellipsoid
Lat	north	4042	geographic 2D	Unknown datum based upon the Everest (1830 Definition) ellipsoid
Lat	north	4043	geographic 2D	Unknown datum based upon the WGS 72 ellipsoid
Lat	north	4044	geographic 2D	Unknown datum based upon the Everest 1830 (1962 Definition) ellipsoid
Lat	north	4045	geographic 2D	Unknown datum based upon the Everest 1830 (1975 Definition) ellipsoid
Lat	north	4046	geographic 2D	RGRDC 2005
Lat	north	4047	geographic 2D	Unspecified datum based upon the GRS 1980 Authalic Sphere
Lat	north	4052	geographic 2D	Unspecified datum based upon the Clarke 1866 Authalic Sphere
Lat	north	4053	geographic 2D	Unspecified datum based upon the International 1924 Authalic Sphere
Lat	north	4054	geographic 2D	Unspecified datum based upon the Hughes 1980 ellipsoid
Lat	north	4055	geographic 2D	Popular Visualisation CRS
Lat	north	4074	geographic 3D	SREF98
Lat	north	4075	geographic 2D	SREF98
Lat	north	4080	geographic 3D	REGCAN95
Lat	north	4081	geographic 2D	REGCAN95
Lat	north	4120	geographic 2D	Greek
Lat	north	4121	geographic 2D	GGRS87
Lat	north	4122	geographic 2D	ATS77
Lat	north	4123	geographic 2D	KKJ
Lat	north	4124	geographic 2D	RT90
Lat	north	4125	geographic 2D	Samboja
Lat	north	4126	geographic 2D	LKS94 (ETRS89)
Lat	north	4127	geographic 2D	Tete
Lat	north	4128	geographic 2D	Madzansua
Lat	north	4129	geographic 2D	Observatario
Lat	north	4130	geographic 2D	Moznet
Lat	north	4131	geographic 2D	Indian 1960
Lat	north	4132	geographic 2D	FD58
Lat	north	4133	geographic 2D	EST92
Lat	north	4134	geographic 2D	PSD93
Lat	north	4135	geographic 2D	Old Hawaiian
Lat	north	4136	geographic 2D	St. Lawrence Island
Lat	north	4137	geographic 2D	St. Paul Island
Lat	north	4138	geographic 2D	St. George Island
Lat	north	4139	geographic 2D	Puerto Rico
Lat	north	4140	geographic 2D	NAD83(CSRS98)
Lat	north	4141	geographic 2D	Israel
Lat	north	4142	geographic 2D	Locodjo 1965
Lat	north	4143	geographic 2D	Abidjan 1987
Lat	north	4144	geographic 2D	Kalianpur 1937
Lat	north	4145	geographic 2D	Kalianpur 1962
Lat	north	4146	geographic 2D	Kalianpur 1975
Lat	north	4147	geographic 2D	Hanoi 1972
Lat	north	4148	geographic 2D	Hartebeesthoek94
Lat	north	4149	geographic 2D	CH1903
Lat	north	4150	geographic 2D	CH1903+
Lat	north	4151	geographic 2D	CHTRF95
Lat	north	4152	geographic 2D	NAD83(HARN)
Lat	north	4153	geographic 2D	Rassadiran
Lat	north	4154	geographic 2D	ED50(ED77)
Lat	north	4155	geographic 2D	Dabola 1981
Lat	north	4156	geographic 2D	S-JTSK
Lat	north	4157	geographic 2D	Mount Dillon
Lat	north	4158	geographic 2D	Naparima 1955
Lat	north	4159	geographic 2D	ELD79
Lat	north	4160	geographic 2D	Chos Malal 1914
Lat	north	4161	geographic 2D	Pampa del Castillo
Lat	north	4162	geographic 2D	Korean 1985
Lat	north	4163	geographic 2D	Yemen NGN96
Lat	north	4164	geographic 2D	South Yemen
Lat	north	4165	geographic 2D	Bissau
Lat	north	4166	geographic 2D	Korean 1995
Lat	north	4167	geographic 2D	NZGD2000
Lat	north	4168	geographic 2D	Accra
Lat	north	4169	geographic 2D	American Samoa 1962
Lat	north	4170	geographic 2D	SIRGAS 1995
Lat	north	4171	geographic 2D	RGF93
Lat	north	4172	geographic 2D	POSGAR
Lat	north	4173	geographic 2D	IRENET95
Lat	north	4174	geographic 2D	Sierra Leone 1924
Lat	north	4175	geographic 2D	Sierra Leone 1968
Lat	north	4176	geographic 2D	Australian Antarctic
Lat	north	4178	geographic 2D	Pulkovo 1942(83)
Lat	north	4179	geographic 2D	Pulkovo 1942(58)
Lat	north	4180	geographic 2D	EST97
Lat	north	4181	geographic 2D	Luxembourg 1930
Lat	north	4182	geographic 2D	Azores Occidental 1939
Lat	north	4183	geographic 2D	Azores Central 1948
Lat	north	4184	geographic 2D	Azores Oriental 1940
Lat	north	4185	geographic 2D	Madeira 1936
Lat	north	4188	geographic 2D	OSNI 1952
Lat	north	4189	geographic 2D	REGVEN
Lat	north	4190	geographic 2D	POSGAR 98
Lat	north	4191	geographic 2D	Albanian 1987
Lat	north	4192	geographic 2D	Douala 1948
Lat	north	4193	geographic 2D	Manoca 1962
Lat	north	4194	geographic 2D	Qornoq 1927
Lat	north	4195	geographic 2D	Scoresbysund 1952
Lat	north	4196	geographic 2D	Ammassalik 1958
Lat	north	4197	geographic 2D	Garoua
Lat	north	4198	geographic 2D	Kousseri
Lat	north	4199	geographic 2D	Egypt 1930
Lat	north	4200	geographic 2D	Pulkovo 1995
Lat	north	4201	geographic 2D	Adindan
Lat	north	4202	geographic 2D	AGD66
Lat	north	4203	geographic 2D	AGD84
Lat	north	4204	geographic 2D	Ain el Abd
Lat	north	4205	geographic 2D	Afgooye
Lat	north	4206	geographic 2D	Agadez
Lat	north	4207	geographic 2D	Lisbon
Lat	north	4208	geographic 2D	Aratu
Lat	north	4209	geographic 2D	Arc 1950
Lat	north	4210	geographic 2D	Arc 1960
Lat	north	4211	geographic 2D	Batavia
Lat	north	4212	geographic 2D	Barbados 1938
Lat	north	4213	geographic 2D	Beduaram
Lat	north	4214	geographic 2D	Beijing 1954
Lat	north	4215	geographic 2D	Belge 1950
Lat	north	4216	geographic 2D	Bermuda 1957
Lat	north	4218	geographic 2D	Bogota 1975
Lat	north	4219	geographic 2D	Bukit Rimpah
Lat	north	4220	geographic 2D	Camacupa
Lat	north	4221	geographic 2D	Campo Inchauspe
Lat	north	4222	geographic 2D	Cape
Lat	north	4223	geographic 2D	Carthage
Lat	north	4224	geographic 2D	Chua
Lat	north	4225	geographic 2D	Corrego Alegre 1970-72
Lat	north	4226	geographic 2D	Cote d'Ivoire
Lat	north	4227	geographic 2D	Deir ez Zor
Lat	north	4228	geographic 2D	Douala
Lat	north	4229	geographic 2D	Egypt 1907
Lat	north	4230	geographic 2D	ED50
Lat	north	4231	geographic 2D	ED87
Lat	north	4232	geographic 2D	Fahud
Lat	north	4233	geographic 2D	Gandajika 1970
Lat	north	4234	geographic 2D	Garoua
Lat	north	4235	geographic 2D	Guyane Francaise
Lat	north	4236	geographic 2D	Hu Tzu Shan 1950
Lat	north	4237	geographic 2D	HD72
Lat	north	4238	geographic 2D	ID74
Lat	north	4239	geographic 2D	Indian 1954
Lat	north	4240	geographic 2D	Indian 1975
Lat	north	4241	geographic 2D	Jamaica 1875
Lat	north	4242	geographic 2D	JAD69
Lat	north	4243	geographic 2D	Kalianpur 1880
Lat	north	4244	geographic 2D	Kandawala
Lat	north	4245	geographic 2D	Kertau 1968
Lat	north	4246	geographic 2D	KOC
Lat	north	4247	geographic 2D	La Canoa
Lat	north	4248	geographic 2D	PSAD56
Lat	north	4249	geographic 2D	Lake
Lat	north	4250	geographic 2D	Leigon
Lat	north	4251	geographic 2D	Liberia 1964
Lat	north	4252	geographic 2D	Lome
Lat	north	4253	geographic 2D	Luzon 1911
Lat	north	4254	geographic 2D	Hito XVIII 1963
Lat	north	4255	geographic 2D	Herat North
Lat	north	4256	geographic 2D	Mahe 1971
Lat	north	4257	geographic 2D	Makassar
Lat	north	4258	geographic 2D	ETRS89
Lat	north	4259	geographic 2D	Malongo 1987
Lat	north	4260	geographic 2D	Manoca
Lat	north	4261	geographic 2D	Merchich
Lat	north	4262	geographic 2D	Massawa
Lat	north	4263	geographic 2D	Minna
Lat	north	4264	geographic 2D	Mhast
Lat	north	4265	geographic 2D	Monte Mario
Lat	north	4266	geographic 2D	M'poraloko
Lat	north	4267	geographic 2D	NAD27
Lat	north	4268	geographic 2D	NAD27 Michigan
Lat	north	4269	geographic 2D	NAD83
Lat	north	4270	geographic 2D	Nahrwan 1967
Lat	north	4271	geographic 2D	Naparima 1972
Lat	north	4272	geographic 2D	NZGD49
Lat	north	4273	geographic 2D	NGO 1948
Lat	north	4274	geographic 2D	Datum 73
Lat	north	4275	geographic 2D	NTF
Lat	north	4276	geographic 2D	NSWC 9Z-2
Lat	north	4277	geographic 2D	OSGB 1936
Lat	north	4278	geographic 2D	OSGB70
Lat	north	4279	geographic 2D	OS(SN)80
Lat	north	4280	geographic 2D	Padang
Lat	north	4281	geographic 2D	Palestine 1923
Lat	north	4282	geographic 2D	Pointe Noire
Lat	north	4283	geographic 2D	GDA94
Lat	north	4284	geographic 2D	Pulkovo 1942
Lat	north	4285	geographic 2D	Qatar 1974
Lat	north	4286	geographic 2D	Qatar 1948
Lat	north	4287	geographic 2D	Qornoq
Lat	north	4288	geographic 2D	Loma Quintana
Lat	north	4289	geographic 2D	Amersfoort
Lat	north	4291	geographic 2D	SAD69
Lat	north	4292	geographic 2D	Sapper Hill 1943
Lat	north	4293	geographic 2D	Schwarzeck
Lat	north	4294	geographic 2D	Segora
Lat	north	4295	geographic 2D	Serindung
Lat	north	4296	geographic 2D	Sudan
Lat	north	4297	geographic 2D	Tananarive
Lat	north	4298	geographic 2D	Timbalai 1948
Lat	north	4299	geographic 2D	TM65
Lat	north	4300	geographic 2D	TM75
Lat	north	4301	geographic 2D	Tokyo
Lat	north	4302	geographic 2D	Trinidad 1903
Lat	north	4303	geographic 2D	TC(1948)
Lat	north	4304	geographic 2D	Voirol 1875
Lat	north	4306	geographic 2D	Bern 1938
Lat	north	4307	geographic 2D	Nord Sahara 1959
Lat	north	4308	geographic 2D	RT38
Lat	north	4309	geographic 2D	Yacare
Lat	north	4310	geographic 2D	Yoff
Lat	north	4311	geographic 2D	Zanderij
Lat	north	4312	geographic 2D	MGI
Lat	north	4313	geographic 2D	Belge 1972
Lat	north	4314	geographic 2D	DHDN
Lat	north	4315	geographic 2D	Conakry 1905
Lat	north	4316	geographic 2D	Dealul Piscului 1930
Lat	north	4317	geographic 2D	Dealul Piscului 1970
Lat	north	4318	geographic 2D	NGN
Lat	north	4319	geographic 2D	KUDAMS
Lat	north	4322	geographic 2D	WGS 72
Lat	north	4324	geographic 2D	WGS 72BE
Lat	north	4326	geographic 2D	WGS 84
Lat	north	4327	geographic 3D	WGS 84 (geographic 3D)
Lat	north	4329	geographic 3D	WGS 84 (3D)
Lat	north	4339	geographic 3D	Australian Antarctic (3D)
Lat	north	4341	geographic 3D	EST97 (3D)
Lat	north	4343	geographic 3D	CHTRF95 (3D)
Lat	north	4345	geographic 3D	ETRS89 (3D)
Lat	north	4347	geographic 3D	GDA94 (3D)
Lat	north	4349	geographic 3D	Hartebeesthoek94 (3D)
Lat	north	4351	geographic 3D	IRENET95 (3D)
Lat	north	4353	geographic 3D	JGD2000 (3D)
Lat	north	4355	geographic 3D	LKS94 (ETRS89) (3D)
Lat	north	4357	geographic 3D	Moznet (3D)
Lat	north	4359	geographic 3D	NAD83(CSRS) (3D)
Lat	north	4361	geographic 3D	NAD83(HARN) (3D)
Lat	north	4363	geographic 3D	NZGD2000 (3D)
Lat	north	4365	geographic 3D	POSGAR 98 (3D)
Lat	north	4367	geographic 3D	REGVEN (3D)
Lat	north	4369	geographic 3D	RGF93 (3D)
Lat	north	4371	geographic 3D	RGFG95 (3D)
Lat	north	4373	geographic 3D	RGR92 (3D)
Lat	north	4375	geographic 3D	SIRGAS (3D)
Lat	north	4377	geographic 3D	SWEREF99 (3D)
Lat	north	4379	geographic 3D	Yemen NGN96 (3D)
Lat	north	4381	geographic 3D	RGNC 1991 (3D)
Lat	north	4383	geographic 3D	RRAF 1991 (3D)
Lat	north	4386	geographic 3D	ISN93 (3D)
Lat	north	4388	geographic 3D	LKS92 (3D)
Lat	north	4463	geographic 2D	RGSPM06
Lat	north	4466	geographic 3D	RGSPM06
Lat	north	4469	geographic 3D	RGM04
Lat	north	4470	geographic 2D	RGM04
Lat	north	4472	geographic 3D	Cadastre 1997
Lat	north	4475	geographic 2D	Cadastre 1997
Lat	north	4480	geographic 3D	China Geodetic Coordinate System 2000
Lat	north	4482	geographic 3D	Mexico ITRF92
Lat	north	4483	geographic 2D	Mexico ITRF92
Lat	north	4490	geographic 2D	China Geodetic Coordinate System 2000
Lat	north	4555	geographic 2D	New Beijing
Lat	north	4557	geographic 3D	RRAF 1991
Lat	north	4558	geographic 2D	RRAF 1991
Lat	north	4600	geographic 2D	Anguilla 1957
Lat	north	4601	geographic 2D	Antigua 1943
Lat	north	4602	geographic 2D	Dominica 1945
Lat	north	4603	geographic 2D	Grenada 1953
Lat	north	4604	geographic 2D	Montserrat 1958
Lat	north	4605	geographic 2D	St. Kitts 1955
Lat	north	4606	geographic 2D	St. Lucia 1955
Lat	north	4607	geographic 2D	St. Vincent 1945
Lat	north	4608	geographic 2D	NAD27(76)
Lat	north	4609	geographic 2D	NAD27(CGQ77)
Lat	north	4610	geographic 2D	Xian 1980
Lat	north	4611	geographic 2D	Hong Kong 1980
Lat	north	4612	geographic 2D	JGD2000
Lat	north	4613	geographic 2D	Segara
Lat	north	4614	geographic 2D	QND95
Lat	north	4615	geographic 2D	Porto Santo
Lat	north	4616	geographic 2D	Selvagem Grande
Lat	north	4617	geographic 2D	NAD83(CSRS)
Lat	north	4618	geographic 2D	SAD69
Lat	north	4619	geographic 2D	SWEREF99
Lat	north	4620	geographic 2D	Point 58
Lat	north	4621	geographic 2D	Fort Marigot
Lat	north	4622	geographic 2D	Guadeloupe 1948
Lat	north	4623	geographic 2D	CSG67
Lat	north	4624	geographic 2D	RGFG95
Lat	north	4625	geographic 2D	Martinique 1938
Lat	north	4626	geographic 2D	Reunion 1947
Lat	north	4627	geographic 2D	RGR92
Lat	north	4628	geographic 2D	Tahiti 52
Lat	north	4629	geographic 2D	Tahaa 54
Lat	north	4630	geographic 2D	IGN72 Nuku Hiva
Lat	north	4631	geographic 2D	K0 1949
Lat	north	4632	geographic 2D	Combani 1950
Lat	north	4633	geographic 2D	IGN56 Lifou
Lat	north	4634	geographic 2D	IGN72 Grand Terre
Lat	north	4635	geographic 2D	ST87 Ouvea
Lat	north	4636	geographic 2D	Petrels 1972
Lat	north	4637	geographic 2D	Perroud 1950
Lat	north	4638	geographic 2D	Saint Pierre et Miquelon 1950
Lat	north	4639	geographic 2D	MOP78
Lat	north	4640	geographic 2D	RRAF 1991
Lat	north	4641	geographic 2D	IGN53 Mare
Lat	north	4642	geographic 2D	ST84 Ile des Pins
Lat	north	4643	geographic 2D	ST71 Belep
Lat	north	4644	geographic 2D	NEA74 Noumea
Lat	north	4645	geographic 2D	RGNC 1991
Lat	north	4646	geographic 2D	Grand Comoros
Lat	north	4657	geographic 2D	Reykjavik 1900
Lat	north	4658	geographic 2D	Hjorsey 1955
Lat	north	4659	geographic 2D	ISN93
Lat	north	4660	geographic 2D	Helle 1954
Lat	north	4661	geographic 2D	LKS92
Lat	north	4662	geographic 2D	IGN72 Grande Terre
Lat	north	4663	geographic 2D	Porto Santo 1995
Lat	north	4664	geographic 2D	Azores Oriental 1995
Lat	north	4665	geographic 2D	Azores Central 1995
Lat	north	4666	geographic 2D	Lisbon 1890
Lat	north	4667	geographic 2D	IKBD-92
Lat	north	4668	geographic 2D	ED79
Lat	north	4669	geographic 2D	LKS94
Lat	north	4670	geographic 2D	IGM95
Lat	north	4671	geographic 2D	Voirol 1879
Lat	north	4672	geographic 2D	Chatham Islands 1971
Lat	north	4673	geographic 2D	Chatham Islands 1979
Lat	north	4674	geographic 2D	SIRGAS 2000
Lat	north	4675	geographic 2D	Guam 1963
Lat	north	4676	geographic 2D	Vientiane 1982
Lat	north	4677	geographic 2D	Lao 1993
Lat	north	4678	geographic 2D	Lao 1997
Lat	north	4679	geographic 2D	Jouik 1961
Lat	north	4680	geographic 2D	Nouakchott 1965
Lat	north	4681	geographic 2D	Mauritania 1999
Lat	north	4682	geographic 2D	Gulshan 303
Lat	north	4683	geographic 2D	PRS92
Lat	north	4684	geographic 2D	Gan 1970
Lat	north	4685	geographic 2D	Gandajika
Lat	north	4686	geographic 2D	MAGNA-SIRGAS
Lat	north	4687	geographic 2D	RGPF
Lat	north	4688	geographic 2D	Fatu Iva 72
Lat	north	4689	geographic 2D	IGN63 Hiva Oa
Lat	north	4690	geographic 2D	Tahiti 79
Lat	north	4691	geographic 2D	Moorea 87
Lat	north	4692	geographic 2D	Maupiti 83
Lat	north	4693	geographic 2D	Nakhl-e Ghanem
Lat	north	4694	geographic 2D	POSGAR 94
Lat	north	4695	geographic 2D	Katanga 1955
Lat	north	4696	geographic 2D	Kasai 1953
Lat	north	4697	geographic 2D	IGC 1962 6th Parallel South
Lat	north	4698	geographic 2D	IGN 1962 Kerguelen
Lat	north	4699	geographic 2D	Le Pouce 1934
Lat	north	4700	geographic 2D	IGN Astro 1960
Lat	north	4701	geographic 2D	IGCB 1955
Lat	north	4702	geographic 2D	Mauritania 1999
Lat	north	4703	geographic 2D	Mhast 1951
Lat	north	4704	geographic 2D	Mhast (onshore)
Lat	north	4705	geographic 2D	Mhast (offshore)
Lat	north	4706	geographic 2D	Egypt Gulf of Suez S-650 TL
Lat	north	4707	geographic 2D	Tern Island 1961
Lat	north	4708	geographic 2D	Cocos Islands 1965
Lat	north	4709	geographic 2D	Iwo Jima 1945
Lat	north	4710	geographic 2D	St. Helena 1971
Lat	north	4711	geographic 2D	Marcus Island 1952
Lat	north	4712	geographic 2D	Ascension Island 1958
Lat	north	4713	geographic 2D	Ayabelle Lighthouse
Lat	north	4714	geographic 2D	Bellevue
Lat	north	4715	geographic 2D	Camp Area Astro
Lat	north	4716	geographic 2D	Phoenix Islands 1966
Lat	north	4717	geographic 2D	Cape Canaveral
Lat	north	4718	geographic 2D	Solomon 1968
Lat	north	4719	geographic 2D	Easter Island 1967
Lat	north	4720	geographic 2D	Fiji 1986
Lat	north	4721	geographic 2D	Fiji 1956
Lat	north	4722	geographic 2D	South Georgia 1968
Lat	north	4723	geographic 2D	GCGD59
Lat	north	4724	geographic 2D	Diego Garcia 1969
Lat	north	4725	geographic 2D	Johnston Island 1961
Lat	north	4726	geographic 2D	SIGD61
Lat	north	4727	geographic 2D	Midway 1961
Lat	north	4728	geographic 2D	Pico de las Nieves 1984
Lat	north	4729	geographic 2D	Pitcairn 1967
Lat	north	4730	geographic 2D	Santo 1965
Lat	north	4731	geographic 2D	Viti Levu 1916
Lat	north	4732	geographic 2D	Marshall Islands 1960
Lat	north	4733	geographic 2D	Wake Island 1952
Lat	north	4734	geographic 2D	Tristan 1968
Lat	north	4735	geographic 2D	Kusaie 1951
Lat	north	4736	geographic 2D	Deception Island
Lat	north	4737	geographic 2D	Korea 2000
Lat	north	4738	geographic 2D	Hong Kong 1963
Lat	north	4739	geographic 2D	Hong Kong 1963(67)
Lat	north	4740	geographic 2D	PZ-90
Lat	north	4741	geographic 2D	FD54
Lat	north	4742	geographic 2D	GDM2000
Lat	north	4743	geographic 2D	Karbala 1979
Lat	north	4744	geographic 2D	Nahrwan 1934
Lat	north	4745	geographic 2D	RD/83
Lat	north	4746	geographic 2D	PD/83
Lat	north	4747	geographic 2D	GR96
Lat	north	4748	geographic 2D	Vanua Levu 1915
Lat	north	4749	geographic 2D	RGNC91-93
Lat	north	4750	geographic 2D	ST87 Ouvea
Lat	north	4751	geographic 2D	Kertau (RSO)
Lat	north	4752	geographic 2D	Viti Levu 1912
Lat	north	4753	geographic 2D	fk89
Lat	north	4754	geographic 2D	LGD2006
Lat	north	4755	geographic 2D	DGN95
Lat	north	4756	geographic 2D	VN-2000
Lat	north	4757	geographic 2D	SVY21
Lat	north	4758	geographic 2D	JAD2001
Lat	north	4759	geographic 2D	NAD83(NSRS2007)
Lat	north	4760	geographic 2D	WGS 66
Lat	north	4761	geographic 2D	HTRS96
Lat	north	4762	geographic 2D	BDA2000
Lat	north	4763	geographic 2D	Pitcairn 2006
Lat	north	4764	geographic 2D	RSRGD2000
Lat	north	4765	geographic 2D	Slovenia 1996
Lat	north	4801	geographic 2D	Bern 1898 (Bern)
Lat	north	4802	geographic 2D	Bogota 1975 (Bogota)
Lat	north	4803	geographic 2D	Lisbon (Lisbon)
Lat	north	4804	geographic 2D	Makassar (Jakarta)
Lat	north	4805	geographic 2D	MGI (Ferro)
Lat	north	4806	geographic 2D	Monte Mario (Rome)
Lat	north	4807	geographic 2D	NTF (Paris)
Lat	north	4808	geographic 2D	Padang (Jakarta)
Lat	north	4809	geographic 2D	Belge 1950 (Brussels)
Lat	north	4810	geographic 2D	Tananarive (Paris)
Lat	north	4811	geographic 2D	Voirol 1875 (Paris)
Lat	north	4813	geographic 2D	Batavia (Jakarta)
Lat	north	4814	geographic 2D	RT38 (Stockholm)
Lat	north	4815	geographic 2D	Greek (Athens)
Lat	north	4816	geographic 2D	Carthage (Paris)
Lat	north	4817	geographic 2D	NGO 1948 (Oslo)
Lat	north	4818	geographic 2D	S-JTSK (Ferro)
Lat	north	4819	geographic 2D	Nord Sahara 1959 (Paris)
Lat	north	4820	geographic 2D	Segara (Jakarta)
Lat	north	4821	geographic 2D	Voirol 1879 (Paris)
Lat	north	4823	geographic 2D	Sao Tome
Lat	north	4824	geographic 2D	Principe
N	north	4839	projected	ETRS89 / LCC Germany (N-E)
N	north	4855	projected	ETRS89 / NTM zone 5
N	north	4856	projected	ETRS89 / NTM zone 6
N	north	4857	projected	ETRS89 / NTM zone 7
N	north	4858	projected	ETRS89 / NTM zone 8
N	north	4859	projected	ETRS89 / NTM zone 9
N	north	4860	projected	ETRS89 / NTM zone 10
N	north	4861	projected	ETRS89 / NTM zone 11
N	north	4862	projected	ETRS89 / NTM zone 12
N	north	4863	projected	ETRS89 / NTM zone 13
N	north	4864	projected	ETRS89 / NTM zone 14
N	north	4865	projected	ETRS89 / NTM zone 15
N	north	4866	projected	ETRS89 / NTM zone 16
N	north	4867	projected	ETRS89 / NTM zone 17
N	north	4868	projected	ETRS89 / NTM zone 18
N	north	4869	projected	ETRS89 / NTM zone 19
N	north	4870	projected	ETRS89 / NTM zone 20
N	north	4871	projected	ETRS89 / NTM zone 21
N	north	4872	projected	ETRS89 / NTM zone 22
N	north	4873	projected	ETRS89 / NTM zone 23
N	north	4874	projected	ETRS89 / NTM zone 24
N	north	4875	projected	ETRS89 / NTM zone 25
N	north	4876	projected	ETRS89 / NTM zone 26
N	north	4877	projected	ETRS89 / NTM zone 27
N	north	4878	projected	ETRS89 / NTM zone 28
N	north	4879	projected	ETRS89 / NTM zone 29
N	north	4880	projected	ETRS89 / NTM zone 30
Lat	north	4883	geographic 3D	Slovenia 1996
Lat	north	4885	geographic 3D	RSRGD2000
Lat	north	4887	geographic 3D	BDA2000
Lat	north	4889	geographic 3D	HTRS96
Lat	north	4891	geographic 3D	WGS 66
Lat	north	4893	geographic 3D	NAD83(NSRS2007)
Lat	north	4895	geographic 3D	JAD2001
Lat	north	4898	geographic 3D	DGN95
Lat	north	4900	geographic 3D	LGD2006
Lat	north	4901	geographic 2D	ATF (Paris)
Lat	north	4902	geographic 2D	NDG (Paris)
Lat	north	4903	geographic 2D	Madrid 1870 (Madrid)
Lat	north	4904	geographic 2D	Lisbon 1890 (Lisbon)
Lat	north	4907	geographic 3D	RGNC91-93
Lat	north	4909	geographic 3D	GR96
Lat	north	4921	geographic 3D	GDM2000
Lat	north	4923	geographic 3D	PZ-90
Lat	north	4925	geographic 3D	Mauritania 1999
Lat	north	4927	geographic 3D	Korea 2000
Lat	north	4929	geographic 3D	POSGAR 94
Lat	north	4931	geographic 3D	Australian Antarctic
Lat	north	4933	geographic 3D	CHTRF95
Lat	north	4935	geographic 3D	EST97
Lat	north	4937	geographic 3D	ETRS89
Lat	north	4939	geographic 3D	GDA94
Lat	north	4941	geographic 3D	Hartebeesthoek94
Lat	north	4943	geographic 3D	IRENET95
Lat	north	4945	geographic 3D	ISN93
Lat	north	4947	geographic 3D	JGD2000
Lat	north	4949	geographic 3D	LKS92
Lat	north	4951	geographic 3D	LKS94
Lat	north	4953	geographic 3D	Moznet
Lat	north	4955	geographic 3D	NAD83(CSRS)
Lat	north	4957	geographic 3D	NAD83(HARN)
Lat	north	4959	geographic 3D	NZGD2000
Lat	north	4961	geographic 3D	POSGAR 98
Lat	north	4963	geographic 3D	REGVEN
Lat	north	4965	geographic 3D	RGF93
Lat	north	4967	geographic 3D	RGFG95
Lat	north	4969	geographic 3D	RGNC 1991
Lat	north	4971	geographic 3D	RGR92
Lat	north	4973	geographic 3D	RRAF 1991
Lat	north	4975	geographic 3D	SIRGAS 1995
Lat	north	4977	geographic 3D	SWEREF99
Lat	north	4979	geographic 3D	WGS 84
Lat	north	4981	geographic 3D	Yemen NGN96
Lat	north	4983	geographic 3D	IGM95
Lat	north	4985	geographic 3D	WGS 72
Lat	north	4987	geographic 3D	WGS 72BE
Lat	north	4989	geographic 3D	SIRGAS 2000
Lat	north	4991	geographic 3D	Lao 1993
Lat	north	4993	geographic 3D	Lao 1997
Lat	north	4995	geographic 3D	PRS92
Lat	north	4997	geographic 3D	MAGNA-SIRGAS
Lat	north	4999	geographic 3D	RGPF
Lat	north	5012	geographic 3D	PTRA08
Lat	north	5013	geographic 2D	PTRA08
N	north	5048	projected	ETRS89 / TM35FIN(N,E)
N	north	5105	projected	ETRS89 / NTM zone 5
N	north	5106	projected	ETRS89 / NTM zone 6
N	north	5107	projected	ETRS89 / NTM zone 7
N	north	5108	projected	ETRS89 / NTM zone 8
N	north	5109	projected	ETRS89 / NTM zone 9
N	north	5110	projected	ETRS89 / NTM zone 10
N	north	5111	projected	ETRS89 / NTM zone 11
N	north	5112	projected	ETRS89 / NTM zone 12
N	north	5113	projected	ETRS89 / NTM zone 13
N	north	5114	projected	ETRS89 / NTM zone 14
N	north	5115	projected	ETRS89 / NTM zone 15
N	north	5116	projected	ETRS89 / NTM zone 16
N	north	5117	projected	ETRS89 / NTM zone 17
N	north	5118	projected	ETRS89 / NTM zone 18
N	north	5119	projected	ETRS89 / NTM zone 19
N	north	5120	projected	ETRS89 / NTM zone 20
N	north	5121	projected	ETRS89 / NTM zone 21
N	north	5122	projected	ETRS89 / NTM zone 22
N	north	5123	projected	ETRS89 / NTM zone 23
N	north	5124	projected	ETRS89 / NTM zone 24
N	north	5125	projected	ETRS89 / NTM zone 25
N	north	5126	projected	ETRS89 / NTM zone 26
N	north	5127	projected	ETRS89 / NTM zone 27
N	north	5128	projected	ETRS89 / NTM zone 28
N	north	5129	projected	ETRS89 / NTM zone 29
N	north	5130	projected	ETRS89 / NTM zone 30
Lat	north	5132	geographic 2D	Tokyo 1892
Lat	north	5228	geographic 2D	S-JTSK/05
Lat	north	5229	geographic 2D	S-JTSK/05 (Ferro)
Lat	north	5233	geographic 2D	SLD99
Lat	north	5245	geographic 3D	GDBD2009
Lat	north	5246	geographic 2D	GDBD2009
Lat	north	5251	geographic 3D	TUREF
Lat	north	5252	geographic 2D	TUREF
Lat	north	5263	geographic 3D	DRUKREF 03
Lat	north	5264	geographic 2D	DRUKREF 03
Lat	north	5323	geographic 3D	ISN2004
Lat	north	5324	geographic 2D	ISN2004
Lat	north	5340	geographic 2D	POSGAR 2007
Lat	north	5342	geographic 3D	POSGAR 2007
Lat	north	5353	geographic 3D	MARGEN
Lat	north	5354	geographic 2D	MARGEN
Lat	north	5359	geographic 3D	SIRGAS-Chile
Lat	north	5360	geographic 2D	SIRGAS-Chile
Lat	north	5364	geographic 3D	CR05
Lat	north	5365	geographic 2D	CR05
N	north	5367	projected	CR05 / CRTM05
Lat	north	5370	geographic 3D	MACARIO SOLIS
Lat	north	5371	geographic 2D	MACARIO SOLIS
Lat	north	5372	geographic 3D	Peru96
Lat	north	5373	geographic 2D	Peru96
Lat	north	5380	geographic 3D	SIRGAS-ROU98
Lat	north	5381	geographic 2D	SIRGAS-ROU98
Lat	north	5392	geographic 3D	SIRGAS_ES2007.8
Lat	north	5393	geographic 2D	SIRGAS_ES2007.8
Lat	north	5451	geographic 2D	Ocotepeque 1935
Lat	north	5464	geographic 2D	Sibun Gorge 1922
Lat	north	5467	geographic 2D	Panama-Colon 1911
N	north	5479	projected	RSRGD2000 / MSLC2000
N	north	5480	projected	RSRGD2000 / BCLC2000
N	north	5481	projected	RSRGD2000 / PCLC2000
N	north	5482	projected	RSRGD2000 / RSPS2000
Lat	north	5488	geographic 3D	RGAF09
Lat	north	5489	geographic 2D	RGAF09
N	north	5518	projected	CI1971 / Chatham Islands Map Grid
N	north	5519	projected	CI1979 / Chatham Islands Map Grid
Lat	north	5524	geographic 2D	Corrego Alegre 1961
Lat	north	5527	geographic 2D	SAD69(96)
Lat	north	5545	geographic 3D	PNG94
Lat	north	5546	geographic 2D	PNG94
Lat	north	5560	geographic 3D	UCS-2000
Lat	north	5561	geographic 2D	UCS-2000
N	north	5588	projected	NAD27 / New Brunswick Stereographic (NAD27)
Lat	north	5592	geographic 3D	FEH2010
Lat	north	5593	geographic 2D	FEH2010
N	north	5632	projected	PTRA08 / LCC Europe
Y	north	5633	projected	PTRA08 / LAEA Europe
N	north	5634	projected	REGCAN95 / LCC Europe
N	north	5635	projected	REGCAN95 / LAEA Europe
Y	north	5636	projected	TUREF / LAEA Europe
N	north	5637	projected	TUREF / LCC Europe
Y	north	5638	projected	ISN2004 / LAEA Europe
N	north	5639	projected	ISN2004 / LCC Europe
N	north	5651	projected	ETRS89 / UTM zone 31N (N-zE)
N	north	5652	projected	ETRS89 / UTM zone 32N (N-zE)
N	north	5653	projected	ETRS89 / UTM zone 33N (N-zE)
Lat	north	5681	geographic 2D	DB_REF
Lat	north	5830	geographic 3D	DB_REF
Lat	north	5885	geographic 3D	TGD2005
Lat	north	5886	geographic 2D	TGD2005
Lat	north	6134	geographic 3D	CIGD11
Lat	north	6135	geographic 2D	CIGD11
Lat	north	6207	geographic 2D	Nepal 1981
N	north	6244	projected	MAGNA-SIRGAS / Arauca urban grid
N	north	6245	projected	MAGNA-SIRGAS / Armenia urban grid
N	north	6246	projected	MAGNA-SIRGAS / Barranquilla urban grid
N	north	6247	projected	MAGNA-SIRGAS / Bogota urban grid
N	north	6248	projected	MAGNA-SIRGAS / Bucaramanga urban grid
N	north	6249	projected	MAGNA-SIRGAS / Cali urban grid
N	north	6250	projected	MAGNA-SIRGAS / Cartagena urban grid
N	north	6251	projected	MAGNA-SIRGAS / Cucuta urban grid
N	north	6252	projected	MAGNA-SIRGAS / Florencia urban grid
N	north	6253	projected	MAGNA-SIRGAS / Ibague urban grid
N	north	6254	projected	MAGNA-SIRGAS / Inirida urban grid
N	north	6255	projected	MAGNA-SIRGAS / Leticia urban grid
N	north	6256	projected	MAGNA-SIRGAS / Manizales urban grid
N	north	6257	projected	MAGNA-SIRGAS / Medellin urban grid
N	north	6258	projected	MAGNA-SIRGAS / Mitu urban grid
N	north	6259	projected	MAGNA-SIRGAS / Mocoa urban grid
N	north	6260	projected	MAGNA-SIRGAS / Monteria urban grid
N	north	6261	projected	MAGNA-SIRGAS / Neiva urban grid
N	north	6262	projected	MAGNA-SIRGAS / Pasto urban grid
N	north	6263	projected	MAGNA-SIRGAS / Pereira urban grid
N	north	6264	projected	MAGNA-SIRGAS / Popayan urban grid
N	north	6265	projected	MAGNA-SIRGAS / Puerto Carreno urban grid
N	north	6266	projected	MAGNA-SIRGAS / Quibdo urban grid
N	north	6267	projected	MAGNA-SIRGAS / Riohacha urban grid
N	north	6268	projected	MAGNA-SIRGAS / San Andres urban grid
N	north	6269	projected	MAGNA-SIRGAS / San Jose del Guaviare urban grid
N	north	6270	projected	MAGNA-SIRGAS / Santa Marta urban grid
N	north	6271	projected	MAGNA-SIRGAS / Sucre urban grid
N	north	6272	projected	MAGNA-SIRGAS / Tunja urban grid
N	north	6273	projected	MAGNA-SIRGAS / Valledupar urban grid
N	north	6274	projected	MAGNA-SIRGAS / Villavicencio urban grid
N	north	6275	projected	MAGNA-SIRGAS / Yopal urban grid
Lat	north	6318	geographic 2D	NAD83(2011)
Lat	north	6319	geographic 3D	NAD83(2011)
Lat	north	6321	geographic 3D	NAD83(PA11)
Lat	north	6322	geographic 2D	NAD83(PA11)
Lat	north	6324	geographic 3D	NAD83(MA11)
Lat	north	6325	geographic 2D	NAD83(MA11)
N	north	6362	projected	Mexico ITRF92 / LCC
Lat	north	6364	geographic 3D	Mexico ITRF2008
Lat	north	6365	geographic 2D	Mexico ITRF2008
N	north	6372	projected	Mexico ITRF2008 / LCC
N	north	27205	projected	NZGD49 / Mount Eden Circuit
N	north	27206	projected	NZGD49 / Bay of Plenty Circuit
N	north	27207	projected	NZGD49 / Poverty Bay Circuit
N	north	27208	projected	NZGD49 / Hawkes Bay Circuit
N	north	27209	projected	NZGD49 / Taranaki Circuit
N	north	27210	projected	NZGD49 / Tuhirangi Circuit
N	north	27211	projected	NZGD49 / Wanganui Circuit
N	north	27212	projected	NZGD49 / Wairarapa Circuit
N	north	27213	projected	NZGD49 / Wellington Circuit
N	north	27214	projected	NZGD49 / Collingwood Circuit
N	north	27215	projected	NZGD49 / Nelson Circuit
N	north	27216	projected	NZGD49 / Karamea Circuit
N	north	27217	projected	NZGD49 / Buller Circuit
N	north	27218	projected	NZGD49 / Grey Circuit
N	north	27219	projected	NZGD49 / Amuri Circuit
N	north	27220	projected	NZGD49 / Marlborough Circuit
N	north	27221	projected	NZGD49 / Hokitika Circuit
N	north	27222	projected	NZGD49 / Okarito Circuit
N	north	27223	projected	NZGD49 / Jacksons Bay Circuit
N	north	27224	projected	NZGD49 / Mount Pleasant Circuit
N	north	27225	projected	NZGD49 / Gawler Circuit
N	north	27226	projected	NZGD49 / Timaru Circuit
N	north	27227	projected	NZGD49 / Lindis Peak Circuit
N	north	27228	projected	NZGD49 / Mount Nicholas Circuit
N	north	27229	projected	NZGD49 / Mount York Circuit
N	north	27230	projected	NZGD49 / Observation Point Circuit
N	north	27231	projected	NZGD49 / North Taieri Circuit
N	north	27232	projected	NZGD49 / Bluff Circuit
N	South along 180°E	32661	projected	WGS 84 / UPS North (N,E)
N	North along 0°E	32761	projected	WGS 84 / UPS South (N,E)
Lat	north	61206405	geographic 2D	Greek (deg)
Lat	north	61216405	geographic 2D	GGRS87 (deg)
Lat	north	61226405	geographic 2D	ATS77 (deg)
Lat	north	61236405	geographic 2D	KKJ (deg)
Lat	north	61246405	geographic 2D	RT90 (deg)
Lat	north	61266405	geographic 2D	LKS94 (ETRS89) (deg)
Lat	north	61266413	geographic 2D	LKS94 (ETRS89) (3D deg)
Lat	north	61276405	geographic 2D	Tete (deg)
Lat	north	61286405	geographic 2D	Madzansua (deg)
Lat	north	61296405	geographic 2D	Observatario (deg)
Lat	north	61306405	geographic 2D	Moznet (deg)
Lat	north	61306413	geographic 3D	Moznet (3D deg)
Lat	north	61316405	geographic 2D	Indian 1960 (deg)
Lat	north	61326405	geographic 2D	FD58 (deg)
Lat	north	61336405	geographic 2D	EST92 (deg)
Lat	north	61346405	geographic 2D	PDO Survey Datum 1993 (deg)
Lat	north	61356405	geographic 2D	Old Hawaiian (deg)
Lat	north	61366405	geographic 2D	St. Lawrence Island (deg)
Lat	north	61376405	geographic 2D	St. Paul Island (deg)
Lat	north	61386405	geographic 2D	St. George Island (deg)
Lat	north	61396405	geographic 2D	Puerto Rico (deg)
Lat	north	61406405	geographic 2D	NAD83(CSRS) (deg)
Lat	north	61406413	geographic 3D	NAD83(CSRS) (3D deg)
Lat	north	61416405	geographic 2D	Israel (deg)
Lat	north	61426405	geographic 2D	Locodjo 1965 (deg)
Lat	north	61436405	geographic 2D	Abidjan 1987 (deg)
Lat	north	61446405	geographic 2D	Kalianpur 1937 (deg)
Lat	north	61456405	geographic 2D	Kalianpur 1962 (deg)
Lat	north	61466405	geographic 2D	Kalianpur 1975 (deg)
Lat	north	61476405	geographic 2D	Hanoi 1972 (deg)
Lat	north	61486405	geographic 2D	Hartebeesthoek94 (deg)
Lat	north	61486413	geographic 3D	Hartebeesthoek94 (3D deg)
Lat	north	61496405	geographic 2D	CH1903 (deg)
Lat	north	61506405	geographic 2D	CH1903+ (deg)
Lat	north	61516405	geographic 2D	CHTRF95 (deg)
Lat	north	61516413	geographic 3D	CHTRF95 (3D deg)
Lat	north	61526405	geographic 2D	NAD83(HARN) (deg)
Lat	north	61526413	geographic 3D	NAD83(HARN) (3D deg)
Lat	north	61536405	geographic 2D	Rassadiran (deg)
Lat	north	61546405	geographic 2D	ED50(ED77) (deg)
Lat	north	61556405	geographic 2D	Dabola 1981 (deg)
Lat	north	61566405	geographic 2D	S-JTSK (deg)
Lat	north	61576405	geographic 2D	Mount Dillon (deg)
Lat	north	61586405	geographic 2D	Naparima 1955 (deg)
Lat	north	61596405	geographic 2D	ELD79 (deg)
Lat	north	61606405	geographic 2D	Chos Malal 1914 (deg)
Lat	north	61616405	geographic 2D	Pampa del Castillo (deg)
Lat	north	61626405	geographic 2D	Korean 1985 (deg)
Lat	north	61636405	geographic 2D	Yemen NGN96 (deg)
Lat	north	61636413	geographic 3D	Yemen NGN96 (3D deg)
Lat	north	61646405	geographic 2D	South Yemen (deg)
Lat	north	61656405	geographic 2D	Bissau (deg)
Lat	north	61666405	geographic 2D	Korean 1995 (deg)
Lat	north	61676405	geographic 2D	NZGD2000 (deg)
Lat	north	61676413	geographic 3D	NZGD2000 (3D deg)
Lat	north	61686405	geographic 2D	Accra (deg)
Lat	north	61696405	geographic 2D	American Samoa 1962 (deg)
Lat	north	61706405	geographic 2D	SIRGAS (deg)
Lat	north	61706413	geographic 3D	SIRGAS (3D deg)
Lat	north	61716405	geographic 2D	RGF93 (deg)
Lat	north	61716413	geographic 3D	RGF93 (3D deg)
Lat	north	61736405	geographic 2D	IRENET95 (deg)
Lat	north	61736413	geographic 3D	IRENET95 (3D deg)
Lat	north	61746405	geographic 2D	Sierra Leone 1924 (deg)
Lat	north	61756405	geographic 2D	Sierra Leone 1968 (deg)
Lat	north	61766405	geographic 2D	Australian Antarctic (deg)
Lat	north	61766413	geographic 3D	Australian Antarctic (3D deg)
Lat	north	61786405	geographic 2D	Pulkovo 1942(83) (deg)
Lat	north	61796405	geographic 2D	Pulkovo 1942(58) (deg)
Lat	north	61806405	geographic 2D	EST97 (deg)
Lat	north	61806413	geographic 3D	EST97 (3D deg)
Lat	north	61816405	geographic 2D	Luxembourg 1930 (deg)
Lat	north	61826405	geographic 2D	Azores Occidental 1939 (deg)
Lat	north	61836405	geographic 2D	Azores Central 1948 (deg)
Lat	north	61846405	geographic 2D	Azores Oriental 1940 (deg)
Lat	north	61886405	geographic 2D	OSNI 1952 (deg)
Lat	north	61896405	geographic 2D	REGVEN (deg)
Lat	north	61896413	geographic 3D	REGVEN (3D deg)
Lat	north	61906405	geographic 2D	POSGAR 98 (deg)
Lat	north	61906413	geographic 3D	POSGAR 98 (3D deg)
Lat	north	61916405	geographic 2D	Albanian 1987 (deg)
Lat	north	61926405	geographic 2D	Douala 1948 (deg)
Lat	north	61936405	geographic 2D	Manoca 1962 (deg)
Lat	north	61946405	geographic 2D	Qornoq 1927 (deg)
Lat	north	61956405	geographic 2D	Scoresbysund 1952 (deg)
Lat	north	61966405	geographic 2D	Ammassalik 1958 (deg)
Lat	north	61976405	geographic 2D	Garoua (deg)
Lat	north	61986405	geographic 2D	Kousseri (deg)
Lat	north	61996405	geographic 2D	Egypt 1930 (deg)
Lat	north	62006405	geographic 2D	Pulkovo 1995 (deg)
Lat	north	62016405	geographic 2D	Adindan (deg)
Lat	north	62026405	geographic 2D	AGD66 (deg)
Lat	north	62036405	geographic 2D	AGD84 (deg)
Lat	north	62046405	geographic 2D	Ain el Abd (deg)
Lat	north	62056405	geographic 2D	Afgooye (deg)
Lat	north	62066405	geographic 2D	Agadez (deg)
Lat	north	62076405	geographic 2D	Lisbon (deg)
Lat	north	62086405	geographic 2D	Aratu (deg)
Lat	north	62096405	geographic 2D	Arc 1950 (deg)
Lat	north	62106405	geographic 2D	Arc 1960 (deg)
Lat	north	62116405	geographic 2D	Batavia (deg)
Lat	north	62126405	geographic 2D	Barbados 1938 (deg)
Lat	north	62136405	geographic 2D	Beduaram (deg)
Lat	north	62146405	geographic 2D	Beijing 1954 (deg)
Lat	north	62156405	geographic 2D	Belge 1950 (deg)
Lat	north	62166405	geographic 2D	Bermuda 1957 (deg)
Lat	north	62186405	geographic 2D	Bogota 1975 (deg)
Lat	north	62196405	geographic 2D	Bukit Rimpah (deg)
Lat	north	62206405	geographic 2D	Camacupa (deg)
Lat	north	62216405	geographic 2D	Campo Inchauspe (deg)
Lat	north	62226405	geographic 2D	Cape (deg)
Lat	north	62236405	geographic 2D	Carthage (deg)
Lat	north	62246405	geographic 2D	Chua (deg)
Lat	north	62256405	geographic 2D	Corrego Alegre (deg)
Lat	north	62276405	geographic 2D	Deir ez Zor (deg)
Lat	north	62296405	geographic 2D	Egypt 1907 (deg)
Lat	north	62306405	geographic 2D	ED50 (deg)
Lat	north	62316405	geographic 2D	ED87 (deg)
Lat	north	62326405	geographic 2D	Fahud (deg)
Lat	north	62336405	geographic 2D	Gandajika 1970 (deg)
Lat	north	62366405	geographic 2D	Hu Tzu Shan (deg)
Lat	north	62376405	geographic 2D	HD72 (deg)
Lat	north	62386405	geographic 2D	ID74 (deg)
Lat	north	62396405	geographic 2D	Indian 1954 (deg)
Lat	north	62406405	geographic 2D	Indian 1975 (deg)
Lat	north	62416405	geographic 2D	Jamaica 1875 (deg)
Lat	north	62426405	geographic 2D	JAD69 (deg)
Lat	north	62436405	geographic 2D	Kalianpur 1880 (deg)
Lat	north	62446405	geographic 2D	Kandawala (deg)
Lat	north	62456405	geographic 2D	Kertau (deg)
Lat	north	62466405	geographic 2D	KOC (deg)
Lat	north	62476405	geographic 2D	La Canoa (deg)
Lat	north	62486405	geographic 2D	PSAD56 (deg)
Lat	north	62496405	geographic 2D	Lake (deg)
Lat	north	62506405	geographic 2D	Leigon (deg)
Lat	north	62516405	geographic 2D	Liberia 1964 (deg)
Lat	north	62526405	geographic 2D	Lome (deg)
Lat	north	62536405	geographic 2D	Luzon 1911 (deg)
Lat	north	62546405	geographic 2D	Hito XVIII 1963 (deg)
Lat	north	62556405	geographic 2D	Herat North (deg)
Lat	north	62566405	geographic 2D	Mahe 1971 (deg)
Lat	north	62576405	geographic 2D	Makassar (deg)
Lat	north	62586405	geographic 2D	ETRS89 (deg)
Lat	north	62586413	geographic 3D	ETRS89 (3D deg)
Lat	north	62596405	geographic 2D	Malongo 1987 (deg)
Lat	north	62616405	geographic 2D	Merchich (deg)
Lat	north	62626405	geographic 2D	Massawa (deg)
Lat	north	62636405	geographic 2D	Minna (deg)
Lat	north	62646405	geographic 2D	Mhast (deg)
Lat	north	62656405	geographic 2D	Monte Mario (deg)
Lat	north	62666405	geographic 2D	M'poraloko (deg)
Lat	north	62676405	geographic 2D	NAD27 (deg)
Lat	north	62686405	geographic 2D	NAD27 Michigan (deg)
Lat	north	62696405	geographic 2D	NAD83 (deg)
Lat	north	62706405	geographic 2D	Nahrwan 1967 (deg)
Lat	north	62716405	geographic 2D	Naparima 1972 (deg)
Lat	north	62726405	geographic 2D	NZGD49 (deg)
Lat	north	62736405	geographic 2D	NGO 1948 (deg)
Lat	north	62746405	geographic 2D	Datum 73 (deg)
Lat	north	62756405	geographic 2D	NTF (deg)
Lat	north	62766405	geographic 2D	NSWC 9Z-2 (deg)
Lat	north	62776405	geographic 2D	OSGB 1936 (deg)
Lat	north	62786405	geographic 2D	OSGB70 (deg)
Lat	north	62796405	geographic 2D	OS(SN)80 (deg)
Lat	north	62806405	geographic 2D	Padang (deg)
Lat	north	62816405	geographic 2D	Palestine 1923 (deg)
Lat	north	62826405	geographic 2D	Pointe Noire (deg)
Lat	north	62836405	geographic 2D	GDA94 (deg)
Lat	north	62836413	geographic 3D	GDA94 (3D deg)
Lat	north	62846405	geographic 2D	Pulkovo 1942 (deg)
Lat	north	62856405	geographic 2D	Qatar 1974 (deg)
Lat	north	62866405	geographic 2D	Qatar 1948 (deg)
Lat	north	62886405	geographic 2D	Loma Quintana (deg)
Lat	north	62896405	geographic 2D	Amersfoort (deg)
Lat	north	62926405	geographic 2D	Sapper Hill 1943 (deg)
Lat	north	62936405	geographic 2D	Schwarzeck (deg)
Lat	north	62956405	geographic 2D	Serindung (deg)
Lat	north	62976405	geographic 2D	Tananarive (deg)
Lat	north	62986405	geographic 2D	Timbalai 1948 (deg)
Lat	north	62996405	geographic 2D	TM65 (deg)
Lat	north	63006405	geographic 2D	TM75 (deg)
Lat	north	63016405	geographic 2D	Tokyo (deg)
Lat	north	63026405	geographic 2D	Trinidad 1903 (deg)
Lat	north	63036405	geographic 2D	TC(1948) (deg)
Lat	north	63046405	geographic 2D	Voirol 1875 (deg)
Lat	north	63066405	geographic 2D	Bern 1938 (deg)
Lat	north	63076405	geographic 2D	Nord Sahara 1959 (deg)
Lat	north	63086405	geographic 2D	RT38 (deg)
Lat	north	63096405	geographic 2D	Yacare (deg)
Lat	north	63106405	geographic 2D	Yoff (deg)
Lat	north	63116405	geographic 2D	Zanderij (deg)
Lat	north	63126405	geographic 2D	MGI (deg)
Lat	north	63136405	geographic 2D	Belge 1972 (deg)
Lat	north	63146405	geographic 2D	DHDN (deg)
Lat	north	63156405	geographic 2D	Conakry 1905 (deg)
Lat	north	63166405	geographic 2D	Dealul Piscului 1933 (deg)
Lat	north	63176405	geographic 2D	Dealul Piscului 1970 (deg)
Lat	north	63186405	geographic 2D	NGN (deg)
Lat	north	63196405	geographic 2D	KUDAMS (deg)
Lat	north	63226405	geographic 2D	WGS 72 (deg)
Lat	north	63246405	geographic 2D	WGS 72BE (deg)
Lat	north	63266405	geographic 2D	WGS 84 (deg)
Lat	north	63266406	geographic 2D	WGS 84 (degH)
Lat	north	63266407	geographic 2D	WGS 84 (Hdeg)
Lat	north	63266408	geographic 2D	WGS 84 (DM)
Lat	north	63266409	geographic 2D	WGS 84 (DMH)
Lat	north	63266410	geographic 2D	WGS 84 (HDM)
Lat	north	63266411	geographic 2D	WGS 84 (DMS)
Lat	north	63266412	geographic 2D	WGS 84 (HDMS)
Lat	north	63266413	geographic 3D	WGS 84 (3D deg)
Lat	north	63266414	geographic 3D	WGS 84 (3D degH)
Lat	north	63266415	geographic 3D	WGS 84 (3D Hdeg)
Lat	north	63266416	geographic 3D	WGS 84 (3D DM)
Lat	north	63266417	geographic 3D	WGS 84 (3D DMH)
Lat	north	63266418	geographic 3D	WGS 84 (3D HDM)
Lat	north	63266419	geographic 3D	WGS 84 (3D DMS)
Lat	north	63266420	geographic 3D	WGS 84 (3D HDMS)
Lat	north	66006405	geographic 2D	Anguilla 1957 (deg)
Lat	north	66016405	geographic 2D	Antigua 1943 (deg)
Lat	north	66026405	geographic 2D	Dominica 1945 (deg)
Lat	north	66036405	geographic 2D	Grenada 1953 (deg)
Lat	north	66046405	geographic 2D	Montserrat 1958 (deg)
Lat	north	66056405	geographic 2D	St. Kitts 1955 (deg)
Lat	north	66066405	geographic 2D	St. Lucia 1955 (deg)
Lat	north	66076405	geographic 2D	St. Vincent 1945 (deg)
Lat	north	66086405	geographic 2D	NAD27(76) (deg)
Lat	north	66096405	geographic 2D	NAD27(CGQ77) (deg)
Lat	north	66106405	geographic 2D	Xian 1980 (deg)
Lat	north	66116405	geographic 2D	Hong Kong 1980 (deg)
Lat	north	66126405	geographic 2D	JGD2000 (deg)
Lat	north	66126413	geographic 3D	JGD2000 (3D deg)
Lat	north	66136405	geographic 2D	Segara (deg)
Lat	north	66146405	geographic 2D	QND95 (deg)
Lat	north	66156405	geographic 2D	Porto Santo (deg)
Lat	north	66166405	geographic 2D	Selvagem Grande (deg)
Lat	north	66186405	geographic 2D	SAD69 (deg)
Lat	north	66196405	geographic 2D	SWEREF99 (deg)
Lat	north	66196413	geographic 3D	SWEREF99 (3D deg)
Lat	north	66206405	geographic 2D	Point 58 (deg)
Lat	north	66216405	geographic 2D	Fort Marigot (deg)
Lat	north	66226405	geographic 2D	Sainte Anne (deg)
Lat	north	66236405	geographic 2D	CSG67 (deg)
Lat	north	66246405	geographic 2D	RGFG95 (deg)
Lat	north	66246413	geographic 3D	RGFG95 (3D deg)
Lat	north	66256405	geographic 2D	Fort Desaix (deg)
Lat	north	66266405	geographic 2D	Piton des Neiges (deg)
Lat	north	66276405	geographic 2D	RGR92 (deg)
Lat	north	66276413	geographic 3D	RGR92 (3D deg)
Lat	north	66286405	geographic 2D	Tahiti (deg)
Lat	north	66296405	geographic 2D	Tahaa (deg)
Lat	north	66306405	geographic 2D	IGN72 Nuku Hiva (deg)
Lat	north	66316405	geographic 2D	K0 1949 (deg)
Lat	north	66326405	geographic 2D	Combani 1950 (deg)
Lat	north	66336405	geographic 2D	IGN56 Lifou (deg)
Lat	north	66346405	geographic 2D	IGN72 Grande Terre (deg)
Lat	north	66356405	geographic 2D	ST87 Ouvea (deg)
Lat	north	66366405	geographic 2D	Petrels 1972 (deg)
Lat	north	66376405	geographic 2D	Perroud 1950 (deg)
Lat	north	66386405	geographic 2D	Saint Pierre et Miquelon 1950 (deg)
Lat	north	66396405	geographic 2D	MOP78 (deg)
Lat	north	66406405	geographic 2D	RRAF 1991 (deg)
Lat	north	66406413	geographic 3D	RRAF 1991 (3D deg)
Lat	north	66416405	geographic 2D	IGN53 Mare (deg)
Lat	north	66426405	geographic 2D	ST84 Ile des Pins (deg)
Lat	north	66436405	geographic 2D	ST71 Belep (deg)
Lat	north	66446405	geographic 2D	NEA74 Noumea (deg)
Lat	north	66456405	geographic 2D	RGNC 1991 (deg)
Lat	north	66456413	geographic 3D	RGNC 1991 (3D deg)
Lat	north	66466405	geographic 2D	Grand Comoros (deg)
Lat	north	66576405	geographic 2D	Reykjavik 1900 (deg)
Lat	north	66586405	geographic 2D	Hjorsey 1955 (deg)
Lat	north	66596405	geographic 2D	ISN93 (deg)
Lat	north	66596413	geographic 3D	ISN93 (3D deg)
Lat	north	66606405	geographic 2D	Helle 1954 (deg)
Lat	north	66616405	geographic 2D	LKS92 (deg)
Lat	north	66616413	geographic 3D	LKS92 (3D deg)
Lat	north	66636405	geographic 2D	Porto Santo 1995 (deg)
Lat	north	66646405	geographic 2D	Azores Oriental 1995 (deg)
Lat	north	66656405	geographic 2D	Azores Central 1995 (deg)
Lat	north	66666405	geographic 2D	Lisbon 1890 (deg)
Lat	north	66676405	geographic 2D	IKBD-92 (deg)
Lat	north	68016405	geographic 2D	Bern 1898 (Bern) (deg)
Lat	north	68026405	geographic 2D	Bogota 1975 (Bogota) (deg)
Lat	north	68036405	geographic 2D	Lisbon (Lisbon) (deg)
Lat	north	68046405	geographic 2D	Makassar (Jakarta) (deg)
Lat	north	68056405	geographic 2D	MGI (Ferro) (deg)
Lat	north	68066405	geographic 2D	Monte Mario (Rome) (deg)
Lat	north	68086405	geographic 2D	Padang (Jakarta) (deg)
Lat	north	68096405	geographic 2D	Belge 1950 (Brussels) (deg)
Lat	north	68136405	geographic 2D	Batavia (Jakarta) (deg)
Lat	north	68146405	geographic 2D	RT38 (Stockholm) (deg)
Lat	north	68156405	geographic 2D	Greek (Athens) (deg)
Lat	north	68186405	geographic 2D	S-JTSK (Ferro) (deg)
Lat	north	68206405	geographic 2D	Segara (Jakarta) (deg)
Lat	north	69036405	geographic 2D	Madrid 1870 (Madrid) (deg)
\.
