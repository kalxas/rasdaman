#!/usr/bin/env python2
#
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

import argparse
import os
import sys
import errno
import itertools
from util.log import log
from jinja2 import Environment, FileSystemLoader

#
# Get the global dictionary with various variables to be available in the templates
#
def get_globals():

    ret = {}

    dimension_max = 4
    ret["dimension_max"] = dimension_max
    ret["dimension_list"] = range(1, dimension_max + 1)

    #
    # cell types, suffixes, min/max values
    #
    ret["cell_type_name_list"] = [
        "bool",
        "octet",
        "char",
        "short",
        "ushort",
        "long",
        "ulong",
        "float",
        "double",
        # TODO: cannot create arrays of complex(d) cell type (http://rasdaman.org/ticket/1859)
        # "complex",
        # "complexd",
        "char_char_char",
        "short_float",
        ]
    ret["cell_type_suffix_dic"] = {
        'bool' : "",
        'octet' : "o",
        'char' : "c",
        'short' : "s",
        'ushort' : "us",
        'long' : "l",
        'ulong' : "ul",
        'float' : "f",
        'double' : "d",
        'complex' : "",
        'complexd' : "",
        "char_char_char" : "",
        "short_float" : "",
        }
    ret["cell_type_min_dic"] = {
        'bool' : "false",
        'octet' : "-128o",
        'char' : "0c",
        'short' : "-32768s",
        'ushort' : "0us",
        'long' : "-2147483648l",
        'ulong' : "0ul",
        'float' : "-3.40282e+38f",
        'double' : "-1.79769e+308d",
        'complex' : "complex(-3.40282e+38f, -3.40282e+38f)",
        'complexd' : "complex(-1.79769e+308d, -1.79769e+308d)",
        "char_char_char" : "{ 255c, 255c, 255c }",
        "short_float" : "{ 32767s, 3.4e+38f }",
        }
    ret["cell_type_max_dic"] = {
        'bool' : "true",
        'octet' : "127o",
        'char' : "255c",
        'short' : "32767s",
        'ushort' : "65535us",
        'long' : "2147483647l",
        'ulong' : "4294967295ul",
        'float' : "3.40282e+38f",
        'double' : "1.79769e+308d",
        'complex' : "complex(3.40282e+38f, 3.40282e+38f)",
        'complexd' : "complex(1.79769e+308d, 1.79769e+308d)",
        "char_char_char" : "{ 255c, 255c, 255c }",
        "short_float" : "{ 32767s, 3.4e+38f }",
        }
    ret["cell_type_val_dic"] = {
        'bool' : "true",
        'octet' : "-13o",
        'char' : "13c",
        'short' : "-151s",
        'ushort' : "151us",
        'long' : "-4888567l",
        'ulong' : "4888567ul",
        'float' : "3.1415927f",
        'double' : "-2.5e-3d",
        'complex' : "complex(3.1415927f, -2.232e+2f)",
        'complexd' : "complex(-2.5e-3d, 3.1415927d)",
        "char_char_char" : "{ 13c, 115c, 212c }",
        "short_float" : "{ -1503s, 3.4e+3f }",
        }
    ret["cell_type_zero_dic"] = {
        'bool' : "false",
        'octet' : "0o",
        'char' : "0c",
        'short' : "0s",
        'ushort' : "0us",
        'long' : "0l",
        'ulong' : "0ul",
        'float' : "0f",
        'double' : "0d",
        'complex' : "complex(0f, 0f)",
        'complexd' : "complex(0d, 0d)",
        "char_char_char" : "{ 0c, 0c, 0c }",
        "short_float" : "{ 0s, 0f }",
        }
    ret["cell_type_size_dic"] = {
        'bool' : 1,
        'octet' : 1,
        'char' : 1,
        'short' : 2,
        'ushort' : 2,
        'long' : 4,
        'ulong' : 4,
        'float' : 4,
        'double' : 8,
        'complex' : 8,
        'complexd' : 16,
        "char_char_char" : 3,
        "short_float" : 6,
        }
    ret["cell_type_signed_dic"] = {
        'bool' : False,
        'octet' : True,
        'char' : False,
        'short' : True,
        'ushort' : False,
        'long' : True,
        'ulong' : False,
        'float' : True,
        'double' : True,
        'complex' : True,
        'complexd' : True,
        "char_char_char" : False,
        "short_float" : True,
        }

    def is_atomic_cell_type(cell_type):
        return cell_type in ret["cell_type_name_list"][:9]
    ret["is_atomic_cell_type"] = is_atomic_cell_type

    def is_complex_cell_type(cell_type):
        return cell_type in ["complex", "complexd"]
    ret["is_complex_cell_type"] = is_complex_cell_type

    def is_composite_cell_type(cell_type):
        return not (is_complex_cell_type(cell_type) or is_atomic_cell_type(cell_type))
    ret["is_composite_cell_type"] = is_composite_cell_type

    # component cell types for composite cell types
    cell_type_components_dic = {}
    for cell_type in ret["cell_type_name_list"]:
        components = []
        if is_composite_cell_type(cell_type):
            components = [("b" + str(i), type_name) \
                          for i, type_name in enumerate(cell_type.split("_"))]
        cell_type_components_dic[cell_type] = components
    ret["cell_type_components_dic"] = cell_type_components_dic

    #
    # collection name / type + mdd type
    #
    coll_names_dic = {}
    coll_types_dic = {}
    mdd_types_dic = {}
    for dimension in ret["dimension_list"]:
        for cell_type in ret["cell_type_name_list"]:
            name = "test_" + str(dimension) + "d_" + cell_type
            key = (dimension, cell_type)
            coll_names_dic[key] = name
            coll_types_dic[key] = name + "_set"
            mdd_types_dic[key] = name + "_mdd"
    ret["coll_name_dic"] = coll_names_dic
    ret["coll_type_name_dic"] = coll_types_dic
    ret["mdd_type_name_dic"] = mdd_types_dic

    #
    # Operations
    #
    ret["oper_induced_binary"] = [
        "+","-","*","/",
        "overlay",
        "is","and","or","xor",
        "=","<",">","<=",">=","!=",
        ]
    ret["oper_induced_binary_name"] = [
        "plus","minus","multiplication","division",
        "overlay",
        "is","and","or","xor",
        "equals","less","greater","lessorequal","greaterorequal","notequal",
        ]
    ret["oper_induced_unary"] = ["+","-","not "]
    ret["oper_induced_unary_name"] = ["plus","minus","not"]
    ret["oper_condense_op"] = ["+","*","and","or","max","min"]
    ret["oper_condense_name"] = ["plus","multiplication","and","or","max","min"]

    #
    # Functions
    #
    ret["func_induced_unary"] = [
        "sqrt","abs","exp","log","ln",
        "sin","cos","tan","sinh","cosh","tanh",
        "arcsin","asin","arccos","acos","arctan","atan",
        ]
    ret["func_induced_binary"] = [
        "pow","power",
        "mod","div",
        "bit",
        # TODO: not supported atm
        #"max","min",
        "complex",
        ]
    ret["func_condense"] = [
        "max_cells","min_cells",
        "all_cells","some_cells",
        "count_cells",
        "add_cells","avg_cells",
        "var_pop","var_samp",
        "stddev_pop","stddev_samp",
        ]

    #
    # sample mdd constants
    #

    # all mdd constants have 16 cells
    mdd_constant_cell_count = 16
    ret["mdd_constant_cell_count"] = mdd_constant_cell_count
    # axis extents for each dimension (0 - 4): 
    # 16 for 1D, 4,4 for 2D, 2,2,4 for 3D, 2,2,2,2 for 4D
    mdd_constant_extents_list = [
        [],
        [mdd_constant_cell_count],
        [int(mdd_constant_cell_count**0.5), int(mdd_constant_cell_count**0.5)],
        [2, 2, 4],
        [int(mdd_constant_cell_count**0.25), int(mdd_constant_cell_count**0.25), \
         int(mdd_constant_cell_count**0.25), int(mdd_constant_cell_count**0.25)],
        ]
    ret["mdd_constant_extents_list"] = mdd_constant_extents_list

    #
    # sdoms for each dimension, e.g. [0:15] for 1D, [0:3,0:3] for 2D, etc.
    #
    mdd_constant_sdom_list = []
    # a list of subset dicts for each dimension
    mdd_constant_subsets_list = []
    subset_desc = ["trims_proper", "trims_containing", "trims_intersecting",
                   "trims_nonintersecting", "slices_intersecting",
                   "slices_nonintersecting"]
    subset_stars_desc = ["trims_star_left", "trims_star_right",
                   "trims_star_left_intersecting", "trims_star_right_intersecting",
                   "trims_star_left_nonintersecting", "trims_star_right_nonintersecting",
                   "trims_star_both", "mixed_slice_intersecting_trims_star_both",
                   "mixed_slice_nonintersecting_trims_star_both"]
    for i in range(dimension_max + 1):
        sdom = ""
        subsets = {}
        subsets_stars = {}
        for j in range(0,i):
            if j > 0:
                sdom += ","
            else:
                for s in subset_desc:
                    subsets[s] = []
                for s in subset_stars_desc:
                    subsets_stars[s] = []

            hi = mdd_constant_extents_list[i][j] - 1
            sdom += "0:" + str(hi)
            # subsets without '*'
            subsets["trims_proper"].append("1:" + str(hi))
            subsets["trims_containing"].append("-2:" + str(hi + 2))
            subsets["slices_intersecting"].append(str(hi - 1))
            if j % 2 == 0:
                # negative
                subsets["trims_intersecting"].append("-2:" + str(hi - 1))
                subsets["trims_nonintersecting"].append("-4:-1")
                subsets["slices_nonintersecting"].append("-10")
            else:
                # positive
                subsets["trims_intersecting"].append("1:" + str(hi + 2))
                subsets["trims_nonintersecting"].append(str(hi + 2) + ":" + str(hi + 4))
                subsets["slices_nonintersecting"].append(str(hi + 2))

            # subsets with '*'
            subsets_stars["trims_star_left"].append("*:" + str(hi - 1))
            subsets_stars["trims_star_right"].append("1:*")
            subsets_stars["trims_star_left_intersecting"].append("*:" + str(hi + 2))
            subsets_stars["trims_star_right_intersecting"].append("-2:*")
            subsets_stars["trims_star_left_nonintersecting"].append("*:-2")
            subsets_stars["trims_star_right_nonintersecting"].append(str(hi + 2) + ":*")
            subsets_stars["trims_star_both"].append("*:*")
            if i > 1:
                if j == 0:
                    subsets_stars["mixed_slice_intersecting_trims_star_both"].append("1")
                    subsets_stars["mixed_slice_nonintersecting_trims_star_both"].append("-2")
                else:
                    subsets_stars["mixed_slice_intersecting_trims_star_both"].append("*:*")
                    subsets_stars["mixed_slice_nonintersecting_trims_star_both"].append("*:*")

        if i > 0:
            sintervals = [subsets[k][0] for k in subset_desc]
            sintervals_desc = dict(zip(sintervals, subset_desc))
            curr_subsets = list(subsets.itervalues())
            for subset in itertools.product(sintervals, repeat=i):
                if set(subset) == 1 or list(subset) in curr_subsets:
                    continue
                k = "mixed"
                for sinterval in subset:
                    k += "_" + sintervals_desc[sinterval]
                subsets[k] = subset

        subsets = {k: "[" + ",".join(v) + "]" for k, v in subsets.iteritems()}
        for k, v in subsets_stars.iteritems():
            if len(v) > 0:
                subsets[k] = "[" + ",".join(v) + "]"
        mdd_constant_sdom_list.append("[" + sdom + "]")
        mdd_constant_subsets_list.append(subsets)

    ret["mdd_constant_sdom_list"] = mdd_constant_sdom_list
    ret["mdd_constant_subsets_list"] = mdd_constant_subsets_list

    #
    # mdd constants cell values for each cell type.
    # general structure: start with 7 "standard values", add some more random values
    #

    # returns 7 "standard values": [0, min, max, 1, 99, -1/9, -99/109]
    def get_standard_vals(cell_type):
        suffix = ret["cell_type_suffix_dic"][cell_type]
        res = [
            "0" + suffix, 
            ret["cell_type_min_dic"][cell_type],
            ret["cell_type_max_dic"][cell_type],
            "1" + suffix,
            "99" + suffix,
            ]
        if ret["cell_type_signed_dic"][cell_type]:
            res += ["-1" + suffix, "-99" + suffix]
        else:
            res += ["9" + suffix, "109" + suffix]
        return res

    mdd_constant_cell_values_dic = {}
    # ['true', 'false', 'true', 'false', ...]
    cell_type = "bool"
    mdd_constant_cell_values_dic[cell_type] = \
        ["true" if i % 2 == 0 else "false" for i in range(0,16)]

    cell_type = "octet"
    mdd_constant_cell_values_dic[cell_type] = get_standard_vals(cell_type) + \
        [val + ret["cell_type_suffix_dic"][cell_type] \
         for val in ["-100","-52","-22","3","23","67","89","123","125"]]

    cell_type = "char"
    mdd_constant_cell_values_dic[cell_type] = get_standard_vals(cell_type) + \
        [val + ret["cell_type_suffix_dic"][cell_type] \
         for val in ["2","5","12","23","45","123","123","234","250"]]

    cell_type = "short"
    mdd_constant_cell_values_dic[cell_type] = get_standard_vals(cell_type) + \
        [val + ret["cell_type_suffix_dic"][cell_type] \
         for val in ["-32700","-12000","-1022","-12","-2","12","1234","12345","23456"]]

    cell_type = "ushort"
    mdd_constant_cell_values_dic[cell_type] = get_standard_vals(cell_type) + \
        [val + ret["cell_type_suffix_dic"][cell_type] \
         for val in ["2","5","12","234","567","8910","34567","45678","56789"]]

    cell_type = "long"
    mdd_constant_cell_values_dic[cell_type] = get_standard_vals(cell_type) + \
        [val + ret["cell_type_suffix_dic"][cell_type] \
         for val in ["-1147483648","-214748364","-21474836","-214748","5","98765", \
                     "12345789","1234567890","2147483633"]]

    cell_type = "ulong"
    mdd_constant_cell_values_dic[cell_type] = get_standard_vals(cell_type) + \
        [val + ret["cell_type_suffix_dic"][cell_type] \
         for val in ["2","12","123","2345","56789","987654","12345789", \
                     "1234567890","2147483633"]]

    cell_type = "float"
    mdd_constant_cell_values_dic[cell_type] = get_standard_vals(cell_type) + \
        ["nan","-nan","inf","-inf"] + \
        [val + ret["cell_type_suffix_dic"][cell_type] \
         for val in ["0.00001","-0.00001","-2147483633","2147483633","123.456789"]]

    cell_type = "double"
    mdd_constant_cell_values_dic[cell_type] = get_standard_vals(cell_type) + \
        [val + ret["cell_type_suffix_dic"][cell_type] \
         for val in ["-1e+3", "12", "-3.145", "0.123456789", "0.00001","-0.00001",
                     "-2147483633","2147483633","123.456789"]]

    cell_type = "complex"
    mdd_constant_cell_values_dic[cell_type] = \
        ["(" + val + "," + val + ")" for val in mdd_constant_cell_values_dic["float"]]

    cell_type = "complexd"
    mdd_constant_cell_values_dic[cell_type] = \
        ["(" + val + "," + val + ")" for val in mdd_constant_cell_values_dic["double"]]

    cell_type = "char_char_char"
    mdd_constant_cell_values_dic[cell_type] = \
        ["{" + val + "," + val + "," + val + "}" \
         for val in mdd_constant_cell_values_dic["char"]]

    cell_type = "short_float"
    mdd_constant_cell_values_dic[cell_type] = \
        ["{" + val1 + "," + val2 + "}" for val1, val2 in \
         zip(mdd_constant_cell_values_dic["short"], mdd_constant_cell_values_dic["float"])]

    ret["mdd_constant_cell_values_dic"] = mdd_constant_cell_values_dic

    #
    # mdd constants based on the sdoms and cell values
    #
    mdd_constant_dic = {}
    for dimension in ret["dimension_list"]:
        sdom = mdd_constant_sdom_list[int(dimension)]
        last_extent = mdd_constant_extents_list[int(dimension)][-1]
        for cell_type in ret["cell_type_name_list"]:
            cell_values = mdd_constant_cell_values_dic[cell_type]
            key = (dimension, cell_type)
            values = ""
            for i, val in enumerate(cell_values):
                values += val
                index = i + 1
                if index != mdd_constant_cell_count:
                    if index % last_extent == 0:
                        values += ";"
                    else:
                        values += ","
            mdd_constant_dic[key] = "<" + sdom + " " + values + ">"

    ret["mdd_constant_dic"] = mdd_constant_dic

    return ret

