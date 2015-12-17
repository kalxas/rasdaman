#!/usr/bin/env python

from setuptools import find_packages, setup

setup(
    name='PatchManager',
    description='Patch Manager',
    author='Constantin Jucovschi,Dimitar Misev',
    author_email='misev@rasdaman.com',
    keywords='patch submit commit apply',
    version='0.2',
    license="GPL",
    long_description="""
    """,
    entry_points = {
      'trac.plugins': [
      'patchmanager = patchmanager',
      ],
    },
    packages=['patchmanager', 'patchmanager.db'],
    package_dir={'patchmanager':'patchmanager'},
    package_data={'patchmanager':['htdocs/agreement.txt', 'htdocs/icons/*','db/*','templates/*', 'model/*']},
    namespace_packages=['patchmanager'],
)
