/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import java.io.BufferedReader;

import com.poesys.db.InvalidParametersException;


/**
 * Data transfer object for the account mapping data, which maps a fiscal year
 * account number to a new-accounting account name, enabling the data loader to
 * map accounts in any given fiscal year to the accumulating set of accounts
 * that span fiscal years in the new accounting system; there are 2 data fields,
 * account number and account name
 * 
 * @author Robert J. Muller
 */
public class AccountMap extends AbstractReaderDto {

  /** the old-accounting account number for the fiscal year */
  private Float accountNumber;
  /** the new-accounting account name */
  private String name;

  // messages
  
  /** null parameter to constructor */
  private static final String NULL_PARAMETER_ERROR =
    "Account Map parameters are required but one is null";

  /**
   * Create a AccountMap object.
   * 
   * @param accountNumber the old-accounting account number for the fiscal year
   * @param name the new-accounting account name
   */
  public AccountMap(Float accountNumber, String name) {
    if (accountNumber == null || name == null || name.isEmpty()) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.accountNumber = accountNumber;
    this.name = name;
  }

  /**
   * Create a AccountMap object.
   * 
   * @param reader the buffered reader for the account map data
   */
  public AccountMap(BufferedReader reader) {
    super(reader);
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
      prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
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
    AccountMap other = (AccountMap)obj;
    if (accountNumber == null) {
      if (other.accountNumber != null)
        return false;
    } else if (!accountNumber.equals(other.accountNumber))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AccountMap [accountNumber=" + accountNumber + ", name=" + name
           + "]";
  }

  @Override
  protected void init(String[] fields) {
    if (fields[0] == null || fields[1] == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.accountNumber = new Float(fields[0]);
    this.name = fields[1];
  }

  @Override
  protected int numberOfFields() {
    return 2;
  }
}
