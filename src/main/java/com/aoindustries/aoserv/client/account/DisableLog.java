/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2002-2013, 2016, 2017, 2018  AO Industries, Inc.
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
package com.aoindustries.aoserv.client.account;

import com.aoindustries.aoserv.client.CachedObjectIntegerKey;
import com.aoindustries.aoserv.client.schema.AoservProtocol;
import com.aoindustries.aoserv.client.schema.Table;
import com.aoindustries.aoserv.client.validator.AccountingCode;
import com.aoindustries.aoserv.client.validator.UserId;
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import com.aoindustries.validation.ValidationException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * When a resource or resources are disabled, the reason and time is logged.
 *
 * @author  AO Industries, Inc.
 */
final public class DisableLog extends CachedObjectIntegerKey<DisableLog> {

	static final int COLUMN_PKEY=0;
	static final String COLUMN_TIME_name = "time";
	static final String COLUMN_ACCOUNTING_name = "accounting";
	static final String COLUMN_PKEY_name = "pkey";

	private long time;
	private AccountingCode accounting;
	private UserId disabled_by;
	private String disable_reason;

	/**
	 * Determines if the current <code>AOServConnector</code> can enable
	 * things disabled by this <code>DisableLog</code>.
	 */
	public boolean canEnable() throws SQLException, IOException {
		Administrator disabledBy=getDisabledBy();
		return disabledBy!=null && table
			.getConnector()
			.getThisBusinessAdministrator()
			.getUsername()
			.getPackage()
			.getBusiness()
			.isBusinessOrParentOf(
				disabledBy
				.getUsername()
				.getPackage()
				.getBusiness()
			)
		;
	}

	@Override
	protected Object getColumnImpl(int i) {
		if(i==COLUMN_PKEY) return pkey;
		if(i==1) return getTime();
		if(i==2) return accounting;
		if(i==3) return disabled_by;
		if(i==4) return disable_reason;
		throw new IllegalArgumentException("Invalid index: "+i);
	}

	public Account getBusiness() throws SQLException, IOException {
		Account bu=table.getConnector().getAccount().getAccount().get(accounting);
		if(bu==null) throw new SQLException("Unable to find Business: "+accounting);
		return bu;
	}

	public Timestamp getTime() {
		return new Timestamp(time);
	}

	public UserId getDisabledByUsername() {
		return disabled_by;
	}

	public Administrator getDisabledBy() throws IOException, SQLException {
		// May be filtered
		return table.getConnector().getAccount().getAdministrator().get(disabled_by);
	}

	public String getDisableReason() {
		return disable_reason;
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.DISABLE_LOG;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		try {
			pkey=result.getInt(1);
			time=result.getTimestamp(2).getTime();
			accounting=AccountingCode.valueOf(result.getString(3));
			disabled_by = UserId.valueOf(result.getString(4));
			disable_reason=result.getString(5);
		} catch(ValidationException e) {
			throw new SQLException(e);
		}
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		try {
			pkey=in.readCompressedInt();
			time=in.readLong();
			accounting=AccountingCode.valueOf(in.readUTF()).intern();
			disabled_by = UserId.valueOf(in.readUTF()).intern();
			disable_reason=in.readNullUTF();
		} catch(ValidationException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void write(CompressedDataOutputStream out, AoservProtocol.Version protocolVersion) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeLong(time);
		out.writeUTF(accounting.toString());
		out.writeUTF(disabled_by.toString());
		out.writeNullUTF(disable_reason);
	}
}
