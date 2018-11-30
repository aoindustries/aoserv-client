/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2001-2009, 2016, 2017, 2018  AO Industries, Inc.
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

import com.aoindustries.aoserv.client.schema.AoservProtocol;
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import java.io.IOException;

/**
 * An <code>AOServConnection</code> is one stream of communication
 * between the client and the server.
 *
 * @see  AOServConnector
 *
 * @author  AO Industries, Inc.
 */
abstract public class AOServConnection {

	/**
	 * The connector that this connection is part of.
	 */
	protected final AOServConnector connector;

	/**
	 * Creates a new <code>AOServConnection</code>.
	 */
	protected AOServConnection(AOServConnector connector) {
		this.connector=connector;
	}

	/**
	 * Closes this connection to the server
	 * so that a reconnect is forced in the
	 * future.
	 */
	abstract void close();

	/**
	 * Gets the stream to write to the server.
	 */
	abstract CompressedDataOutputStream getRequestOut(AoservProtocol.CommandID commID) throws IOException;

	/**
	 * Gets the stream to read from the server.
	 */
	abstract CompressedDataInputStream getResponseIn() throws IOException;
}
