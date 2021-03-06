/*
 * Copyright (C) 2017 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.loader.csv;

import static java.util.stream.Collectors.toList;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.StandardId;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.Guavate;
import com.opengamma.strata.collect.io.CsvIterator;
import com.opengamma.strata.collect.io.CsvRow;
import com.opengamma.strata.collect.io.ResourceLocator;
import com.opengamma.strata.collect.io.UnicodeBom;
import com.opengamma.strata.collect.result.FailureItem;
import com.opengamma.strata.collect.result.FailureReason;
import com.opengamma.strata.collect.result.ValueWithFailures;
import com.opengamma.strata.loader.LoaderUtils;
import com.opengamma.strata.product.SecurityTrade;
import com.opengamma.strata.product.Trade;
import com.opengamma.strata.product.TradeInfo;
import com.opengamma.strata.product.TradeInfoBuilder;
import com.opengamma.strata.product.deposit.TermDepositTrade;
import com.opengamma.strata.product.deposit.type.TermDepositConventions;
import com.opengamma.strata.product.fra.FraTrade;
import com.opengamma.strata.product.fra.type.FraConventions;
import com.opengamma.strata.product.swap.SwapTrade;
import com.opengamma.strata.product.swap.type.SingleCurrencySwapConvention;

/**
 * Loads trades from CSV files.
 * <p>
 * The trades are expected to be in a CSV format known to Strata.
 * The parser is flexible, understanding a number of different ways to define each trade.
 * Columns may occur in any order.
 * 
 * <h4>Common</h4>
 * <p>
 * The following standard columns are supported:<br />
 * <ul>
 * <li>The 'Strata Trade Type' column is required, and must be the instrument type,
 *   such as 'Fra' or 'Swap'
 * <li>The 'Id Scheme' column is optional, and is the name of the scheme that the trade
 *   identifier is unique within, such as 'OG-Trade'
 * <li>The 'Id' column is optional, and is the identifier of the trade,
 *   such as 'FRA12345'
 * <li>The 'Trade Date' column is optional, and is the date that the trade occurred,
 *   such as '2017-08-01'
 * <li>The 'Trade Time' column is optional, and is the time of day that the trade occurred,
 *   such as '11:30'
 * <li>The 'Trade Zone' column is optional, and is the time-zone that the trade occurred,
 *   such as 'Europe/London'
 * </ul>
 * 
 * <h4>Fra</h4>
 * <p>
 * The following columns are supported for 'Fra' trades:
 * <ul>
 * <li>'Buy Sell' - mandatory
 * <li>'Notional' - mandatory
 * <li>'Fixed Rate' - mandatory, percentage
 * <li>'Convention' - see below, see {@link FraConventions}
 * <li>'Period To Start' - see below
 * <li>'Start Date' - see below
 * <li>'End Date' - see below
 * <li>'Index' - see below
 * <li>'Interpolated Index' - see below
 * <li>'Day Count' - see below
 * <li>'Date Convention' - optional
 * <li>'Date Calendar' - optional
 * </ul>
 * <p>
 * Valid combinations to define a FRA are:
 * <ul>
 * <li>'Convention', 'Trade Date', 'Period To Start'
 * <li>'Convention', 'Start Date', 'End Date'
 * <li>'Index', 'Start Date', 'End Date' plus optionally 'Interpolated Index', 'Day Count'
 * </ul>
 * 
 * <h4>Swap</h4>
 * <p>
 * The following columns are supported for 'Swap' trades:
 * <ul>
 * <li>'Buy Sell' - mandatory
 * <li>'Notional' - mandatory
 * <li>'Fixed Rate' - mandatory, percentage (treated as the spread for some swap types)
 * <li>'Convention' - mandatory, see {@link SingleCurrencySwapConvention} implementations
 * <li>'Period To Start'- see below
 * <li>'Tenor'- see below
 * <li>'Start Date'- see below
 * <li>'End Date'- see below
 * <li>'Roll Convention' - optional
 * <li>'Stub Convention' - optional
 * <li>'First Regular Start Date' - optional
 * <li>'Last Regular End Date' - optional
 * <li>'Date Convention' - optional
 * <li>'Date Calendar' - optional
 * </ul>
 * <p>
 * Valid combinations to define a Swap are:
 * <ul>
 * <li>'Convention', 'Trade Date', 'Period To Start', 'Tenor'
 * <li>'Convention', 'Start Date', 'End Date'
 * <li>'Convention', 'Start Date', 'Tenor'
 * <li>Explicitly by defining each leg (not detailed here)
 * </ul>
 * 
 * <h4>Term Deposit</h4>
 * <p>
 * The following columns are supported for 'TermDeposit' trades:
 * <ul>
 * <li>'Buy Sell' - mandatory
 * <li>'Notional' - mandatory
 * <li>'Fixed Rate' - mandatory, percentage
 * <li>'Convention'- see below, see {@link TermDepositConventions} implementations
 * <li>'Tenor'- see below
 * <li>'Start Date'- see below
 * <li>'End Date'- see below
 * <li>'Currency'- see below
 * <li>'Day Count'- see below
 * <li>'Date Convention' - optional
 * <li>'Date Calendar' - optional
 * </ul>
 * <p>
 * Valid combinations to define a Term Deposit are:
 * <ul>
 * <li>'Convention', 'Trade Date', 'Period To Start'
 * <li>'Convention', 'Start Date', 'End Date'
 * <li>'Start Date', 'End Date', 'Currency', 'Day Count'
 * </ul>
 * 
 * <h4>Security</h4>
 * <p>
 * The following columns are supported for 'Security' trades:
 * <ul>
 * <li>'Security Id Scheme' - optional, defaults to 'OG-Security'
 * <li>'Security Id' - mandatory
 * <li>'Quantity' - see below
 * <li>'Long Quantity' - see below
 * <li>'Short Quantity' - see below
 * <li>'Price' - optional
 * </ul>
 * <p>
 * The quantity will normally be set from the 'Quantity' column.
 * If that column is not found, the 'Long Quantity' and 'Short Quantity' columns will be used instead.
 */
