/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import java.io.BufferedReader;

import com.poesys.db.InvalidParametersException;


/**
 * A data transfer object that holds raw data for a receivable item, essentially
 * tagging an existing item identified by year, transaction id, and account as a
 * receivable item; the amount is a negative dollar amount representing a debit
 * on the receivables account; the receivable account number must be between
 * 110.0 and 119.99. Fields: year, transaction id, account #, amount
 * 
 * @author Robert J. Muller
 */
public class Receivable extends AbstractReaderDto {
  /** the item year */
  private Integer year;
  /** the old account number for the account */
  private Float accountNumber;
  /** the unique transaction identifier (unique within year) */
  private Integer transactionId;
  /** the receivable amount, a negative dollar amount */
  private Double amount;

  /**
   * Create a Receivable object.
   * 
   * @param year the item year
   * @param accountNumber the old account number for the account (between 110
   *          and 119.99)
   * @param transactionId the unique transaction identifier (unique within year)
   * @param amount the receivable amount (positive amount)
   */
  public Receivable(Integer year,
                    Float accountNumber,
                    Integer transactionId,
                    Double amount) {
    if (year == null || accountNumber == null || transactionId == null
        || amount == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.year = year;
    if (accountNumber < 110F || accountNumber >= 120) {
      throw new RuntimeException("receivable account number not between 110 and 119.99: "
                                 + accountNumber);
    }
    this.accountNumber = accountNumber;
    this.transactionId = transactionId;
    this.amount = amount;
  }

  /**
   * Create a Receivable object from an input data reader.
   * 
   * @param reader the input data reader set to the current line
   */
  public Receivable(BufferedReader reader) {
    super(reader);
  }

  @Override
  protected void init(String[] fields) {
    year = new Integer(fields[0]);
    transactionId = new Integer(fields[1]);
    accountNumber = new Float(fields[2]);
    amount = new Double(fields[3]);
  }

  @Override
  protected int numberOfFields() {
    return 4;
  }

  // Implement hashCode() to use transaction id and year as primary key
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
      prime * result + ((transactionId == null) ? 0 : transactionId.hashCode());
    result = prime * result + ((year == null) ? 0 : year.hashCode());
    return result;
  }

  // Implement equals() to use transaction id and year as primary key
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Receivable other = (Receivable)obj;
    if (transactionId == null) {
      if (other.transactionId != null)
        return false;
    } else if (!transactionId.equals(other.transactionId))
      return false;
    if (year == null) {
      if (other.year != null)
        return false;
    } else if (!year.equals(other.year))
      return false;
    return true;
  }

  /**
   * Get the year.
   * 
   * @return an Integer year
   */
  public Integer getYear() {
    return year;
  }

  /**
   * Get the account number.
   * 
   * @return a float account number
   */
  public Float getAccountNumber() {
    return accountNumber;
  }

  /**
   * Get the transaction id.
   * 
   * @return an Integer id
   */
  public Integer getTransactionId() {
    return transactionId;
  }

  /**
   * Get the amount.
   * 
   * @return a negative dollar amount representing a debit on an AR account
   */
  public Double getAmount() {
    return -amount;
  }

  @Override
  public String toString() {
    return "Receivable [year=" + year + ", accountNumber=" + accountNumber
           + ", transactionId=" + transactionId + ", amount=" + getAmount() + "]";
  }
}
