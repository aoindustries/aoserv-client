/*
 * Copyright 2001-2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.aoserv.client.validator.*;

/**
 * @see  Shell
 *
 * @author  AO Industries, Inc.
 */
@ServiceAnnotation(ServiceName.shells)
public interface ShellService extends AOServService<UnixPath,Shell> {
}
