/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;


/**
 * A data transfer object containing the sum of a set of items against a
 * specific account contained within a statement for a fiscal year
 * 
 * @author Robert J. Muller
 */
public class Rollup {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(Rollup.class);

  /** the parent statement */
  private final Statement statement;

  /** the account being summed */
  private final Account account;

  // Messages

  /** null parameter to constructor */
  private static final String NULL_PARAMETER_ERROR =
    "Rollup parameters are required but one is null";

  /**
   * Create a Rollup object.
   * 
   * @param statement the parent statement
   * @param account the account being summed
   */
  public Rollup(Statement statement, Account account) {
    if (statement == null || account == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.statement = statement;
    this.account = account;
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
   * Sum the items against the account for the fiscal year and return the total.
   * 
   * @return the sum of the items against the account taking credits as positive
   *         numbers and debits as negative numbers
   */
  public Double getTotal() {
    Double total = 0.00D;
    FiscalYear year = statement.getYear();
    for (Item item : account.getItems()) {
      Transaction transaction = item.getTransaction();
      if (year.isIn(transaction.getDate())) {
        total += item.isDebit() ? -item.getAmount() : item.getAmount();
        logger.debug("Added item to " + account.getName()
                     + " total for fiscal year " + year.getYear() + ": "
                     + item.getAmount() + ", total = " + total);
      }
    }
    return total;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((account == null) ? 0 : account.hashCode());
    result = prime * result + ((statement == null) ? 0 : statement.hashCode());
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
    Rollup other = (Rollup)obj;
    if (account == null) {
      if (other.account != null)
        return false;
    } else if (!account.equals(other.account))
      return false;
    if (statement == null) {
      if (other.statement != null)
        return false;
    } else if (!statement.equals(other.statement))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Rollup [statement=" + statement + ", account=" + account + "]";
  }
}
