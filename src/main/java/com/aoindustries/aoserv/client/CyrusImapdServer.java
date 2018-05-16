/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2018  AO Industries, Inc.
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

import com.aoindustries.aoserv.client.validator.UnixPath;
import com.aoindustries.io.CompressedDataInputStream;
import com.aoindustries.io.CompressedDataOutputStream;
import com.aoindustries.lang.ObjectUtils;
import com.aoindustries.math.SafeMath;
import com.aoindustries.net.DomainName;
import com.aoindustries.validation.ValidationException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * An <code>CyrusImapdServer</code> represents one running instance of Cyrus IMAPD.
 *
 * @see  CyrusImapdBind
 *
 * @author  AO Industries, Inc.
 */
final public class CyrusImapdServer extends CachedObjectIntegerKey<CyrusImapdServer> {

	static final int
		COLUMN_AO_SERVER = 0,
		COLUMN_SIEVE_NET_BIND = 1
	;
	static final String COLUMN_AO_SERVER_name = "ao_server";

	public enum TimeUnit {
		DAYS('d') {
			@Override
			float convertToDays(float duration) {
				return duration;
			}
		},
		HOURS('h') {
			@Override
			float convertToDays(float duration) {
				return duration / 24;
			}
		},
		MINUTES('m') {
			@Override
			float convertToDays(float duration) {
				return duration / (24 * 60);
			}
		},
		SECONDS('s') {
			@Override
			float convertToDays(float duration) {
				return duration / (24 * 60 * 60);
			}
		};

		private static final TimeUnit[] values = values();
		private static TimeUnit getFromSuffix(String suffix) {
			if(suffix == null) return null;
			if(suffix.length() != 1) throw new IllegalArgumentException("Suffix must be one character: " + suffix);
			char ch = suffix.charAt(0);
			for(TimeUnit value : values) {
				if(ch == value.suffix) return value;
			}
			throw new IllegalArgumentException("TimeUnit not found from suffix: " + ch);
		}

		private final char suffix;

		private TimeUnit(char suffix) {
			this.suffix = suffix;
		}

		public char getSuffix() {
			return suffix;
		}

		/**
		 * Converts to a number of days, rounded-up.
		 */
		public int getDays(float duration) {
			if(Float.isNaN(duration)) throw new IllegalArgumentException("duration is NaN");
			return SafeMath.castInt(Math.round(Math.ceil(convertToDays(duration))));
		}

		abstract float convertToDays(float duration);
	}

	private int sieveNetBind;
	private DomainName servername;
	private UnixPath tlsCertFile;
	private UnixPath tlsKeyFile;
	private UnixPath tlsCaFile;
	private boolean allowPlaintextAuth;
	private float deleteDuration;
	private TimeUnit deleteDurationUnit;
	private float expireDuration;
	private TimeUnit expireDurationUnit;
	private float expungeDuration;
	private TimeUnit expungeDurationUnit;

	@Override
	String toStringImpl() throws IOException, SQLException {
		return "Cyrus IMAPD @ " + (servername != null ? servername : getAOServer().getHostname());
	}

	@Override
	Object getColumnImpl(int i) {
		switch(i) {
			case COLUMN_AO_SERVER: return pkey;
			case COLUMN_SIEVE_NET_BIND: return sieveNetBind==-1 ? null : sieveNetBind;
			case 2: return servername;
			case 3: return tlsCertFile;
			case 4: return tlsKeyFile;
			case 5: return tlsCaFile;
			case 6: return allowPlaintextAuth;
			case 7: return deleteDuration;
			case 8: return deleteDurationUnit==null ? null : String.valueOf(deleteDurationUnit.getSuffix());
			case 9: return expireDuration;
			case 10: return expireDurationUnit==null ? null : String.valueOf(expireDurationUnit.getSuffix());
			case 11: return expungeDuration;
			case 12: return expungeDurationUnit==null ? null : String.valueOf(expungeDurationUnit.getSuffix());
			default: throw new IllegalArgumentException("Invalid index: " + i);
		}
	}

	@Override
	public SchemaTable.TableID getTableID() {
		return SchemaTable.TableID.CYRUS_IMAPD_SERVERS;
	}

	@Override
	public void init(ResultSet result) throws SQLException {
		try {
			int pos = 1;
			pkey = result.getInt(pos++);
			sieveNetBind = result.getInt(pos++);
			if(result.wasNull()) sieveNetBind = -1;
			servername = DomainName.valueOf(result.getString(pos++));
			tlsCertFile = UnixPath.valueOf(result.getString(pos++));
			tlsKeyFile = UnixPath.valueOf(result.getString(pos++));
			tlsCaFile = UnixPath.valueOf(result.getString(pos++));
			allowPlaintextAuth = result.getBoolean(pos++);
			deleteDuration = result.getFloat(pos++);
			if(result.wasNull()) deleteDuration = Float.NaN;
			deleteDurationUnit = TimeUnit.getFromSuffix(result.getString(pos++));
			expireDuration = result.getFloat(pos++);
			expireDurationUnit = TimeUnit.getFromSuffix(result.getString(pos++));
			expungeDuration = result.getFloat(pos++);
			if(result.wasNull()) expungeDuration = Float.NaN;
			expungeDurationUnit = TimeUnit.getFromSuffix(result.getString(pos++));
		} catch(ValidationException e) {
			throw new SQLException(e);
		}
	}

