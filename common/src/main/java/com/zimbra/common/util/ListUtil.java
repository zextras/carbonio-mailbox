// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ListUtil {

    /**
     * Returns {@code true} if the collection is {@code null} or empty.
     */
    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    /**
     * Returns either {@code c} or an empty collection if {@code c} is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> nullToEmpty(Collection<T> c) {
        return (c == null ? (Collection<T>) Collections.emptyList() : c);
    }

    /**
     * Returns the first element, or {@code null} if the collection is
     * {@code null} or empty.
     */
    public static <T> T getFirstElement(Iterable<T> iterable) {
        return iterable == null ? null : Iterables.getFirst(iterable, null);
    }

    /**
     * Given two unsorted lists, return TRUE if they contain exactly the same things
     * (regardless of order)
     *
     * @param <T>
     * @param lhs
     * @param rhs
     * @return
     */
    public static <T> boolean listsEqual(List<T> lhs, List<T> rhs) {
        if (lhs.size() != rhs.size())
            return false;

        HashSet<T> set = new HashSet<T>();
        set.addAll(lhs);
        for (T t: rhs) {
            if (!set.remove(t))
                return false;
        }
        return (set.size() == 0);
    }

    /**
     * Merge two sorted Lists
     *
     * I could have used the Java Set collection here...unfortunately the only ordered set
     * is their TreeSet, and set union/subtraction therefore take NlogN with a pretty big
     * constant overhead.
     *
     * @param dest
     * @param src
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> void mergeSortedLists(
            List<T> dest, List<T>[] src, boolean removeDuplicates)
    {
        int numSrc = 0;
        for (int i = 0; i < src.length; i++) {
            if (src[i] != null) {
                numSrc++;
            }
        }

        if (numSrc == 1) {
            for (int i = 0; i < src.length; i++) {
                if (src[i] != null) {
                    dest.addAll(src[i]);
                    return;
                }
            }
        }

        Iterator<T> iter[] = new Iterator[numSrc];
        int iterOffset = 0;
        for (int i = 0; i < src.length; i++) {
            if (src[i] != null) {
                iter[iterOffset++] = src[i].iterator();
            }
        }


        int numItersActive = src.length;

        // holds the next values of each iterator
        T nextValue[] = (T[]) new Comparable[src.length];

        T lowestValue = null;
        int lowestValueOffset = -1;

        T lastAdded = null;

        // prime the pump
        for (int i = 0; i < iter.length; i++) {
            if (iter[i].hasNext()) {
                nextValue[i] = iter[i].next();

                if (lowestValue == null || (lowestValue.compareTo(nextValue[i]) > 0)) {
                    lowestValue = nextValue[i];
                    lowestValueOffset = i;
                }
            } else {
                iter[i] = null;
                numItersActive--;
            }
        }

        while (numItersActive > 0) {
            // grab lowest value from the src list, put it on the return list
            if ((!removeDuplicates) || (lastAdded == null)) {
                dest.add(nextValue[lowestValueOffset]);
                lastAdded = nextValue[lowestValueOffset];
                nextValue[lowestValueOffset] = null;
            } else {
                if (!lastAdded.equals(nextValue[lowestValueOffset])) {
                    dest.add(nextValue[lowestValueOffset]);
                    lastAdded = nextValue[lowestValueOffset];
                }
                nextValue[lowestValueOffset] = null;
            }

            // iterate the proper src list
            if (iter[lowestValueOffset].hasNext()) {
                nextValue[lowestValueOffset] = iter[lowestValueOffset].next();
            } else {
                iter[lowestValueOffset] = null;
                numItersActive--;
            }

            // find the next-lowest-value
            lowestValue = null;
            lowestValueOffset = -1;
            for (int i = 0; i < src.length; i++) {
                if (lowestValue == null ||
                        ((nextValue[i] != null) && (lowestValue.compareTo(nextValue[i]) > 0))) {
                    lowestValue = nextValue[i];
                    lowestValueOffset = i;
                }
            }
        }
    }

    /**
     * @param a
     * @param b
     *
     * Subtract two sorted lists
     *
     * returns (a-b);
     */
    public static <T> List<T> subtractSortedLists(
            List<T> a, List<T> b, Comparator<T> comparator) {
        List<T> result = new ArrayList<T>(a.size());

        Iterator<T> aIter = a.iterator();
        Iterator<T> bIter = b.iterator();

        T aVal = null;
        if (aIter.hasNext()) {
            aVal = aIter.next();
        }
        T bVal = null;
        if (bIter.hasNext()) {
            bVal = bIter.next();
        }

        while (aVal != null) {
            if (bVal == null) {
                result.add(aVal);
            } else {
                int comp = comparator.compare(aVal, bVal);
                if (comp < 0) {
                    // a < b
                    result.add(aVal);
                } else if (comp > 0) {
                    // a > b
                    if (bIter.hasNext()) {
                        bVal = bIter.next();
                    } else {
                        bVal = null;
                    }
                    continue; // DON'T move A fwd...
                } else {
                    // a==b, so skip A!
                }
            }

            if (aIter.hasNext()) {
                aVal = aIter.next();
            } else {
                aVal = null;
            }
        }

        return result;
    }

    /**
     * Splits a <code>Collection</code> into <i>n</i> <code>List</code>s.
     * Lists <i>1</i> through <i>n-1</i> are of size <code>listSize</code>.  List <i>n</i>
     * contains the remaining elements.
     *
     * @return the split lists.  Returns <tt>null</tt> if <tt>c</tt> is <tt>null</tt>
     * or an empty <tt>List</tt> if <tt>c</tt> is empty.
     */
    public static <E> List<List<E>> split(Collection<E> c, int listSize) {
        if (c == null) {
            return null;
        }
        List<List<E>> splitLists = new ArrayList<List<E>>();
        if (c.size() == 0) {
            return splitLists;
        }

        List<E> curList = new ArrayList<E>(listSize);
        int i = 0;

        for (E item : c) {
            if (i == listSize) {
                splitLists.add(curList);
                curList = new ArrayList<E>(listSize);
                i = 0;
            }
            curList.add(item);
            i++;
        }
        splitLists.add(curList);

        return splitLists;
    }

    private static class Test {

        @SuppressWarnings("unchecked")
        static void doit() {
            List<Integer>[] in = new List[5];

            int i = 0;

            in[i] = new ArrayList<Integer>();
            in[i].add(Integer.valueOf(1));
            in[i].add(Integer.valueOf(3));
            in[i].add(Integer.valueOf(5));
            in[i].add(Integer.valueOf(7));
            in[i].add(Integer.valueOf(9));

            i = 1;
            in[i] = new ArrayList<Integer>();
            in[i].add(Integer.valueOf(1));
            in[i].add(Integer.valueOf(7));
            in[i].add(Integer.valueOf(12));
            in[i].add(Integer.valueOf(13));
            in[i].add(Integer.valueOf(13));

            i = 2;
            in[i] = new ArrayList<Integer>();
            in[i].add(Integer.valueOf(1));
            in[i].add(Integer.valueOf(2));
            in[i].add(Integer.valueOf(3));
            in[i].add(Integer.valueOf(4));
            in[i].add(Integer.valueOf(5));

            i = 3;
            in[i] = new ArrayList<Integer>();
            in[i].add(Integer.valueOf(5));
            in[i].add(Integer.valueOf(6));
            in[i].add(Integer.valueOf(7));
            in[i].add(Integer.valueOf(8));
            in[i].add(Integer.valueOf(9));

            i = 4;
            in[i] = new ArrayList<Integer>();
            in[i].add(Integer.valueOf(100));
            in[i].add(Integer.valueOf(101));
            in[i].add(Integer.valueOf(102));
            in[i].add(Integer.valueOf(103));
            in[i].add(Integer.valueOf(104));

            List<Integer> test;

            test = new ArrayList<Integer>();
            mergeSortedLists(test, in, false);
            System.out.print("DUPES_NOT_REMOVED: ");
            for (Integer cur : test) {
                System.out.print(cur+", ");
            }
            System.out.println();

            test = new ArrayList<Integer>();
            mergeSortedLists(test, in, true);
            System.out.print("DUPES_REMOVED: ");
            for (Integer cur : test) {
                System.out.print(cur+", ");
            }
            System.out.println();

            test = subtractSortedLists(in[2], in[0], new IntegerComparator());
            System.out.print("(1,2,3,4,5) - (1,3,5,7,9): ");
            for (Iterator<Integer> iter = test.iterator(); iter.hasNext();) {
                Integer cur = iter.next();
                System.out.print(cur+", ");
            }
            System.out.println();

            test = subtractSortedLists(in[0], in[1], new IntegerComparator());
            System.out.print("(1,3,5,7,9) - (1,7,12,13,13): ");
            for (Iterator<Integer> iter = test.iterator(); iter.hasNext();) {
                Integer cur = iter.next();
                System.out.print(cur+", ");
            }
            System.out.println();

        }

        private static class IntegerComparator implements Comparator<Integer> {
            IntegerComparator()  {}

            @Override public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        }
    }

    /**
     * Returns a new {@code List} whose elements are the transformed versions of the data
     * stored in {@code fromIterable}.
     *
     * @param fromIterable source data
     * @param function transformation function
     */
    public static <F, T> List<T> newArrayList(Iterable<F> fromIterable, Function<? super F, ? extends T> function) {
        return Lists.newArrayList(Iterables.transform(fromIterable, function));
    }

    /**
     * Filters the given {@code Iterable} and returns a new {@code List} whose
     * elements match the {@code Predicate}.
     */
    public static <T> List<T> newArrayList(Iterable<T> unfiltered, Predicate<T> predicate) {
        return Lists.newArrayList(Iterables.filter(unfiltered, predicate));
    }

    public static void main(String[] args) {
        Test.doit();
    }
}
