/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.key;

import static com.opengamma.strata.basics.currency.Currency.CHF;
import static com.opengamma.strata.basics.currency.Currency.EUR;
import static com.opengamma.strata.basics.currency.Currency.GBP;
import static com.opengamma.strata.basics.currency.Currency.USD;
import static com.opengamma.strata.collect.TestHelper.assertSerialization;
import static com.opengamma.strata.collect.TestHelper.coverBeanEquals;
import static com.opengamma.strata.collect.TestHelper.coverImmutableBean;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.market.key.FxRateKey.Meta;

/**
 * Test {@link FxRateKey}.
 */
@Test
public class FxRateKeyTest {

  private static final CurrencyPair PAIR = CurrencyPair.of(GBP, USD);

  //-------------------------------------------------------------------------
  public void test_of_pair() {
    FxRateKey test = FxRateKey.of(PAIR);
    assertEquals(test.getPair(), PAIR);
    assertEquals(test.getMarketDataType(), Double.class);
  }

  public void test_of_currencies() {
    FxRateKey test = FxRateKey.of(GBP, USD);
    assertEquals(test.getPair(), PAIR);
    assertEquals(test.getMarketDataType(), Double.class);
  }

  //-------------------------------------------------------------------------
  public void coverage() {
    FxRateKey test = FxRateKey.of(GBP, USD);
    coverImmutableBean(test);
    FxRateKey test2 = FxRateKey.of(EUR, CHF);
    coverBeanEquals(test, test2);
  }

  public void coverage_builder() {
    Meta meta = FxRateKey.meta();
    FxRateKey test1 = meta.builder().setString(meta.pair(), "EUR/GBP").build();
    FxRateKey test2 = meta.builder().setString(meta.pair().name(), "EUR/GBP").build();
    coverBeanEquals(test1, test2);
  }

  public void test_serialization() {
    FxRateKey test = FxRateKey.of(GBP, USD);
    assertSerialization(test);
  }

}
