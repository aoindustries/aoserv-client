/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2017, 2018, 2019, 2020, 2021  AO Industries, Inc.
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
package com.aoindustries.aoserv.client.web;

import com.aoapps.hodgepodge.io.stream.StreamableInput;
import com.aoapps.hodgepodge.io.stream.StreamableOutput;
import com.aoindustries.aoserv.client.CachedObjectIntegerKey;
import com.aoindustries.aoserv.client.schema.AoservProtocol;
import com.aoindustries.aoserv.client.schema.Table;
import com.aoindustries.aoserv.client.util.ApacheEscape;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Each {@link VirtualHost} may have header configurations attached to it.
 * See <a href="https://httpd.apache.org/docs/2.4/mod/mod_headers.html">mod_headers - Apache HTTP Server Version 2.4</a>.
 *
 * @see  VirtualHost
 *
 * @author  AO Industries, Inc.
 */
final public class Header extends CachedObjectIntegerKey<Header> {

	static final int
		COLUMN_PKEY = 0,
		COLUMN_HTTPD_SITE_BIND = 1
	;
	static final String COLUMN_HTTPD_SITE_BIND_name = "httpd_site_bind";
	static final String COLUMN_SORT_ORDER_name = "sort_order";

	private int httpd_site_bind;
	private short sortOrder;

	// Matches aoserv-master-db/aoindustries/aoweb/Header.Type-type.sql
	public enum Type {
		Header,
		RequestHeader
	}
	private Type type;

	private boolean always;
	private String action;
	private String header;
	private String value;
	private String replacement;
	private String when;
	private String comment;

	@Override
	protected Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_PKEY: return pkey;
			case COLUMN_HTTPD_SITE_BIND: return httpd_site_bind;
			case 2: return sortOrder;
			case 3: return type.name();
			case 4: return always;
			case 5: return action;
			case 6: return header;
			case 7: return value;
			case 8: return replacement;
			case 9: return when;
			case 10: return comment;
			default: throw new IllegalArgumentException("Invalid index: " + i);
		}
	}

	public VirtualHost getHttpdSiteBind() throws SQLException, IOException {
		VirtualHost obj = table.getConnector().getWeb().getVirtualHost().get(httpd_site_bind);
		if(obj == null) throw new SQLException("Unable to find HttpdSiteBind: " + httpd_site_bind);
		return obj;
	}

	public short getSortOrder() {
		return sortOrder;
	}

	public Type getType() {
		return type;
	}

	/**
	 * The <code>condition</code> is either "onsuccess" (default, can be omitted) or "always".
	 * We model this as a simple flag to enable "always".
	 * {@link Type#Header}-only.
	 */
	public boolean getAlways() {
		return always;
	}

	/**
	 * One of (with potentially more supported in the future):
	 * <ul>
	 * <li>"add"</li>
	 * <li>"append"</li>
	 * <li>"echo" ({@link Type#Header}-only)</li>
	 * <li>"edit"</li>
	 * <li>"edit*"</li>
	 * <li>"merge"</li>
	 * <li>"set"</li>
	 * <li>"setifempty"</li>
	 * <li>"unset"</li>
	 * <li>"note" ({@link Type#Header}-only)</li>
	 * </ul>
	 */
	public String getAction() {
		return action;
	}

	/**
	 * The header name, without any final colon.
	 * <p>
	 * In the Apache directive, the final colon is optional.  We choose to not
	 * allow the final colon to avoid any unnecessary ambiguities.
	 * </p>
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * The value as used by any of the following actions:
	 * <ul>
	 * <li>"add"</li>
	 * <li>"append"</li>
	 * <li>"edit"</li>
	 * <li>"edit*"</li>
	 * <li>"merge"</li>
	 * <li>"set"</li>
	 * <li>"setifempty"</li>
	 * <li>"note" ({@link Type#Header}-only)</li>
	 * </ul>
	 * The following actions do not have any value:
	 * <ul>
	 * <li>"echo" ({@link Type#Header}-only)</li>
	 * <li>"unset"</li>
	 * </ul>
	 */
	public String getValue() {
		return value;
	}

	/**
	 *
	 * The replacement, only used by the following actions:
	 * <ul>
	 * <li>"edit"</li>
	 * <li>"edit*"</li>
	 * </ul>
	 */
	public String getReplacement() {
		return replacement;
	}

	/**
	 * Optional control over when is this header directive is applied.
	 * Will be one of (with potentially more supported in the future):
	 * <ul>
	 * <li>"early"</li>
	 * <li>"env=*"</li>
	 * <li>"expr=*"</li>
	 * </ul>
	 */
	public String getWhen() {
		return when;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.HTTPD_SITE_BIND_HEADERS;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		int pos = 1;
		pkey            = result.getInt(pos++);
		httpd_site_bind = result.getInt(pos++);
		sortOrder       = result.getShort(pos++);
		type            = Type.valueOf(result.getString(pos++));
		always          = result.getBoolean(pos++);
		action          = result.getString(pos++);
		header          = result.getString(pos++);
		value           = result.getString(pos++);
		replacement     = result.getString(pos++);
		when            = result.getString(pos++);
		comment         = result.getString(pos++);
	}

	@Override
	public void read(StreamableInput in, AoservProtocol.Version protocolVersion) throws IOException {
		pkey            = in.readCompressedInt();
		httpd_site_bind = in.readCompressedInt();
		sortOrder       = in.readShort();
		type            = in.readEnum(Type.class);
		always          = in.readBoolean();
		action          = in.readUTF();
		header          = in.readUTF();
		value           = in.readNullUTF();
		replacement     = in.readNullUTF();
		when            = in.readNullUTF();
		comment         = in.readNullUTF();
	}

	/**
	 * Gets the Apache directive for this header.
	 */
	public String getApacheDirective(String dollarVariable) {
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		if(always) sb.append(" always");
		sb.append(' ').append(action).append(' ').append(ApacheEscape.escape(dollarVariable, header));
		if(value != null) sb.append(' ').append(ApacheEscape.escape(dollarVariable, value, true));
		if(replacement != null) sb.append(' ').append(ApacheEscape.escape(dollarVariable, replacement, true));
		if(when != null) sb.append(' ').append(ApacheEscape.escape(dollarVariable, when, true));
		return sb.toString();
	}

	/**
	 * @see #getApacheDirective(java.lang.String)
	 * @see ApacheEscape#DEFAULT_DOLLAR_VARIABLE
	 */
	@Override
	public String toStringImpl() {
		return getApacheDirective(ApacheEscape.DEFAULT_DOLLAR_VARIABLE);
	}

	@Override
	public void write(StreamableOutput out, AoservProtocol.Version protocolVersion) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeCompressedInt(httpd_site_bind);
		out.writeShort(sortOrder);
		out.writeEnum(type);
		out.writeBoolean(always);
		out.writeUTF(action);
		out.writeUTF(header);
		out.writeNullUTF(value);
		out.writeNullUTF(replacement);
		out.writeNullUTF(when);
		out.writeNullUTF(comment);
	}
}
