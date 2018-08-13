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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#include <iostream>
#include <iomanip>
#include <limits>
#include <fstream>
#include <string>
#include <vector>
#include <unordered_map>

using namespace std;

// can be max 8 bytes for double
const size_t maxCellBytes = 8;
const char multibandSep = '"';
const char multibandComponentSep = ',';
const char cellSep = ' ';

// type abbreviation -> type size in bytes
unordered_map<string, int> typeSizes = {
    {"b", 1}, {"c", 1}, {"o", 1}, {"s", 2}, {"us", 2}, {"l", 4}, {"ul", 4}, {"f", 4}, {"d", 8}
};

// type abbreviation -> average chars when printed
unordered_map<string, int> typeSizesPrinted = {
    {"b", 4}, {"c", 4}, {"o", 4}, {"s", 4}, {"us", 4}, {"l", 4}, {"ul", 4}, {"f", 18}, {"d", 18}
};

void usage() {
    cout << "Print the array values from binary output of rasdaman.\n\n"
         << "Usage: binprint <filepath> <cellsperline> <celltype> [ <celltype> ... ]\n\n"
         << "<filepath>     - path to the binary file\n"
         << "<cellsperline> - how many cell values per one line to print\n"
         << "                 (specify 0 to let the program determine)\n"
         << "<celltype>     - b | c | o | s | us | l | ul | f | d,\n"
         << "                 (b = bool, c = char, o = octet, ...;\n"
         << "                  specify more than one for multiband data)" << endl;
}

int main(int argc, char *argv[]) {
    if (argc < 4) {
        usage();
        return 1;
    }

    // get args
    string filepath{argv[1]};
    string cellsPerLineArg{argv[2]};
    size_t cellsPerLine{0};
    if (cellsPerLineArg != "0") {
        cellsPerLine = stoi(cellsPerLineArg);
    }
    vector<string> celltypes;
    for (size_t i = 3; i < argc; ++i) {
        string celltype{argv[i]};
        if (typeSizes.count(celltype) > 0) {
            celltypes.emplace_back(celltype);
        } else {
            cerr << "invalid cell type: " << celltype;
            usage();
            return 1;
        }
    }
    const auto cellno = celltypes.size();

    // open file
    ifstream f{filepath, ios::binary | ios::in};
    if (!f) {
        perror(filepath.c_str());
        return 1;
    }

    // get file size
    f.seekg(0, ios::end);
    auto filesize = f.tellg();
    f.seekg(0, ios::beg);

    // calculate how many cells per line to show (less for floats and such)
    size_t avgCellSize{};
    vector<size_t> bandsizes;
    size_t cellsize{};
    for (const auto &celltype: celltypes) {
        auto currcellsize = typeSizes[celltype];
        avgCellSize += typeSizesPrinted[celltype];
        cellsize += currcellsize;
        bandsizes.push_back(currcellsize);
    }
    // double quotes + whitespace
    if (cellno > 1) avgCellSize += 3;
    if (cellsPerLine == 0) {
        cellsPerLine = 80/avgCellSize;
    }

    // read values
    char buffer[maxCellBytes];
    size_t currsize{};
    size_t valuecount{};
    while (currsize < filesize) {

        if (cellno > 1) cout << multibandSep;

        for (size_t i = 0; i < cellno; ++i) {
            if (i > 0) cout << multibandComponentSep;

            const auto &celltype = celltypes[i];
            f.read(buffer, bandsizes[i]);
            if (celltype == "bool") {
                cout << (*((bool*) buffer) ? "t" : "f");
            } else if (celltype == "c") {
                cout << static_cast<int>(*((unsigned char*) buffer));
            } else if (celltype == "o") {
                cout << static_cast<int>(*((signed char*) buffer));
            } else if (celltype == "s") {
                cout << *((signed short*) buffer);
            } else if (celltype == "us") {
                cout << *((unsigned short*) buffer);
            } else if (celltype == "l") {
                cout << *((signed int*) buffer);
            } else if (celltype == "ul") {
                cout << *((unsigned long*) buffer);
            } else if (celltype == "f") {
                cout << std::setprecision(std::numeric_limits<float>::digits10 + 1)
                     << *((float*) buffer);
            } else if (celltype == "d") {
                cout << std::setprecision(std::numeric_limits<double>::digits10 + 1)
                     << *((double*) buffer);
            }
        }
        currsize += cellsize;

        // multiband separator
        if (cellno > 1) cout << multibandSep;

        // print new line if necessary, otherwise value separator
        valuecount += celltypes.size();
        if (valuecount >= cellsPerLine) {
            valuecount = 0;
            cout << endl;
        } else {
            cout << cellSep;
        }
    }
}
