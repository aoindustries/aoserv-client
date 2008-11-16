package com.aoindustries.aoserv.client;

/*
 * Copyright 2002-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import java.util.List;

/**
 * An expression in used in select statements and internal sorting.
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
abstract public class SQLExpression {

    abstract public String getColumnName();

    abstract public Object getValue(AOServConnector conn, AOServObject obj);
    
    abstract public SchemaType getType();

    /**
     * Gets all of the tables referenced by this expression.
     */
    public void getReferencedTables(AOServConnector conn, List<SchemaTable> tables) {
    }
}
