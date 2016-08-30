/*
 * Copyright 2002-2009, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A <code>PostgresVersion</code> flags which <code>TechnologyVersion</code>s
 * are a version of PostgreSQL.
 *
 * @see  PostgresServer
 * @see  TechnologyVersion
 *
 * @author  AO Industries, Inc.
 */
final public class PostgresVersion extends GlobalObjectIntegerKey<PostgresVersion> {

	static final int COLUMN_VERSION = 0;
	static final String COLUMN_MINOR_VERSION_name = "minor_version";
	static final String COLUMN_VERSION_name = "version";

	private String minorVersion;
	private int postgisVersion;

	public static final String TECHNOLOGY_NAME = "postgresql";

	public static final String
		VERSION_7_1 = "7.1",
		VERSION_7_2 = "7.2",
		VERSION_7_3 = "7.3",
		VERSION_8_0 = "8.0",
		VERSION_8_1 = "8.1",
		VERSION_8_3 = "8.3",
		VERSION_9_2 = "9.2",
		VERSION_9_4 = "9.4"
	;

	/**
	 * Gets the versions of PostgreSQL in order of
	 * preference.  Index <code>0</code> is the most
	 * preferred.
	 */
	public static String[] getPreferredMinorVersions() {
		return new String[] {
			VERSION_9_4,
			VERSION_9_2,
			VERSION_8_3,
			VERSION_8_1,
			VERSION_8_0,
			VERSION_7_3,
			VERSION_7_2,
			VERSION_7_1
		};
	}

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_VERSION: return pkey;
			case 1: return minorVersion;
			case 2: return postgisVersion == -1 ? null : postgisVersion;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public String getMinorVersion() {
		return minorVersion;
	}

	/**
	 * Gets the PostGIS version of <code>null</code> if not supported by this PostgreSQL version....
	 */
	public TechnologyVersion getPostgisVersion(AOServConnector connector) throws SQLException, IOException {
		if(postgisVersion == -1) return null;
		TechnologyVersion tv = connector.getTechnologyVersions().get(postgisVersion);
		if(tv == null) throw new SQLException("Unable to find TechnologyVersion: " + postgisVersion);
		if(
			tv.getOperatingSystemVersion(connector).getPkey()
			!= getTechnologyVersion(connector).getOperatingSystemVersion(connector).getPkey()
		) {
			throw new SQLException("postgresql/postgis version mismatch on PostgresVersion: #" + pkey);
		}
		return tv;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.POSTGRES_VERSIONS;
	}

	public List<PostgresEncoding> getPostgresEncodings(AOServConnector connector) throws IOException, SQLException {
		return connector.getPostgresEncodings().getPostgresEncodings(this);
	}

	public PostgresEncoding getPostgresEncoding(AOServConnector connector, String encoding) throws IOException, SQLException {
		return connector.getPostgresEncodings().getPostgresEncoding(this, encoding);
	}

	public TechnologyVersion getTechnologyVersion(AOServConnector connector) throws SQLException, IOException {
		TechnologyVersion obj = connector.getTechnologyVersions().get(pkey);
		if(obj == null) throw new SQLException("Unable to find TechnologyVersion: "+pkey);
		return obj;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey = result.getInt(1);
		minorVersion = result.getString(2);
		postgisVersion = result.getInt(3);
		if(result.wasNull()) postgisVersion = -1;
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey = in.readCompressedInt();
		minorVersion = in.readUTF().intern();
		postgisVersion = in.readCompressedInt();
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version protocolVersion) throws IOException {
		out.writeCompressedInt(pkey);
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_0_A_109) <= 0) out.writeCompressedInt(5432);
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_0_A_121) >= 0) out.writeUTF(minorVersion);
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_27) >= 0) out.writeCompressedInt(postgisVersion);
	}
}