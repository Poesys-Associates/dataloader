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

import com.poesys.db.InvalidParametersException;
import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * A data transfer object containing data about a fiscal year, an accounting period defined by a
 * start date and end date. The fiscal year may be a calendar year (Jan. 1 - Dec. 31) or any
 * arbitrary annual period.</p>
 *
 * <p>TODO: the current fiscal-year implementation assumes only a single fiscal year in the
 * database for a specific numeric year. This should expand to a named fiscal year with no
 * assumptions about the calendar year. Fiscal year should remain independent of a specific
 * entity, though.</p>
 *
 * @author Robert J. Muller
 */
public class FiscalYear {
  private static final Logger logger = Logger.getLogger(FiscalYear.class);
  /** the name of the fiscal year; usually the calendar year of the start date */
  private final Integer year;
  /** the date and time at which the fiscal year starts */
  private final Timestamp start;
  /** the date and time at which the fiscal year ends */
  private final Timestamp end;
  /** the last "old" identifier allocated to a transaction */
  private BigInteger id = BigInteger.ZERO;

  /** the set of accounts in the fiscal year */
  private final List<FiscalYearAccount> accounts = new ArrayList<>();

  // Messages

  private static final String NULL_PARAMETER_ERROR =
    "FiscalYear parameters are required but one is null";
  private static final String NULL_ACCOUNT_ERROR = "account to add is null but is required";
  private static final String NO_DATE_ERROR = "date for comparison is null but is required";
  public static final String NO_ID_ERROR = "no last id supplied but one is required";

  /**
   * Create a FiscalYear object.
   *
   * @param year the name of the fiscal year; usually the calendar year of the start date
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
   * Get the last id for the fiscal year; this is either the default id ONE or the last id
   * registered with setLastId().
   *
   * @return the current id
   */
  public BigInteger getId() {
    return id;
  }

  /**
   * Get the next "old" id for the fiscal year, incrementing the stored "last" id by one. This
   * method supports creating new transactions for the accounting year, such as the capital and
   * distribution and income summary transactions. Make sure you call setLastId() before calling
   * this method, or the method will return ONE.
   *
   * @return the next identifier
   */
  public BigInteger getNextId() {
    id = id.add(BigInteger.ONE);
    return id;
  }

  /**
   * If the current "old" id is less than the specified id, reset the id to the new id. This logic
   * maintains the greatest id value processed from a set of transactions with ids already
   * allocated and does not depend on the order of reading those transactions.
   *
   * @param id the identifier value to set to become the last id allocated if it is greater than
   *           the current id
   */
  public void setLastId(BigInteger id) {
    if (id == null) {
      throw new InvalidParametersException(NO_ID_ERROR);
    }
    if (id.compareTo(this.id) > 0) {
      this.id = id;
    }
  }

  /**
   * Is a date in the fiscal year? That means the date is between the start date and the end date,
   * inclusive.
   *
   * @param date the timestamp to test against the start and end dates
   * @return true if the date is within the year, false if not
   */
  Boolean isIn(Timestamp date) {
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
   * Is a date in the fiscal year or a previous year? That means the specified date is less than the
   * end date of the fiscal year. This test helps identify items relevant to computing the balance
   * sheet totals.
   *
   * @param date the timestamp to test against the end date
   * @return true if the date is less than the end date of the fiscal year, false if greater than
   * that date
   */
  Boolean isInYearOrPriorYear(Timestamp date) {
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
   * Link an account to the fiscal year. Ensure that the links are ordered by account type, group,
   * and account within the year.
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FiscalYear other = (FiscalYear)obj;
    return year.equals(other.year);
  }

  @Override
  public String toString() {
    return "FiscalYear [year=" + year + ", start=" + start + ", end=" + end + "]";
  }
}
