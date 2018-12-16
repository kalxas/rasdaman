# rasdaman documentation

This directory contains the rasdaman documentation in reStructuredText format.


## Getting started

### Install dependencies

*CentOS 7.x*

- `sudo yum install doxygen python-pip latexmk texlive-cm texlive-ec texlive-ucs texlive-cmap texlive-metafont-bin texlive-fncychap texlive-pdftex-def texlive-fancyhdr texlive-titlesec texlive-framed texlive-wrapfig texlive-parskip texlive-upquote texlive-ifluatex texlive-makeindex-bin texlive-times texlive-courier texlive-helvetic texlive-dvips`
- `sudo pip install -U sphinx sphinx_rtd_theme`

*Ubuntu 16.04*

- `apt install python-pip doxygen latexmk texlive-latex-base texlive-latex-extra texlive-fonts-recommended --no-install-recommends`
- `sudo pip install -U sphinx sphinx_rtd_theme`

### Build the docs

1. Rasdaman has to be configured with `-DGENERATE_DOCS=ON` (turned on by default).
2. Build the docs:
 - `make html` or `make latexpdf`
 - alternatively try `./build.sh`. If you get an error
   `import name 'EscapeFormatter'` then:
   - sudo pip uninstall markupsafe
   - sudo pip install markupsafe==0.23
3. Find the docs in `_build/html/index.html` or `_build/latex/rasdaman.pdf`

### Contributing

- Check the short intro below for the reST syntax
 - ... but it should be fairly clear from looking at the docs sources
- Create a review request with `arc diff` before pushing changes.



## Quick intro to reST

### Section headers

In each case the underline or overline marker should be as long as the section
header (use monospace font to do this correctly). From highest level to most 
granular section level:

1. "#" - Parts (overline and underline)
2. "*" - Chapters (overline and underline)
3. "=" - Sections (underline)
4. "-" - Subsections (underline)
5. "^" - Subsubsections (underline)

Example from the QL guide:

    ####################
    Query Language Guide
    ####################

    ************
    Introduction
    ************

    Multidimensional Data
    =====================

    Subsection
    ----------

    Subsubsection
    ^^^^^^^^^^^^^

### Text formatting

- *Italics*
- **Bold**
- ``Code``

Cannot be nested, may not start/end with whitespace, and has to be
separated from surrounding text with some non-word characters.


### Lists

* Bulleted list
* Item two

    * Nested list (note it has to have blank line before and after!)

- Bulleted list continues; you can use `-` instead of `*`

1. Numbered list
2. Item two

#. Automatically numbered list
#. Item two


term (single line)
    Definition of the term (indented on the next line)

    Definition continues with another paragraph (maintain indentation)


| Line block
| line breaks are preserved
| and appear exactly like this (without the `|` characters)


Option lists (e.g. the output of ``rasql -h``) can be simply copy pasted, you
just need to make sure the options and their descriptions form two columns.
More info:
http://docutils.sourceforge.net/docs/ref/rst/restructuredtext.html#option-lists


### Source code

Any source code can go as an indented text after ``::`` (plus blank line).
In the QL guide ``::`` automatically does rasql highlighting. For example:

::

    select c from mr2 as c

For different highlighting you have to use the code-block directive indicating
the language, e.g. java, cpp, xml, javascript, text, ini, etc. Example for java:

.. code-block:: java

    public static void main(...) {
        ...
    }

You can see all lexers with ``pygmentize -L lexers``; see also 
http://pygments.org/languages/


### Images

If an image has no caption then use the image directive, e.g:

.. image:: media/logo_full.png
    :align: center
    :scale: 50%


If it has a caption then use the figure directive; the caption is added as an
indented paragraph after a blank line:

.. _my-label:

.. figure:: media/logo_full.png
    :align: center
    :scale: 50%

    Caption for the figure.



### Hyperlinks

To just have a URL as is nothing special needs to be done, just put as is:

    http://rasdaman.org

To render the URL with alternative text, then the following form should be used:

    `Link text <http://rasdaman.org>`_

Internal cross-referencing can be done by first setting up a label before a
section header or a figure (see above this section Hyperlinks) and then using
it to generate a link anywhere with

    :ref:`my-label`

Instead of :ref: you can use :numref: to get automatic Figure number added to
the link, e.g.

    :numref:`my-label` -> Sec. 2

You can change the default text that :ref: generates like this:

    :ref:`Custom text <my-label>`


### Further resources

- Cannonical specification: 
  http://docutils.sourceforge.net/docs/ref/rst/restructuredtext.html

- Sphinx guide:
  http://www.sphinx-doc.org/en/master/usage/restructuredtext/index.html
