/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.util.List;
import java.util.Set;


/**
 * Unit test implementation of the IDataAccessService interface that does
 * nothing when the methods are called; throws no exceptions.
 * 
 * @author Robert J. Muller
 */
public class DoNothingDataAccessService implements IDataAccessService {

  @Override
  public void storeEntity(String entityName, List<FiscalYear> years) {
    // Does nothing, no exceptions thrown
  }

  @Override
  public void storeTransactions(Set<Transaction> transactions) {
    // Does nothing, no exceptions thrown
  }

  @Override
  public void storeAccountGroups(List<AccountGroup> groups) {
    // Does nothing, no exceptions thrown
  }
}
