/*
 * Copyright 2003-2009, 2016 by AO Industries, Inc.,
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
 * The <code>EmailSmtpRelayType</code> of an <code>EmailSmtpRelay</code>
 * controls the servers response.
 *
 * @see  EmailSmtpRelay
 *
 * @author  AO Industries, Inc.
 */
final public class EmailSmtpRelayType extends GlobalObjectStringKey<EmailSmtpRelayType> {

	static final int COLUMN_NAME=0;
	static final String COLUMN_NAME_name = "name";

	/**
	 * The different relay types.
	 */
	public static final String
		ALLOW="allow",
		ALLOW_RELAY="allow_relay",
		DENY_SPAM="deny_spam",
		DENY="deny"
	;

	private String sendmail_config;
	private String qmail_config;

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_NAME: return pkey;
			case 1: return sendmail_config;
			case 2: return qmail_config;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public String getName() {
		return pkey;
	}

	public String getSendmailConfig() {
		return sendmail_config;
	}

	public String getQmailConfig() {
		return qmail_config;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.EMAIL_SMTP_RELAY_TYPES;
	}

	public String getVerb() throws SQLException {
		if(pkey.equals(ALLOW)) return "allowed regular access";
		if(pkey.equals(ALLOW_RELAY)) return "allowed unauthenticated relay access";
		if(pkey.equals(DENY_SPAM)) return "blocked for sending unsolicited bulk email";
		if(pkey.equals(DENY)) return "blocked";
		throw new SQLException("Unknown value for name: "+pkey);
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey=result.getString(1);
		sendmail_config=result.getString(2);
		qmail_config=result.getString(3);
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey=in.readUTF().intern();
		sendmail_config=in.readUTF();
		qmail_config=in.readUTF();
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
		out.writeUTF(pkey);
		out.writeUTF(sendmail_config);
		out.writeUTF(qmail_config);
	}
}