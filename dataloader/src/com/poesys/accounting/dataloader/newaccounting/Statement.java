/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;


/**
 * An accounting statement for a fiscal year (balance sheet or income statement)
 * that contains a set of rollups for each account in the fiscal year of a type
 * appropriate for the kind of statement (Assets for Balance Sheet, for
 * example).
 * 
 * @author Robert J. Muller
 */
public class Statement {
  private static final Logger logger = Logger.getLogger(Statement.class);

  /** line delimiter string for data sets */
  private static final String LINE = "\n";

  /**
   * The types of statement supported by this program
   */
  public enum StatementType {
    /**
     * income statement displays rollups of transactions against income and
     * expense accounts
     */
    INCOME_STATEMENT, /**
     * balance sheet displays rollups of transactions against
     * asset, liability, and equity accounts
     */
    BALANCE_SHEET
  }

  /** the fiscal year of the statement */
  private final FiscalYear year;
  /** the statement name for display */
  private final String name;
  /** the statement type (balance sheet or income statement) */
  private final StatementType type;

  // Messages

  /** null parameter to constructor */
  private static final String NULL_PARAMETER_ERROR =
    "Statement parameters are required but one is null";

  /**
   * Create a Statement object.
   * 
   * @param year the fiscal year of the statement
   * @param name the name of the statement for display
   * @param type the kind of statement (balance sheet, income statement)
   */
  public Statement(FiscalYear year, String name, StatementType type) {
    if (year == null || name == null || type == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }

    this.type = type;
    this.year = year;
    this.name = name;
  }

  /**
   * Get the rollups. This is a factory method as well as a "getter" that
   * generates the Rollup objects for each account in the FiscalYear and puts it
   * into a map of Rollups indexed by account. There will be one entry in the
   * map for each account registered in the fiscal year.
   * 
   * @return a Map of Rollup objects indexed by Account
   */
  public Map<Account, Rollup> getRollups() {
    Map<Account, Rollup> rollups =
      new HashMap<Account, Rollup>(year.getAccounts().size());
    for (Account account : year.getAccounts()) {
      // Create a rollup for each account.
      Rollup rollup = new Rollup(this, account);
      // Index the rollup in the map.
      rollups.put(account, rollup);
    }
    return rollups;
  }

  /**
   * Get the year.
   * 
   * @return a year
   */
  public FiscalYear getYear() {
    return year;
  }

  /**
   * Get the name.
   * 
   * @return a name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the type.
   * 
   * @return a type
   */
  public StatementType getType() {
    return type;
  }

  /**
   * Get the current statement balance (sum of all rollups in the statement).
   * 
   * @return the balance
   */
  public Double getBalance() {
    Double balance = 0.00D;
    // Iterate through all the rollups in the dynamic rollup map, getting and
    // accumulating the sum of the rollup totals. This takes into account the
    // credit/debit, taking credits as positive numbers and debits as negative
    // numbers. Accumulation only happens for accounts of the right type for
    // the kind of statement this is (balance sheet or income statement).
    for (Rollup rollup : getRollups().values()) {
      Account account = rollup.getAccount();
      switch (type) {
      case BALANCE_SHEET:
        switch (account.getAccountType()) {
        case ASSET:
        case LIABILITY:
        case EQUITY:
          Double total = rollup.getTotal();
          balance += total;
          logger.debug("Balance Sheet account " + account.getName() + ": "
                       + total + ", balance = " + balance);
          break;
        default:
          // Ignore other type values, not part of balance sheet
          break;
        }
        break;
      case INCOME_STATEMENT:
        switch (account.getAccountType()) {
        case INCOME:
        case EXPENSE:
          Double total = rollup.getTotal();
          balance += total;
          logger.debug("Income statement account " + account.getName() + ": "
                       + total + ", balance = " + balance);
          break;
        default:
          // Ignore other type values, not part of income statement
          break;
        }
        break;
      default:
        // Should never happen, constructor checks type
        throw new RuntimeException("Invalid statement type");
      }
    }
    return balance;
  }

  @Override
  public String toString() {
    return "Statement [year=" + year + ", name=" + name + ", type=" + type
           + "]";
  }

  /**
   * Produce a data set of rollup data lines in a String suitable for writing
   * out as a data file, with line delimiters. Note that the last line should
   * not have a line delimiter.
   * 
   * @return a data set as a String
   */
  public String toData() {
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (Rollup rollup : getRollups().values()) {
      // append line delim between lines so last line is not delimited
      if (!first) {
        builder.append(LINE);
      } else {
        first = false;
      }
      builder.append(rollup.toData());
    }
    return builder.toString();
  }
}
