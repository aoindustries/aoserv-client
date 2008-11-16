package com.aoindustries.aoserv.client;

/*
 * Copyright 2002-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
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

    @Override
    public void getReferencedTables(AOServConnector conn, List<SchemaTable> tables) {
        expression.getReferencedTables(conn, tables);
    }
}