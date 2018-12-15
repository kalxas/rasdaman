"""
    util.rasql
    ~~~~~~~~~~

    Lexer for rasql.

    :copyright: Copyright 2018 Dimitar Misev
    :license: BSD
"""
from pygments.lexer import RegexLexer
from pygments.token import Punctuation, \
    Text, Comment, Operator, Keyword, Name, String, Number, Generic
import sphinx
import re

__all__ = ['RasqlLexer', 'RasqlDomain']

class RasqlLexer(RegexLexer):
    """
    Lexer for rasql.
    """

    name = 'rasql'
    aliases = ['rasdaman']
    filenames = ['*.rasql']
    mimetypes = ['text/x-rasql']

    flags = re.IGNORECASE
    tokens = {
        'root': [
            (r'\s+', Text),
            (r'--.*?\n', Comment.Single),
            (r'//.*?\n', Comment.Single),
            (r'/\*', Comment.Multiline, 'multiline-comments'),
            (r'(select|from|where|as|commit|'
             r'insitu|referencing|expand|up|down|'
             r'overlay|concat|along|case|when|then|else|end|'
             r'insert|into|values|delete|drop|create|collection|alter|type|typedef|describe|'
             r'update|set|assign|in|marray|mdarray|condense|over|using|coordinates|'
             r'view|function|get|returns|language|extern|blocking|deterministic|'
             r'nonblocking|nondeterministic|list|begin|end|'
             r'index|rc_index|tc_index|a_index|d_index|rd_index|rpt_index|rrpt_index|it_index|auto|'
             r'tiling|aligned|regular|directional|with|subtiling|no_limit|regroup|'
             r'regroup_and_subtiling|area|of|interest|statistic|tile|size|border|threshold|'
             r'storage|compression|AutoCompression|ZLib|RLE|HaarWavelet|DaubechiesWavelet|'
             r'SepZLib|SepRLE|Daubechies6Wavelet|Daubechies8Wavelet|Daubechies10Wavelet|'
             r'Daubechies12Wavelet|Daubechies14Wavelet|Daubechies16Wavelet|Daubechies18Wavelet|'
             r'Daubechies20Wavelet|LeastAsym8Wavelet|LeastAsym10Wavelet|LeastAsym12Wavelet|'
             r'LeastAsym14Wavelet|LeastAsym16Wavelet|LeastAsym18Wavelet|LeastAsym20Wavelet|'
             r'Coiflet6Wavelet|Coiflet12Wavelet|Coiflet18Wavelet|Coiflet24Wavelet|'
             r'Coiflet30Wavelet|QHaarWavelet|PACKBITS|TMC|'
             r'accessed|modified|exception|'
             r'trigger|role|roles|user|users|password|grant|revoke|before|after|instead|'
             r'array|string|struct|'
             r'max|min|and|or|xor|is|not|null)\b', Keyword),
            (r'nan', Keyword.Constant),
            (r'$[0-9]+', Keyword.Constant),
            (r'#MDD[0-9]+#', Keyword.Constant),
            (r'(true|false|t|f)\b', Keyword.Constant),
            (r'(unsigned|bool|char|octet|short|ushort|long|ulong|float|double|'
             r'complex|complexd)\b', Keyword.Type),
            (r'[+*/<>=~!-]', Operator),
            (r'0x[0-9A-Fa-f]+(c|us|ul|o|s|l)?', Number.Hex),
            (r'[0-9]+(c|us|ul|o|s|l)?', Number.Integer),
            (r'([0-9]+|([0-9]+(\.[0-9]+)?)([eE][-+]?[0-9]+)?)[df]?', Number.Float),
            # TODO: Backslash escapes?
            (r"'(''|[^'])*'", String.Single),
            (r'"(""|[^"])*"', String.Symbol), # not a real string literal in ANSI SQL
            (r'[a-z_]\w*', Name),
            (r'[;:()\[\],\.\{\}]', Punctuation)
        ],
        'multiline-comments': [
            (r'/\*', Comment.Multiline, 'multiline-comments'),
            (r'\*/', Comment.Multiline, '#pop'),
            (r'[^/\*]+', Comment.Multiline),
            (r'[/*]', Comment.Multiline)
        ]
    }


# from sphinxcontrib.domaintools import custom_domain
# from sphinx.util.docfields import *
# from docutils import nodes
# from sphinx import addnodes

# rasql_types = ['mdd', 'scalar', 'minterval', 'sinterval', 'point', 'string']

# def parse_func(env, sig, signode):
#     m = re.match(r'(\w+) *\((.*)\)', sig)
#     if not m:
#         signode += addnodes.desc_name(sig, sig)
#         return sig
#     func_name, args = m.groups()
#     signode += addnodes.desc_name(func_name, func_name)
#     params = addnodes.desc_parameterlist()
#     args = args.split(',')
#     for arg in args:
#         arg = arg.strip()
#         params += addnodes.desc_parameter(arg, arg)
#     signode += params
#     return func_name

# RasqlDomain = custom_domain('RasqlDomain',
#     name  = 'rasql',
#     label = "rasql",

#     elements = dict(
#         function = dict(
#             objname = "rasql function",
#             parse=parse_func,
#             indextemplate = "%s (rasql function)",
#             fields = [
#                 TypedField('param',
#                     label = "Parameters",
#                     names = ['param', 'parameter', 'arg', 'argument', 'input'],
#                     typenames = rasql_types,
#                     can_collapse = True,
#                 ),
#                 TypedField('return',
#                     label = "Return",
#                     names = ['ret', 'return', 'res', 'result', 'output'],
#                     typenames = rasql_types,
#                     can_collapse = True,
#                 )
#             ],
#         ),
#     )
# )
