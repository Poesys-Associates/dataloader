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

/**
 * Manages storing the new accounting data into the accounting database; interface to Accounting/DB;
 * implementations may be production or test to support fake-based unit testing
 *
 * @author Robert J. Muller
 */
public interface IStorageManager {
  /**
   * Validate a set of fiscal years by creating balance sheet and income statements and checking the
   * balances for each year. Returns true if all years balance, false if at least one year does not
   * balance.
   *
   * @param years the set of fiscal years
   * @return true if all data are valid, false if any are not
   */

  Boolean validate(List<FiscalYear> years);

  /**
   * Store the data in a set of fiscal years to the database.
   *
   * @param entityName     the name of the accounting entity being stored
   * @param structure      the capital structure of the accounting system
   * @param years          the list of new years (order is preserved)
   * @param transactions   the set of transactions
   * @param storageService the storage service to call; permits unit testing through a fake storage
   *                       service object
   */
  void store(String entityName, CapitalStructure structure, List<FiscalYear> years,
             Set<Transaction> transactions, IDataAccessService storageService);
}