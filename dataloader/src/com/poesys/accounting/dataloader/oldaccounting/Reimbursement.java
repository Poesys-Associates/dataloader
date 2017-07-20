/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import java.io.BufferedReader;

import com.poesys.db.InvalidParametersException;


/**
 * A data transfer object that holds raw data for a reimbursement item; used to
 * construct the old accounting reimbursement item object and to link up to
 * receivables; the amounts are positive, representing credits to the
 * receivables account. Fields: reimbursement transaction id, receivable year,
 * receivable transaction id, receivable account #, reimbursed amount, allocated
 * amount
 * 
 * @author Robert J. Muller
 */
public class Reimbursement extends AbstractReaderDto {
  /** the old receivables account number */
  private Float accountNumber;
  /** the fiscal year of the receivable */
  private Integer receivableYear;
  /** the unique receivable transaction id within year */
  private Integer receivableTransactionId;
  /** the fiscal year of the reimbursement */
  private Integer reimbursementYear;
  /** the unique reimbursement transaction id within year */
  private Integer reimbursementTransactionId;
  /** the amount reimbursed by the reimbursement */
  private Double reimbursedAmount;
  /** the amount allocated by the reimbursement */
  private Double allocatedAmount;

  /**
   * Create a Reimbursement object.
   * 
   * @param accountNumber the old receivables account number
   * @param receivableYear the fiscal year of the receivable
   * @param receivableTransactionId the unique receivable transaction id within
   *          year
   * @param reimbursementYear the fiscal year of the reimbursement
   * @param reimbursementTransactionId the unique reimbursement transaction id
   *          within year
   * @param reimbursedAmount the amount reimbursed by the reimbursement
   * @param allocatedAmount the optional amount allocated by the reimbursement
   */
  public Reimbursement(Float accountNumber,
                       Integer receivableYear,
                       Integer receivableTransactionId,
                       Integer reimbursementYear,
                       Integer reimbursementTransactionId,
                       Double reimbursedAmount,
                       Double allocatedAmount) {
    if (accountNumber == null || receivableYear == null
        || receivableTransactionId == null || reimbursementYear == null
        || reimbursementTransactionId == null || reimbursedAmount == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.accountNumber = accountNumber;
    this.receivableYear = receivableYear;
    this.receivableTransactionId = receivableTransactionId;
    this.reimbursementYear = reimbursementYear;
    this.reimbursementTransactionId = reimbursementTransactionId;
    this.reimbursedAmount = reimbursedAmount;
    if (allocatedAmount == null) {
      this.allocatedAmount = 0.00D;
    } else {
      this.allocatedAmount = allocatedAmount;
    }
  }

  /**
   * Create a Reimbursement object from an input data reader.
   * 
   * @param year the fiscal year of the reimbursement
   * @param reader the input data reader set at the current line
   */
  public Reimbursement(Integer year, BufferedReader reader) {
    super(reader);
    if (year == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.reimbursementYear = year;
  }

  @Override
  protected void init(String[] fields) {
    this.reimbursementTransactionId = new Integer(fields[0]);
    this.receivableYear = new Integer(fields[1]);
    this.receivableTransactionId = new Integer(fields[2]);
    this.accountNumber = new Float(fields[3]);
    this.reimbursedAmount = new Double(fields[4]);
    this.allocatedAmount = new Double(fields[5]);
  }

  @Override
  protected int numberOfFields() {
    return 6;
  }

  // Implement hashCode() to make year and transaction id the primary key
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
      prime
          * result
          + ((reimbursementTransactionId == null) ? 0
              : reimbursementTransactionId.hashCode());
    result =
      prime * result
          + ((reimbursementYear == null) ? 0 : reimbursementYear.hashCode());
    return result;
  }

  // Implement equals() to make year and transaction id the primary key
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Reimbursement other = (Reimbursement)obj;
    if (reimbursementTransactionId == null) {
      if (other.reimbursementTransactionId != null)
        return false;
    } else if (!reimbursementTransactionId.equals(other.reimbursementTransactionId))
      return false;
    if (reimbursementYear == null) {
      if (other.reimbursementYear != null)
        return false;
    } else if (!reimbursementYear.equals(other.reimbursementYear))
      return false;
    return true;
  }

  /**
   * Get the account number.
   * 
   * @return a Float account number
   */
  public Float getAccountNumber() {
    return accountNumber;
  }

  /**
   * Get the receivable year.
   * 
   * @return a year
   */
  public Integer getReceivableYear() {
    return receivableYear;
  }

  /**
   * Get the receivable transaction id.
   * 
   * @return an Integer id
   */
  public Integer getReceivableTransactionId() {
    return receivableTransactionId;
  }

  /**
   * Get the reimbursement year.
   * 
   * @return a year
   */
  public Integer getReimbursementYear() {
    return reimbursementYear;
  }

  /**
   * Get the reimbursement transaction id.
   * 
   * @return an Integer id
   */
  public Integer getReimbursementTransactionId() {
    return reimbursementTransactionId;
  }

  /**
   * Get the reimbursed amount.
   * 
   * @return a positive (credit) dollar amount
   */
  public Double getReimbursedAmount() {
    return reimbursedAmount;
  }

  /**
   * Get the allocated amount.
   * 
   * @return a positive (credit) dollar amount
   */
  public Double getAllocatedAmount() {
    return allocatedAmount;
  }

  @Override
  public String toString() {
    return "Reimbursement [accountNumber=" + accountNumber
           + ", receivableYear=" + receivableYear
           + ", receivableTransactionId=" + receivableTransactionId
           + ", reimbursementYear=" + reimbursementYear
           + ", reimbursementTransactionId=" + reimbursementTransactionId
           + ", reimbursedAmount=" + reimbursedAmount + ", allocatedAmount="
           + allocatedAmount + "]";
  }
}
