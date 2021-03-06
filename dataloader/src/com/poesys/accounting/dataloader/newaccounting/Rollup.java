/*
 * Copyright (c) 2018 Poesys Associates. All rights reserved.
 *
 * This file is part of Poesys/Dataloader.
 *
 * Poesys/Dataloader is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Poesys/Dataloader is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Poesys/Dataloader. If not, see <http://www.gnu.org/licenses/>.
 */
package com.poesys.accounting.dataloader.newaccounting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.accounting.dataloader.newaccounting.Statement.StatementType;
import com.poesys.db.InvalidParametersException;

/**
 * A data transfer object containing the sum of a set of items against a specific account contained
 * within a statement for a fiscal year
 *
 * @author Robert J. Muller
 */
public class Rollup implements Comparable<Rollup> {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(Rollup.class);

  /** the parent statement */
  private final Statement statement;

  /** the account being summed */
  private final Account account;

  /** data value delimiter for data strings */
  private static final String DELIMITER = "\t";
  /** line delimiter for data strings */
  private static final String LINE_DELIMITER = "\n";

  /** format for double data values - [n]+.nn */
  private static DecimalFormat format = new DecimalFormat("0.00");

  // Messages

  private static final String NULL_PARAMETER_ERROR =
    "Rollup parameters are required but one is null";
  private static final String NO_STATEMENT_YEAR_ERROR = "no year for statement";
  private static final String NO_ACCOUNT_TYPE_ERROR = "no account type for rollup account";
  private static final String NULL_ROLLUP_ERROR = "null rollup in compareTo()";

