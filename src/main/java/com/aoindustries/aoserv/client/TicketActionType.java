/*
 * aoserv-client - Java client for the AOServ platform.
 * Copyright (C) 2001-2009, 2016  AO Industries, Inc.
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
 * All of the types of ticket changes are represented by these
 * <code>TicketActionType</code>s.
 *
 * @see TicketAction
 * @see Ticket
 *
 * @author  AO Industries, Inc.
 */
final public class TicketActionType extends GlobalObjectStringKey<TicketActionType> {

	static final int COLUMN_TYPE = 0;

	static final String COLUMN_TYPE_name = "type";

	private boolean visible_admin_only;

	public static final String
		SET_BUSINESS="set_business",
		SET_CONTACT_EMAILS="set_contact_emails",
		SET_CONTACT_PHONE_NUMBERS="set_contact_phone_numbers",
		SET_CLIENT_PRIORITY="set_client_priority",
		SET_SUMMARY="set_summary",
		ADD_ANNOTATION="add_annotation",
		SET_STATUS="set_status",
		SET_ADMIN_PRIORITY="set_admin_priority",
		ASSIGN="assign",
		SET_CATEGORY="set_category",
		SET_INTERNAL_NOTES="set_internal_notes",
		SET_TYPE="set_type"
	;

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_TYPE: return pkey;
			case 1: return visible_admin_only;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.TICKET_ACTION_TYPES;
	}

	public String getType() {
		return pkey;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey = result.getString(1);
		visible_admin_only = result.getBoolean(2);
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey = in.readUTF().intern();
		visible_admin_only = in.readBoolean();
	}

	@Override
	String toStringImpl() {
		return accessor.getMessage("TicketActionType."+pkey+".toString");
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
		out.writeUTF(pkey);
		out.writeBoolean(visible_admin_only);
	}

	/**
	 * Generates a locale-specific summary.
	 */
	String generateSummary(AOServConnector connector, String oldValue, String newValue) throws IOException, SQLException {
		if(oldValue==null) {
			if(newValue==null) return accessor.getMessage("TicketActionType."+pkey+".generatedSummary.null.null");
			return accessor.getMessage("TicketActionType."+pkey+".generatedSummary.null.notNull", newValue);
		} else {
			if(newValue==null) return accessor.getMessage("TicketActionType."+pkey+".generatedSummary.notNull.null", oldValue);
			return accessor.getMessage("TicketActionType."+pkey+".generatedSummary.notNull.notNull", oldValue, newValue);
		}
	}
}
