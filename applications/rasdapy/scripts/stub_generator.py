"""
 *
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""

import os
import re
import subprocess
import sys
from distutils.spawn import find_executable

# Find the Protocol Compiler.
# TODO: Take windows cases into account
if 'PROTOC' in os.environ and os.path.exists(os.environ['PROTOC']):
    protoc = os.environ['PROTOC']
    grpc_plugin = os.environ['GRPC_PYTHON_PLUGIN']
elif os.path.exists('/usr/local/scripts/protoc'):
    protoc = "/usr/local/scripts/protoc"
    grpc_plugin = "/usr/local/scripts/grpc_python_plugin"
elif os.path.exists('/usr/scripts/protoc'):
    protoc = "/usr/scripts/protoc"
    grpc_plugin = "/usr/scripts/grpc_python_plugin"
else:
    protoc = find_executable("protoc")
    grpc_plugin = find_executable("grpc_python_plugin")


def generate_proto(source, destination, proto_dir, stubs_dir, require=True):
    """
    Invokes the Protocol Compiler to generate a _pb2.py from the given
    .proto file.  Does nothing if the output already exists and is newer than
    the input.

    Args:
        destination: the _pb2 file path
        source: the proto file path
    """

    if not require and not os.path.exists(source):
        sys.stderr.write("Source is not a valid file path")
        return

    if not require and protoc is None:
        sys.stderr.write(
            "Can't find protoc. Make sure you've installed protocol buffers")
        return

    if (not os.path.exists(destination) or
            (os.path.exists(source) and
                     os.path.getmtime(source) > os.path.getmtime(destination))):
        print("Generating %s..." % destination)

        if not os.path.exists(source):
            sys.stderr.write("Can't find required file: %s\n" % source)
            sys.exit(-1)

        if protoc is None:
            sys.stderr.write(
                "protoc is not installed nor found in ../src.  Please compile it "
                "or install the binary package.\n")
            sys.exit(-1)

        protoc_command = [protoc, "-I" + proto_dir, "--python_out=" + stubs_dir,
                          "--grpc_out=" + stubs_dir,
                          "--plugin=protoc-gen-grpc=" + grpc_plugin, source]
        if subprocess.call(protoc_command) != 0:
            sys.exit(-1)


def main(args=None):
    proto_list = ['client_rassrvr_service.proto', 'common_service.proto',
                  'rasmgr_client_service.proto']
    current_dir = os.path.dirname(os.path.realpath(__file__))
    proto_dir = current_dir + "/../../../rasnet/protomessages/"
    stubs_dir = current_dir + "/../rasdapy/stubs/"
    for proto_file in proto_list:
        pb2_file = proto_file.replace(".proto", "_pb2.py")
        generate_proto(proto_dir + proto_file, stubs_dir + pb2_file, proto_dir,
                       stubs_dir, require=True)
        f = open(stubs_dir + pb2_file, "r+b")
        f_content = f.read()
        f_content = re.sub(r"syntax='proto3',", r"#syntax='proto3'", f_content)
        f.seek(0)
        f.truncate()
        f.write(f_content)
        f.close()


if __name__ == '__main__':
    main()
