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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
/*
 * JOMDoc - A Java library for OMDoc documents (http://omdoc.org/jomdoc).
 *
 * Original author    Normen Müller <n.mueller@jacobs-university.de>
 * Web                http://kwarc.info/nmueller/
 * Created            Oct 17, 2007
 * Filename           $Id: ListUtil.java 1976 2010-07-31 12:07:20Z dmisev $
 * Revision           $Revision: 1976 $
 *
 * Last modified on   $Date:2007-10-25 18:50:01 +0200 (Thu, 25 Oct 2007) $
 *               by   $Author:nmueller $
 *
 * Copyright (C) 2007,2008 the KWARC group (http://kwarc.info)
 * Licensed under the GNU  Public License v3 (GPL3).
 * For other licensing contact Michael Kohlhase <m.kohlhase@jacobs-university.de>
 */
package petascope.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import nu.xom.Node;

/**
 * List utilities.
 *
 * @author Normen M&#xFC;ller&nbsp;&#60;n.mueller@jacobs-university.de&#62;
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class ListUtil {

    // TODO comment
    public static final <T> List<T> reverse(List<T> l) {
        List<T> nl = new LinkedList<T>(l);
        Collections.reverse(nl);
        return nl;
    }

    /**
     * @return sublist(l, n, m) --> [x_n,...,x_m] if 0 <= n < m < size(l) else empty_list
     */
    public static <T> List<T> sublist(List<T> l, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            return Collections.<T>emptyList();
        }
        return l.subList(fromIndex, toIndex + 1);
    }

    /**
     * Converts a collection to a string, separating the elements by ","
     *
     * @param l The StringList
     * @param c The delimiter
     * @return A String of the ListElements separated by c.
     */
    public static <T> String ltos(Collection<T> l, String c) {
        String s = "";
        for (Iterator<T> iter = l.iterator(); iter.hasNext();) {
            if (!s.equalsIgnoreCase("")) {
                s = s + c + iter.next().toString();
            } else {
                s = s + iter.next().toString();
            }
        }
        return s;
    }

    /**
     * Converts a collection  to a string
     *
     * @param l the element list
     * @return A String of the ListElements separated by new lines
     */
    public static <T> String ltos(Collection<T> l) {
        String s = "";
        for (Iterator<T> iter = l.iterator(); iter.hasNext();) {
            Object o = iter.next();
            String tmp = null;
            if (o instanceof Node) {
                tmp = ((Node) o).toXML();
            } else if (o instanceof Collection) {
                tmp = ltos((Collection) o);
            } else {
                tmp = o.toString();
            }
            if (!s.equals("")) {
                s += "\n";
                s += tmp;
            } else {
                s += tmp;
            }
        }
        return s;
    }

    /**
     * Converts a string to a list
     *
     * @param s The StringList
     * @param c The delimiter
     * @return A String of the ListElements separated by c.
     */
    public static List<String> stol(String s, String c) {
        List<String> l = new LinkedList<String>();
        if (s == null) {
            return l;
        }
        s = s.replaceAll(" *, *", c);
        String[] sl = s.split(c, s.length());
        for (int i = 0; i < sl.length; i++) {
            l.add(sl[i]);
        }
        return l;
    }

    /* head(e_1,...,e_n) --> e_1 */
    public static <T> T head(List<T> l) {
        if (l == null || l.isEmpty()) {
            return null;
        } else {
            return l.get(0);
        }
    }

    /* head(e_1,...,e_n) --> (e_2,...,e_n) */
    public static <T> List<T> tail(List<T> l) {
        if (l == null) {
            return null;
        }
        if (l.isEmpty() || l.size() == 1) {
            return Collections.<T>emptyList();
        }
        return ListUtil.sublist(l, 1, l.size() - 1);
    }

    /**
     * con (e, [x_1,...x_n]) --> [e,x_1,...x_n]
     */
    public static <T> List<T> cons(T e, List<T> l) {
        return concat(Collections.singletonList(e), l);
    }

    /**
     * [x_1,...x_n]@[y_1,...y_m] --> [x_1,...x_n,y_1,...,y_m]
     */
    public static <T> List<T> concat(List<T> l, List<T> r) {
        List<T> res = new ArrayList<T>();
        for (T a : l) {
            res.add(a);
        }
        for (T a : r) {
            res.add(a);
        }
        return res;
    }

    public static <T> List<T> toList(T... e) {
         List<T> ret = new ArrayList<T>();
         if (e != null) {
             for (T o : e) {
                ret.add(o);
            }
         }
         return ret;
    }

    /**
     * New iteration [x_1,...x_m] --> [x_1,...x_m, n]
     */
    public static final List<Integer> add(List<Integer> l, int n) {
        List<Integer> nl = new LinkedList<Integer>(l);
        nl.add(n);
        return nl;
    }

    /**
     * Goto next iteration [x_1,...x_m] --> [x_1,...x_m + 1]
     */
    public static final List<Integer> inc(List<Integer> l) {
        List<Integer> nl = new LinkedList<Integer>(l);
        if (!nl.isEmpty()) {
            nl.add(nl.remove(nl.size() - 1).intValue() + 1);
        }
        return nl;
    }

    /**
     * @param <T> return type
     * @param <E> input type
     * @param l input list
     */
    public static <T, E> List<T> cast(List<E> l) {
        List<T> ret = new ArrayList<T>();
        for (Object object : l) {
            ret.add((T) object);
        }
        return ret;
    }

    public static <T> T[] append(T[] a, T... b) {
        T[] ret = (T[]) new Object[a.length + b.length];
        for (int i = 0; i < a.length; i++) {
            ret[i] = a[i];
        }
        for (int i = 0; i < b.length; i++) {
            ret[i + a.length] = b[i];
        }
        return ret;
    }

    // Returns the index of the last element lower than "el".
    // NOTE: it is assumed the array has been sorted and is non-decreasing.
    public static <T extends Number> int minIndex(List<T> a, T el) {

        if (el.getClass().equals(BigDecimal.class)) {
            for (int i=0; i < a.size(); i++) {
                if (((BigDecimal)a.get(i)).compareTo((BigDecimal)el) >= 0) {
                    return i;
                }
            }
        } else {
            for (int i=0; i < a.size(); i++) {
                if (a.get(i).doubleValue() >= el.doubleValue()) {
                    return i;
                }
            }
        }

        // "el" was outside bounds
        return a.size()-1;
    }


    /**
     * Returns the relative orders of the element in a numeric list.
     * @param <T>
     * @param list
     * @return
     */
    public static <T extends Number> List<Integer> relativeOrders(List<T> list) {
        List<Integer> relativeOrders = new ArrayList<Integer>(list.size());

        // sort the input list
        List<T> sortedList = new ArrayList<T>(list.size());
        sortedList.addAll(list);
        Collections.sort(sortedList, new NumericComparator());

        for (T el : list) {
            relativeOrders.add(sortedList.indexOf(el));
        }

        return relativeOrders;
    }

    /**
     * Print all elements in the list, separated by the specified Field Separator.
     * @param <T>
     * @param l    The list
     * @param FS   The field separator (e.g. comma for CSV outputs)
     * @return Readable listing of all elements in the list. Example: printList({a,b,c}, "-") --> "a-b-c"
     */
    public static <T> String printList(List<T> l, String FS) {
        String out = "";
        StringBuilder sb = new StringBuilder(out);
        if (l.size()>0) {
            boolean isBigDecimal = l.get(0).getClass().equals(BigDecimal.class);
            for (int i=0; i<l.size()-1; i++) {
                if (isBigDecimal) {
                    sb.append(BigDecimalUtil.stripDecimalZeros((BigDecimal)l.get(i)));
                } else {
                    sb.append(l.get(i));
                }
                sb.append(FS);
            }
            if (isBigDecimal) {
                    sb.append(BigDecimalUtil.stripDecimalZeros((BigDecimal)l.get(l.size()-1)));
                } else {
                    sb.append(l.get(l.size()-1));
                }
        }
        return sb.toString();
    }

    /**
     * Generic comparator for numbers.
     * @param <T>
     */
    private static class NumericComparator<T extends Number> implements Comparator<T> {

        @Override
        public int compare(T number1, T number2) {
            int out = 0; // n1=n2

            if (number1.getClass().equals(BigDecimal.class)) {
                out = ((BigDecimal)number1).compareTo((BigDecimal)number2);
            } else {
                if (number1.doubleValue() > number2.doubleValue()) {
                    out = 1;
                } else if (number1.doubleValue() < number2.doubleValue()) {
                    out = -1;
                }
            }

            return out;
        }
    }
}
