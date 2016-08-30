/*
 * Copyright 2000-2013, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * The entire contents of servers are periodically replicated to another server.  In the
 * event of hardware failure, this other server may be booted to take place of the
 * failed machine.  All transfers to the failover server are logged.
 *
 * @author  AO Industries, Inc.
 */
final public class FailoverFileLog extends AOServObject<Integer,FailoverFileLog> implements SingleTableObject<Integer,FailoverFileLog> {

	static final String COLUMN_REPLICATION_name = "replication";
	static final String COLUMN_END_TIME_name = "end_time";

	private AOServTable<Integer,FailoverFileLog> table;

	private int pkey;
	private int replication;
	private long startTime;
	private long endTime;
	private int scanned;
	private int updated;
	private long bytes;
	private boolean is_successful;

	@Override
	boolean equalsImpl(Object O) {
		return
			O instanceof FailoverFileLog
			&& ((FailoverFileLog)O).pkey==pkey
		;
	}

	public long getBytes() {
		return bytes;
	}

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case 0: return pkey;
			case 1: return replication;
			case 2: return getStartTime();
			case 3: return getEndTime();
			case 4: return scanned;
			case 5: return updated;
			case 6: return bytes;
			case 7: return is_successful;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public Timestamp getStartTime() {
		return new Timestamp(startTime);
	}

	public Timestamp getEndTime() {
		return new Timestamp(endTime);
	}

	public int getPkey() {
		return pkey;
	}

	@Override
	public Integer getKey() {
		return pkey;
	}

	public int getScanned() {
		return scanned;
	}

	public FailoverFileReplication getFailoverFileReplication() throws SQLException, IOException {
		FailoverFileReplication ffr=table.connector.getFailoverFileReplications().get(replication);
		if(ffr==null) throw new SQLException("Unable to find FailoverFileReplication: "+replication);
		return ffr;
	}

	/**
	 * Gets the <code>AOServTable</code> that contains this <code>AOServObject</code>.
	 *
	 * @return  the <code>AOServTable</code>.
	 */
	@Override
	public AOServTable<Integer,FailoverFileLog> getTable() {
		return table;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.FAILOVER_FILE_LOG;
	}

	public int getUpdated() {
		return updated;
	}

	@Override
	int hashCodeImpl() {
		return pkey;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey=result.getInt(1);
		replication=result.getInt(2);
		startTime=result.getTimestamp(3).getTime();
		endTime=result.getTimestamp(4).getTime();
		scanned=result.getInt(5);
		updated=result.getInt(6);
		bytes=result.getLong(7);
		is_successful=result.getBoolean(8);
	}

	public boolean isSuccessful() {
		return is_successful;
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey=in.readCompressedInt();
		replication=in.readCompressedInt();
		startTime=in.readLong();
		endTime=in.readLong();
		scanned=in.readCompressedInt();
		updated=in.readCompressedInt();
		bytes=in.readLong();
		is_successful=in.readBoolean();
	}

	@Override
	public void setTable(AOServTable<Integer,FailoverFileLog> table) {
		if(this.table!=null) throw new IllegalStateException("table already set");
		this.table=table;
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeCompressedInt(replication);
		out.writeLong(startTime);
		out.writeLong(endTime);
		out.writeCompressedInt(scanned);
		out.writeCompressedInt(updated);
		out.writeLong(bytes);
		out.writeBoolean(is_successful);
	}
}