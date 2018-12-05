/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2006-2012, 2016, 2017, 2018  AO Industries, Inc.
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

import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.CachedTableIntegerKey;
import com.aoindustries.aoserv.client.aosh.AOSH;
import com.aoindustries.aoserv.client.aosh.Command;
import com.aoindustries.aoserv.client.linux.Server;
import com.aoindustries.aoserv.client.schema.AoservProtocol;
import com.aoindustries.aoserv.client.schema.Table;
import com.aoindustries.io.TerminalWriter;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

/**
 * @see  ContextParameter
 *
 * @author  AO Industries, Inc.
 */
final public class ContextParameterTable extends CachedTableIntegerKey<ContextParameter> {

	public ContextParameterTable(AOServConnector connector) {
		super(connector, ContextParameter.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(ContextParameter.COLUMN_TOMCAT_CONTEXT_name+'.'+Context.COLUMN_TOMCAT_SITE_name+'.'+Site.COLUMN_HTTPD_SITE_name+'.'+com.aoindustries.aoserv.client.web.Site.COLUMN_NAME_name, ASCENDING),
		new OrderBy(ContextParameter.COLUMN_TOMCAT_CONTEXT_name+'.'+Context.COLUMN_TOMCAT_SITE_name+'.'+Site.COLUMN_HTTPD_SITE_name+'.'+com.aoindustries.aoserv.client.web.Site.COLUMN_AO_SERVER_name+'.'+Server.COLUMN_HOSTNAME_name, ASCENDING),
		new OrderBy(ContextParameter.COLUMN_TOMCAT_CONTEXT_name+'.'+Context.COLUMN_PATH_name, ASCENDING),
		new OrderBy(ContextParameter.COLUMN_NAME_name, ASCENDING)
	};
	@Override
	protected OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	int addHttpdTomcatParameter(
		Context htc,
		String name,
		String value,
		boolean override,
		String description
	) throws IOException, SQLException {
		return connector.requestIntQueryIL(true,
			AoservProtocol.CommandID.ADD,
			Table.TableID.HTTPD_TOMCAT_PARAMETERS,
			htc.getPkey(),
			name,
			value,
			override,
			description==null ? "" : description
		);
	}

	@Override
	public ContextParameter get(int pkey) throws IOException, SQLException {
		return getUniqueRow(ContextParameter.COLUMN_PKEY, pkey);
	}

	List<ContextParameter> getHttpdTomcatParameters(Context htc) throws IOException, SQLException {
		return getIndexedRows(ContextParameter.COLUMN_TOMCAT_CONTEXT, htc.getPkey());
	}

	ContextParameter getHttpdTomcatParameter(Context htc, String name) throws IOException, SQLException {
		// Use index first
		List<ContextParameter> parameters=getHttpdTomcatParameters(htc);
		for(ContextParameter parameter : parameters) {
			if(parameter.name.equals(name)) return parameter;
		}
		return null;
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.HTTPD_TOMCAT_PARAMETERS;
	}

	@Override
	public boolean handleCommand(String[] args, Reader in, TerminalWriter out, TerminalWriter err, boolean isInteractive) throws IllegalArgumentException, IOException, SQLException {
		String command=args[0];
		if(command.equalsIgnoreCase(Command.ADD_HTTPD_TOMCAT_PARAMETER)) {
			if(AOSH.checkParamCount(Command.ADD_HTTPD_TOMCAT_PARAMETER, args, 7, err)) {
				out.println(
					connector.getSimpleAOClient().addHttpdTomcatParameter(
						args[1],
						args[2],
						args[3],
						args[4],
						args[5],
						AOSH.parseBoolean(args[6], "override"),
						args[7]
					)
				);
				out.flush();
			}
			return true;
		} else if(command.equalsIgnoreCase(Command.REMOVE_HTTPD_TOMCAT_PARAMETER)) {
			if(AOSH.checkParamCount(Command.REMOVE_HTTPD_TOMCAT_PARAMETER, args, 1, err)) {
				connector.getSimpleAOClient().removeHttpdTomcatParameter(AOSH.parseInt(args[1], "pkey"));
			}
			return true;
		} else if(command.equalsIgnoreCase(Command.UPDATE_HTTPD_TOMCAT_PARAMETER)) {
			if(AOSH.checkParamCount(Command.UPDATE_HTTPD_TOMCAT_PARAMETER, args, 8, err)) {
				connector.getSimpleAOClient().updateHttpdTomcatParameter(
					args[1],
					args[2],
					args[3],
					args[4],
					args[5],
					args[6],
					AOSH.parseBoolean(args[7], "override"),
					args[8]
				);
			}
			return true;
		} else return false;
	}
}