public final class TradeCsvLoader {

  // default schemes
  private static final String DEFAULT_TRADE_SCHEME = "OG-Trade";

  // shared CSV headers
  static final String TRADE_DATE_FIELD = "Trade Date";
  static final String CONVENTION_FIELD = "Convention";
  static final String BUY_SELL_FIELD = "Buy Sell";
  static final String CURRENCY_FIELD = "Currency";
  static final String NOTIONAL_FIELD = "Notional";
  static final String INDEX_FIELD = "Index";
  static final String INTERPOLATED_INDEX_FIELD = "Interpolated Index";
  static final String FIXED_RATE_FIELD = "Fixed Rate";
  static final String PERIOD_TO_START_FIELD = "Period To Start";
  static final String TENOR_FIELD = "Tenor";
  static final String START_DATE_FIELD = "Start Date";
  static final String END_DATE_FIELD = "End Date";
  static final String DATE_ADJ_CNV_FIELD = "Date Convention";
  static final String DATE_ADJ_CAL_FIELD = "Date Calendar";
  static final String DAY_COUNT_FIELD = "Day Count";

  // CSV column headers
  private static final String TYPE_FIELD = "Strata Trade Type";
  private static final String ID_SCHEME_FIELD = "Id Scheme";
  private static final String ID_FIELD = "Id";
  private static final String TRADE_TIME_FIELD = "Trade Time";
  private static final String TRADE_ZONE_FIELD = "Trade Zone";

  /**
   * The resolver, providing additional information.
   */
  private final CsvInfoResolver resolver;

