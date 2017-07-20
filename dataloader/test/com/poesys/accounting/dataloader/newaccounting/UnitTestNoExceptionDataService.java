/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.util.List;
import java.util.Set;


/**
 * Implementation of the IDataAccessService interface for unit testing; throws
 * no exceptions and tracks calls to the methods.
 * 
 * @author Robert J. Muller
 */
public class UnitTestNoExceptionDataService implements IDataAccessService {
  private int storeEntityCalls = 0;
  private int storeTransactionCalls = 0;

  @Override
  public void storeEntity(String entityName, List<FiscalYear> years) {
    storeEntityCalls++;
  }

  @Override
  public void storeTransactions(Set<Transaction> transactions) {
    storeTransactionCalls++;
  }

  /**
   * Get the count of storeEntity() calls.
   * 
   * @return a count
   */
  public int getStoreEntityCalls() {
    return storeEntityCalls;
  }

  /**
   * Get the count of storeTransaction() calls.
   * 
   * @return a count
   */
  public int getStoreTransactionCalls() {
    return storeTransactionCalls;
  }
}
