/*
 * aoserv-client - Java client for the AOServ platform.
 * Copyright (C) 2005-2013, 2016  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aoserv-client.
 *
 * aoserv-client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aoserv-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aoserv-client.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.aoserv.client;

import static com.aoindustries.aoserv.client.ApplicationResources.accessor;
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A <code>TransactionType</code> is one type that may be used
 * in a <code>Transaction</code>.  Each <code>PackageDefinition</code>
 * and <code>PackageDefinitionLimit</code> defines which type will be
 * used for billing.
 *
 * @see  PackageDefinition
 * @see  PackageDefinitionLimit
 * @see  Transaction
 *
 * @author  AO Industries, Inc.
 */
public final class TransactionType extends GlobalObjectStringKey<TransactionType> {

	static final int COLUMN_NAME=0;
	static final String COLUMN_NAME_name = "name";

	public static final String
		HTTPD="httpd",
		PAYMENT="payment",
		VIRTUAL="virtual"
	;

	/**
	 * If <code>true</code> this <code>TransactionType</code> represents a credit to
	 * an account and will be listed in payments received reports.
	 */
	private boolean isCredit;

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_NAME: return pkey;
			case 1: return isCredit;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public String getDescription() {
		return accessor.getMessage("TransactionType."+pkey+".description");
	}

	/**
	 * Gets the unique name of this transaction type.
	 */
	public String getName() {
		return pkey;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.TRANSACTION_TYPES;
	}

	public String getUnit() {
		return accessor.getMessage("TransactionType."+pkey+".unit");
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey = result.getString(1);
		isCredit = result.getBoolean(2);
	}

	public boolean isCredit() {
		return isCredit;
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey = in.readUTF().intern();
		isCredit = in.readBoolean();
	}

	@Override
	String toStringImpl() {
		return accessor.getMessage("TransactionType."+pkey+".toString");
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
		out.writeUTF(pkey);
		if(version.compareTo(AOServProtocol.Version.VERSION_1_60)<=0) {
			out.writeUTF(toStringImpl()); // display
			out.writeUTF(getDescription()); // description
			out.writeUTF(getUnit()); // unit
		}
		out.writeBoolean(isCredit);
	}
}
