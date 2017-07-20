/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;


/**
 * A data transfer object containing data about a single item within a
 * transaction; the item has an amount which is a debit or credit against a
 * specific account; it also may have an optional set of Reimbursement objects
 * that represent links from receivable items to reimbursing items.
 * 
 * @author Robert J. Muller
 */
public class Item {
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
  private final Set<Reimbursement> reimbursements =
    new HashSet<Reimbursement>();

  /**
   * An association class that links this item to a reimbursing item; valid only
   * for items with receivable accounts.
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
     * Create a Reimbursement object. The reimbursed amount must be less than or
     * equal to the amount of the reimbursing item. The two items must be
     * against the same account.
     * 
     * @param receivable the item being reimbursed
     * @param reimbursingItem the item reimbursing this receivable item
     * @param reimbursedAmount the dollar amount reimbursed
     * @param allocatedAmount the dollar amount written off
     */
    public Reimbursement(Item receivable,
                         Item reimbursingItem,
                         Double reimbursedAmount,
                         Double allocatedAmount) {
      if (receivable == null) {
        throw new InvalidParametersException("Reimbursement requires a receivable item");
      }
      if (reimbursingItem == null) {
        throw new InvalidParametersException("Reimbursement requires a reimbursing item");
      }
      if (reimbursedAmount == null) {
        throw new InvalidParametersException("Reimbursement requires a reimbursed amount");
      }
      this.receivable = receivable;
      this.reimbursingItem = reimbursingItem;
      this.reimbursedAmount = reimbursedAmount;
      // default allocated amount to 0.00
      this.allocatedAmount = allocatedAmount == null ? 0.00D : allocatedAmount;

      // Validate reimbursed amount against reimbursing item amount
      if (reimbursedAmount.compareTo(reimbursingItem.amount) > 0) {
        throw new InvalidParametersException("Reimbursed amount "
                                             + reimbursedAmount
                                             + " must be less than or equal to item amount "
                                             + reimbursingItem.amount);

      }

      // Ensure that both items are against the same receivable account.
      if (!(receivable.getAccount().isReceivable() && receivable.getAccount().equals(reimbursingItem.getAccount()))) {
        throw new InvalidParametersException("Reimbursement must have same receivable account as receivable: "
                                             + receivable.getAmount()
                                             + " vs. "
                                             + reimbursingItem.getAccount());
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result =
        prime * result + ((receivable == null) ? 0 : receivable.hashCode());
      result =
        prime * result
            + ((reimbursingItem == null) ? 0 : reimbursingItem.hashCode());
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
      Reimbursement other = (Reimbursement)obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (receivable == null) {
        if (other.receivable != null)
          return false;
      } else if (!receivable.equals(other.receivable))
        return false;
      if (reimbursingItem == null) {
        if (other.reimbursingItem != null)
          return false;
      } else if (!reimbursingItem.equals(other.reimbursingItem))
        return false;
      return true;
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
      return "Reimbursement [receivable=" + receivable + ", reimbursingItem="
             + reimbursingItem + ", reimbursedAmount=" + reimbursedAmount
             + ", allocatedAmount=" + allocatedAmount + "]";
    }
  }

  // Messages

  /** null parameter to constructor */
  private static final String NULL_PARAMETER_ERROR =
    "Item parameters are required but one is null";

  /**
   * Create an Item object.
   * 
   * @param transaction the parent transaction that owns this item
   * @param amount the dollar amount of the item
   * @param account the account to which the item applies
   * @param debit whether the item is a debit (true) or credit (false) item
   * @param checked whether the item has been reconciled against an external
   *          data source; default false
   */
  public Item(Transaction transaction,
              Double amount,
              Account account,
              Boolean debit,
              Boolean checked) {
    if (transaction == null || amount == null || account == null
        || debit == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
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
    result =
      prime * result + ((transaction == null) ? 0 : transaction.hashCode());
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
    Item other = (Item)obj;
    if (account == null) {
      if (other.account != null)
        return false;
    } else if (!account.equals(other.account))
      return false;
    if (transaction == null) {
      if (other.transaction != null)
        return false;
    } else if (!transaction.equals(other.transaction))
      return false;
    return true;
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
   * Get a thread-safe set of the reimbursements against this receivable. If
   * this is not a receivable item, returns an empty set
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
   * Create a Reimbursement link and add it to the reimbursements for this item
   * and to the reimbursements in the reimbursing item. The account for this
   * item and the account for the reimbursing item must be the same. The
   * allocated amount is optional and defaults to 0.00. The reimbursing amount
   * must be less than or equal to the amount from the reimbursing item. The sum
   * of the reimbursed and allocated amounts for all Reimbursements must be less
   * than or equal to the amount of this item.
   * 
   * @param reimbursingItem the reimbursing item, required
   * @param reimbursedAmount the amount reimbursed by the reimbursing item,
   *          required
   * @param allocatedAmount the amount written off by this item, optional,
   *          default 0.090
   */
  public void reimburse(Item reimbursingItem, Double reimbursedAmount,
                        Double allocatedAmount) {
    // Item must be a debit against a receivable account to be reimbursed.
    if (!(account.isReceivable() && debit)) {
      throw new InvalidParametersException("Cannot reimburse non-receivable item "
                                           + this);
    }
    // Validate against receivable amount and current set of reimbursements.
    Double total = reimbursedAmount + allocatedAmount;
    // Sum up the existing reimbursement amounts along with the current ones.
    for (Reimbursement reimbursement : reimbursements) {
      total += reimbursement.reimbursedAmount + reimbursement.allocatedAmount;
    }
    // Total must be less than or equal to the receivable amount.
    if (total.compareTo(amount) > 0) {
      throw new InvalidParametersException("Total reimbursement is " + total
                                           + " but receivable amount is "
                                           + amount);
    }
    // Add the reimbursement to the set of reimbursements in both this item and
    // the reimbursing item (two-way visibility).
    Reimbursement reimbursement = new Reimbursement(this,
                                                    reimbursingItem,
                                                    reimbursedAmount,
                                                    allocatedAmount);
    reimbursements.add(reimbursement);
    reimbursingItem.getReimbursements().add(reimbursement);
  }

  @Override
  public String toString() {
    // Get only transaction id and description to avoid infinite loop in display
    // of items within transaction
    return "Item [transaction=" + transaction.getId() + ": "
           + transaction.getDescription() + ", amount=" + amount + ", account="
           + account + ", debit=" + debit + ", checked=" + checked + "]";
  }
}
