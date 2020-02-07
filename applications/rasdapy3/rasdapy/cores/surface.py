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

"""
This module contains the necessary classes and methods for query construction
and evaluation using the rasdapy library via monkey patching (i.e. overloading)
the python magic methods for certain operations.
"""

from copy import deepcopy

from rasdapy.core import Database

from rasdapy.cores.utils import represent_subsetting, slice_tuple


class ExpNode(object):
    """
    A class to represent a node in the expression for the construction of
    query evaluation tree
    """

    def __init__(self, parent=None, value=None, lchild=None, rchild=None,
                 reflected=False, function=False, parenthesis=False):
        """
        Constructor for the class
        """
        self._parent = parent
        self._lchild = lchild
        self._rchild = rchild
        self._value = value
        self._reflected = reflected
        self._function = function
        self._parenthesis = parenthesis

    def set_parent(self, parent):
        """
        setter for the parent of the node
        """
        self._parent = parent

    def set_value(self, value):
        """
        setter for the value of the node
        """
        self._value = value

    def set_lchild(self, child):
        """
        setter for adding a child to the node
        """
        self._lchild = child

    def set_rchild(self, child):
        """
        setter for adding a child to the node
        """
        self._rchild = child

    def remove_lchild(self):
        """
        delete the left child from node
        """
        self._lchild = None

    def remove_rchild(self):
        """
        delete the right child from node
        """
        self._rchild = None

    @property
    def is_reflected(self):
        """
        A getter for whether the node represents a reflected operation or not
        """
        return self._reflected

    @property
    def is_function(self):
        """
        A getter for whether the node represents a function or not
        """
        return self._function

    @property
    def parent(self):
        """
        A getter for the parent of the node
        """
        return self._parent

    @property
    def value(self):
        """
        A getter for the  value of the node
        """
        return self._value

    @property
    def lchild(self):
        """
        A getter for the children of the node
        """
        return self._lchild

    @property
    def rchild(self):
        """
        A getter for the children of the node
        """
        return self._rchild


class Filter(object):
    """
    Class representing filters. Currently each filter just has a condition
    string
    """

    def __init__(self, condition=None):
        """
        Contructor for class Filter
        Parameters
        ----------
        condition String representing the filter condition
        """
        self._condition = condition

    def __str__(self):
        """
        overloading for str() function. Returns the condition value of Filter
        """
        return self._condition


