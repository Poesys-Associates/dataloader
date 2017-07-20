/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import java.io.BufferedReader;

import com.poesys.accounting.dataloader.newaccounting.Account.AccountType;
import com.poesys.db.InvalidParametersException;


/**
 * Data transfer object for old-accounting Account data. There are 3 data
 * fields: account number, name, and default debit.
 * 
 * @author Robert J. Muller
 */
public class Account extends AbstractReaderDto {
  /** fiscal year in which the account exists */
  private Integer year;
  /** the unique identifier for the account within the fiscal year */
  private Float accountNumber;
  /** the account name */
  private String name;
  /** whether items in the account default to debit */
  private Boolean defaultDebit;

  // Messages

  /** null parameter to constructor */
  private static final String NULL_PARAMETER_ERROR =
    "Account parameters are required but one is null";

  /**
   * Create an Account object.
   * 
   * @param year the number of the fiscal year in which the account exists
   * @param accountNumber the unique identifier for the account in the old
   *          system
   * @param name the account name, which should be unique for the same account
   *          across multiple years
   * @param defaultDebit whether items in the account default to debit
   */
  public Account(Integer year,
                 Float accountNumber,
                 String name,
                 Boolean defaultDebit) {
    if (year == null || accountNumber == null || name == null
        || defaultDebit == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.year = year;
    this.accountNumber = accountNumber;
    this.name = name;
    this.defaultDebit = defaultDebit;
  }

  /**
   * Create an Account object from an input data reader. The client is
   * responsible for opening the reader and handling the end-of-stream
   * exception, then closing the reader.
   * 
   * @param year the number of the year in which the account exists
   * @param reader the buffered reader containing the next line of data
   */
  public Account(Integer year, BufferedReader reader) {
    super(reader);
    if (year == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.year = year;
  }

  @Override
  protected void init(String[] fields) {
    this.accountNumber = new Float(fields[0]);
    this.name = fields[1];
    if (fields[0].equalsIgnoreCase("DR")) {
      this.defaultDebit = true;
    } else {
      defaultDebit = false;
    }
  }

  @Override
  protected int numberOfFields() {
    return 3;
  }

  // Override to hash name as primary key
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  // Override to compare by name as primary key
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Account other = (Account)obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
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
   * Get the new-accounting account type based on the account number. This gets
   * the new-accounting type rather than any value read from data.
   * 
   * @return the account type
   */
  public AccountType getAccountType() {
    AccountType type = null;
    if (accountNumber >= 100 && accountNumber < 200) {
      type = AccountType.ASSET;
    } else if (accountNumber >= 200 && accountNumber < 300) {
      type = AccountType.LIABILITY;
    } else if (accountNumber >= 300 && accountNumber < 400) {
      type = AccountType.EQUITY;
    } else if (accountNumber >= 400 && accountNumber < 600) {
      type = AccountType.INCOME;
    } else if (accountNumber >= 600 && accountNumber < 800) {
      type = AccountType.EXPENSE;
    }
    return type;
  }

  /**
   * Get the accountNumber.
   * 
   * @return a accountNumber
   */
  public Float getAccountNumber() {
    return accountNumber;
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
   * Get the defaultDebit.
   * 
   * @return a defaultDebit
   */
  public Boolean getDefaultDebit() {
    return defaultDebit;
  }

  @Override
  public String toString() {
    return "Account [year=" + year + ", name=" + name + ", accountNumber="
           + accountNumber + ", defaultDebit=" + defaultDebit
           + ", getAccountType()=" + getAccountType() + "]";
  }
}
