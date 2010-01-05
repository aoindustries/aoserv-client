package com.aoindustries.aoserv.client.noswing;

/*
 * Copyright 2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.aoserv.client.PostgresReservedWord;
import com.aoindustries.aoserv.client.PostgresReservedWordService;

/**
 * @author  AO Industries, Inc.
 */
final class NoSwingPostgresReservedWordService extends NoSwingServiceStringKey<PostgresReservedWord> implements PostgresReservedWordService<NoSwingConnector,NoSwingConnectorFactory> {

    NoSwingPostgresReservedWordService(NoSwingConnector connector, PostgresReservedWordService<?,?> wrapped) {
        super(connector, PostgresReservedWord.class, wrapped);
    }
}