class RasCollection(object):
    """
    Class denoting a Rasdaman Collection object on a client library.
    Operators are overloaded on this class
    and each of them modify the query tree which can be stringified into a
    query string to be passed to the eval()
    method.
    """

    def __init__(self, name, db=None):
        """
        Constructor for the RasCollection class
        Parameters
        ----------
        name: Name of the collection that the user wants to access
        db: The database object required to query the database
        """
        self._collection = name
        self._query = None
        self._leaf = ExpNode(value=name)
        self._root = self._leaf
        self._filters = []
        self._db = None
        self.use_db(db)

    def use_db(self, db):
        """
        Method to specify the databse object required for accessing the
        collection. Usage not necessary if passed in the
        constructor already.
        Parameters
        ----------
        db: object of class Database
        """
        if db is not None:
            if isinstance(db, Database):
                self._db = db
            else:
                raise Exception(
                        "Argument passed not an instance of ras.Database")

    def eval(self):
        """
        Method to evaluate the query constructed on the server. Starts
        transaction, evaluates query, and returns data.
        Returns
        -------
        Return Array object with the necessary arrays or scalars
        """
        if self._db is not None:
            txn = self._db.transaction()
            query = txn.query(str(self.query))
            data = query.eval()
            txn.abort()
            return data
        else:
            raise Exception("No database object associated with the collection")

    def _operation_helper(self, operator, operand, reflected=False,
                          function=False, parenthesis=False):
        """
        Generic operation method. Creates nodes and keeps track of the query
        tree for all operations.
        Syntactically, there are three possible types of operations:
            * Regular Binary operation
            * Reflected Binary operation
            * Function-like operation
        Parameters
        ----------
        operator: The operator or the function name
        operand: The other operand besides the base MDD
        reflected: Boolean value signifying whether the operation is
        reflected or not
        function: Boolean value signifying whether the operation is a
        function like operation or not
        Returns
        -------
        A new copy of the object with the added operation in the query tree.
        """
        exp = deepcopy(self)
        par = ExpNode(value=operator, reflected=reflected, function=function,
                      parenthesis=parenthesis)
        par.set_rchild(ExpNode(value=operand, parent=par))
        if exp.expression is not None:
            exp._root.set_parent(par)
            par.set_lchild(exp._root)
        exp._root = par
        return exp

    # Begin Mathematical operations

    def __add__(self, other):
        """
        Overloading for + operator
        Parameters
        ----------
        other : other operand
        """
        return self._operation_helper("+", other)

    def __radd__(self, other):
        """
        Overloading for reflected + operator
        Parameters
        ----------
        other : other operand
        """
        return self._operation_helper("+", other, reflected=True)

    def __sub__(self, other):
        """
        Overloading for - operator
        Parameters
        ----------
        other : other operand
        """
        return self._operation_helper("-", other)

    def __rsub__(self, other):
        """
        Overloading for reflected - operator
        Parameters
        ----------
        other : other operand
        """
        return self._operation_helper("-", other, reflected=True)

    def __mul__(self, other):
        """
        Overloading for * operator
        Parameters
        ----------
        other : other operand
        """
        return self._operation_helper("*", other)

    def __rmul__(self, other):
        """
        Overloading for reflected * operator
        Parameters
        ----------
        other : other operand
        """
        return self._operation_helper("*", other, reflected=True)

    def __div__(self, other):
        """
        Overloading for / operator
        Parameters
        ----------
        other : other operand
        """
        return self._operation_helper("/", other)

    def __rdiv__(self, other):
        """
        Overloading for reflected / operator
        Parameters
        ----------
        other : other operand
        """
        return self._operation_helper("/", other, reflected=True)

    def __pow__(self, other):
        """
        Overloading for ** operator
        Parameters
        ----------
        other : other operand
        """
        return self._operation_helper("pow", [other], function=True)

    def __abs__(self):
        """
        Overloading for abs() function
        """
        return self._operation_helper("abs", [], function=True)

    def exp(self, other):
        """
        Method for exp function in RasQL
        """
        return self._operation_helper("exp", [other], function=True)

    def sqrt(self):
        """
        Method for sqrt function in RasQL
        """
        return self._operation_helper("sqrt", [], function=True)

    def log(self):
        """
        Method for log function in RasQL
        """
        return self._operation_helper("log", [], function=True)

    def ln(self):
        """
        Method for ln function in RasQL
        """
        return self._operation_helper("ln", [], function=True)

    def sin(self):
        """
        Method for sin function in RasQL
        """
        return self._operation_helper("sin", [], function=True)

    def cos(self):
        """
        Method for cos function in RasQL
        """
        return self._operation_helper("cos", [], function=True)

    def tan(self):
        """
        Method for tan function in RasQL
        """
        return self._operation_helper("tan", [], function=True)

    def sinh(self):
        """
        Method for sinh function in RasQL
        """
        return self._operation_helper("sinh", [], function=True)

    def cosh(self):
        """
        Method for cosh function in RasQL
        """
        return self._operation_helper("cosh", [], function=True)

    def tanh(self):
        """
        Method for tanh function in RasQL
        """
        return self._operation_helper("tanh", [], function=True)

    def arcsin(self):
        """
        Method for arcsin function in RasQL
        """
        return self._operation_helper("arcsin", [], function=True)

    def arccos(self):
        """
        Method for arccos function in RasQL
        """
        return self._operation_helper("arccos", [], function=True)

    def arctan(self):
        """
        Method for arctan function in RasQL
        """
        return self._operation_helper("arctan", [], function=True)

    # End Mathematical operations

    def __getitem__(self, *args):
        """
        Overloading for [...] operator
        Parameters
        ----------
        other : Arguments in the form of tuples of splice objects
        representing subsetting operations
        Returns
        -------
        A new copy of the object with the added operation in the query tree.
        """
        exp = deepcopy(self)
        if len(args) == 0:
            return exp
        if type(args[0]) is tuple:  # If type is tuple
            value = [slice_tuple(arg) for arg in args[0]]
            exp._leaf.set_value(represent_subsetting(exp._leaf.value, value))
        elif type(args[0]) is slice:  # If type is slice
            exp._leaf.set_value(
                    exp._leaf.value + "[" + str(args[0].start) + ":" + str(
                            args[0].stop) + "]")
        else:
            exp._leaf.set_value(exp._leaf.value + "[" + str(args[0]) + "]")
        return exp

    def __eq__(self, other):
        """
        Overloading for == operator
        Parameters
        ----------
        other: other operand
        Returns
        -------
        True if the objects have same generated query string otherwise
        returns False
        """
        if str(self.query) == str(other.query):
            return True
        else:
            return False

    def __ne__(self, other):
        """
        Overloading for != operator
        Parameters
        ----------
        other: other operand
        Returns
        -------
        False if the objects have same generated query string otherwise
        returns True
        """
        return not self.__eq__(other)

    def __lt__(self, other):
        return self._operation_helper("<", other)

    def __leq__(self, other):
        return self._operation_helper("<=", other)

    def __gt__(self, other):
        return self._operation_helper(">", other)

    def __geq__(self, other):
        return self._operation_helper(">=", other)

    # Begin Condensers

    def avg_cells(self):
        """
        Method for avg_cells function in RasQL
        """
        return self._operation_helper("avg_cells", [], function=True)

    def add_cells(self):
        """
        Method for add_cells function in RasQL
        """
        return self._operation_helper("add_cells", [], function=True)

    def min_cells(self):
        """
        Method for min_cells function in RasQL
        """
        return self._operation_helper("min_cells", [], function=True)

    def max_cells(self):
        """
        Method for max_cells function in RasQL
        """
        return self._operation_helper("max_cells", [], function=True)

    def count_cells(self):
        """
        Method for count_cells function in RasQL
        """
        return self._operation_helper("count_cells", [], function=True)

    def some_cells(self):
        """
        Method for some_cells function in RasQL
        """
        return self._operation_helper("some_cells", [], function=True)

    def all_cells(self):
        """
        Method for all_cells function in RasQL
        """
        return self._operation_helper("all_cells", [], function=True)

    # End Condensers

    def band(self, band_):
        return self._operation_helper(".", band_)

    def encode(self, format):
        """
        Method for specifying encoding of data in RasQL
        """
        return self._operation_helper("encode", [format], function=True,
                                      parenthesis=True)

    def filter(self, **kwargs):
        """
        Method for adding filters to the collection via keyword arguments.
        Any keyword argument is added as a filter as
         is. Perhaps we can have a list of arguments that can be used for
         validation and hence invalid filters can be
         avoided.
        """
        for key in kwargs:
            filter = str(key) + "=" + str(kwargs[key])
            self._filters.append(Filter(condition=filter))

    def sdom(self):
        """
        returns the spatial domain of the query
        """
        sdom_q = self._operation_helper("sdom", [], function=True)
        return sdom_q.eval()

    @property
    def query(self):
        """
        A property for getting the query object created from the query tree.
        Use within str for getting the stringified
        query i.e. str(col.query) for a RasCollection object stored in a
        variable called col
        Returns
        -------
        RasQuery object
        """
        return RasQuery(collection=self.collection, expression=self.expression,
                        condition=self.condition)

    @property
    def collection(self):
        """
        Returns the collection of the object
        """
        return self._collection

    @property
    def expression(self):
        """
        Constructs the expression clause (i.e. clause between select and
        from) of the query from the query tree.
        Returns
        -------
        A string representing the expression clause of the query
        """
        temp = self._leaf
        exp = temp.value
        while temp.parent is not None:
            if temp.parent.is_reflected is False and temp.parent.is_function \
                    is False:
                if type(temp.parent.rchild.value) == RasCollection:
                    exp = "(" + exp + temp.parent.value + str(
                            temp.parent.rchild.value.expression) + ")"
                else:
                    exp = "(" + exp + temp.parent.value + str(
                            temp.parent.rchild.value) + ")"
            elif temp.parent.is_function is True and temp.parent.is_reflected\
                    is False:
                args = temp.parent.rchild.value
                arg_str = ","
                for arg in args:
                    if temp.parent._parenthesis:
                        arg_str = arg_str + '"' + str(arg) + '"'
                    else:
                        arg_str += str(arg)
                    arg_str += ","
                if arg_str != ",":
                    arg_str = arg_str[:-1]
                else:
                    arg_str = ""

                exp = temp.parent.value + "(" + exp + arg_str + ")"
            else:
                if type(temp.parent.rchild.value) == RasCollection:
                    exp = "(" + temp.parent.rchild.value.expression + \
                          temp.parent.value + exp + ")"
                else:
                    exp = "(" + str(
                            temp.parent.rchild.value) + temp.parent.value + \
                          exp + ")"
            temp = temp.parent
        return exp

    @property
    def condition(self):
        """
        Returns the filters stringified together in a composite manner via
        the `and` keyword
        """
        return " and ".join(str(fltr) for fltr in self._filters)


class RasQuery(object):
    """
    Class representing a RasQuery object
    """

    def __init__(self, collection=None, expression=None, condition=None):
        """
        Constructor for the RasQuery object
        Parameters
        ----------
        collection: The clause representing the collection in a query (i.e.
        select ...)
        expression: The clause representing the expression in a query (i.e.
        from ...)
        condition: The clause representing the condition in a query (i.e.
        where ...)
        """
        self._collection = collection
        self._expression = expression
        self._condition = condition

    def __str__(self):
        """
        Overloading the str() method for returning the complete query
        """
        if self._condition != "":
            query_str = "select " + self._expression + " from " + \
                        self._collection + " where " + self._condition
        else:
            query_str = "select " + self._expression + " from " + self._collection
        return query_str