	@Override
	public void read(CompressedDataInputStream in) throws IOException {
		try {
			pkey = in.readCompressedInt();
			sieveNetBind = in.readCompressedInt();
			servername = DomainName.valueOf(in.readNullUTF());
			tlsCertFile = UnixPath.valueOf(in.readUTF());
			tlsKeyFile = UnixPath.valueOf(in.readUTF());
			tlsCaFile = UnixPath.valueOf(in.readUTF());
			allowPlaintextAuth = in.readBoolean();
			deleteDuration = in.readFloat();
			deleteDurationUnit = in.readNullEnum(TimeUnit.class);
			expireDuration = in.readFloat();
			expireDurationUnit = in.readNullEnum(TimeUnit.class);
			expungeDuration = in.readFloat();
			expungeDurationUnit = in.readNullEnum(TimeUnit.class);
		} catch(ValidationException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void write(CompressedDataOutputStream out, AOServProtocol.Version protocolVersion) throws IOException {
		out.writeCompressedInt(pkey);
		out.writeCompressedInt(sieveNetBind);
		out.writeNullUTF(ObjectUtils.toString(servername));
		out.writeUTF(tlsCertFile.toString());
		out.writeUTF(tlsKeyFile.toString());
		out.writeUTF(tlsCaFile.toString());
		out.writeBoolean(allowPlaintextAuth);
		out.writeFloat(deleteDuration);
		out.writeNullEnum(deleteDurationUnit);
		out.writeFloat(expireDuration);
		out.writeNullEnum(expireDurationUnit);
		out.writeFloat(expungeDuration);
		out.writeNullEnum(expungeDurationUnit);
	}

	public AOServer getAOServer() throws SQLException, IOException {
		AOServer obj = table.connector.getAoServers().get(pkey);
		if(obj == null) throw new SQLException("Unable to find AOServer: " + pkey);
		return obj;
	}

	public NetBind getSieveNetBind() throws IOException, SQLException {
		if(sieveNetBind == -1) return null;
		NetBind nb = table.connector.getNetBinds().get(sieveNetBind);
		// May be filtered
		if(nb == null) return null;
		String protocol = nb.getAppProtocol().getProtocol();
		if(!Protocol.SIEVE.equals(protocol)) throw new SQLException("Sieve NetBind is incorrect app_protocol for NetBind #" + nb.getPkey() + ": " + protocol);
		Server server = nb.getServer();
		if(!server.equals(getAOServer().getServer())) throw new SQLException("Sieve NetBind is not on this server for NetBind #" + nb.getPkey());
		return nb;
	}

	/**
	 * The fully qualified hostname for <code>servername</code>.
	 *
	 * When {@code null}, defaults to {@link AOServer#getHostname()}.
	 */
	public DomainName getServername() {
		return servername;
	}

	/**
	 * The path for <code>tls_cert_file</code>.
	 */
	public UnixPath getTlsCertFile() {
		return tlsCertFile;
	}

	/**
	 * The path for <code>tls_key_file</code>.
	 */
	public UnixPath getTlsKeyFile() {
		return tlsKeyFile;
	}

	/**
	 * The path for <code>tls_ca_file</code>.
	 */
	public UnixPath getTlsCaFile() {
		return tlsCaFile;
	}

	/**
	 * Allows plaintext authentication (PLAIN/LOGIN) on non-TLS links.
	 */
	public boolean getAllowPlaintextAuth() {
		return allowPlaintextAuth;
	}

	/**
	 * Gets the duration after which delayed delete folders are removed.
	 * Enables <code>delete_mode: delayed</code>
	 *
	 * @return  the duration or {@link Float#NaN} when not set
	 *
	 * @see  #getDeleteDurationUnit()
	 */
	public float getDeleteDuration() {
		return deleteDuration;
	}

	/**
	 * Gets the time unit for {@link #getDeleteDuration()}.
	 * When not set, the duration represents days.
	 *
	 * @return  the unit or {@code null} when not set
	 */
	public TimeUnit getDeleteDurationUnit() {
		return deleteDurationUnit;
	}

	/**
	 * Prune the duplicate database of entries older than expire-duration.
	 *
	 * @return  the duration (never {@link Float#NaN})
	 *
	 * @see  #getExpireDurationUnit()
	 */
	public float getExpireDuration() {
		return expireDuration;
	}

	/**
	 * Gets the time unit for {@link #getExpireDuration()}.
	 * When not set, the duration represents days.
	 *
	 * @return  the unit or {@code null} when not set
	 */
	public TimeUnit getExpireDurationUnit() {
		return expireDurationUnit;
	}

	/**
	 * Gets the duration after which delayed expunge messages are removed.
	 * Enables <code>expunge_mode: delayed</code>
	 *
	 * @return  the duration or {@link Float#NaN} when not set
	 *
	 * @see  #getExpungeDurationUnit()
	 */
	public float getExpungeDuration() {
		return expungeDuration;
	}

	/**
	 * Gets the time unit for {@link #getExpungeDuration()}.
	 * When not set, the duration represents days.
	 *
	 * @return  the unit or {@code null} when not set
	 */
	public TimeUnit getExpungeDurationUnit() {
		return expungeDurationUnit;
	}

	public List<CyrusImapdBind> getCyrusImapdBinds() throws IOException, SQLException {
		return table.connector.getCyrusImapdBinds().getCyrusImapdBinds(this);
	}
}
