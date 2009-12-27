package com.aoindustries.aoserv.client;

/*
 * Copyright 2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */

/**
 * @see  Resource
 *
 * @author  AO Industries, Inc.
 */
@ServiceAnnotation(ServiceName.resources)
public interface ResourceService<C extends AOServConnector<C,F>, F extends AOServConnectorFactory<C,F>> extends AOServServiceIntegerKey<C,F,Resource> {
}
