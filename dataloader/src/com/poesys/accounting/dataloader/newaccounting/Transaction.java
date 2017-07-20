/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;


/**
 * A data transfer object containing data about transaction (a set of items that
 * represent a balanced exchange of money between accounts on a certain date);
 * the transaction contains a single balance item (a "balance" transaction,
 * which happens at the beginning of the set of transactions to initialize the
 * balance sheet accounts) or a set of items that balance to zero; the
 * transaction may or may not have been officially reconciled against an
 * external data source
 * 
 * @author Robert J. Muller
 */
public class Transaction {
  /** Logger for this class */
  private static final Logger logger = Logger.getLogger(Transaction.class);

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
  private final Set<Item> items = new HashSet<Item>();

  // Messages

  /** null parameter to constructor */
  private static final String NULL_PARAMETER_ERROR =
    "Transaction parameters are required but one is null";

  /**
   * Create a Transaction object.
   * 
   * @param id unique id from old accounting system
   * @param description text describing the nature of the transaction, including
   *          ids
   * @param date date and time at which the transaction occurred (required)
   * @param checked whether the transaction amounts have been fully reconciled
   * @param balance whether the transaction is a balance transaction
   */
  public Transaction(BigInteger id,
                     String description,
                     Timestamp date,
                     Boolean checked,
                     Boolean balance) {
    if (date == null) {
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
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    Transaction other = (Transaction)obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
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
   * Has the transaction been reconciled against an external data source? If so,
   * all items should have been checked. If they aren't, the method returns
   * false, ignoring the input checked=true data.
   * 
   * @return true if reconciled, false if not reconciled
   */
  public Boolean isChecked() {
    Boolean valid = checked;
    // Make sure items are checked if transaction is checked; set the re
    if (valid) {
      for (Item item : items) {
        if (!item.isChecked()) {
          valid = false;
          // end at first not-checked item
          break;
        }
      }
    }

    return valid;
  }

  /**
   * Is this a balance transaction? If true, then there must be a single item;
   * if false, the items must sum to zero, treating debits as negative and
   * credits as positive numbers.
   * 
   * @return true for balance transaction, false for regular transaction
   */
  public Boolean isBalance() {
    return balance;
  }

  /**
   * Is the transaction valid? To be valid, a transaction must have balanced
   * transaction items (debits and credits sum to zero) or must be a balance
   * transaction with a single item.
   * 
   * @return true if valid, false if invalid
   */
  public Boolean isValid() {
    Boolean valid = Boolean.FALSE;

    if (balance && items.size() == 1) {
      valid = true;
    } else if (balance && items.size() > 1) {
      logger.error("Balance transaction has more than one item: " + this);
    } else if (balance && items.size() == 0) {
      logger.error("Balance transaction has no balance item: " + this);
    } else if (items.size() < 2) {
      logger.error("Transaction has less than 2 items: " + this);
    } else {
      // Sum the items, taking debits as negative values.
      Double sum = 0.00D;
      for (Item item : items) {
        sum += item.isDebit() ? -item.getAmount() : item.getAmount();
      }
      if (sum != 0.00D) {
        logger.error("Transaction does not balance (" + sum + "): " + this);
      } else {
        valid = Boolean.TRUE;
      }
    }
    return valid;
  }

  /**
   * Get the item in the transaction that is against the specified account.
   * 
   * @param account the account to look up
   * @return the item against the account or null if there is no item against
   *         that account
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
   * @param amount the dollar amount of the item (non-negative double)
   * @param account the account against which the item applies
   * @param debit whether the item is a debit or credit
   * @param checked whether the system has reconciled the item against an
   *          external data source
   * @return the created item
   */
  public Item addItem(Double amount, Account account, Boolean debit,
                      Boolean checked) {
    Item item = new Item(this, amount, account, debit, checked);
    items.add(item);
    return item;
  }

  @Override
  public String toString() {
    return "Transaction [id=" + id + ", description=" + description + ", date="
           + date + ", checked=" + checked + ", balance=" + balance
           + ", items=" + items + "]";
  }
}
