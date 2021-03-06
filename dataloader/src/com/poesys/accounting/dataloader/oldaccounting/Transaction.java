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
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;

/**
 * Data transfer object for old-accounting transaction data; includes the items
 * for the transaction, loaded separately; contains a factory method that
 * generates the new-accounting transaction data transfer object corresponding
 * to the old-accounting transaction; fields: transaction id (Integer),
 * description (string enclosed by double quotes with trailing blanks),
 * transaction date (format dd-MON-yy), checked (boolean Y/N)
 *
 * @author Robert J. Muller
 */
public class Transaction extends AbstractReaderDto {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(Transaction.class);
  /** the fiscal year of the transaction */
  private Integer year;
  /** the unique identifier for the transaction */
  private Integer transactionId;
  /** the date of the transaction */
  private Timestamp transactionDate;
  /** the text describing the nature of the transaction */
  private String description;
  /** whether the transaction is reconciled with an external statement */
  private Boolean checked;

  // Messages

  private static final String NULL_PARAMETER_ERROR =
    "transaction parameters are required but one is null";
  private static final String BAD_DATE_ERROR = "reader date field not valid: ";
  public static final String NULL_DATE_ERROR = "null date from transaction file for id ";

  /**
   * Create a Transaction object.
   *
   * @param year            the fiscal year of the transaction
   * @param transactionId   the unique identifier for the transaction
   * @param transactionDate the date of the transaction
   * @param description     the text describing the nature of the transaction
   * @param checked         whether the transaction is reconciled with an external
   *                        statement
   */
  public Transaction(Integer year, Integer transactionId, Timestamp transactionDate, String
    description, Boolean checked) {
    if (year == null || transactionId == null || transactionDate == null || description == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.year = year;
    this.transactionId = transactionId;
    this.transactionDate = transactionDate;
    this.description = description;
    this.checked = checked;
  }

  /**
   * Create a Transaction object reading from a tab-delimited line. The client is responsible for
   * opening and closing the reader. The client should catch the EndOfStream throwable to
   * determine when reading is complete and to then close the reader. The method also checks that
   * the transaction date is in the fiscal year of the file.
   *
   * @param year   the fiscal year being read
   * @param reader the buffered reader set at the current line to read
   */
  public Transaction(Integer year, BufferedReader reader) {
    super(reader);
    if (year == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.year = year;
    // At this point we have the year and the transaction date; validate it.
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date(transactionDate.getTime()));
    int transactionYear = cal.get(Calendar.YEAR);
    if (year != transactionYear) {
      String message = "Transaction date not in fiscal year of file: " + transactionYear + ", id " +
                       transactionId + " for fiscal year " + year;
      logger.error(message);
      throw new InvalidParametersException(message);
    }
  }

  @Override
  protected void init(String[] fields) {
    transactionId = new Integer(fields[0]);
    // trim leading and trailing double quotes, trailing blanks (within quotes)
    description = fields[1].replaceAll("^\"|\"$", "").trim();
    // Oracle-formatted transaction date
    String format = "dd-MMM-yy";
    SimpleDateFormat formatter = new SimpleDateFormat(format);
    try {
      transactionDate = new Timestamp(formatter.parse(fields[2]).getTime());
    } catch (ParseException e) {
      logger.error("bad date: " + fields[2] + ", format " + format, e);
      throw new InvalidParametersException(BAD_DATE_ERROR + fields[2]);
    } catch (NullPointerException e) {
      logger.error(NULL_DATE_ERROR + transactionId, e);
      throw new InvalidParametersException(BAD_DATE_ERROR + fields[2]);
    }
    // checked is null in input file, default to "N"
    checked = fields[3] != null && fields[3].equalsIgnoreCase("Y");
  }

  @Override
  protected int numberOfFields() {
    return 4;
  }

  // Override hashCode() to specify primary key (year, transaction id)
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((transactionId == null) ? 0 : transactionId.hashCode());
    result = prime * result + ((year == null) ? 0 : year.hashCode());
    return result;
  }

  // Override equals() to specify primary key (year, transaction id)
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
    Transaction other = (Transaction)obj;
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
   * Get the year.
   *
   * @return a year
   */
  public Integer getYear() {
    return year;
  }

  /**
   * Get the transactionId.
   *
   * @return a transactionId
   */
  public Integer getTransactionId() {
    return transactionId;
  }

  /**
   * Get the transactionDate.
   *
   * @return a transactionDate
   */
  public Timestamp getTransactionDate() {
    return transactionDate;
  }

  /**
   * Get the description.
   *
   * @return a description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Whether the transaction is reconciled with an external statement.
   *
   * @return true if reconciled, false if not
   */
  public Boolean isChecked() {
    return checked;
  }

  @Override
  public String toString() {
    return "Transaction [year=" + year + ", transactionId=" + transactionId + ", transactionDate=" +
           transactionDate + ", description=" + description + "]";
  }
}
