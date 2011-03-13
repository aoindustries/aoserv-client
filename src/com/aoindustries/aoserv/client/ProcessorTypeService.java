/*
 * Copyright 2008-2011 by AO Industries, Inc.,
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
public interface ProcessorTypeService extends AOServService<String,ProcessorType> {
}