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
package com.aoindustries.aoserv.client.email;

import com.aoapps.hodgepodge.io.TerminalWriter;
import com.aoindustries.aoserv.client.AOServConnector;
import com.aoindustries.aoserv.client.CachedTableIntegerKey;
import com.aoindustries.aoserv.client.aosh.AOSH;
import com.aoindustries.aoserv.client.aosh.Command;
import com.aoindustries.aoserv.client.linux.GroupServer;
import com.aoindustries.aoserv.client.linux.Server;
import com.aoindustries.aoserv.client.linux.UserServer;
import com.aoindustries.aoserv.client.schema.AoservProtocol;
import com.aoindustries.aoserv.client.schema.Table;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @see  MajordomoServer
 *
 * @author  AO Industries, Inc.
 */
final public class MajordomoServerTable extends CachedTableIntegerKey<MajordomoServer> {

	MajordomoServerTable(AOServConnector connector) {
		super(connector, MajordomoServer.class);
	}

	private static final OrderBy[] defaultOrderBy = {
		new OrderBy(MajordomoServer.COLUMN_DOMAIN_name+'.'+Domain.COLUMN_DOMAIN_name, ASCENDING),
		new OrderBy(MajordomoServer.COLUMN_DOMAIN_name+'.'+Domain.COLUMN_AO_SERVER_name+'.'+Server.COLUMN_HOSTNAME_name, ASCENDING),
	};
	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	protected OrderBy[] getDefaultOrderBy() {
		return defaultOrderBy;
	}

	void addMajordomoServer(
		Domain emailDomain,
		UserServer linuxServerAccount,
		GroupServer linuxServerGroup,
		MajordomoVersion majordomoVersion
	) throws IOException, SQLException {
		connector.requestUpdateIL(
			true,
			AoservProtocol.CommandID.ADD,
			Table.TableID.MAJORDOMO_SERVERS,
			emailDomain.getPkey(),
			linuxServerAccount.getPkey(),
			linuxServerGroup.getPkey(),
			majordomoVersion.getVersion()
		);
	}

	@Override
	public MajordomoServer get(int domain) throws IOException, SQLException {
		return getUniqueRow(MajordomoServer.COLUMN_DOMAIN, domain);
	}

	public List<MajordomoServer> getMajordomoServers(Server ao) throws IOException, SQLException {
		int aoPKey=ao.getPkey();
		List<MajordomoServer> cached=getRows();
		int size=cached.size();
		List<MajordomoServer> matches=new ArrayList<>(size);
		for(int c=0;c<size;c++) {
			MajordomoServer ms=cached.get(c);
			if(ms.getDomain().getLinuxServer_host_id() == aoPKey) matches.add(ms);
		}
		return matches;
	}

	@Override
	public Table.TableID getTableID() {
		return Table.TableID.MAJORDOMO_SERVERS;
	}

	@Override
	public boolean handleCommand(String[] args, Reader in, TerminalWriter out, TerminalWriter err, boolean isInteractive) throws IllegalArgumentException, IOException, SQLException {
		String command=args[0];
		if(command.equalsIgnoreCase(Command.ADD_MAJORDOMO_SERVER)) {
			if(AOSH.checkParamCount(Command.ADD_MAJORDOMO_SERVER, args, 5, err)) {
				connector.getSimpleAOClient().addMajordomoServer(
					AOSH.parseDomainName(args[1], "domain"),
					args[2],
					AOSH.parseLinuxUserName(args[3], "linux_account"),
					AOSH.parseGroupName(args[4], "linux_group"),
					args[5]
				);
			}
			return true;
		} else if(command.equalsIgnoreCase(Command.REMOVE_MAJORDOMO_SERVER)) {
			if(AOSH.checkParamCount(Command.REMOVE_MAJORDOMO_SERVER, args, 2, err)) {
				connector.getSimpleAOClient().removeMajordomoServer(
					AOSH.parseDomainName(args[1], "domain"),
					args[2]
				);
			}
			return true;
		}
		return false;
	}
}