def parse_cmdline():
    """Setup a command line parser and return the parsed args object"""
    parser = argparse.ArgumentParser(description="rasql query template engine takes "
        "a Jinja2 template file as an input and renders it into a concrete output; "
        "various global variables and functions are available in the template (see "
        "option -g and the documentation). The template is rendered multiple times "
        "for different variable configurations; each output is appended to the same "
        "output file in the directory specified with -d, separated by a line with a "
        "unique separator string (=== by default).")
    parser.add_argument("-t", "--template",
                        help="Template file to be rendered; the output should be multiple "
                        "lines of the form key:value, e.g. query:SELECT version(); "
                        "Consult the documentation for more details.")
    parser.add_argument("-d", "--outdir", default=".",
                        help="Directory for output files ('.' by default).")
    parser.add_argument("-s", "--separator", default="===",
                        help="Separator for different renderings of the same template "
                        "('===' by default).")
    parser.add_argument("-g", "--globals", action='store_true',
                        help="Print all global variables/functions.")
    args = parser.parse_args()
    return args

def main():
    args = parse_cmdline()
    separator = args.separator
    template = args.template
    outdir = args.outdir + ("/" if args.outdir[-1] != "/" else "")

    # additional values to the globals
    globals_dic = get_globals()
    globals_dic['separator'] = separator

    # print globals and exit
    if args.globals:
        for k, v in sorted(globals_dic.iteritems(), key=lambda item: item[0]):
            val = str(v)
            if not "jinja2" in val:
                log.info(" - %s: %s\n", k, v)
        sys.exit(0)

    # create out directory if it doesn't exist
    if args.outdir != ".":
        try:
            os.makedirs(args.outdir)
        except OSError as exc:
            if exc.errno == errno.EEXIST and os.path.isdir(args.outdir):
                pass
            else:
                raise

    # get output file
    template_name = os.path.basename(template)
    template_name = os.path.splitext(template_name)[0]
    outfile = outdir + template_name

    log.title("Rendering templates")
    log.info("""  template:   %s
  output:     %s
  dimensions: %s
  cell types: %s
  separator:  %s""", template, outfile, globals_dic['dimension_list'],
                     globals_dic['cell_type_name_list'], separator)

    if os.path.isfile(outfile):
        log.warn("Output file '" + outfile + "' exists, removing.")
        os.remove(outfile)

    #
    # render template
    #

    file_loader = FileSystemLoader('')
    env = Environment(loader=file_loader)
    env.globals.update(globals_dic)

    template_opener = env.get_template(template)

    # render the template for each dimension and cell type
    with open(outfile, 'a') as f:
        for dimension in env.globals["dimension_list"]:
            env.globals["dimension"] = dimension
            test_id_prefix = template_name + "_" + str(dimension) + "d_"
            for cell_type in env.globals["cell_type_name_list"]:
                test_id = test_id_prefix + cell_type
                key = (dimension, cell_type)
                output = template_opener.render(
                    dimension=dimension,
                    cell_type_name=cell_type,
                    cell_type_suffix=env.globals["cell_type_suffix_dic"][cell_type],
                    cell_type_max=env.globals["cell_type_max_dic"][cell_type],
                    cell_type_min=env.globals["cell_type_min_dic"][cell_type],
                    cell_type_val=env.globals["cell_type_val_dic"][cell_type],
                    cell_type_zero=env.globals["cell_type_zero_dic"][cell_type],
                    cell_type_size=env.globals["cell_type_size_dic"][cell_type],
                    cell_type_signed=env.globals["cell_type_signed_dic"][cell_type],
                    cell_type_components=env.globals["cell_type_components_dic"][cell_type],
                    coll_name=env.globals["coll_name_dic"][key],
                    coll_type_name=env.globals["coll_type_name_dic"][key],
                    mdd_type_name=env.globals["mdd_type_name_dic"][key],
                    mdd_constant_extents=env.globals["mdd_constant_extents_list"][dimension],
                    mdd_constant_sdom=env.globals["mdd_constant_sdom_list"][dimension],
                    mdd_constant_subsets=env.globals["mdd_constant_subsets_list"][dimension],
                    mdd_constant_cell_values=env.globals["mdd_constant_cell_values_dic"][cell_type],
                    mdd_constant=env.globals["mdd_constant_dic"][key],
                    test_id=test_id,
                    template_name=template_name,
                )
                f.write(output)

    log.success("Done.")


if __name__ == "__main__":
    main()
