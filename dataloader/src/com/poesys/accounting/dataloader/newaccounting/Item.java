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

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;

/**
 * A data transfer object containing data about a single item within a transaction; the item has an
 * amount which is a debit or credit against a specific account; it also may have an optional set of
 * Reimbursement objects that represent links from receivable items to reimbursing items.
 *
 * @author Robert J. Muller
 */
public class Item implements Comparable<Item> {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(Item.class);
  /** the parent transaction that owns this item */
  private final Transaction transaction;
  /** the dollar amount of the item */
  private final Double amount;
  /** the account to which the item applies */
  private final Account account;
  /** whether the item is a debit (true) or credit (false) item */
  private final Boolean debit;
  /** whether the item has been reconciled against an external data source */
  private final Boolean checked;
  /** the set of reimbursements for a receivable item */
  private final Set<Reimbursement> reimbursements = new HashSet<>();

  // messages

  private static final String NULL_PARAMETER_ERROR = "item parameters are required but one is null";
  private static final String NOT_SAME_ACCOUNT_ERROR =
    "reimbursement must have same receivable account as receivable: ";
  private static final String NOT_RECEIVABLE_ACCOUNT_ERROR = "account is not receivable account: ";
  private static final String INVALID_AMOUNT_ERROR =
    "reimbursed amount must be less than or equal to item amount ";
  private static final String NULL_AMOUNT_ERROR = "reimbursement requires a reimbursed amount";
  private static final String NULL_REIMBURSEMENT_ERROR =
    "reimbursement requires a reimbursing item";
  private static final String NULL_RECEIVABLE_ERROR = "reimbursement requires a receivable item";
  private static final String NULL_ITEM_ERROR = "null item in compareTo()";
  public static final String NO_ACCOUNT_TYPE_ERROR = "no account type for account ";
  public static final String YEAR_OUT_OF_RANGE_ERROR =
    "last fiscal year does not contain transaction year: ";

  /**
   * An association class that links this item to a reimbursing item; valid only for items with
   * receivable accounts.
   */
  public class Reimbursement {
    /** the item being reimbursed */
    private final Item receivable;
    /** the item that reimburses the receivable item */
    private final Item reimbursingItem;
    /** the dollar amount reimbursed by the reimbursing item */
    private final Double reimbursedAmount;
    /** the dollar amount written off by the reimbursing transaction */
    private final Double allocatedAmount;

    /**
     * Create a Reimbursement object. The reimbursed amount must be less than or equal to the amount
     * of the reimbursing item. The two items must be against the same account.
     *
     * @param receivable       the item being reimbursed
     * @param reimbursingItem  the item reimbursing this receivable item
     * @param reimbursedAmount the dollar amount reimbursed
     * @param allocatedAmount  the dollar amount written off
     */
    public Reimbursement(Item receivable, Item reimbursingItem, Double reimbursedAmount, Double
      allocatedAmount) {
      if (receivable == null) {
        throw new InvalidParametersException(NULL_RECEIVABLE_ERROR);
      }
      if (reimbursingItem == null) {
        throw new InvalidParametersException(NULL_REIMBURSEMENT_ERROR);
      }
      if (reimbursedAmount == null) {
        throw new InvalidParametersException(NULL_AMOUNT_ERROR);
      }
      this.receivable = receivable;
      this.reimbursingItem = reimbursingItem;
      this.reimbursedAmount = reimbursedAmount;
      // default allocated amount to 0.00
      this.allocatedAmount = allocatedAmount == null ? 0.00D : allocatedAmount;

      // Validate reimbursed amount against reimbursing item amount
      if (reimbursedAmount.compareTo(reimbursingItem.amount) > 0) {
        throw new InvalidParametersException(
          INVALID_AMOUNT_ERROR + reimbursingItem.amount + ": " + reimbursedAmount);
      }

      if (!receivable.getAccount().isReceivable()) {
        throw new InvalidParametersException(
          NOT_RECEIVABLE_ACCOUNT_ERROR + receivable.getAccount());
      }

      // Ensure that both items are against the same receivable account.
      if (!receivable.getAccount().equals(reimbursingItem.getAccount())) {
        throw new InvalidParametersException(
          NOT_SAME_ACCOUNT_ERROR + receivable.getAccount() + " vs. " +
          reimbursingItem.getAccount() + " for " + reimbursingItem);
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + ((receivable == null) ? 0 : receivable.hashCode());
      result = prime * result + ((reimbursingItem == null) ? 0 : reimbursingItem.hashCode());
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
      Reimbursement other = (Reimbursement)obj;
      if (!getOuterType().equals(other.getOuterType())) {
        return false;
      }
      if (receivable == null) {
        if (other.receivable != null) {
          return false;
        }
      } else if (!receivable.equals(other.receivable)) {
        return false;
      }
      if (reimbursingItem == null) {
        return other.reimbursingItem == null;
      } else {
        return reimbursingItem.equals(other.reimbursingItem);
      }
    }

    /**
     * Get the receivable item.
     *
     * @return an item against a receivable account
     */
    public Item getReceivable() {
      return receivable;
    }

    /**
     * Get the reimbursingItem.
     *
     * @return a reimbursingItem
     */
    public Item getReimbursingItem() {
      return reimbursingItem;
    }

    /**
     * Get the reimbursedAmount.
     *
     * @return a reimbursedAmount
     */
    public Double getReimbursedAmount() {
      return reimbursedAmount;
    }

    /**
     * Get the allocatedAmount.
     *
     * @return a allocatedAmount
     */
    public Double getAllocatedAmount() {
      return allocatedAmount;
    }

    private Item getOuterType() {
      return Item.this;
    }

    @Override
    public String toString() {
      return "Reimbursement [receivable=" + receivable + ", reimbursingItem=" + reimbursingItem +
             ", reimbursedAmount=" + reimbursedAmount + ", allocatedAmount=" + allocatedAmount +
             "]";
    }
  }

