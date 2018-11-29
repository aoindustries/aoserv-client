/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2000-2009, 2014, 2016, 2017, 2018  AO Industries, Inc.
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

import com.aoindustries.aoserv.client.validator.GroupId;
import com.aoindustries.aoserv.client.validator.UserId;
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import com.aoindustries.validation.ValidationException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Each <code>LinuxGroup</code> may be accessed by any number
 * of <code>LinuxAccount</code>s.  The accounts are granted access
 * to a group via a <code>LinuxGroupAccount</code>.  One account
 * may access a maximum of 31 different groups.  Also, a
 * <code>LinuxAccount</code> must have one and only one primary
 * <code>LinuxGroupAccount</code>.
 *
 * @see  LinuxAccount
 * @see  LinuxGroup
 *
 * @author  AO Industries, Inc.
 */
final public class LinuxGroupAccount extends CachedObjectIntegerKey<LinuxGroupAccount> implements Removable {

	static final int COLUMN_ID = 0;
	static final String COLUMN_GROUP_name = "group";
	static final String COLUMN_USER_name = "user";

	/**
	 * The maximum number of groups allowed for one account.
	 * 
	 * <pre>/usr/include/linux/limits.h:#define NGROUPS_MAX    65536</pre>
	 */
	public static final int MAX_GROUPS = 65536;

	private GroupId group;
	private UserId user;
	private boolean isPrimary;
	private int operatingSystemVersion;

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_ID: return pkey;
			case 1: return group;
			case 2: return user;
			case 3: return isPrimary;
			case 4: return operatingSystemVersion == -1 ? null : operatingSystemVersion;
			default: throw new IllegalArgumentException("Invalid index: " + i);
		}
	}

	public int getId() {
		return pkey;
	}

	public GroupId getGroup_name() {
		return group;
	}

	public LinuxGroup getGroup() throws SQLException, IOException {
		LinuxGroup groupNameObject = table.connector.getLinuxGroups().get(group);
		if (groupNameObject == null) throw new SQLException("Unable to find LinuxGroup: " + group);
		return groupNameObject;
	}

	public UserId getUser_username() {
		return user;
	}

	public LinuxAccount getUser() throws SQLException, IOException {
		LinuxAccount usernameObject = table.connector.getUsernames().get(user).getLinuxAccount();
		if (usernameObject == null) throw new SQLException("Unable to find LinuxAccount: " + user);
		return usernameObject;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public Integer getOperatingSystemVersion_pkey() {
		return operatingSystemVersion == -1 ? null : operatingSystemVersion;
	}

	public OperatingSystemVersion getOperatingSystemVersion() throws SQLException, IOException {
		if(operatingSystemVersion == -1) return null;
		OperatingSystemVersion osv = table.connector.getOperatingSystemVersions().get(operatingSystemVersion);
		if(osv == null) throw new SQLException("Unable to find OperatingSystemVersion: " + operatingSystemVersion);
		return osv;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		try {
			int pos = 1;
			pkey = result.getInt(pos++);
			group = GroupId.valueOf(result.getString(pos++));
			user = UserId.valueOf(result.getString(pos++));
			isPrimary = result.getBoolean(pos++);
			operatingSystemVersion = result.getInt(pos++);
			if(result.wasNull()) operatingSystemVersion = -1;
		} catch(ValidationException e) {
			throw new SQLException(e);
		}
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		try {
			pkey = in.readCompressedInt();
			group = GroupId.valueOf(in.readUTF()).intern();
			user = UserId.valueOf(in.readUTF()).intern();
			isPrimary = in.readBoolean();
			operatingSystemVersion = in.readCompressedInt();
		} catch(ValidationException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version protocolVersion) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeUTF(group.toString());
		out.writeUTF(user.toString());
		out.writeBoolean(isPrimary);
		if(protocolVersion.compareTo(AOServProtocol.Version.VERSION_1_80_1) >= 0) {
			out.writeCompressedInt(operatingSystemVersion);
		}
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.LINUX_GROUP_ACCOUNTS;
	}

	@Override
	public List<CannotRemoveReason<LinuxGroupAccount>> getCannotRemoveReasons() {
		List<CannotRemoveReason<LinuxGroupAccount>> reasons=new ArrayList<>();
		if(isPrimary) reasons.add(new CannotRemoveReason<>("Not allowed to drop a primary group", this));
		return reasons;
	}

	@Override
	public void remove() throws IOException, SQLException {
		table.connector.requestUpdateIL(
			true,
			AOServProtocol.CommandID.REMOVE,
			SchemaTable.TableID.LINUX_GROUP_ACCOUNTS,
			pkey
		);
	}

	void setAsPrimary() throws IOException, SQLException {
		table.connector.requestUpdateIL(
			true,
			AOServProtocol.CommandID.SET_PRIMARY_LINUX_GROUP_ACCOUNT,
			pkey
		);
	}

	@Override
	String toStringImpl() {
		return group.toString()+'|'+user.toString()+(isPrimary?"|p":"|a");
	}
}
