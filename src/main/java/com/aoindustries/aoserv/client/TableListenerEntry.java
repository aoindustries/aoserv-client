/*
 * Copyright 2001-2009, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.table.TableListener;

/**
 * Used by <code>AOServTable</code> to store the list of
 * <code>TableListener</code>s.
 *
 * @author  AO Industries, Inc.
 */
final public class TableListenerEntry {

	final TableListener listener;
	final long delay;
	// All accesses should be protected by the table.eventLock
	long delayStart=-1;

	TableListenerEntry(TableListener listener, long delay) {
		this.listener=listener;
		this.delay=delay;
	}
}