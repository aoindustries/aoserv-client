/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2001-2013, 2016, 2017, 2018, 2019, 2020, 2021  AO Industries, Inc.
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
package com.aoindustries.aoserv.client.web.tomcat;

import com.aoapps.hodgepodge.io.stream.StreamableInput;
import com.aoapps.hodgepodge.io.stream.StreamableOutput;
import com.aoapps.lang.validation.ValidationException;
import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.GlobalObjectIntegerKey;
import com.aoindustries.aoserv.client.distribution.SoftwareVersion;
import com.aoindustries.aoserv.client.linux.PosixPath;
import com.aoindustries.aoserv.client.schema.AoservProtocol;
import com.aoindustries.aoserv.client.schema.Table;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An <code>HttpdTomcatVersion</code> flags which
 * <code>TechnologyVersion</code>s are a version of the Jakarta
 * Tomcat servlet engine.  Multiple versions of the Tomcat servlet
 * engine are supported, but only one version may be configured within
 * each Java virtual machine.
 *
 * @see  Site
 * @see  SoftwareVersion
 *
 * @author  AO Industries, Inc.
 */
final public class Version extends GlobalObjectIntegerKey<Version> {

	static final int COLUMN_VERSION = 0;

	static final String COLUMN_VERSION_name = "version";

	private PosixPath install_dir;
	private boolean requires_mod_jk;

	public static final String TECHNOLOGY_NAME="jakarta-tomcat";

	public static final String
		VERSION_3_1 = "3.1",
		VERSION_3_2_4 = "3.2.4",
		VERSION_4_1_PREFIX = "4.1.",
		VERSION_5_5_PREFIX = "5.5.",
		VERSION_6_0_PREFIX = "6.0.",
		VERSION_7_0_PREFIX = "7.0.",
		VERSION_8_0_PREFIX = "8.0.",
		VERSION_8_5_PREFIX = "8.5.",
		VERSION_9_0_PREFIX = "9.0.",
		VERSION_10_0_PREFIX = "10.0."
	;

	/**
	 * In-place upgrades are supported from Tomcat 4.1 and newer.
	 * In-place downgrades are supported from Tomcat 8.5 and newer.
	 */
	public static boolean canUpgradeFrom(String version) {
		return
			!version.equals(VERSION_3_1)
			&& !version.equals(VERSION_3_2_4);
	}

	/**
	 * In-place upgrades and downgrades are supported to Tomcat 8.5 and newer.
	 */
	public static boolean canUpgradeTo(String version) {
		return
			!version.equals(VERSION_3_1)
			&& !version.equals(VERSION_3_2_4)
			&& !version.startsWith(VERSION_4_1_PREFIX)
			&& !version.startsWith(VERSION_5_5_PREFIX)
			&& !version.startsWith(VERSION_6_0_PREFIX)
			&& !version.startsWith(VERSION_7_0_PREFIX)
			&& !version.startsWith(VERSION_8_0_PREFIX);
	}

	@Override
	protected Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_VERSION: return pkey;
			case 1: return install_dir;
			case 2: return requires_mod_jk;
			default: throw new IllegalArgumentException("Invalid index: " + i);
		}
	}

	public PosixPath getInstallDirectory() {
		return install_dir;
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.HTTPD_TOMCAT_VERSIONS;
	}

	public SoftwareVersion getTechnologyVersion(AOServConnector connector) throws SQLException, IOException {
		SoftwareVersion obj = connector.getDistribution().getSoftwareVersion().get(pkey);
		if(obj == null) throw new SQLException("Unable to find TechnologyVersion: " + pkey);
		return obj;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		try {
			pkey = result.getInt(1);
			install_dir = PosixPath.valueOf(result.getString(2));
			requires_mod_jk = result.getBoolean(3);
		} catch(ValidationException e) {
			throw new SQLException(e);
		}
	}

	/**
	 * @deprecated  Please check all uses of this, because it also returns <code>true</code> for Tomcat 5, which doesn't seem
	 *              to match the method name very well.
	 *
	 * @see  #isTomcat4_1_X(AOServConnector)
	 * @see  #isTomcat5_5_X(AOServConnector)
	 * @see  #isTomcat6_0_X(AOServConnector)
	 */
	@Deprecated
	public boolean isTomcat4(AOServConnector connector) throws SQLException, IOException {
		String version = getTechnologyVersion(connector).getVersion();
		return version.startsWith("4.") || version.startsWith("5.");
	}

	public boolean isTomcat3_1(AOServConnector connector) throws SQLException, IOException {
		String version = getTechnologyVersion(connector).getVersion();
		return version.equals(VERSION_3_1);
	}

	public boolean isTomcat3_2_4(AOServConnector connector) throws SQLException, IOException {
		String version = getTechnologyVersion(connector).getVersion();
		return version.equals(VERSION_3_2_4);
	}

	public boolean isTomcat4_1_X(AOServConnector connector) throws SQLException, IOException {
		String version = getTechnologyVersion(connector).getVersion();
		return version.startsWith(VERSION_4_1_PREFIX);
	}

	public boolean isTomcat5_5_X(AOServConnector connector) throws SQLException, IOException {
		String version = getTechnologyVersion(connector).getVersion();
		return version.startsWith(VERSION_5_5_PREFIX);
	}

	public boolean isTomcat6_0_X(AOServConnector connector) throws SQLException, IOException {
		String version = getTechnologyVersion(connector).getVersion();
		return version.startsWith(VERSION_6_0_PREFIX);
	}

	public boolean isTomcat7_0_X(AOServConnector connector) throws SQLException, IOException {
		String version = getTechnologyVersion(connector).getVersion();
		return version.startsWith(VERSION_7_0_PREFIX);
	}

	public boolean isTomcat8_0_X(AOServConnector connector) throws SQLException, IOException {
		String version = getTechnologyVersion(connector).getVersion();
		return version.startsWith(VERSION_8_0_PREFIX);
	}

	public boolean isTomcat8_5_X(AOServConnector connector) throws SQLException, IOException {
		String version = getTechnologyVersion(connector).getVersion();
		return version.startsWith(VERSION_8_5_PREFIX);
	}

	public boolean isTomcat9_0_X(AOServConnector connector) throws SQLException, IOException {
		String version = getTechnologyVersion(connector).getVersion();
		return version.startsWith(VERSION_9_0_PREFIX);
	}

	public boolean isTomcat10_0_X(AOServConnector connector) throws SQLException, IOException {
		String version = getTechnologyVersion(connector).getVersion();
		return version.startsWith(VERSION_10_0_PREFIX);
	}

	@Override
	public void read(StreamableInput in, AoservProtocol.Version protocolVersion) throws IOException {
		try {
			pkey = in.readCompressedInt();
			install_dir = PosixPath.valueOf(in.readUTF());
			requires_mod_jk = in.readBoolean();
		} catch(ValidationException e) {
			throw new IOException(e);
		}
	}

	public boolean requiresModJK() {
		return requires_mod_jk;
	}

	@Override
	public void write(StreamableOutput out, AoservProtocol.Version protocolVersion) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeUTF(install_dir.toString());
		out.writeBoolean(requires_mod_jk);
	}
}
