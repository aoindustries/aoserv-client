/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2013, 2016, 2017, 2018  AO Industries, Inc.
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

import com.aoindustries.net.DomainName;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @see  GlobalObjectDomainNameKey
 *
 * @author  AO Industries, Inc.
 */
public abstract class GlobalTableDomainNameKey<V extends GlobalObjectDomainNameKey<V>> extends GlobalTable<DomainName,V> {

	protected GlobalTableDomainNameKey(AOServConnector connector, Class<V> clazz) {
		super(connector, clazz);
	}

	/**
	 * Gets the object with the provided key.  The key must be a DomainName.
	 *
	 * @deprecated  Always try to lookup by specific keys; the compiler will help you more when types change.
	 */
	@Deprecated
	@Override
	public V get(Object pkey) throws IOException, SQLException {
		return get((DomainName)pkey);
	}

	abstract public V get(DomainName pkey) throws IOException, SQLException;
}
