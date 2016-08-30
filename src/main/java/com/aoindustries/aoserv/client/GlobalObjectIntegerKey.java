/*
 * aoserv-client - Java client for the AOServ platform.
 * Copyright (C) 2006-2009, 2016  AO Industries, Inc.
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

/**
 * An object that is cached and uses an int as its primary key,
 *
 * @author  AO Industries, Inc.
 */
public abstract class GlobalObjectIntegerKey<T extends GlobalObjectIntegerKey<T>> extends GlobalObject<Integer,T> {

	protected int pkey;

	@Override
	boolean equalsImpl(Object O) {
		return
			O!=null
			&& O.getClass()==getClass()
			&& ((GlobalObjectIntegerKey)O).pkey==pkey
		;
	}

	public int getPkey() {
		return pkey;
	}

	@Override
	public Integer getKey() {
		return pkey;
	}

	@Override
	int hashCodeImpl() {
		return pkey;
	}

	@Override
	String toStringImpl() {
		return Integer.toString(pkey);
	}
}
