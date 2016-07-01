/*
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
 */
/**
 * This file contains all the tokens needed in the wcps grammar.
 * To add a new token, use the grammar provided by antlr and place the token in its corresponding place in alphabetical order.
 *
 * @author Alex Dumitru <alex@flanche.net>
 * @author Vlad Merticariu <vlad@flanche.net>
 */
lexer grammar wcpsLexerTokens;
FOR: ('f'|'F')('o'|'O')('r'|'R');
ABSOLUTE_VALUE: ('a'|'A')('b'|'B')('s'|'S');
ADD: ('a'|'A')('d'|'D')('d'|'D');
ALL: ('a'|'A')('l'|'L')('l'|'L');
AND: ('a'|'A')('n'|'N')('d'|'D');
ARCSIN: ('a'|'A')('r'|'R')('c'|'C')('s'|'S')('i'|'I')('n'|'N');
ARCCOS: ('a'|'A')('r'|'R')('c'|'C')('c'|'C')('o'|'O')('s'|'S');
ARCTAN: ('a'|'A')('r'|'R')('c'|'C')('t'|'T')('a'|'A')('n'|'N');
AVG: ('a'|'A')('v'|'V')('g'|'G');
BIT: ('b'|'B')('i'|'I')('t'|'T');
CASE: ('c'|'C')('a'|'A')('s'|'S')('e'|'E');
COLON : ':';
COMMA : ',';
CONDENSE: ('c'|'C')('o'|'O')('n'|'N')('d'|'D')('e'|'E')('n'|'N')('s'|'S')('e'|'E');
COS: ('c'|'C')('o'|'O')('s'|'S');
COSH: ('c'|'C')('o'|'O')('s'|'S')('h'|'H');
COUNT:('c'|'C')('o'|'O')('u'|'U')('n'|'N')('t'|'T');
COVERAGE: ('c'|'C')('o'|'O')('v'|'V')('e'|'E')('r'|'R')('a'|'A')('g'|'G')('e'|'E');
COVERAGE_VARIABLE_NAME_PREFIX: '$';
CRS_TRANSFORM: ('c' | 'C')('r' | 'R')('s' | 'S')('t' | 'T')('r' | 'R')('a' | 'A')('n' | 'N')('s' | 'S')('f' | 'F')('o' | 'O')('r' | 'R')('m' | 'M');
DECODE: ('d' | 'D')('e' | 'E')('c' | 'C')('o' | 'O')('d' | 'D')('e' | 'E');
DEFAULT: ('d' | 'D')('e' | 'E')('f' | 'F')('a' | 'A')('u' | 'U')('l' | 'L')('t' | 'T');
DESCRIBE_COVERAGE: ('d' | 'D')('e' | 'E')('s' | 'S')('c' | 'C')('r' | 'R')('i' | 'I')('b' | 'B')('e' | 'E')('c' | 'C')('o' | 'O')('v' | 'V')('e' | 'E')('r' | 'R')('a' | 'A')('g' | 'G')('e' | 'E');
DIVISION: '/';
DOT: '.';
ENCODE: ('e' | 'E')('n' | 'N')('c' | 'C')('o' | 'O')('d' | 'D')('e' | 'E');
EQUAL: '=';
EXP: ('e'|'E')('x'|'X')('p'|'P');
EXTEND: ('e' | 'E')('x' | 'X')('t' | 'T')('e' | 'E')('n' | 'N')('d' | 'D');
FALSE : ('F' | 'f')('A' | 'a')('L' | 'l')('S' | 's')('E' | 'e');
GREATER_THAN: '>';
GREATER_OR_EQUAL_THAN: '>=';
IMAGINARY_PART: ('i'|'I')('m'|'M');
IDENTIFIER:	('i'|'I')('d'|'D')('e'|'E')('n'|'N')('t'|'T')('i'|'i')('f'|'F')('i'|'I')('e'|'E')('r'|'R');
CRSSET:	('c'|'C')('r'|'R')('s'|'S')('s'|'S')('e'|'E')('t'|'T');
IMAGECRSDOMAIN: ('i'|'I')('m'|'M')('a'|'A')('g'|'G')('e'|'E')('c'|'C')('r'|'R')('s'|'S')('d'|'D')('o'|'O')('m'|'M')('a'|'A')('i'|'I')('n'|'N');
IMAGECRS: ('i'|'I')('m'|'M')('a'|'A')('g'|'G')('e'|'E')('c'|'C')('r'|'R')('s'|'S');
DOMAIN: ('d'|'D')('o'|'O')('m'|'M')('a'|'A')('i'|'I')('n'|'N');
IN:	('i'|'I')('n'|'N');
LEFT_BRACE: '{';
LEFT_BRACKET: '[';
LEFT_PARENTHESIS: '(';
LN: ('l'|'L')('n'|'N');
LIST: ('l'|'L')('i'|'I')('s'|'S')('t'|'T');
LOG: ('l'|'L')('o'|'O')('g'|'G');
LOWER_THAN: '<';
LOWER_OR_EQUAL_THAN: '<=';
MAX:('m'|'M')('a'|'A')('x'|'X');
MIN: ('m'|'M')('i'|'I')('n'|'N');
MINUS: '-';
MULTIPLICATION: '*';
NOT: ('n'|'N')('o'|'O')('t'|'T');
NOT_EQUAL: '!=';
NAN_NUMBER_CONSTANT: ('n'|'N')('a'|'A')('n'|'N');
OR: ('o'|'O')('r'|'R');
OVER:('o'|'O')('v'|'V')('e'|'E')('r'|'R');
OVERLAY: ('o'|'O')('v'|'V')('e'|'E')('r'|'R')('l'|'L')('a'|'A')('y'|'Y');
PLUS: '+';
POWER: ('p'|'P')('o'|'O')('w'|'W');
REAL_PART: ('r'|'R')('e'|'E');
ROUND: ('r'|'R')('o'|'O')('u'|'U')('n'|'N')('d'|'D');
RETURN: ('r'|'R')('e'|'E')('t'|'T')('u'|'U')('r'|'R')('n'|'N');
RIGHT_BRACE: '}';
RIGHT_BRACKET: ']';
RIGHT_PARENTHESIS: ')';
SCALE: ('s'|'S')('c'|'C')('a'|'A')('l'|'L')('e'|'E');
SEMICOLON: ';';
SIN: ('s'|'S')('i'|'I')('n'|'N');
SINH: ('s'|'S')('i'|'I')('n'|'N')('h'|'H');
SLICE: ('s'|'S')('l'|'L')('i'|'I')('c'|'C')('e'|'E');
SOME:('s'|'S')('o'|'O')('m'|'M')('e'|'E');
SQUARE_ROOT: ('s'|'S')('q'|'Q')('r'|'R')('t'|'T');
STRUCT: ('s'|'S')('t'|'T')('r'|'R')('u'|'U')('c'|'C')('t'|'T');
SWITCH: ('s'|'S')('w'|'W')('i'|'I')('t'|'T')('c'|'C')('h'|'H');
TAN: ('t'|'T')('a'|'A')('n'|'N');
TANH: ('t'|'T')('a'|'A')('n'|'N')('h'|'H');
TRIM: ('T' | 't')('R' | 'r')('I' | 'i')('M' | 'm');
TRUE: ('T' | 't')('R' | 'r')('U' | 'u')('E' | 'e');
USING: ('u'|'U')('s'|'S')('i'|'I')('n'|'N')('g'|'G');
VALUE:('v'|'V')('a'|'A')('l'|'L')('u'|'U')('e'|'E');
VALUES:('v'|'V')('a'|'A')('l'|'L')('u'|'U')('e'|'E')('s'|'S');
WHERE: ('w'|'W')('h'|'H')('e'|'E')('r'|'R')('e'|'E');
XOR: ('x'|'X')('o'|'O')('r'|'R');
REAL_NUMBER_CONSTANT: ('-')?[0-9]+('.'[0-9]*)?;
//COVERAGE_VARIABLE_NAME: '$'[a-zA-Z0-9_]+; disabled for backwards compatibility with WCPS1
COVERAGE_VARIABLE_NAME: [$a-zA-Z0-9_]+; // added $ for backwards compatibility with WCPS1
NAME: [a-z|A-Z]+;
//FORMAT_NAME: replaced with STRING_LITERAL for backward compatibility with WCPS1. The regex for a valid mime type is: '"'[a-zA-Z0-9!#$&.+-^_]+'/'[a-zA-Z0-9!#$&.+-^_]+'"'
STRING_LITERAL: '"'[a-zA-Z0-9!#$&.+-^_ ]+'"';
WS: [ \n\t\r]+ -> skip;
CRS_TRANSFORM: ('c'|'C')('r'|'R')('s'|'S')('t'|'T')('r'|'R')('a'|'A')('n'|'N')('s'|'S')('f'|'F')('o'|'O')('r'|'R')('m'|'M');
//for testing
