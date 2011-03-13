/*
 * Copyright 2010-2011 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client;

import com.aoindustries.aoserv.client.validator.*;

/**
 * @see  GroupName
 *
 * @author  AO Industries, Inc.
 */
@ServiceAnnotation(ServiceName.group_names)
public interface GroupNameService extends AOServService<GroupId,GroupName> {
}
