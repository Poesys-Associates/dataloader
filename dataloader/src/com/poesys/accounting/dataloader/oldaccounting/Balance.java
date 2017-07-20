/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import java.io.BufferedReader;

import com.poesys.db.InvalidParametersException;


/**
 * A data transfer object containing an initial balance for the system. Fields:
 * account number, debit, amount
 * 
 * @author Robert J. Muller
 */
public class Balance extends AbstractReaderDto {
  private Integer year;
  private Float accountNumber;
  private Double amount;
  private Boolean debit;

  /**
   * Create a Balance object.
   * 
   * @param year the year number for the fiscal year
   * @param accountNumber the account for which the amount applies
   * @param amount the dollar amount of the balance
   * @param debit whether the amount is a debit or credit balance
   */
  public Balance(Integer year, Float accountNumber, Double amount, Boolean debit) {
    if (year == null || accountNumber == null || amount == null
        || debit == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.year = year;
    this.accountNumber = accountNumber;
    this.amount = amount;
    this.debit = debit;
  }

  /**
   * Create a Balance object from an input data reader.
   * 
   * @param year the year number for the balance (first year of series)
   * @param reader the input data reader
   */
  public Balance(Integer year, BufferedReader reader) {
    super(reader);
    this.year = year;
  }

  @Override
  protected void init(String[] fields) {
    this.accountNumber = new Float(fields[0]);
    this.debit = fields[1].equals("DR") ? true : false;
    this.amount = new Double(fields[2]);
  }

  @Override
  protected int numberOfFields() {
    return 3;
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
    Balance other = (Balance)obj;
    if (accountNumber == null) {
      if (other.accountNumber != null)
        return false;
    } else if (!accountNumber.equals(other.accountNumber))
      return false;
    return true;
  }

  /**
   * Get the account number.
   * 
   * @return an account number
   */
  public Float getAccountNumber() {
    return accountNumber;
  }

  /**
   * Get the amount.
   * 
   * @return a amount
   */
  public Double getAmount() {
    return amount;
  }

  /**
   * Is this a debit balance?
   * 
   * @return true if debit, false if credit
   */
  public Boolean isDebit() {
    return debit;
  }

  @Override
  public String toString() {
    return "Balance [year=" + year + ", accountNumber=" + accountNumber
           + ", amount=" + amount + ", debit=" + debit + "]";
  }
}
