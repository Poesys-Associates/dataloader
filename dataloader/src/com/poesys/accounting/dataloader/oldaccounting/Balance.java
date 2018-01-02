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
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;

/**
 * A data transfer object containing an initial balance for the system. Fields: account number,
 * balance date, debit, amount
 *
 * @author Robert J. Muller
 */
public class Balance extends AbstractReaderDto {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(Balance.class);

  private Integer year;
  private Float accountNumber;
  private Timestamp balanceDate;
  private Double amount;
  private Boolean debit;

  // messages

  /** badly formatted date input from reader */
  private static final String BAD_DATE_ERROR = "reader date field not valid: ";
  /** badly formatted number data input from reader */
  private static final String NUM_FMT_ERROR =
    "Number format exception for balance amount for account ";
  /** message about defaulting to value */
  private static final String DEFAULT_MSG = ", defaulting to 0.00";

  /**
   * Create a Balance object.
   *
   * @param year          the year number for the fiscal year
   * @param accountNumber the account for which the amount applies
   * @param balanceDate   the date on which the balance took effect
   * @param amount        the dollar amount of the balance
   * @param debit         whether the amount is a debit or credit balance
   */
  public Balance(Integer year, Float accountNumber, Timestamp balanceDate, Double amount, Boolean
    debit) {
    if (year == null || accountNumber == null || balanceDate == null || amount == null ||
        debit == null) {
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
   * @param year   the year number for the balance (first year of series)
   * @param reader the input data reader
   */
  public Balance(Integer year, BufferedReader reader) {
    super(reader);
    this.year = year;
  }

  @Override
  protected void init(String[] fields) {
    this.accountNumber = new Float(fields[0]);
    // Oracle-formatted transaction date
    String format = "dd-MMM-yy";
    SimpleDateFormat formatter = new SimpleDateFormat(format);
    try {
      balanceDate = new Timestamp(formatter.parse(fields[1]).getTime());
    } catch (ParseException e) {
      logger.error("bad date: " + fields[2] + ", format " + format, e);
      throw new InvalidParametersException(BAD_DATE_ERROR + fields[2]);
    }
    this.debit = fields[2].equals("DR");
    try {
      this.amount = new Double(fields[3]);
    } catch (NumberFormatException e) {
      // Problem with input, null or not a number
      logger.warn(NUM_FMT_ERROR + accountNumber + DEFAULT_MSG);
      this.amount = 0.00D;
    }
  }

  @Override
  protected int numberOfFields() {
    return 4;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
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
    Balance other = (Balance)obj;
    if (accountNumber == null) {
      if (other.accountNumber != null) {
        return false;
      }
    } else if (!accountNumber.equals(other.accountNumber)) {
      return false;
    }
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
   * Get the date the balance took effect.
   *
   * @return the balance date
   */
  public Timestamp getBalanceDate() {
    return balanceDate;
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
    return "Balance [year=" + year + ", accountNumber=" + accountNumber + ", amount=" + amount +
           ", debit=" + debit + "]";
  }
}
