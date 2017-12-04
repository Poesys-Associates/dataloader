/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.util.List;
import java.util.Set;


/**
 * Unit test implementation of the IDataAccessService interface that throws
 * runtime exceptions from the methods.
 * 
 * @author Robert J. Muller
 */
public class RuntimeExceptionDataAccessService implements IDataAccessService {

  @Override
  public void storeEntity(String entityName, List<FiscalYear> years) {
    throw new RuntimeException("storeEntity() runtime exception");
  }

  @Override
  public void storeTransactions(Set<Transaction> transactions) {
    throw new RuntimeException("storeTransactions() runtime exception");
  }
}