  /**
   * Create a Rollup object.
   *
   * @param statement the parent statement
   * @param account   the account being summed
   */
  public Rollup(Statement statement, Account account) {
    if (statement == null || account == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.statement = statement;
    this.account = account;
  }

  /**
   * Get the account.
   *
   * @return an account
   */
  public Account getAccount() {
    return account;
  }

  /**
   * Sum the items against the account for the fiscal year and return the total. Arithmetic uses
   * BigDecimal and the total returned is a BigDecimal for accurately scaled comparisons.
   *
   * @return the sum of the items against the account taking credits as positive numbers and debits
   * as negative numbers
   */
  public BigDecimal getTotal() {
    BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    FiscalYear year = statement.getYear();
    for (Item item : account.getItems()) {
      Transaction transaction = item.getTransaction();
      Statement.StatementType type = statement.getType();
      // Iterate through all years up to the current year for balance sheet,
      // only current year for income statement.
      if ((type.equals(StatementType.BALANCE_SHEET) &&
           year.isInYearOrPriorYear(transaction.getDate())) ||
          (type.equals(StatementType.INCOME_STATEMENT) && year.isIn(transaction.getDate()))) {
        BigDecimal amount = new BigDecimal(item.getAmount()).setScale(2, RoundingMode.HALF_DOWN);
        // Negate the amount for debit items.
        amount = item.isDebit() ? amount.negate() : amount;
        total = total.add(amount);
        logger.debug(
          "Added item to " + account.getName() + " total for fiscal year " + year.getYear() + ": " +
          amount + ", total = " + total);
      }
    }
    return total;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + account.hashCode();
    result = prime * result + statement.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Rollup other = (Rollup)obj;
    return account.equals(other.account) && statement.equals(other.statement);
  }

  @Override
  public int compareTo(Rollup obj) {
    int returnValue = 0;

    // Check inputs valid
    if (obj == null) {
      throw new RuntimeException(NULL_ROLLUP_ERROR);
    }

    // First compare for equality.
    if (!this.equals(obj)) {
      // not equal, compare account type
      // Get the fiscal-year-account links for the two objects.
      FiscalYearAccount thisLink = getFiscalYearAccount(statement.getYear(), account);
      FiscalYearAccount thatLink = getFiscalYearAccount(statement.getYear(), obj.account);
      AccountType thisType = account.getAccountType(statement.getYear());
      AccountType thatType = obj.account.getAccountType(statement.getYear());
      returnValue = thisType.compareTo(thatType);
      if (returnValue == 0) {
        // same type, compare account group order
        Integer thisGroupOrder = thisLink.getGroupOrderNumber();
        Integer thatGroupOrder = thatLink.getGroupOrderNumber();
        returnValue = thisGroupOrder.compareTo(thatGroupOrder);
        if (returnValue == 0) {
          // same group order, compare account order
          Integer thisAccountOrder = thisLink.getAccountOrderNumber();
          Integer thatAccountOrder = thatLink.getAccountOrderNumber();
          returnValue = thisAccountOrder.compareTo(thatAccountOrder);
        }
      }
    }

    return returnValue;
  }

  /**
   * Build a fiscal-year-account link given a year and account.
   *
   * @param year    the fiscal year to link
   * @param account the account to link
   * @return a fiscal-year-account link
   */
  private FiscalYearAccount getFiscalYearAccount(FiscalYear year, Account account) {
    FiscalYearAccount returnLink = null;

    for (FiscalYearAccount link : account.getYears()) {
      if (link.getFiscalYear().equals(year)) {
        returnLink = link;
        break;
      }
    }
    return returnLink;
  }

  @Override
  public String toString() {
    return "Rollup [statement=" + statement + ", account=" + account + ", total=" +
           format.format(getTotal()) + "]";
  }

  /**
   * Return the rollup as a tab-delimited data string with four fields: account type, account group,
   * account name, and total (formatted to two decimal places).
   *
   * @return the tab-delimited data string
   */
  public String toData() {
    if (statement.getYear() == null) {
      throw new RuntimeException(NO_STATEMENT_YEAR_ERROR);
    }
    AccountType type = account.getAccountType(statement.getYear());
    if (type == null) {
      throw new RuntimeException(NO_ACCOUNT_TYPE_ERROR);
    }
    StringBuilder builder =
      new StringBuilder(account.getAccountType(statement.getYear()).toString());
    builder.append(DELIMITER);
    builder.append(account.getGroup(statement.getYear()).getName());
    builder.append(DELIMITER);
    builder.append(account.getName());
    builder.append(DELIMITER);
    builder.append(format.format(getTotal()));
    return builder.toString();
  }

  /**
   * Return the rollup as a set of tab-delimited data lines with each line containing a detail with
   * the fields account type, account group, account name, the old transaction id, the transaction
   * date, and the amount (debits are negative). Do not write out a line for rollups with no items.
   * The items sort in natural order.
   *
   * @return the detail data
   */
  public String toDetailsData() {
    StringBuilder data = new StringBuilder("");
    FiscalYear year = statement.getYear();
    String line = "";

    // Get the items and sort.
    List<Item> items = new ArrayList<>(account.getItems());
    Collections.sort(items);

    for (Item item : items) {
      Transaction transaction = item.getTransaction();

      boolean balanceSheetAccount =
        account.getAccountType(statement.getYear()).equals(AccountType.ASSETS) ||
        account.getAccountType(statement.getYear()).equals(AccountType.LIABILITIES) ||
        account.getAccountType(statement.getYear()).equals(AccountType.EQUITY);

      // Process all transactions for balance sheet accounts, only those for the
      // current year for income statement accounts.
      if ((balanceSheetAccount && year.isInYearOrPriorYear(transaction.getDate())) ||
          year.isIn(transaction.getDate())) {
        // Item is in year

        // Construct the date formatted as an Oracle date.
        String format = "dd-MMM-yy";
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String date = formatter.format(transaction.getDate());

        // Scale the amount as 2digit, then negate the amount for debit items.
        BigDecimal amount = new BigDecimal(item.getAmount()).setScale(2, RoundingMode.HALF_DOWN);
        amount = item.isDebit() ? amount.negate() : amount;

        // Build the data line.
        data.append(line);
        data.append(account.getAccountType(statement.getYear()).toString());
        data.append(DELIMITER);
        data.append(account.getGroup(statement.getYear()).getName());
        data.append(DELIMITER);
        data.append(item.getAccount().getName());
        data.append(DELIMITER);
        data.append(item.getTransaction().getId());
        data.append(DELIMITER);
        data.append(date);
        data.append(DELIMITER);
        data.append(amount);
        line = LINE_DELIMITER;
      }
    }

    return data.toString();
  }
}