  //-------------------------------------------------------------------------
  /**
   * Obtains an instance that uses the standard set of reference data.
   * 
   * @return the loader
   */
  public static TradeCsvLoader standard() {
    return new TradeCsvLoader(CsvInfoResolver.standard());
  }

  /**
   * Obtains an instance that uses the specified set of reference data.
   * 
   * @param refData  the reference data
   * @return the loader
   */
  public static TradeCsvLoader of(ReferenceData refData) {
    return new TradeCsvLoader(CsvInfoResolver.of(refData));
  }

  /**
   * Obtains an instance that uses the specified resolver for additional information.
   * 
   * @param resolver  the resolver used to parse additional information
   * @return the loader
   */
  public static TradeCsvLoader of(CsvInfoResolver resolver) {
    return new TradeCsvLoader(resolver);
  }

  // restricted constructor
  private TradeCsvLoader(CsvInfoResolver resolver) {
    this.resolver = ArgChecker.notNull(resolver, "resolver");
  }

  //-------------------------------------------------------------------------
  /**
   * Loads one or more CSV format trade files.
   * <p>
   * CSV files sometimes contain a Unicode Byte Order Mark.
   * This method uses {@link UnicodeBom} to interpret it.
   * 
   * @param resources  the CSV resources
   * @return the loaded trades, trade-level errors are captured in the result
   */
  public ValueWithFailures<List<Trade>> load(ResourceLocator... resources) {
    return load(Arrays.asList(resources));
  }

