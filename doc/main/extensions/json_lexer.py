from pygments.lexer import include, RegexLexer
from pygments.token import Token
import re


# The JSONLexer is an attempt at parsing JSON that is more JSON-aware
# than the JavaScript lexer.  The key difference is that it makes an attempt
# to distinguish between attribute names and values to provide more useful
# syntax highlighting than would be available when parsed as a JavaScript
# expression

class JSONLexer(RegexLexer):
    name = 'JSON Lexer'
    aliases = ['json']
    filenames = ['*.json']
    mimetypes = []


    flags = re.DOTALL
    tokens = {
        'whitespace': [
            (r'\s+', Token.Text),
        ],

        'comment': [
            (r'//[^\n]+', Token.Comment.Singleline),
            (r'\.\.+', Token.Comment.Singleline),
        ],

        # represents a simple terminal value
        'simplevalue': [
            (r'(true|false|null)\b', Token.Keyword.Constant),
            (r'[-]?(0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]+)?', Token.Literal.Number),
            (r'"(\\\\|\\"|[^"])*"', Token.Literal.String.Double),
        ],

        # the right hand side of an object, after the attribute name
        'objectattribute': [
            include('value'),
            (r':', Token.Punctuation),
            # comma terminates the attribute but expects more
            (r',', Token.Punctuation, '#pop'),
            # a closing bracket terminates the entire object, so pop twice
            (r'}', Token.Punctuation, ('#pop', '#pop')),
        ],

        # a json object - { attr, attr, ... }
        'objectvalue': [
            include('whitespace'),
            (r'"(\\\\|\\"|[^"])*"', Token.Name.Tag, 'objectattribute'),
            (r'}', Token.Punctuation, '#pop'),
            include('comment'),
        ],

        # json array - [ value, value, ... }
        'arrayvalue': [
            include('whitespace'),
            include('value'),
            (r',', Token.Punctuation),
            (r']', Token.Punctuation, '#pop'),
            include('comment'),
        ],

        # a json value - either a simple value or a complex value (object or array)
        'value': [
            include('whitespace'),
            include('simplevalue'),
            (r'{', Token.Punctuation, 'objectvalue'),
            (r'\[', Token.Punctuation, 'arrayvalue'),
            include('comment'),
        ],


        # the root of a json document whould be a value
        'root': [
            include('objectvalue'),
            include('value'),
        ],

    }
    
