package com.aoindustries.aoserv.client;

/*
 * Copyright 2002-2006 by AO Industries, Inc.,
 * 2200 Dogwood Ct N, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.profiler.*;
import com.aoindustries.util.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Compares columns.
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class SQLComparator implements Comparator {

    private final AOServConnector connector;
    private final SQLExpression[] exprs;
    private final boolean[] sortOrders;

    public SQLComparator(
        AOServConnector connector,
        SQLExpression[] exprs,
        boolean[] sortOrders
    ) {
        this.connector=connector;
        this.exprs=exprs;
        this.sortOrders=sortOrders;
    }

    public int compare(Object O1, Object O2) {
        if(O1 instanceof AOServObject) {
            AOServObject AO1=(AOServObject)O1;
            if(O2 instanceof AOServObject) {
                AOServObject AO2=(AOServObject)O2;
                return AO1.compareTo(connector, AO2, exprs, sortOrders);
            } else if(O2 instanceof Object[]) {
                return AO1.compareTo(connector, (Object[])O2, exprs, sortOrders);
            } else if(O2 instanceof Comparable) {
                return AO1.compareTo(connector, (Comparable)O2, exprs, sortOrders);
            } else throw new IllegalArgumentException("O2 must be either AOServObject, Object[], or Comparable");
        } else if(O1 instanceof Object[]) {
            Object[] OA1=(Object[])O1;
            if(O2 instanceof AOServObject) {
                AOServObject AO2=(AOServObject)O2;
                return -AO2.compareTo(connector, OA1, exprs, sortOrders);
            } else if(O2 instanceof Object[]) {
                return compare(OA1, (Object[])O2);
            } else if(O2 instanceof Comparable) {
                throw new IllegalArgumentException("Comparing of Object[] and Comparable not supported.");
            } else throw new IllegalArgumentException("O2 must be either AOServObject, Object[], or Comparable");
        } else if(O1 instanceof Comparable) {
            Comparable C1=(Comparable)O1;
            if(O2 instanceof AOServObject) {
                AOServObject AO2=(AOServObject)O2;
                return -AO2.compareTo(connector, C1, exprs, sortOrders);
            } else if(O2 instanceof Object[]) {
                throw new IllegalArgumentException("Comparing of Comparable and Object[] not supported.");
            } else if(O2 instanceof Comparable) {
                return C1.compareTo(O2);
            } else throw new IllegalArgumentException("O2 must be either AOServObject or Comparable");
        } else throw new IllegalArgumentException("O1 must be either AOServObject or Comparable");
    }

    public int compare(Object[] OA1, Object[] OA2) {
        int OA1Len=OA1.length;
        int OA2Len=OA2.length;
        if(OA1Len!=OA2Len) throw new IllegalArgumentException("Mismatched array lengths when comparing two Object[]s: OA1.length="+OA1Len+", OA2.length="+OA2Len);
        for(int c=0;c<OA1Len;c++) {
            int diff=compare(OA1[c], OA2[c]);
            if(diff!=0) return diff;
        }
        return 0;
    }
}