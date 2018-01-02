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

/**
 * A Data Transfer Object that represents a link between a fiscal year and an account. The link
 * specifies the account type and account group that contain the account in the year. The group has
 * a relative order within the account type, and the account has a relative order within the account
 * group. For a given fiscal year, the orderings produce a sortable ordering of the types, groups,
 * and accounts useful in producing accounting statements.
 *
 * @author Robert J. Muller
 */
public class FiscalYearAccount implements Comparable<FiscalYearAccount> {
  /** the fiscal year */
  private final FiscalYear fiscalYear;
  /** the kind of account: Asset, Liability, Equity, Income, Expense */
  private final AccountType accountType;
  /** the account group of the account in the fiscal year */
  private final AccountGroup group;
  /** relative order of group within account type */
  private final Integer groupOrderNumber;
  /** the account */
  private final Account account;
  /** relative order of account within account group */
  private final Integer accountOrderNumber;

  /**
   * Create a FiscalYearAccount object.
   *
   * @param fiscalYear         the linked fiscal year
   * @param accountType        the type of account
   * @param group              the grouping of the account within the account type
   * @param groupOrderNumber   the relative order of the group within the account type
   * @param account            the linked account
   * @param accountOrderNumber the relative order of the account within the group
   */
  public FiscalYearAccount(FiscalYear fiscalYear, AccountType accountType, AccountGroup group,
                           Integer groupOrderNumber, Account account, Integer accountOrderNumber) {
    this.fiscalYear = fiscalYear;
    this.accountType = accountType;
    this.group = group;
    this.groupOrderNumber = groupOrderNumber;
    this.account = account;
    this.accountOrderNumber = accountOrderNumber;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((account == null) ? 0 : account.hashCode());
    result = prime * result + ((fiscalYear == null) ? 0 : fiscalYear.hashCode());
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
    FiscalYearAccount other = (FiscalYearAccount)obj;
    if (account == null) {
      if (other.account != null) {
        return false;
      }
    } else if (!account.equals(other.account)) {
      return false;
    }
    if (fiscalYear == null) {
      if (other.fiscalYear != null) {
        return false;
      }
    } else if (!fiscalYear.equals(other.fiscalYear)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(FiscalYearAccount obj) {
    int returnValue = 0;

    // Ensure object for comparison exists.
    if (obj == null) {
      throw new RuntimeException("null fiscal year account for compareTo()");
    }

    if (getClass() != obj.getClass()) {
      throw new RuntimeException(
        "comparison object is not class FiscalYearAccount: " + obj.getClass().getName());
    }

    // First compare for equality.
    if (this != obj && !this.equals(obj)) {
      // compare fiscal year
      returnValue = fiscalYear.getYear().compareTo(obj.getFiscalYear().getYear());
      if (returnValue == 0) {
        // same year, compare account type
        returnValue = accountType.compareTo(obj.accountType);
        if (returnValue == 0) {
          // same type, compare group order
          returnValue = groupOrderNumber.compareTo(obj.groupOrderNumber);
          if (returnValue == 0) {
            // same group order within type, compare account order within group
            returnValue = accountOrderNumber.compareTo(obj.accountOrderNumber);
          }
        }
      }
    }

    return returnValue;
  }

  /**
   * Get the fiscal year.
   *
   * @return a fiscal year
   */
  public FiscalYear getFiscalYear() {
    return fiscalYear;
  }

  /**
   * Get the accountType.
   *
   * @return a accountType
   */
  public AccountType getAccountType() {
    return accountType;
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
   * Get the accountOrderNumber.
   *
   * @return a accountOrderNumber
   */
  public Integer getAccountOrderNumber() {
    return accountOrderNumber;
  }

  /**
   * Get the account group.
   *
   * @return an account group
   */
  public AccountGroup getGroup() {
    return group;
  }

  /**
   * Get the groupOrderNumber.
   *
   * @return a groupOrderNumber
   */
  public Integer getGroupOrderNumber() {
    return groupOrderNumber;
  }

  @Override
  public String toString() {
    return "FiscalYearAccount [fiscalYear=" + fiscalYear.getYear() + ", accountType=" +
           accountType.name() + ", group=" + group.getName() + ", groupOrderNumber=" +
           groupOrderNumber + ", account=" + account.getName() + ", accountOrderNumber=" +
           accountOrderNumber + "]";
  }
}
