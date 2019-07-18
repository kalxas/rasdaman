from __future__ import print_function
from future import standard_library
standard_library.install_aliases()
from builtins import str
from builtins import object
import sys
import os
import time, datetime
import urllib.request, urllib.parse, urllib.error
import socket

global __version__
__version__ = '2.0'

global dsep
dsep = os.sep

# sets a storage location in case the user doesn't provide one (to be on the safe side) - eg. for error msgs.
global temp_storage
temp_storage = None
try_dir = ['TMP', 'TEMP', 'HOME', 'USER']
for elem in try_dir:
    temp_storage = os.getenv(elem)
    if temp_storage != None:
        break

if temp_storage is None:
    cur_dir = os.getcwd()
    temp_storage = cur_dir # +'/tmp'

global outputDir

class WCPSUtil(object):

	_timeout = 180
	socket.setdefaulttimeout(_timeout)

	def __init__(self):
		pass

	def ProcessCoverage(self, input_params):
		global outputDir
		query = input_params['query']
		outputLoc = input_params['outputDir']
		serv_url = input_params['serv_url']
		print(query)
		print(outputLoc)

		post_query = urllib.parse.urlencode({"query": query})
		result = self._exec_Process_Coverage(serv_url, post_query, outputLoc)
		return result

	def _exec_Process_Coverage(self, serv_url, post_query, outputLoc):
		now = time.strftime('_%Y%m%dT%H%M%S')

		if outputLoc is not None:
			outfile = outputLoc+"wcps"+now
		else:
			outfile = temp_storage+dsep+"wcps"+now

		try:
			request_handle = urllib.request.urlopen(serv_url, post_query.encode('utf-8'))
			status = request_handle.code
			http_info = request_handle.info()
			http_type = http_info.get_content_type()

			try:
				file_PC = open(outfile, 'w+b')
				file_PC.write(request_handle.read())
				file_PC.flush()
				os.fsync(file_PC.fileno())
				file_PC.close()
				request_handle.close()
				return_arr = {"status":status,
							   "outfile":outfile,
							   "mimetype":http_type
							}

				return return_arr

			except IOError as err:
				errno, strerror = err.args
				IOmessage =  "I/O error({0}): {1}".format(errno, strerror)
				print(IOmessage)
				return_arr = {"status":-1, "message":IOmessage}
				return return_arr
			except:
				unknownError = "Unexpected error:", sys.exc_info()[0]
				print(unknownError)
				return_arr = {"status":-1, "message":unknownError}
				return return_arr
				raise

		except urllib.error.URLError as url_ERROR:
			if hasattr(url_ERROR, 'reason'):
				print('\n', time.strftime("%Y-%m-%dT%H:%M:%S%Z"), "- ERROR:  Server not accessible -", url_ERROR.reason)
				servError = str(url_ERROR)
				return_arr = {"status":-1, "message":servError}
				return return_arr
			elif hasattr(url_ERROR, 'code'):
				print(time.strftime("%Y-%m-%dT%H:%M:%S%Z"), "- ERROR:  The server couldn\'t fulfill the request - Code returned:  ", url_ERROR.code, url_ERROR.read())
				err_msg = str(url_ERROR.code)+'--'+str(url_ERROR)
				return_arr = {"status":-1, "message":err_msg}
				return return_arr
		except TypeError as err:
			return_arr = {"status":-1, "message":str(err)}
			return return_arr

		return