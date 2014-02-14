#!/usr/bin/python

import sys
import argparse
import os
import logging
import ntpath

# Import psycopg2 module or exit the script if it is not installed.
try:
	import psycopg2
except ImportError:
        sys.stderr.write('\nCannot import the point cloud coverage, because the psycopg2 module is not installed.\n')
	sys.exit(1)

MAX_BATCH_INSERT = 100
DB_TABLE_PREFIX = "ps"

DB_NAME = os.environ['PS_DB']
HOST_NAME = os.environ['PG_HOST']

conn = psycopg2.connect("dbname=%s  host=%s" % (DB_NAME, HOST_NAME))
cursor = conn.cursor()
conn.autocommit = True

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

parser = argparse.ArgumentParser()
parser.add_argument('--crs', help='CRS of the input file')
parser.add_argument('--files', help='Point cloud file.')
args = parser.parse_args()

if not args.files:
	sys.stderr.write('\nUsage: no input file provided, exit the program.\n')
	sys.exit(1)
else:
	fileName, fileExtension = os.path.splitext(args.files)
	filepath, coverage_name = ntpath.split(fileName)
	if fileExtension == '.xyz':
		logger.info('\nReading a %s file' % fileExtension)
	try:
		# Inserting the general coverage info to ps_coverage table
		insert_stmt = ""
		insert_stmt = ''.join(["INSERT INTO ",
			DB_TABLE_PREFIX,
			"_coverage(name, gml_type_id, native_format_id) VALUES('%s', (SELECT id FROM ",
			DB_TABLE_PREFIX,
			"_gml_subtype WHERE subtype='MultiPointCoverage'), (SELECT id FROM ",
			DB_TABLE_PREFIX,
			"_mime_type WHERE mime_Type='application/x-octet-stream'));"])
		insert_stmt = insert_stmt % (coverage_name)
		cursor.execute(insert_stmt)
		conn.commit()
		#logger.info('\nInserting general information of the coverage into ps_coverage table')

		# Inserting additional descriptive information
		#TBD

		#logger.info('\nInserting crs.')
		# Finding the coverage id.
		coverage_id_stmt = "SELECT id FROM " + DB_TABLE_PREFIX + "_coverage WHERE name='%s'" % (coverage_name)
		cursor.execute(coverage_id_stmt)
		coverage_id = cursor.fetchone()[0]

		# Inserting the range type (attributes) information (rgb) in ps_range_type_component
		# 	coverage_id | name (band name) | data_type_id (8-bit signed integer from ps_range_data_type) |
		# 	component_order (r:0 g:1 b:2) | field_id (from ps_quantity col for unsigned char) | field_table (ps_quantity)
		insert_stmt = """INSERT INTO %s_range_type_component (coverage_id,
			name, data_type_id, component_order, field_id, field_table) VALUES (%d, 'red',
			3, 0, 2, '%s_quantity');""" % (DB_TABLE_PREFIX, coverage_id, DB_TABLE_PREFIX)
		cursor.execute(insert_stmt);
		conn.commit()

		insert_stmt = """INSERT INTO %s_range_type_component (coverage_id,
			name, data_type_id, component_order, field_id, field_table) VALUES (%d, 'green',
			3, 1, 2, '%s_quantity');""" % (DB_TABLE_PREFIX, coverage_id, DB_TABLE_PREFIX)
		cursor.execute(insert_stmt);
		conn.commit()

		insert_stmt = """INSERT INTO %s_range_type_component (coverage_id,
			name, data_type_id, component_order, field_id, field_table) VALUES (%d, 'blue',
			3, 2, 2, '%s_quantity');""" % (DB_TABLE_PREFIX, coverage_id, DB_TABLE_PREFIX)
		cursor.execute(insert_stmt);
		conn.commit()

		# Setting the crs for the new coverage to index3d (4)
		#insert_stmt = """INSERT INTO ps_domain_set(coverage_id, native_crs_ids ) VALUES(%d, '{4}');""" % (coverage_id)
		#cursor.execute(insert_stmt);
		#conn.commit()

		# Check if the crs already exists
		crs_stmt = "SELECT id FROM " + DB_TABLE_PREFIX + "_crs WHERE uri='%s'" % (args.crs)
		cursor.execute(crs_stmt)
		if cursor.fetchone() is None:
			crs_stmt = "INSERT INTO " + DB_TABLE_PREFIX + "_crs(uri) VALUES('%s')" % (args.crs)
			cursor.execute(crs_stmt)
			conn.commit()
			#logger.info('The crs was added to the ps_crs.')
		crs_stmt = "SELECT id FROM " + DB_TABLE_PREFIX + "_crs WHERE uri='%s'" % (args.crs)
		cursor.execute(crs_stmt)
		crs_id = cursor.fetchone()[0]

		# Insert (coverage_id, crs_id) into ps_domain_set
		crs_stmt = "INSERT INTO " + DB_TABLE_PREFIX + "_domain_set VALUES(%d,'{%d}')" % (coverage_id, crs_id)
		cursor.execute(crs_stmt)
		conn.commit()
		#logger.info('(coverage_id, crs_id) was added into ps_domain_set.')

		with open(args.files,'r') as f:
			# Get the latest point id
			point_id_stmt = "SELECT last_value FROM " + DB_TABLE_PREFIX + "_multipoint_id_seq"
			cursor.execute(point_id_stmt)
			point_id = int(cursor.fetchone()[0] or 1)

			geo_insert_stmt = []
			geo_insert_stmt.append("INSERT INTO " + DB_TABLE_PREFIX + "_multipoint(coverage_id,coordinate,value) VALUES")

			lcount = 0
			moreLine = True

			while moreLine:
				line = f.readline()
				lcount += 1

				if len(line.split()) != 6:
					moreLine = False
				else:
					x = line.split()[0]
					y = line.split()[1]
					z = line.split()[2]
					r = line.split()[3]
					g = line.split()[4]
					b = line.split()[5]

					if len(geo_insert_stmt) > 1:
						geo_insert_stmt.append(",");
					geo_insert_stmt.append("""(%d,'POINT(%s %s %s)','{%s,%s,%s}')""" % (int(coverage_id),x,y,z,r,g,b))

					if lcount == MAX_BATCH_INSERT:
						lcount = 0
						geo_insert_stmt.append(';')
						cursor.execute(''.join(geo_insert_stmt));
						conn.commit()
						geo_insert_stmt = []
						geo_insert_stmt.append("INSERT INTO " + DB_TABLE_PREFIX + "_multipoint(coverage_id,coordinate,value) VALUES")


			if len(geo_insert_stmt) > 1:
				geo_insert_stmt.append(';')
				cursor.execute(''.join(geo_insert_stmt));
				conn.commit()

		logger.info(' %s is imported.' % coverage_name)

	except IOError:
		#logger.debug('The file %s does not exists.' % arg)
		sys.stderr.write('\nThe file "%s" does not exists, exit the program.\n' % args)
		sys.exit(1)

