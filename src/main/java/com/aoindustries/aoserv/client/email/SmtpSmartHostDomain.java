/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2009-2013, 2016, 2017, 2018, 2019, 2021  AO Industries, Inc.
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
package com.aoindustries.aoserv.client.email;

import com.aoapps.hodgepodge.io.stream.StreamableInput;
import com.aoapps.hodgepodge.io.stream.StreamableOutput;
import com.aoapps.lang.validation.ValidationException;
import com.aoapps.net.DomainName;
import com.aoindustries.aoserv.client.CachedObjectIntegerKey;
import com.aoindustries.aoserv.client.schema.AoservProtocol;
import com.aoindustries.aoserv.client.schema.Table;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides non-default per-domain smart host limits.
 *
 * @author  AO Industries, Inc.
 */
final public class SmtpSmartHostDomain extends CachedObjectIntegerKey<SmtpSmartHostDomain> {

	static final int
		COLUMN_PKEY = 0,
		COLUMN_SMART_HOST = 1
	;
	static final String COLUMN_SMART_HOST_name = "smart_host";
	static final String COLUMN_DOMAIN_name = "domain";

	private int smart_host;
	private DomainName domain;
	private int domain_out_burst;
	private float domain_out_rate;

	@Override
	protected Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_PKEY: return pkey;
			case COLUMN_SMART_HOST: return smart_host;
			case 2: return domain;
			case 3: return domain_out_burst==-1 ? null : domain_out_burst;
			case 4: return Float.isNaN(domain_out_rate) ? null : domain_out_rate;
			default: throw new IllegalArgumentException("Invalid index: " + i);
		}
	}

	public SmtpSmartHost getEmailSmtpSmartHost() throws SQLException, IOException {
		SmtpSmartHost obj = table.getConnector().getEmail().getSmtpSmartHost().get(smart_host);
		if(obj==null) throw new SQLException("Unable to find EmailSmtpSmartHost: "+smart_host);
		return obj;
	}

	public DomainName getDomain() {
		return domain;
	}

	/**
	 * Gets the domain-specific outbound burst limit for emails, the number of emails that may be sent before limiting occurs.
	 * A value of <code>-1</code> indicates unlimited.
	 */
	public int getDomainOutBurst() {
		return domain_out_burst;
	}

	/**
	 * Gets the domain-specific outbound sustained email rate in emails/second.
	 * A value of <code>Float.NaN</code> indicates unlimited.
	 */
	public float getDomainOutRate() {
		return domain_out_rate;
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.EMAIL_SMTP_SMART_HOST_DOMAINS;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		try {
			int pos = 1;
			pkey = result.getInt(pos++);
			smart_host = result.getInt(pos++);
			domain = DomainName.valueOf(result.getString(pos++));
			domain_out_burst=result.getInt(pos++);
			if(result.wasNull()) domain_out_burst = -1;
			domain_out_rate=result.getFloat(pos++);
			if(result.wasNull()) domain_out_rate = Float.NaN;
		} catch(ValidationException e) {
			throw new SQLException(e);
		}
	}

	@Override
	public void read(StreamableInput in, AoservProtocol.Version protocolVersion) throws IOException {
		try {
			pkey = in.readCompressedInt();
			smart_host = in.readCompressedInt();
			domain = DomainName.valueOf(in.readUTF());
			domain_out_burst=in.readCompressedInt();
			domain_out_rate=in.readFloat();
		} catch(ValidationException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void write(StreamableOutput out, AoservProtocol.Version protocolVersion) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeCompressedInt(smart_host);
		out.writeCompressedInt(domain_out_burst);
		out.writeFloat(domain_out_rate);
	}
}