  /**
   * Loads one or more CSV format trade files.
   * <p>
   * CSV files sometimes contain a Unicode Byte Order Mark.
   * This method uses {@link UnicodeBom} to interpret it.
   * 
   * @param resources  the CSV resources
   * @return the loaded trades, all errors are captured in the result
   */
  public ValueWithFailures<List<Trade>> load(Collection<ResourceLocator> resources) {
    Collection<CharSource> charSources = resources.stream()
        .map(r -> UnicodeBom.toCharSource(r.getByteSource()))
        .collect(toList());
    return parse(charSources);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks whether the source is a CSV format trade file.
   * <p>
   * This parses the headers as CSV and checks that mandatory headers are present.
   * This is determined entirely from the 'Strata Trade Type' column.
   * 
   * @param charSource  the CSV character source to check
   * @return true if the source is a CSV file with known headers, false otherwise
   */
  public boolean isKnownFormat(CharSource charSource) {
    try (CsvIterator csv = CsvIterator.of(charSource, true)) {
      return csv.containsHeader(TYPE_FIELD);
    } catch (RuntimeException ex) {
      return false;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Parses one or more CSV format trade files.
   * <p>
   * CSV files sometimes contain a Unicode Byte Order Mark.
   * Callers are responsible for handling this, such as by using {@link UnicodeBom}.
   * 
   * @param charSources  the CSV character sources
   * @return the loaded trades, all errors are captured in the result
   */
  public ValueWithFailures<List<Trade>> parse(Collection<CharSource> charSources) {
    return parse(charSources, Trade.class);
  }

  /**
   * Parses one or more CSV format trade files.
   * <p>
   * A type is specified to filter the trades.
   * <p>
   * CSV files sometimes contain a Unicode Byte Order Mark.
   * Callers are responsible for handling this, such as by using {@link UnicodeBom}.
   * 
   * @param <T>  the trade type
   * @param charSources  the CSV character sources
   * @param tradeType  the trade type to return
   * @return the loaded trades, all errors are captured in the result
   */
  public <T extends Trade> ValueWithFailures<List<T>> parse(Collection<CharSource> charSources, Class<T> tradeType) {
    try {
      ValueWithFailures<List<T>> result = ValueWithFailures.of(ImmutableList.of());
      for (CharSource charSource : charSources) {
        ValueWithFailures<List<T>> singleResult = parseFile(charSource, tradeType);
        result = result.combinedWith(singleResult, Guavate::concatToList);
      }
      return result;

    } catch (RuntimeException ex) {
      return ValueWithFailures.of(ImmutableList.of(), FailureItem.of(FailureReason.ERROR, ex));
    }
  }

  // loads a single CSV file, filtering by trade type
  private <T extends Trade> ValueWithFailures<List<T>> parseFile(CharSource charSource, Class<T> tradeType) {
    try (CsvIterator csv = CsvIterator.of(charSource, true)) {
      if (!csv.headers().contains(TYPE_FIELD)) {
        return ValueWithFailures.of(ImmutableList.of(),
            FailureItem.of(FailureReason.PARSING, "CSV file does not contain '{}' header: {}", TYPE_FIELD, charSource));
      }
      return parseFile(csv, tradeType);

    } catch (RuntimeException ex) {
      return ValueWithFailures.of(ImmutableList.of(),
          FailureItem.of(FailureReason.PARSING, ex, "CSV file could not be parsed: {}", charSource));
    }
  }

  // loads a single CSV file
  private <T extends Trade> ValueWithFailures<List<T>> parseFile(CsvIterator csv, Class<T> tradeType) {
    List<T> trades = new ArrayList<>();
    List<FailureItem> failures = new ArrayList<>();
    for (CsvRow row : (Iterable<CsvRow>) () -> csv) {
      try {
        String typeRaw = row.getField(TYPE_FIELD);
        String type = typeRaw.toUpperCase(Locale.ENGLISH);
        TradeInfo info = parseTradeInfo(row);
        switch (type.toUpperCase(Locale.ENGLISH)) {
          case "FRA":
            if (tradeType == FraTrade.class || tradeType == Trade.class) {
              trades.add(tradeType.cast(FraTradeCsvLoader.parse(row, info, resolver)));
            }
            break;
          case "SECURITY":
            if (tradeType == SecurityTrade.class || tradeType == Trade.class) {
              trades.add(tradeType.cast(SecurityCsvLoader.parseTrade(row, info, resolver)));
            }
            break;
          case "SWAP":
            if (tradeType == SwapTrade.class || tradeType == Trade.class) {
              trades.add(tradeType.cast(SwapTradeCsvLoader.parse(row, info, resolver)));
            }
            break;
          case "TERMDEPOSIT":
          case "TERM DEPOSIT":
            if (tradeType == TermDepositTrade.class || tradeType == Trade.class) {
              trades.add(tradeType.cast(TermDepositTradeCsvLoader.parse(row, info, resolver)));
            }
            break;
          default:
            failures.add(FailureItem.of(
                FailureReason.PARSING, "CSV file trade type '{}' is not known at line {}", typeRaw, row.lineNumber()));
            break;
        }
      } catch (RuntimeException ex) {
        failures.add(FailureItem.of(
            FailureReason.PARSING, ex, "CSV file trade could not be parsed at line {}: " + ex.getMessage(), row.lineNumber()));
      }
    }
    return ValueWithFailures.of(trades, failures);
  }

  // parse the trade info
  private TradeInfo parseTradeInfo(CsvRow row) {
    TradeInfoBuilder infoBuilder = TradeInfo.builder();
    String scheme = row.findField(ID_SCHEME_FIELD).orElse(DEFAULT_TRADE_SCHEME);
    row.findValue(ID_FIELD).ifPresent(id -> infoBuilder.id(StandardId.of(scheme, id)));
    row.findValue(TRADE_DATE_FIELD).ifPresent(dateStr -> infoBuilder.tradeDate(LoaderUtils.parseDate(dateStr)));
    row.findValue(TRADE_TIME_FIELD).ifPresent(timeStr -> infoBuilder.tradeTime(LoaderUtils.parseTime(timeStr)));
    row.findValue(TRADE_ZONE_FIELD).ifPresent(zoneStr -> infoBuilder.zone(ZoneId.of(zoneStr)));
    resolver.parseTradeInfo(row, infoBuilder);
    return infoBuilder.build();
  }

}
