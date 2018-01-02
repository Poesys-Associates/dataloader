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
  public void store(String entityName, CapitalStructure structure, List<FiscalYear> years,
                    Set<Transaction> transactions, IDataAccessService storageService) {
    try {
      // Store the capital structure; this works around a persistence bug that doesn't store the
      // structure before storing accounts that refer to it, so it needs to be persisted first.
      storageService.storeCapitalStructure(structure);
      storageService.storeFiscalYears(years);
      // Store the entity, fiscal years, account groups, and accounts.
      storageService.storeEntity(entityName, years);
      // Store all the transactions in one committed transaction.
      storageService.storeTransactions(transactions);
    } catch (Throwable e) {
      // Pass on exception with store failed message
      throw new RuntimeException("exception in fiscal year storage operation", e);
    }

    logger.info(STORED_MSG);
  }
}
