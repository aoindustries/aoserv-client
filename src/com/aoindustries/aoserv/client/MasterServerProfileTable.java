package com.aoindustries.aoserv.client;

/*
 * Copyright 2001-2008 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.profiler.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @see  MasterServerProfile
 *
 * @version  1.0a
 *
 * @author  AO Industries, Inc.
 */
final public class MasterServerProfileTable extends AOServTable<String,MasterServerProfile> {

    MasterServerProfileTable(AOServConnector connector) {
	super(connector, MasterServerProfile.class);
    }

    private static final OrderBy[] defaultOrderBy = {
        new OrderBy(MasterServerProfile.COLUMN_CLASSNAME_name, ASCENDING),
        new OrderBy(MasterServerProfile.COLUMN_METHOD_NAME_name, ASCENDING),
        new OrderBy(MasterServerProfile.COLUMN_PARAMETER_name, ASCENDING)
    };
    @Override
    OrderBy[] getDefaultOrderBy() {
        return defaultOrderBy;
    }

    public MasterServerProfile get(Object key) {
        for(MasterServerProfile msp : getRows()) if(key.equals(msp.getKey())) return msp;
        return null;
    }

    public List<MasterServerProfile> getRows() {
        List<MasterServerProfile> list=new ArrayList<MasterServerProfile>();
        getObjects(list, AOServProtocol.CommandID.GET_TABLE, SchemaTable.TableID.MASTER_SERVER_PROFILE);
        return list;
    }

    public SchemaTable.TableID getTableID() {
	return SchemaTable.TableID.MASTER_SERVER_PROFILE;
    }

    protected MasterServerProfile getUniqueRowImpl(int col, Object value) {
        throw new IllegalArgumentException("Not a unique column: "+col);
    }
}