  /**
   * Create an Item object.
   *
   * @param transaction the parent transaction that owns this item
   * @param amount      the dollar amount of the item
   * @param account     the account to which the item applies
   * @param debit       whether the item is a debit (true) or credit (false) item
   * @param checked     whether the item has been reconciled against an external data source;
   *                    default false
   */
  public Item(Transaction transaction, Double amount, Account account, Boolean debit, Boolean
    checked) {
    if (transaction == null || amount == null || account == null || debit == null) {
      throw new InvalidParametersException(
        NULL_PARAMETER_ERROR + ": " + transaction + ", " + amount + ", " + account + ", " + debit);
    }
    this.transaction = transaction;
    this.amount = amount;
    this.account = account;
    // Set the item into the account.
    account.addItem(this);
    logger.debug("Added item to account " + account.getName() + ": " + this);
    this.debit = debit;
    this.checked = checked == null ? Boolean.FALSE : checked;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((account == null) ? 0 : account.hashCode());
    result = prime * result + ((transaction == null) ? 0 : transaction.hashCode());
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
    if (account == null) {
      if (other.account != null) {
        return false;
      }
    } else if (!account.equals(other.account)) {
      return false;
    }
    if (transaction == null) {
      return other.transaction == null;
    } else {
      return transaction.equals(other.transaction);
    }
  }

  // Natural order of item is account type, account group order number, account order number,
  // transaction date, old transaction id; this produces the correct order in lists of statement
  // detail transactions.
  @Override
  public int compareTo(Item other) {
    int returnValue = 0;

    // Check inputs valid
    if (other == null) {
      throw new RuntimeException(NULL_ITEM_ERROR);
    }

    // First compare for equality.
    if (!this.equals(other)) {
      // not equal, compare account type

      // First get the year of the transaction.
      Integer year = transaction.getYear();
      // Create the fiscal year corresponding to the year.
      FiscalYear fiscalYear = new FiscalYear(year);

      // Get the fiscal-year-account links for the two objects.
      FiscalYearAccount thisLink = getFiscalYearAccount(fiscalYear, account);
      FiscalYearAccount thatLink = getFiscalYearAccount(fiscalYear, other.account);
      AccountType thisType = account.getAccountType(fiscalYear);
      if (thisType == null) {
          throw new RuntimeException(NO_ACCOUNT_TYPE_ERROR + account + " for fiscal year " + year);
      } AccountType thatType = other.account.getAccountType(fiscalYear);
      if (thatType == null) {
        throw new RuntimeException(NO_ACCOUNT_TYPE_ERROR + account + " for fiscal year " + year);
      }
      returnValue = thisType.compareTo(thatType);
      if (returnValue == 0) {
        // same type, compare account group order
        Integer thisGroupOrder = thisLink.getGroupOrderNumber();
        Integer thatGroupOrder = thatLink.getGroupOrderNumber();
        returnValue = thisGroupOrder.compareTo(thatGroupOrder);
        if (returnValue == 0) {
          // same group order, compare account order
          Integer thisAccountOrder = thisLink.getAccountOrderNumber();
          Integer thatAccountOrder = thatLink.getAccountOrderNumber();
          returnValue = thisAccountOrder.compareTo(thatAccountOrder);
          if (returnValue == 0) {
            // same account order, compare date
            Timestamp thisDate = transaction.getDate();
            Timestamp thatDate = other.transaction.getDate();
            returnValue = thisDate.compareTo(thatDate);
            if (returnValue == 0) {
              BigInteger thisId = transaction.getId();
              BigInteger thatId = other.transaction.getId();
              returnValue = thisId.compareTo(thatId);
            }
          }
        }
      }
    }

    return returnValue;
  }

