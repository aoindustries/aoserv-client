/*
 * Copyright 2008-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

/**
 * The table containing all of the possible processor types.
 *
 * @author  AO Industries, Inc.
 */
@ServiceAnnotation(ServiceName.processor_types)
public interface ProcessorTypeService<C extends AOServConnector<C,F>, F extends AOServConnectorFactory<C,F>> extends AOServService<C,F,String,ProcessorType> {
}