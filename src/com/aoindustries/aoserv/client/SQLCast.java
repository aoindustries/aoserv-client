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
import java.util.List;

/**
 * Casts one result type to another.
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class SQLCast extends SQLExpression {

    private SQLExpression expression;
    private SchemaType castToType;

    public SQLCast(SQLExpression expression, SchemaType castToType) {
        this.expression=expression;
        this.castToType=castToType;
    }

    public String getColumnName() {
        return castToType.getType();
    }

    public Object getValue(AOServConnector conn, AOServObject obj) {
        return expression.getType().cast(conn, expression.getValue(conn, obj), castToType);
    }

    public SchemaType getType() {
        return castToType;
    }

    public void getReferencedTables(AOServConnector conn, List<SchemaTable> tables) {
        expression.getReferencedTables(conn, tables);
    }
}