/*
 * Copyright 2002-2009, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <code>BackupPartition</code> stores backup data.
 *
 * @author  AO Industries, Inc.
 */
final public class BackupPartition extends CachedObjectIntegerKey<BackupPartition> {

	static final int
		COLUMN_PKEY=0,
		COLUMN_AO_SERVER=1
	;
	static final String COLUMN_AO_SERVER_name = "ao_server";
	static final String COLUMN_PATH_name = "path";

	int ao_server;
	String path;
	private boolean enabled;
	private boolean quota_enabled;

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_PKEY: return pkey;
			case COLUMN_AO_SERVER: return ao_server;
			case 2: return path;
			case 3: return enabled;
			case 4: return quota_enabled;
			default: throw new IllegalArgumentException("Invalid index: "+i);
		}
	}

	public long getDiskTotalSize() throws IOException, SQLException {
		return table.connector.requestLongQuery(true, AOServProtocol.CommandID.GET_BACKUP_PARTITION_DISK_TOTAL_SIZE, pkey);
	}

	public long getDiskUsedSize() throws IOException, SQLException {
		return table.connector.requestLongQuery(true, AOServProtocol.CommandID.GET_BACKUP_PARTITION_DISK_USED_SIZE, pkey);
	}

	public AOServer getAOServer() throws SQLException, IOException {
		AOServer ao=table.connector.getAoServers().get(ao_server);
		if(ao==null) throw new SQLException("Unable to find AOServer: "+ao_server);
		return ao;
	}

	public String getPath() {
		return path;
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.BACKUP_PARTITIONS;
	}

	@Override
	String toStringImpl() throws SQLException, IOException {
		return getAOServer().getHostname()+":"+path;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		pkey=result.getInt(1);
		ao_server=result.getInt(2);
		path=result.getString(3);
		enabled=result.getBoolean(4);
		quota_enabled=result.getBoolean(5);
	}

	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * When quota is enabled, all replications/backups into the partition must have quota_gid set.
	 * When quota is disabled, all replications/backups into the partition must have quota_gid not set.
	 * This generally means that ao_servers, which backup full Unix permissions, will be backed-up to non-quota partitions,
	 * while other backups (such as from Windows) will go to quota-enabled partitions for billing purposes.
	 * 
	 * @return the enabled flag
	 */
	public boolean isQuotaEnabled() {
		return quota_enabled;
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		pkey=in.readCompressedInt();
		ao_server=in.readCompressedInt();
		path=in.readUTF().intern();
		enabled=in.readBoolean();
		quota_enabled=in.readBoolean();
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version version) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeCompressedInt(ao_server);
		if(version.compareTo(AOServProtocol.Version.VERSION_1_30)<=0) out.writeUTF(path);
		out.writeUTF(path);
		if(version.compareTo(AOServProtocol.Version.VERSION_1_30)<=0) {
			out.writeLong((long)512*1024*1024); // min free space
			out.writeLong((long)1024*1024*1024); // desired free space
		}
		out.writeBoolean(enabled);
		if(
			version.compareTo(AOServProtocol.Version.VERSION_1_0_A_117)>=0
			&& version.compareTo(AOServProtocol.Version.VERSION_1_30)<=0
		) {
			out.writeCompressedInt(1); // fill_order
		}
		if(version.compareTo(AOServProtocol.Version.VERSION_1_31)>=0) out.writeBoolean(quota_enabled);
	}
}