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
 * A concrete DTO holding data for an item, one element of a transaction identified by the year, the
 * transaction id, and the account. Fields: transaction id (Integer), account number (Float), amount
 * (Double), debit (Boolean Y/N), checked (Boolean Y/N)
 *
 * @author Robert J. Muller
 */
public class Item extends AbstractReaderDto {
  /** the fiscal year of the transaction */
  private Integer year;
  /** the unique identifier for the transaction within the year */
  private Integer transactionId;
  /** the account to which the amount applies */
  private Float accountNumber;
  /** the non-negative dollar amount of the item */
  private Double amount;
  /** whether the item is a debit (true) or a credit (false) */
  private Boolean debit;
  /** whether the item is reconciled with an external statement */
  private Boolean checked;

  // Messages

  /** null parameter to constructor */
  private static final String NULL_PARAMETER_ERROR = "Item parameters are required but one is null";

  /**
   * Create an Item object.
   *
   * @param year          the fiscal year of the parent transaction
   * @param transactionId the parent transaction that owns this item
   * @param accountNumber the account number of the account to which the amount applies
   * @param amount        the non-negative dollar amount of the item
   * @param debit         whether the item is a debit or credit
   * @param checked       whether the item is reconciled with an external statement
   */
  public Item(Integer year, Integer transactionId, Float accountNumber, Double amount, Boolean
    debit, Boolean checked) {
    if (year == null || transactionId == null || amount == null || accountNumber == null ||
        debit == null || checked == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.year = year;
    this.transactionId = transactionId;
    this.accountNumber = accountNumber;
    this.amount = amount;
    this.debit = debit;
    this.checked = checked;
  }

  /**
   * Create an Item object with a fiscal year and a data input reader.
   *
   * @param year   the fiscal year of the transaction
   * @param reader the input data reader pointing to the current row of data
   */
  public Item(Integer year, BufferedReader reader) {
    super(reader);
    if (year == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.year = year;
  }

  @Override
  protected void init(String[] fields) {
    String idString = fields[0];
    if (idString != null && !idString.isEmpty()) {
      transactionId = new Integer(idString);
    } else {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR + ": item transaction id");
    }

    String acctString = fields[1];
    if (acctString != null && !acctString.isEmpty()) {
      accountNumber = new Float(acctString);
    } else {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR + ": item account number");
    }

    String amountString = fields[2];
    if (amountString != null && !amountString.isEmpty()) {
      amount = new Double(amountString);
    } else {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR + ": item amount");
    }

    // default to credit (false) for empty string
    debit = fields[3].equalsIgnoreCase("DR");
    // default to not checked (false) for empty string
    checked = fields[4].equalsIgnoreCase("Y");
  }

  @Override
  protected int numberOfFields() {
    return 5;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
    result = prime * result + ((transactionId == null) ? 0 : transactionId.hashCode());
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
    Item other = (Item)obj;
    if (accountNumber == null) {
      if (other.accountNumber != null) {
        return false;
      }
    } else if (!accountNumber.equals(other.accountNumber)) {
      return false;
    }
    if (transactionId == null) {
      if (other.transactionId != null) {
        return false;
      }
    } else if (!transactionId.equals(other.transactionId)) {
      return false;
    }
    if (year == null) {
      if (other.year != null) {
        return false;
      }
    } else if (!year.equals(other.year)) {
      return false;
    }
    return true;
  }

  /**
   * Get the fiscal year.
   *
   * @return a year
   */
  public Integer getYear() {
    return year;
  }

  /**
   * Get the transaction id.
   *
   * @return a transactionId
   */
  public Integer getTransactionId() {
    return transactionId;
  }

  /**
   * Get the account number.
   *
   * @return a account number
   */
  public Float getAccountNumber() {
    return accountNumber;
  }

  /**
   * Get the amount
   *
   * @return a Double amount
   */
  public Double getAmount() {
    return amount;
  }

  /**
   * Is the item a debit item?
   *
   * @return true if debit, false if credit
   */
  public Boolean isDebit() {
    return debit;
  }

  /**
   * Whether the item is reconciled against an external account statement.
   *
   * @return true if reconciled, false if not
   */
  public Boolean isChecked() {
    return checked;
  }

  @Override
  public String toString() {
    return "Item [year=" + year + ", transactionId=" + transactionId + ", accountNumber=" +
           accountNumber + ", amount=" + amount + ", debit=" + debit + ", checked=" + checked + "]";
  }
}
