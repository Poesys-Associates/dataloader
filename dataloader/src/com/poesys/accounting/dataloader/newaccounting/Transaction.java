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
package com.poesys.accounting.dataloader.newaccounting;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;

/**
 * A data transfer object containing data about transaction (a set of items that represent a
 * balanced exchange of money between accounts on a certain date); the transaction contains a single
 * balance item (a "balance" transaction, which happens at the beginning of the set of transactions
 * to initialize the balance sheet accounts) or a set of items that balance to zero; the transaction
 * may or may not have been officially reconciled against an external data source
 *
 * @author Robert J. Muller
 */
public class Transaction {
  /** Logger for this class */
  private static final Logger logger = Logger.getLogger(Transaction.class);

  /** unique id within year from old accounting system */
  private final BigInteger id;
  /** text describing the nature of the transaction, including ids */
  private final String description;
  /** date and time at which the transaction occurred */
  private final Timestamp date;
  /** whether the transaction amounts have been fully reconciled */
  private final Boolean checked;
  /** whether the transaction is a balance transaction */
  private final Boolean balance;
  /** the set of items */
  private final Set<Item> items = new HashSet<>();

  /** format for double data values - [n]+.nn */
  private static DecimalFormat format = new DecimalFormat("0.00");

  // Messages

  /** null parameter to constructor */
  private static final String NULL_PARAMETER_ERROR =
    "Transaction parameters are required but one is null";
  /** transaction does not balance */
  private static final String BALANCE_ERROR = "Transaction does not balance (";

  /**
   * Create a Transaction object.
   *
   * @param id          unique id from old accounting system (required, used as key)
   * @param description text describing the nature of the transaction, including ids
   * @param date        date and time at which the transaction occurred (required)
   * @param checked     whether the transaction amounts have been fully reconciled
   * @param balance     whether the transaction is a balance transaction
   */
  public Transaction(BigInteger id, String description, Timestamp date, Boolean checked, Boolean
    balance) {
    if (id == null || date == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.id = id;
    this.description = description;
    this.date = date;
    this.checked = checked == null ? Boolean.FALSE : checked;
    this.balance = balance == null ? Boolean.FALSE : balance;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (getYear());
    result = prime * result + id.hashCode();
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
    Transaction other = (Transaction)obj;
    return getYear().equals(other.getYear()) && id.equals(other.id);
  }

  /**
   * Get the id.
   *
   * @return a id
   */
  public BigInteger getId() {
    return id;
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
   * Get the date.
   *
   * @return a date
   */
  public Timestamp getDate() {
    return date;
  }

  /**
   * Get the calendar year of the date.
   *
   * @return a year
   */
  public Integer getYear() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar.get(Calendar.YEAR);
  }

  /**
   * Has the transaction been reconciled against an external data source? If so, all items should
   * have been checked. If they aren't, the method returns false, ignoring the input checked=true
   * data.
   *
   * @return true if reconciled, false if not reconciled
   */
  public Boolean isChecked() {
    Boolean valid = checked;
    // Make sure items are checked if transaction is checked; set the re
    if (valid) {
      for (Item item : items)
        if (!item.isChecked()) {
          valid = false;
          // end at first not-checked item
          break;
        }
    }

    return valid;
  }

  /**
   * Is this a balance transaction? If true, then there must be a single item; if false, the items
   * must sum to zero, treating debits as negative and credits as positive numbers.
   *
   * @return true for balance transaction, false for regular transaction
   */
  public Boolean isBalance() {
    return balance;
  }

  /**
   * Is the transaction valid? To be valid, a transaction must have balanced transaction items
   * (debits and credits sum to zero) or must be a balance transaction with a single item.
   *
   * @return true if valid, false if invalid
   */
  public Boolean isValid() {
    Boolean valid = Boolean.FALSE;

    if (balance && items.size() == 1) {
      valid = true;
    } else if (balance && items.size() > 1) {
      logger.error("Balance transaction has more than one item: " + this);
    } else if (balance) {
      logger.error("Balance transaction has no balance item: " + this);
    } else if (items.size() < 2) {
      logger.error("Transaction has less than 2 items: " + this);
    } else {
      // Sum the items, taking debits as negative values.
      valid = isBalanced();
    }
    return valid;
  }

  /**
   * Is the set of items balanced--does the sum of the credits and debits equal zero with debits
   * taken as negative numbers?
   *
   * @return true if balanced, false if not
   */
  private Boolean isBalanced() {
    Boolean valid = Boolean.FALSE;
    // Use BigDecimal to maintain correct scale 2.
    BigDecimal sum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    for (Item item : items) {
      // Convert amount to BigDecimal at scale 2 to insure correct arithmetic.
      BigDecimal amount = new BigDecimal(item.getAmount()).setScale(2, RoundingMode.HALF_DOWN);
      // Negate the amount for debit items.
      amount = item.isDebit() ? amount.negate() : amount;
      sum = sum.add(amount);
    }
    if (sum.compareTo(zero) != 0) {
      logger.error(BALANCE_ERROR + format.format(sum) + "): " + this);
    } else {
      valid = Boolean.TRUE;
    }
    return valid;
  }

  /**
   * Get the item in the transaction that is against the specified account.
   *
   * @param account the account to look up
   * @return the item against the account or null if there is no item against that account
   */
  public Item getItem(Account account) {
    Item foundItem = null;
    for (Item item : items) {
      if (item.getAccount().equals(account)) {
        foundItem = item;
        break;
      }
    }
    return foundItem;
  }

  /**
   * Get the threadsafe set of items.
   *
   * @return a set of items
   */
  public Set<Item> getItems() {
    return Collections.synchronizedSet(items);
  }

  /**
   * Add a regular item to the set of items with the specified elements.
   *
   * @param amount  the dollar amount of the item (non-negative double)
   * @param account the account against which the item applies
   * @param debit   whether the item is a debit or credit
   * @param checked whether the system has reconciled the item against an external data source
   * @return the created item
   */
  public Item addItem(Double amount, Account account, Boolean debit, Boolean checked) {
    Item item = new Item(this, amount, account, debit, checked);
    items.add(item);
    account.addItem(item);
    return item;
  }

  @Override
  public String toString() {
    return "Transaction [id=" + id + ", description=" + description + ", date=" + date +
           ", checked=" + checked + ", balance=" + balance + ", items=" + items + "]";
  }

  /**
   * Check whether the transaction has items equal to zero, meaning it's not really a transaction.
   *
   * @return true if the transaction is a zero transaction, false if not
   */
  public boolean isZero() {
    boolean isZero = true;
    BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    for (Item item : items) {
      // Convert amount to BigDecimal at scale 2 to insure correct arithmetic.
      BigDecimal amount = new BigDecimal(item.getAmount()).setScale(2, RoundingMode.HALF_DOWN);
      if (amount.compareTo(zero) != 0) {
        isZero = false;
        break;
      }
    }

    return isZero;
  }
}
