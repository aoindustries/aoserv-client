package com.aoindustries.aoserv.client;

/*
 * Copyright 2000-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.*;
import com.aoindustries.util.StringUtility;
import java.io.*;
import java.sql.*;

/**
 * For AO Industries use only.
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class ExpenseCategory extends CachedObjectStringKey<ExpenseCategory> {

    static final int COLUMN_EXPENSE_CODE=0;
    static final String COLUMN_EXPENSE_CODE_name = "expense_code";

    public Object getColumn(int i) {
	if(i==COLUMN_EXPENSE_CODE) return pkey;
	throw new IllegalArgumentException("Invalid index: "+i);
    }

    public String getExpenseCode() {
	return pkey;
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.EXPENSE_CATEGORIES;
    }

    void initImpl(ResultSet result) throws SQLException {
	pkey=result.getString(1);
    }

    public void read(CompressedDataInputStream in) throws IOException {
	pkey=in.readUTF().intern();
    }

    public void write(CompressedDataOutputStream out, String version) throws IOException {
	out.writeUTF(pkey);
    }
}