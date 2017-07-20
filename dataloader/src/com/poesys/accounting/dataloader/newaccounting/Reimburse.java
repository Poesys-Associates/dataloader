/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import com.poesys.db.InvalidParametersException;


/**
 * A data transfer object that represents the association of a reimbursing
 * transaction item to a receivable transaction item; contains some combination
 * of reimbursed amount and allocated amount (an amount not included in the
 * reimbursing item to be applied to the receivable--a writeoff, in other
 * words); see Item for details on constraints
 * 
 * @author Robert J. Muller
 */
public class Reimburse {
  /** the receivable Item to be reimbursed */
  private final Item receivable;
  /** the reimbursing item */
  private final Item reimbursement;
  /** the amount reimbursed */
  private final Double reimbursedAmount;
  /** the amount not reimbursed but applied to the receivable */
  private final Double allocatedAmount;

  // Messages

  /** null parameter to constructor */
  private static final String NULL_PARAMETER_ERROR =
    "Reimburse parameters are required but one is null";

  /**
   * Create a Reimbursement object.
   * 
   * @param receivable the receivable Item to be reimbursed
   * @param reimbursement the reimbursing item
   * @param reimbursedAmount the amount reimbursed
   * @param allocatedAmount the amount not reimbursed but applied to the
   *          receivable; default 0.00
   */
  public Reimburse(Item receivable,
                   Item reimbursement,
                   Double reimbursedAmount,
                   Double allocatedAmount) {
    if (receivable == null || reimbursement == null || reimbursedAmount == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.receivable = receivable;
    this.reimbursement = reimbursement;
    this.reimbursedAmount = reimbursedAmount;
    this.allocatedAmount = allocatedAmount == null ? 0.0D : allocatedAmount;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
      prime * result + ((receivable == null) ? 0 : receivable.hashCode());
    result =
      prime * result + ((reimbursement == null) ? 0 : reimbursement.hashCode());
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
    Reimburse other = (Reimburse)obj;
    if (receivable == null) {
      if (other.receivable != null)
        return false;
    } else if (!receivable.equals(other.receivable))
      return false;
    if (reimbursement == null) {
      if (other.reimbursement != null)
        return false;
    } else if (!reimbursement.equals(other.reimbursement))
      return false;
    return true;
  }

  /**
   * Get the reimbursed item.
   * 
   * @return a reimbursedItem
   */
  public Item getReimbursedItem() {
    return receivable;
  }

  /**
   * Get the reimbursing item.
   * 
   * @return a item
   */
  public Item getReimbursingItem() {
    return reimbursement;
  }

  /**
   * Get the reimbursed amount.
   * 
   * @return a reimbursedAmount
   */
  public Double getReimbursedAmount() {
    return reimbursedAmount;
  }

  /**
   * Get the allocated amount.
   * 
   * @return a allocatedAmount
   */
  public Double getAllocatedAmount() {
    return allocatedAmount;
  }
}
