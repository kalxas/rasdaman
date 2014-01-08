#!/usr/bin/env python

from setuptools import setup

setup(
    name='PatchManager',
    description='Patch Manager',
    author='Constantin Jucovschi',
    author_email='jucovschi@gmail.com',
    keywords='patch submit commit apply',
    version='0.2',
    license="GPL",
    long_description="""
    """,
    entry_points="""
      [trac.plugins]
      patchmanager = patchmanager
    """,
    packages=['patchmanager', 'patchmanager.db'],
    package_dir={'patchmanager':'patchmanager'},
    package_data={'patchmanager':['htdocs/agreement.txt', 'htdocs/icons/*','db/*','templates/*']},
    namespace_packages=['patchmanager'],
    )
