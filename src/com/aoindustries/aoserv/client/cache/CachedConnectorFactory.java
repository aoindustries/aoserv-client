package com.aoindustries.aoserv.client.cache;

/*
 * Copyright 2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
import com.aoindustries.aoserv.client.AOServConnectorFactory;
import com.aoindustries.aoserv.client.AOServConnectorFactoryCache;
import com.aoindustries.aoserv.client.validator.DomainName;
import com.aoindustries.aoserv.client.validator.UserId;
import com.aoindustries.security.LoginException;
import java.rmi.RemoteException;
import java.util.Locale;

/**
 * An implementation of <code>AOServConnectorFactory</code> that transfers entire
 * tables as a set and performs local lookups.
 *
 * @author  AO Industries, Inc.
 */
final public class CachedConnectorFactory implements AOServConnectorFactory<CachedConnector,CachedConnectorFactory> {

    final AOServConnectorFactory<?,?> wrapped;

    public CachedConnectorFactory(AOServConnectorFactory<?,?> wrapped) {
        this.wrapped = wrapped;
    }

    private final AOServConnectorFactoryCache<CachedConnector,CachedConnectorFactory> connectors = new AOServConnectorFactoryCache<CachedConnector,CachedConnectorFactory>();

    public CachedConnector getConnector(Locale locale, UserId connectAs, UserId authenticateAs, String password, DomainName daemonServer, boolean readOnly) throws LoginException, RemoteException {
        synchronized(connectors) {
            CachedConnector connector = connectors.get(connectAs, authenticateAs, password, daemonServer, readOnly);
            if(connector!=null) {
                connector.setLocale(locale);
            } else {
                connector = newConnector(
                    locale,
                    connectAs,
                    authenticateAs,
                    password,
                    daemonServer,
                    readOnly
                );
            }
            return connector;
        }
    }

    public CachedConnector newConnector(Locale locale, UserId connectAs, UserId authenticateAs, String password, DomainName daemonServer, boolean readOnly) throws LoginException, RemoteException {
        synchronized(connectors) {
            CachedConnector connector = new CachedConnector(this, wrapped.newConnector(locale, connectAs, authenticateAs, password, daemonServer, readOnly));
            connectors.put(
                connectAs,
                authenticateAs,
                password,
                daemonServer,
                readOnly,
                connector
            );
            return connector;
        }
    }
}
