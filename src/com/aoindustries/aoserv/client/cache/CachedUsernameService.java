package com.aoindustries.aoserv.client.cache;

/*
 * Copyright 2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.aoserv.client.Username;
import com.aoindustries.aoserv.client.UsernameService;

/**
 * @author  AO Industries, Inc.
 */
final class CachedUsernameService extends CachedServiceUserIdKey<Username> implements UsernameService<CachedConnector,CachedConnectorFactory> {

    CachedUsernameService(CachedConnector connector, UsernameService<?,?> wrapped) {
        super(connector, Username.class, wrapped);
    }
}
