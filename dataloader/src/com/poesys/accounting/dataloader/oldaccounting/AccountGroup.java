/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import java.io.BufferedReader;

import com.poesys.db.InvalidParametersException;


/**
 * <p>
 * Data transfer object representing old-accounting account group data; groups
 * the accounts into categories like Cash or Accounts Receivable by grouping the
 * account numbers into ranges. Each account group has a starting account number
 * and and ending account number that defines the range of accounts in the
 * group. The contains() method lets the client determine whether an account
 * number is in this group. The groups within a year should be mutually
 * exclusive.
 * </p>
 * <p>
 * Reads the data from a tab-delimited file with three fields: start, end, name
 * </p>
 * <p>
 * This test suite tests the branches for the AbstractReaderDto superclass
 * constructor by instantiating a concrete subclass and verifying the
 * appropriate exceptions. The test suites for the other concrete subclasses
 * thus do not test these cases.
 * </p>
 * 
 * @author Robert J. Muller
 */
public class AccountGroup extends AbstractReaderDto {
  /** the fiscal year to which the account group applies */
  private final Integer year;
  /** the name of the group */
  private String name;
  /** first account number in group */
  private Float start;
  /** last account number in group */
  private Float end;

  // Messages

  /**
   * Create a AccountGroup object.
   * 
   * @param year the fiscal year to which the account group applies
   * @param name the name of the group
   * @param start first account number in group
   * @param end last account number in group
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
   * Create an AccountGroup object reading from a tab-delimited line. The client
   * is responsible for opening and closing the reader. The client should catch
   * the EndOfStream throwable to determine when reading is complete and to then
   * close the reader.
   * 
   * @param year the fiscal year being read
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
   * Does this account group contain the account?
   * 
   * @param accountNumber the account number
   * @return true if the group includes the account number
   */
  public boolean contains(Float accountNumber) {
    boolean contains = false;
    if (accountNumber >= start && accountNumber <= end) {
      contains = true;
    }
    return contains;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((year == null) ? 0 : year.hashCode());
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
    AccountGroup other = (AccountGroup)obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (year == null) {
      if (other.year != null)
        return false;
    } else if (!year.equals(other.year))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AccountGroup [year=" + year + ", name=" + name + ", start=" + start
           + ", end=" + end + "]";
  }
}
