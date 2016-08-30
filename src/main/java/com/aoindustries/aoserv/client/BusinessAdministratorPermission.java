/*
 * Copyright 2007-2009, 2016 by AO Industries, Inc.,
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
 * Associates a permission with a business administrator.
 *
 * @author  AO Industries, Inc.
 */
final public class BusinessAdministratorPermission extends CachedObjectIntegerKey<BusinessAdministratorPermission> {

	static final int
		COLUMN_PKEY=0,
		COLUMN_USERNAME=1
	;
	static final String COLUMN_USERNAME_name = "username";
	static final String COLUMN_PERMISSION_name = "permission";

	String username;
	String permission;

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_PKEY: return pkey;
			case COLUMN_USERNAME: return username;
			case 2: return permission;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public BusinessAdministrator getBusinessAdministrator() throws SQLException, IOException {
		BusinessAdministrator ba = table.connector.getBusinessAdministrators().get(username);
		if(ba==null) throw new SQLException("Unable to find BusinessAdministrator: "+username);
		return ba;
	}

	public AOServPermission getAOServPermission() throws SQLException, IOException {
		AOServPermission ap = table.connector.getAoservPermissions().get(permission);
		if(ap==null) throw new SQLException("Unable to find AOServPermission: "+permission);
		return ap;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.BUSINESS_ADMINISTRATOR_PERMISSIONS;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey=result.getInt(1);
		username=result.getString(2);
		permission=result.getString(3);
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey=in.readCompressedInt();
		username=in.readUTF().intern();
		permission=in.readUTF().intern();
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeUTF(username);
		out.writeUTF(permission);
	}
}