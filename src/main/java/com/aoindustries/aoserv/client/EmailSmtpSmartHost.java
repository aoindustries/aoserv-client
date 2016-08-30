/*
 * Copyright 2009-2013, 2016 by AO Industries, Inc.,
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
 * Provides SMTP relay services for one or more non-managed servers.
 *
 * @author  AO Industries, Inc.
 */
public final class EmailSmtpSmartHost extends CachedObjectIntegerKey<EmailSmtpSmartHost> {

	static final int COLUMN_NET_BIND=0;
	static final String COLUMN_NET_BIND_name = "net_bind";

	private int total_out_burst;
	private float total_out_rate;
	private int default_domain_out_burst;
	private float default_domain_out_rate;

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_NET_BIND: return pkey;
			case 1: return total_out_burst==-1 ? null : total_out_burst;
			case 2: return Float.isNaN(total_out_rate) ? null : total_out_rate;
			case 3: return default_domain_out_burst==-1 ? null : default_domain_out_burst;
			case 4: return Float.isNaN(default_domain_out_rate) ? null : default_domain_out_rate;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public NetBind getNetBind() throws IOException, SQLException {
		NetBind obj=table.connector.getNetBinds().get(pkey);
		if(obj==null) throw new SQLException("Unable to find NetBind: "+pkey);
		return obj;
	}

	/**
	 * Gets the total smart host outbound burst limit for emails, the number of emails that may be sent before limiting occurs.
	 * A value of <code>-1</code> indicates unlimited.
	 */
	public int getTotalEmailOutBurst() {
		return total_out_burst;
	}

	/**
	 * Gets the total smart host outbound sustained email rate in emails/second.
	 * A value of <code>Float.NaN</code> indicates unlimited.
	 */
	public float getTotalEmailOutRate() {
		return total_out_rate;
	}

	/**
	 * Gets the default per-domain outbound burst limit for emails, the number of emails that may be sent before limiting occurs.
	 * A value of <code>-1</code> indicates unlimited.
	 */
	public int getDefaultDomainOutBurst() {
		return default_domain_out_burst;
	}

	/**
	 * Gets the default per-domain outbound sustained email rate in emails/second.
	 * A value of <code>Float.NaN</code> indicates unlimited.
	 */
	public float getDefaultDomainOutRate() {
		return default_domain_out_rate;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.EMAIL_SMTP_SMART_HOSTS;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		int pos = 1;
		pkey = result.getInt(pos++);
		total_out_burst=result.getInt(pos++);
		if(result.wasNull()) total_out_burst = -1;
		total_out_rate=result.getFloat(pos++);
		if(result.wasNull()) total_out_rate = Float.NaN;
		default_domain_out_burst=result.getInt(pos++);
		if(result.wasNull()) default_domain_out_burst = -1;
		default_domain_out_rate=result.getFloat(pos++);
		if(result.wasNull()) default_domain_out_rate = Float.NaN;
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey = in.readCompressedInt();
		total_out_burst=in.readCompressedInt();
		total_out_rate=in.readFloat();
		default_domain_out_burst=in.readCompressedInt();
		default_domain_out_rate=in.readFloat();
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeCompressedInt(total_out_burst);
		out.writeFloat(total_out_rate);
		out.writeCompressedInt(default_domain_out_burst);
		out.writeFloat(default_domain_out_rate);
	}
}