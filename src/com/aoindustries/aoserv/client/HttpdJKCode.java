package com.aoindustries.aoserv.client;

/*
 * Copyright 2000-2006 by AO Industries, Inc.,
 * 2200 Dogwood Ct N, Mobile, Alabama, 36693, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.io.*;
import com.aoindustries.util.*;
import java.io.*;
import java.sql.*;

/**
 * When using Apache's <code>mod_jk</code>, each connection to a servlet
 * container is assigned a unique two-character identifier.  This
 * identifier is represented by an <code>HttpdJKCode</code>.
 *
 * @see  HttpdWorker
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class HttpdJKCode extends GlobalObjectStringKey<HttpdJKCode> {

    static final int COLUMN_CODE=0;

    public String getCode() {
	return pkey;
    }

    public Object getColumn(int i) {
	if(i==COLUMN_CODE) return pkey;
	throw new IllegalArgumentException("Invalid index: "+i);
    }

    protected int getTableIDImpl() {
	return SchemaTable.HTTPD_JK_CODES;
    }

    void initImpl(ResultSet result) throws SQLException {
	pkey=result.getString(1);
    }

    public void read(CompressedDataInputStream in) throws IOException {
	pkey=in.readUTF();
    }

    public void write(CompressedDataOutputStream out, String version) throws IOException {
	out.writeUTF(pkey);
    }
}