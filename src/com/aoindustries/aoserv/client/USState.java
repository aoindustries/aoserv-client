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
 * A <code>USState</code> represents State of the
 * United States.  This data will eventually merge
 * with <code>CountryCode</code>s to become a master
 * list of all states/providences and countries.
 *
 * @see  CountryCode
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class USState extends GlobalObjectStringKey<USState> {

    static final int COLUMN_CODE=0;
    static final String COLUMN_NAME_name = "name";

    private String name;

    public String getCode() {
	return pkey;
    }

    public Object getColumn(int i) {
	if(i==COLUMN_CODE) return pkey;
	if(i==1) return name;
	throw new IllegalArgumentException("Invalid index: "+i);
    }

    public String getName() {
	return name;
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.US_STATES;
    }

    void initImpl(ResultSet result) throws SQLException {
	pkey = result.getString(1);
	name = result.getString(2);
    }

    public void read(CompressedDataInputStream in) throws IOException {
	pkey=in.readUTF().intern();
	name=in.readUTF();
    }

    String toStringImpl() {
	return name;
    }

    public void write(CompressedDataOutputStream out, String version) throws IOException {
	out.writeUTF(pkey);
	out.writeUTF(name);
    }
}