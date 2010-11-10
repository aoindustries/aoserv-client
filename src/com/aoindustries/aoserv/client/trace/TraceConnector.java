/*
 * Copyright 2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client.trace;

import com.aoindustries.aoserv.client.validator.DomainName;
import com.aoindustries.aoserv.client.validator.UserId;
import com.aoindustries.aoserv.client.wrapped.WrappedConnector;
import com.aoindustries.security.LoginException;
import com.aoindustries.sql.SQLUtility;
import com.aoindustries.util.ErrorPrinter;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

/**
 * @see TraceConnectorFactory
 *
 * @author  AO Industries, Inc.
 */
final public class TraceConnector extends WrappedConnector<TraceConnector,TraceConnectorFactory> {

    TraceConnector(TraceConnectorFactory factory, Locale locale, UserId connectAs, UserId authenticateAs, String password, DomainName daemonServer) throws RemoteException, LoginException {
        super(factory, locale, connectAs, authenticateAs, password, daemonServer);
    }

    @Override
    protected <T> T call(Callable<T> callable, boolean allowRetry) throws RemoteException, NoSuchElementException {
        long startNanos = System.nanoTime();
        try {
            return super.call(callable, allowRetry);
        } finally {
            long nanos = System.nanoTime() - startNanos;
            ErrorPrinter.printStackTraces(new Throwable(SQLUtility.getMilliDecimal(nanos / 1000)+"ms"), System.err);
            System.err.flush();
        }
    }
}