  /**
   * Build a fiscal-year-account link given a year and account. If the specified fiscal year is
   * not in the list of years for the account, the method throws an exception.
   *
   * @param transactionYear    the year of the transaction
   * @param account the account to link
   * @return a fiscal-year-account link
   */
  private FiscalYearAccount getFiscalYearAccount(FiscalYear transactionYear, Account account) {
    FiscalYearAccount returnLink = null;
    List<FiscalYearAccount> years = account.getYears();
    if (years != null) {
      Integer lastYear = years.get(0).getFiscalYear().getYear();

      for (FiscalYearAccount link : account.getYears()) {
        if (link.getFiscalYear().equals(transactionYear)) {
          returnLink = link;
          break;
        }
        lastYear = link.getFiscalYear().getYear();
      }
      // if no link found for fiscal year, test against range and throw exception if not in range
      if (returnLink == null && transactionYear.getYear().compareTo(lastYear) > 0) {
        throw new RuntimeException(YEAR_OUT_OF_RANGE_ERROR + lastYear + " for transaction year " + transactionYear.getYear());
      }
    }

    return returnLink;
  }

  /**
   * Get the transaction.
   *
   * @return a transaction
   */
  public Transaction getTransaction() {
    return transaction;
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
   * Get the amount.
   *
   * @return a dollar amount
   */
  public Double getAmount() {
    return amount;
  }

  /**
   * Get a thread-safe set of the reimbursements against this receivable. If this is not a
   * receivable item, returns an empty set
   *
   * @return a set of reimbursements or an empty set if not a receivable
   */
  public Set<Reimbursement> getReimbursements() {
    return Collections.synchronizedSet(reimbursements);
  }

  /**
   * Is the item is a debit item?
   *
   * @return true if debit, false if credit
   */
  public Boolean isDebit() {
    return debit;
  }

  /**
   * Has the item been reconciled?
   *
   * @return true if reconciled, false if not
   */
  public Boolean isChecked() {
    return checked;
  }

  /**
   * Create a Reimbursement link and add it to the reimbursements for this item and to the
   * reimbursements in the reimbursing item. The account for this item and the account for the
   * reimbursing item must be the same. The allocated amount is optional and defaults to 0.00. The
   * reimbursing amount must be less than or equal to the amount from the reimbursing item. The sum
   * of the reimbursed and allocated amounts for all Reimbursements must be less than or equal to
   * the amount of this item.
   *
   * @param reimbursingItem  the reimbursing item, required
   * @param reimbursedAmount the amount reimbursed by the reimbursing item, required
   * @param allocatedAmount  the amount written off by this item, optional, default 0.090
   */
  public void reimburse(Item reimbursingItem, Double reimbursedAmount, Double allocatedAmount) {
    // Item must be a debit against a receivable account to be reimbursed.
    if (!(account.isReceivable() && debit)) {
      throw new InvalidParametersException("Cannot reimburse non-receivable item " + this);
    }
    // Validate against receivable amount and current set of reimbursements.
    Double total = reimbursedAmount + allocatedAmount;
    // Sum up the existing reimbursement amounts along with the current ones.
    for (Reimbursement reimbursement : reimbursements) {
      total += reimbursement.reimbursedAmount + reimbursement.allocatedAmount;
    }
    // Total must be less than or equal to the receivable amount.
    if (total.compareTo(amount) > 0) {
      throw new InvalidParametersException(
        "Total reimbursement is " + total + " but receivable amount is " + amount +
        "; check for prior-year reimbursement of receivable in these reimbursements " +
        reimbursements + ")");
    }
    // Add the reimbursement to the set of reimbursements in both this item and
    // the reimbursing item (two-way visibility).
    Reimbursement reimbursement =
      new Reimbursement(this, reimbursingItem, reimbursedAmount, allocatedAmount);
    reimbursements.add(reimbursement);
    reimbursingItem.getReimbursements().add(reimbursement);
  }

  @Override
  public String toString() {
    // Get only transaction id and description to avoid infinite loop in display
    // of items within transaction
    return "Item [year=" + transaction.getYear() + ", transaction=" +
           (transaction.getId() == null ? "(no trans id)" : transaction.getId()) +
           ", description=" + transaction.getDescription() + ", amount=" + amount + ", account=" +
           account + ", debit=" + debit + ", checked=" + checked + "]";
  }
}
