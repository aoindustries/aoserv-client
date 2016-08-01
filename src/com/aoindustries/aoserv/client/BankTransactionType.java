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
 * For AO Industries use only.
 *
 * @author  AO Industries, Inc.
 */
final public class BankTransactionType extends CachedObjectStringKey<BankTransactionType> {

	static final int COLUMN_NAME=0;
	static final String COLUMN_DISPLAY_name = "display";

	private String
		display,
		description
	;

	private boolean isNegative;

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_NAME: return pkey;
			case 1: return display;
			case 2: return description;
			case 3: return isNegative;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public String getDescription() {
		return description;
	}

	public String getDisplay() {
		return display;
	}

	public String getName() {
		return pkey;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.BANK_TRANSACTION_TYPES;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey = result.getString(1);
		display = result.getString(2);
		description = result.getString(3);
		isNegative = result.getBoolean(4);
	}

	public boolean isNegative() {
		return isNegative;
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey=in.readUTF().intern();
		display=in.readUTF();
		description=in.readUTF();
		isNegative=in.readBoolean();
	}

	@Override
	String toStringImpl() {
		return display;
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
		out.writeUTF(pkey);
		out.writeUTF(display);
		out.writeUTF(description);
		out.writeBoolean(isNegative);
	}
}
