/*
 * Copyright 2003-2009, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.io.FileListObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A <code>FilesystemCachedObject</code> is stored in
 * a temporary file on disk for local-speed performance while using
 * minimal heap space.
 *
 * @author  AO Industries, Inc.
 */
public abstract class FilesystemCachedObject<K,T extends FilesystemCachedObject<K,T>> extends AOServObject<K,T> implements SingleTableObject<K,T>, FileListObject {

	protected AOServTable<K,T> table;

	protected FilesystemCachedObject() {
	}
	/*
	public FileListObject createInstance() throws IOException {
		T fco=table.getNewObject();
		if(table!=null) fco.setTable(table);
		return fco;
	}
	 */

	/**
	 * Gets the <code>AOServTable</code> that contains this <code>AOServObject</code>.
	 *
	 * @return  the <code>AOServTable</code>.
	 */
	@Override
	final public AOServTable<K,T> getTable() {
		return table;
	}

	@Override
	final public void setTable(AOServTable<K,T> table) {
		if(this.table!=null) throw new IllegalStateException("table already set");
		this.table=table;
	}

	@Override
	public abstract void writeRecord(DataOutputStream out) throws IOException;

	@Override
	public abstract void readRecord(DataInputStream in) throws IOException;
}
