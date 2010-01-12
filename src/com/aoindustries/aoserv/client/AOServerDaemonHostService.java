/*
 * Copyright 2001-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

/**
 * @see  AOServerDaemonHost
 *
 * @author  AO Industries, Inc.
 */
@ServiceAnnotation(ServiceName.ao_server_daemon_hosts)
public interface AOServerDaemonHostService<C extends AOServConnector<C,F>, F extends AOServConnectorFactory<C,F>> extends AOServServiceIntegerKey<C,F,AOServerDaemonHost> {
}
