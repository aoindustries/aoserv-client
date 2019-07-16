/*
 * aoserv-client - Java client for the AOServ Platform.
 * Copyright (C) 2018, 2019  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aoserv-client.
 *
 * aoserv-client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aoserv-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aoserv-client.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.aoserv.client.billing;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class SchemaBeanInfo extends SimpleBeanInfo {

	private static volatile PropertyDescriptor[] properties;

	@Override
	public PropertyDescriptor[] getPropertyDescriptors () {
		try {
			PropertyDescriptor[] props = properties;
			if(props == null) {
				props = new PropertyDescriptor[] {
					new PropertyDescriptor("Currency", Schema.class, "getCurrency", null),
					new PropertyDescriptor("MonthlyCharge", Schema.class, "getMonthlyCharge", null),
					new PropertyDescriptor("NoticeLog", Schema.class, "getNoticeLog", null),
					new PropertyDescriptor("NoticeType", Schema.class, "getNoticeType", null),
					new PropertyDescriptor("Package", Schema.class, "getPackage", null),
					new PropertyDescriptor("PackageCategory", Schema.class, "getPackageCategory", null),
					new PropertyDescriptor("PackageDefinition", Schema.class, "getPackageDefinition", null),
					new PropertyDescriptor("PackageDefinitionLimit", Schema.class, "getPackageDefinitionLimit", null),
					new PropertyDescriptor("Resource", Schema.class, "getResource", null),
					new PropertyDescriptor("Transaction", Schema.class, "getTransaction", null),
					new PropertyDescriptor("TransactionType", Schema.class, "getTransactionType", null),
					new PropertyDescriptor("WhoisHistory", Schema.class, "getWhoisHistory", null),
					new PropertyDescriptor("WhoisHistoryAccount", Schema.class, "getWhoisHistoryAccount", null),
				};
				properties = props;
			}
			return props; // Not copying array for performance
		} catch(IntrospectionException err) {
			throw new AssertionError(err);
		}
	}

	/**
	 * Include base class.
	 */
	@Override
	public BeanInfo[] getAdditionalBeanInfo() {
		try {
			return new BeanInfo[] {
				Introspector.getBeanInfo(Schema.class.getSuperclass())
			};
		} catch(IntrospectionException err) {
			throw new AssertionError(err);
		}
	}
}
