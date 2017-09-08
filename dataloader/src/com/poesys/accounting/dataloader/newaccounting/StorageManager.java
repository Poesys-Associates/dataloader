/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * Production implementation of the IStorageManager interface
 * 
 * @author Robert J. Muller
 */
public class StorageManager extends AbstractValidatingStorageManager {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(StorageManager.class);
  
  // messages
  
  private static final String STORED_MSG = "stored objects for all years";

  @Override
  public void store(String entityName, List<FiscalYear> years,
                    IDataAccessService storageService) {
    Set<Transaction> transactions = new HashSet<Transaction>();

    try {
      // Store the entity, fiscal years, account groups, and accounts.
      storageService.storeEntity(entityName, years);
      // Build a complete set of transactions for all the years.
      for (FiscalYear year : years) {
        transactions.addAll(year.getTransactions());
      }
      // Store all the transactions in one committed transaction.
      storageService.storeTransactions(transactions);
    } catch (Throwable e) {
      // Pass on exception with store failed message
      throw new RuntimeException("exception in fiscal year storage operation",
                                 e);
    }
    
    logger.info(STORED_MSG);
  }
}
