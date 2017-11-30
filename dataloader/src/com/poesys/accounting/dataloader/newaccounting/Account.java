/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;


/**
 * A data transfer object that contains data relating to an account, the
 * conceptual entity grouping a set of transaction items relating to a single
 * concept. Accounts correspond to external accounts, accounting concepts, and
 * categories of income and expense, among other things. Each account has a name
 * and description, a boolean indicating the default debit or credit designation
 * for new items against the account, and a boolean indicator designating the
 * account as an Accounts Receivable account. The account applies only in a
 * designated list of fiscal years. The account contains a set of items against
 * the account; the sum (taking debits as negative and credits as positive) is
 * the current balance of the account.
 * 
 * @author Robert J. Muller
 */
public class Account {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(Account.class);

  /** the unique name for the account */
  private final String name;
  /** text describing the nature of the account */
  private final String description;
  /** whether the default for items is a debit or credit */
  private final Boolean debitDefault;
  /** whether the account is a Receivables account */
  private final Boolean receivable;
  /** an ordered list of fiscal years in which the account is active */
  private final List<FiscalYearAccount> years =
    new ArrayList<FiscalYearAccount>();
  /** the set of items against the account */
  private final Set<Item> items = new HashSet<Item>();

  // Messages

  /** null parameter to constructor */
  private static final String NULL_PARAMETER_ERROR =
    "Account parameters are required but one is null";
  /** null parameter to addYear() */
  private static final String NULL_YEAR_ERROR =
    "fiscal year required but is null";

  /**
   * Create an Account object.
   * 
   * @param name the unique name for the account
   * @param description text describing the nature of the account
   * @param accountType kind of account: Asset, Liability, Equity, Income,
   *          Expense
   * @param debitDefault whether the default for items is a debit or credit
   * @param receivable whether the account is a Receivables account
   */
  public Account(String name,
                 String description,
                 AccountType accountType,
                 Boolean debitDefault,
                 Boolean receivable) {
    if (name == null || description == null || accountType == null
        || debitDefault == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }

    this.name = name;
    this.description = description;
    this.debitDefault = debitDefault;
    // Default receivable flag to FALSE
    this.receivable = receivable == null ? Boolean.FALSE : receivable;
  }

  // Override hashCode() to specify primary key
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  // Override equals() to specify primary key
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Account other = (Account)obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
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
   * Get the description.
   * 
   * @return a description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get the account type of the account in a specified fiscal year.
   * 
   * @param year the fiscal year
   * 
   * @return the account type of the account in the year
   */
  public AccountType getAccountType(FiscalYear year) {
    AccountType accountType = null;

    for (FiscalYearAccount link : years) {
      if (link.getFiscalYear().equals(year)) {
        accountType = link.getAccountType();
        break; // found year, done
      }
    }
    return accountType;
  }

  /**
   * Is the default value for items debit?
   * 
   * @return true if debit, false if credit
   */
  public Boolean isDebitDefault() {
    return debitDefault;
  }

  /**
   * Is this account a Receivables account?
   * 
   * @return true if receivable, false if not
   */
  public Boolean isReceivable() {
    return receivable;
  }

  /**
   * Add the fiscal year link to the list of links for the account. Ensure that
   * the list of links is ordered by year, account type, group, and account.
   * 
   * @param year the fiscal year account link to add
   */
  public void addYear(FiscalYearAccount year) {
    if (year == null) {
      throw new InvalidParametersException(NULL_YEAR_ERROR);
    }
    years.add(year);
    Collections.sort(years);
  }

  /**
   * Get the fiscal years in which the account is active.
   * 
   * @return a threadsafe list of fiscal-year-account links
   */
  public List<FiscalYearAccount> getYears() {
    return Collections.synchronizedList(years);
  }

  /**
   * Get the account group for the account in a specified fiscal year.
   * 
   * @param year the fiscal year
   * @return the account group that contains the account in the fiscal year
   */
  public AccountGroup getGroup(FiscalYear year) {
    AccountGroup group = null;

    for (FiscalYearAccount link : years) {
      if (link.getFiscalYear().equals(link.getFiscalYear())) {
        group = link.getGroup();
        break; // found group, done
      }
    }
    return group;
  }

  /**
   * Get the items associated with this account.
   * 
   * @return an unmodifiable set of items
   */
  public Set<Item> getItems() {
    return Collections.unmodifiableSet(items);
  }

  /**
   * Add an item to the set of items in this account.
   * 
   * @param item the item to add
   */
  public void addItem(Item item) {
    logger.debug("Adding item to " + name + " item list: " + item);
    items.add(item);
  }

  @Override
  public String toString() {
    return "Account [name=" + name + ", description=" + description
           + ", debitDefault=" + debitDefault + ", receivable=" + receivable
           + ", years=" + years + "]";
  }
}
