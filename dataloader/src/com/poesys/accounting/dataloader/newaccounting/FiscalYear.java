/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.poesys.db.InvalidParametersException;


/**
 * A data transfer object containing data about a fiscal year, an accounting
 * period defined by a start date and end date. The fiscal year may be a
 * calendar year (Jan. 1 - Dec. 31) or any arbitrary annual period.
 * 
 * @author Robert J. Muller
 */
public class FiscalYear {
  /** the name of the fiscal year; usually the calendar year of the start date */
  private final Integer year;
  /** the date and time at which the fiscal year starts */
  private final Timestamp start;
  /** the date and time at which the fiscal year ends */
  private final Timestamp end;

  /** the set of accounts in the fiscal year */
  private final List<FiscalYearAccount> accounts =
    new ArrayList<FiscalYearAccount>();

  /**
   * the set of transactions in the fiscal year; each acocunt must exist in the
   * set of accounts for the fiscal year
   */
  private final Set<Transaction> transactions = new HashSet<Transaction>();

  private BigInteger maxId = null;

  /** a map of sets of transactions for each account, indexed on account */
  private final Map<Account, Set<Transaction>> transactionMap =
    new HashMap<Account, Set<Transaction>>();

  // Messages

  /** null parameter to constructor */
  private static final String NULL_PARAMETER_ERROR =
    "FiscalYear parameters are required but one is null";
  private static final String NULL_TRANSACTION_ERROR =
    "Transaction to add to fiscal year is null";
  private static final String NULL_ACCOUNT_ERROR =
    "account to add is null but is required";
  private static final String NO_DATE_ERROR =
    "date for comparison is null but is required";

  /**
   * Create a FiscalYear object.
   * 
   * @param year the name of the fiscal year; usually the calendar year of the
   *          start date
   */
  public FiscalYear(Integer year) {
    if (year == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.year = year;
    // Set start and end to calendar year values assumed by old accounting
    this.start = Timestamp.valueOf(year + "-01-01 00:00:00");
    this.end = Timestamp.valueOf(year + "-12-31 23:59:59");
  }

  /**
   * Get the fiscal year name.
   * 
   * @return a year
   */
  public Integer getYear() {
    return year;
  }

  /**
   * Get the start timestamp.
   * 
   * @return a timestamp
   */
  public Timestamp getStart() {
    return start;
  }

  /**
   * Get the end timestamp.
   * 
   * @return a timestamp
   */
  public Timestamp getEnd() {
    return end;
  }

  /**
   * Get the transaction set.
   * 
   * @return a set of transactions
   */
  public Set<Transaction> getTransactions() {
    return transactions;
  }

  /**
   * Get the largest transaction id in the set of transactions. This lets you
   * create a new transaction with the "next" id in sequence as required.
   * 
   * @return the maximum id in the transaction set
   */
  public BigInteger getMaxId() {
    return maxId;
  }

  /**
   * Add a transaction to the fiscal year, adding it to the set of transactions
   * and indexing the transactions by item accounts to optimize rollup.
   * 
   * @param transaction the transaction to add
   */
  public void addTransaction(Transaction transaction) {
    if (transaction == null) {
      throw new InvalidParametersException(NULL_TRANSACTION_ERROR);
    }
    transactions.add(transaction);
    // Set the new max id if the current id is larger.
    if (maxId == null || transaction.getId().compareTo(maxId) > 0) {
      // first id or new id greater than max id, set max id to current id
      maxId = transaction.getId();
    }
    // Add the transaction to the indexed map of transactions by account.
    for (Item item : transaction.getItems()) {
      Set<Transaction> set = transactionMap.get(item.getAccount());
      if (set == null) {
        // create a new set, add to map
        set = new HashSet<Transaction>();
        transactionMap.put(item.getAccount(), set);
      }
      set.add(transaction);
    }
  }

  /**
   * Is a date in the fiscal year? That means the date is between the start date
   * and the end date, inclusive.
   * 
   * @param date the timestamp to test against the start and end dates
   * @return true if the date is within the year, false if not
   */
  public Boolean isIn(Timestamp date) {
    if (date == null) {
      throw new InvalidParametersException(NO_DATE_ERROR);
    }
    Boolean in = Boolean.FALSE;
    if (date.compareTo(start) >= 0 && date.compareTo(end) <= 0) {
      in = Boolean.TRUE;
    }
    return in;
  }

  /**
   * Is a date in the fiscal year or a previous year? That means the specified
   * date is less than the end date of the fiscal year. This test helps identify
   * items relevant to computing the balance sheet totals.
   * 
   * @param date the timestamp to test against the end date
   * @return true if the date is less than the end date of the fiscal year,
   *         false if greater than that date
   */
  public Boolean isInYearOrPriorYear(Timestamp date) {
    if (date == null) {
      throw new InvalidParametersException(NO_DATE_ERROR);
    }
    Boolean in = Boolean.FALSE;
    if (date.compareTo(end) <= 0) {
      in = Boolean.TRUE;
    }
    return in;
  }

  /**
   * Get a threadsafe version of the set of accounts.
   * 
   * @return a threadsafe list of fiscal-year-account links
   */
  public List<FiscalYearAccount> getAccounts() {
    return Collections.synchronizedList(accounts);
  }

  /**
   * Link an account to the fiscal year. Ensure that the links are ordered by
   * account type, group, and account within the year.
   * 
   * @param account the account to add
   */
  public void addAccount(FiscalYearAccount account) {
    if (account == null) {
      throw new InvalidParametersException(NULL_ACCOUNT_ERROR);
    }
    accounts.add(account);
    Collections.sort(accounts);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((year == null) ? 0 : year.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FiscalYear other = (FiscalYear)obj;
    if (year == null) {
      if (other.year != null)
        return false;
    } else if (!year.equals(other.year))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "FiscalYear [year=" + year + ", start=" + start + ", end=" + end
           + "]";
  }
}
