/*
 * Copyright 2000-2013, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Each <code>LinuxGroup</code>'s use is limited by which
 * <code>LinuxGroupType</code> is associated with it.  Typically,
 * but not required, a <code>LinuxAccount</code> will have a
 * <code>LinuxAccountType</code> that matchs its primary
 * <code>LinuxGroup</code>'s <code>LinuxGroupType</code>.
 *
 * @see  LinuxGroup
 * @see  LinuxAccountType
 * @see  LinuxAccount
 * @see  LinuxGroupAccount
 *
 * @author  AO Industries, Inc.
 */
final public class LinuxGroupType extends GlobalObjectStringKey<LinuxGroupType> {

	static final int COLUMN_NAME=0;
	static final String COLUMN_DESCRIPTION_name = "description";

	private String description;

	/**
	 * The available group types.
	 */
	public static final String
		USER="user",
		EMAIL="email",
		FTPONLY="ftponly",
		SYSTEM="system",
		BACKUP="backup",
		APPLICATION="application"
	;

	@Override
	Object getColumnImpl(int i) {
		if(i==COLUMN_NAME) return pkey;
		if(i==1) return description;
		throw new IllegalArgumentException("Invalid index: "+i);
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return pkey;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.LINUX_GROUP_TYPES;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey = result.getString(1);
		description = result.getString(2);
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey=in.readUTF().intern();
		description=in.readUTF();
	}

	@Override
	String toStringImpl() {
		return description;
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
		out.writeUTF(pkey);
		out.writeUTF(description);
	}
}