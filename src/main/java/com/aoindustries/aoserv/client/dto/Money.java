/*
 * Copyright 2010-2011, 2016 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.client.dto;

import java.math.BigDecimal;

/**
 * @author  AO Industries, Inc.
 */
public class Money {

	private String currency;
	private BigDecimal value;

	public Money() {
	}

	public Money(String currency, BigDecimal value) {
		this.currency = currency;
		this.value = value;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}
}
