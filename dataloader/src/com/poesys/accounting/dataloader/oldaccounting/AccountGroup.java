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
package com.poesys.accounting.dataloader.oldaccounting;

import java.io.BufferedReader;

import com.poesys.db.InvalidParametersException;

/**
 * <p> Data transfer object representing old-accounting account group data; groups the accounts into
 * categories like Cash or Accounts Receivable by grouping the account numbers into ranges. Each
 * account group has a starting account number and and ending account number that defines the range
 * of accounts in the group. The contains() method lets the client determine whether an account
 * number is in this group. The groups within a year should be mutually exclusive; that is, the
 * start-end intervals must not overlap. The equals() method tests for equality based on name, year,
 * and account type. </p> <p> Reads the data from a tab-delimited file with three fields: start,
 * end, name </p> <p> This test suite tests the branches for the AbstractReaderDto superclass
 * constructor by instantiating a concrete subclass and verifying the appropriate exceptions. The
 * test suites for the other concrete subclasses thus do not test these cases. </p>
 *
 * @author Robert J. Muller
 */
public class AccountGroup extends AbstractReaderDto implements Comparable<AccountGroup> {
  /** the fiscal year to which the account group applies */
  private final Integer year;
  /** the name of the group */
  private String name;
  /** first account number in group */
  private Float start;
  /** last account number in group */
  private Float end;
  /** rank order of group within account type */
  private Integer orderNumber;

  // Messages

  /**
   * Create a AccountGroup object.
   *
   * @param year  the fiscal year to which the account group applies
   * @param name  the name of the group
   * @param start first account number in group
   * @param end   last account number in group
   */
  public AccountGroup(Integer year, String name, Float start, Float end) {
    if (year == null || name == null || start == null || end == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.year = year;
    this.name = name;
    this.start = start;
    this.end = end;
  }

  /**
   * Create an AccountGroup object reading from a tab-delimited line. The client is responsible for
   * opening and closing the reader. The client should catch the EndOfStream throwable to determine
   * when reading is complete and to then close the reader.
   *
   * @param year   the fiscal year being read
   * @param reader the buffered reader set at the current line to read
   */
  public AccountGroup(Integer year, BufferedReader reader) {
    super(reader);
    if (year == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.year = year;
  }

  @Override
  protected void init(String[] fields) {
    this.start = new Float(fields[0]);
    this.end = new Float(fields[1]);
    this.name = fields[2].trim();
  }

  @Override
  protected int numberOfFields() {
    return 3;
  }

  /**
   * Get the year.
   *
   * @return a year
   */
  public Integer getYear() {
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
   * Get the starting account.
   *
   * @return an account number
   */
  public Float getStart() {
    return start;
  }

  /**
   * Get the ending account number.
   *
   * @return an account number
   */
  public Float getEnd() {
    return end;
  }

  /**
   * Get the group order number.
   *
   * @return an integer
   */
  public Integer getOrderNumber() {
    return orderNumber;
  }

  /**
   * Set the group order number.
   *
   * @param orderNumber an integer rank ordering of the group
   */
  public void setOrderNumber(Integer orderNumber) {
    this.orderNumber = orderNumber;
  }

  /**
   * Does this account group contain the account?
   *
   * @param year    the fiscal year
   * @param account the account to look up
   * @return true if the group includes the account number
   */
  public boolean contains(Integer year, Float account) {
    boolean contains = false;
    if (this.year.equals(year)) {
      // Same year, do the account lookup
      if (account.compareTo(start) >= 0) {
        // greater than or equal to start of interval
        if (account.compareTo(end) <= 0) {
          // between start and end, inclusive
          contains = true;
        }
      }
    }
    return contains;
  }

  /**
   * Get the account type of the group.
   *
   * @return an account type or null if the interval does not correspond to a known account type
   */
  public com.poesys.accounting.dataloader.newaccounting.AccountType getAccountType() {
    com.poesys.accounting.dataloader.newaccounting.AccountType type = null;
    if (start.compareTo(100.00F) >= 0 && end.compareTo(200.00F) < 0) {
      type = com.poesys.accounting.dataloader.newaccounting.AccountType.ASSETS;
    } else if (start.compareTo(200.00F) >= 0 && end.compareTo(300.00F) < 0) {
      type = com.poesys.accounting.dataloader.newaccounting.AccountType.LIABILITIES;
    } else if (start.compareTo(300.00F) >= 0 && end.compareTo(400.00F) < 0) {
      type = com.poesys.accounting.dataloader.newaccounting.AccountType.EQUITY;
    } else if (start.compareTo(400.00F) >= 0 && end.compareTo(600.00F) < 0) {
      type = com.poesys.accounting.dataloader.newaccounting.AccountType.INCOME;
    } else if (start.compareTo(600.00F) >= 0 && end.compareTo(900.00F) < 0) {
      type = com.poesys.accounting.dataloader.newaccounting.AccountType.EXPENSES;
    }
    return type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((year == null) ? 0 : year.hashCode());
    result = prime * result + ((getAccountType() == null) ? 0 : getAccountType().hashCode());
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
    AccountGroup other = (AccountGroup)obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (year == null) {
      if (other.year != null) {
        return false;
      }
    } else if (!year.equals(other.year)) {
      return false;
    }
    if (getAccountType() == null) {
      if (other.getAccountType() != null) {
        return false;
      }
    } else if (!getAccountType().equals(other.getAccountType())) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(AccountGroup o) {
    int returnValue;
    // for sorting, compare by year, then by non-overlapping start within year
    returnValue = year.compareTo(o.year);
    if (returnValue == 0) {
      returnValue = start.compareTo(o.start);
    }
    return returnValue;
  }

  @Override
  public String toString() {
    return "AccountGroup [year=" + year + ", name=" + name + ", start=" + start + ", end=" + end +
           ", orderNumber=" + orderNumber + ", getAccountType()=" + getAccountType() + "]";
  }
}
