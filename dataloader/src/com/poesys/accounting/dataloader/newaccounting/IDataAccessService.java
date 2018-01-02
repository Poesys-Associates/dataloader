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
 * A set of services that stores accounting data to the database. Provides for both production
 * services and fake services for unit testing. The implementation of this service must be
 * object-based, not static, as it contains data shared between the store methods; these methods are
 * separate because the underlying Accounting/DB system requires separate, sequential store
 * operations.
 *
 * @author Robert J. Muller
 */
public interface IDataAccessService {
  /**
   * <p> Create and store Accounting/DB objects to the database for a capital structure with
   * potentially multiple capital entities. The capital entities should not have nested account
   * objects at this point. </p> <p> <strong>WARNING: run this method before running
   * storeEntities(), as that method may persist capital accounts that require the capital entities
   * be already present (due to a bug in Poesys/DB, the nested capital entity isn't automatically
   * persisted before the account). .</strong> </p>
   *
   * @param structure the capital structure object containing the capital entities
   */
  void storeCapitalStructure(CapitalStructure structure);

  /**
   * <p>Create and store Accounting/DB objects to the database for a list of fiscal years. The input
   * fiscal year objects should not have any nested fiscal-year-account links at this point.</p>
   * <p><strong>WARNING: run this method before running storeEntity(), as that method may encounter
   * problems with fiscal-year-account links and duplicate inserts.</strong></p>
   *
   * @param years a list of years with no fiscal-year-account links
   */
  void storeFiscalYears(List<FiscalYear> years);

  /**
   * <p> Create and store Accounting/DB objects to the database for an entity and a set of fiscal
   * years for that entity. The fiscal years should have a set of fiscal-year-account links and the
   * accounts linked, all of which get inserted during this call. </p> <p> <strong>WARNING: run this
   * method before running storeTransactions(), as that method depends on having the fiscal years,
   * entity, account groups, and accounts created.</strong> </p>
   *
   * @param entityName the name of the accounting entity to store
   * @param years      the list of fiscal years for the entity (order is important)
   */
  void storeEntity(String entityName, List<FiscalYear> years);

  /**
   * Create and store Accounting/DB transaction-related objects to the database for a set of
   * transactions. This method assumes that entity, fiscal year, account group, and account data is
   * already stored to the database. It also makes no assumptions about the transaction set. As this
   * method represents a complete database transaction, the caller should submit a list of
   * transactions that is complete with respect to the logical unit of work, usually transactions
   * for all the existing fiscal years.
   *
   * @param transactions the set of transactions to store
   */
  void storeTransactions(Set<Transaction> transactions);
}
