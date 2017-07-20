/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.util.List;
import java.util.Set;


/**
 * A set of services that stores accounting data to the database. Provides for
 * both production services and fake services for unit testing. The
 * implementation of this service must be object-based, not static, as it
 * contains data shared between the store methods; these methods are separate
 * because the underlying Accounting/DB system requires separate, sequential
 * store operations.
 * 
 * @author Robert J. Muller
 */
public interface IDataAccessService {
  /**
   * Create and store Accounting/DB objects to the database for an entity and a
   * set of fiscal years for that entity.
   * 
   * @param entityName the name of the accounting entity to store
   * @param years the list of fiscal years for the entity (order is important)
   */
  public void storeEntity(String entityName, List<FiscalYear> years);

  /**
   * Create and store Accounting/DB transaction-related objects to the database
   * for a set of transactions. This method assumes that entity, fiscal year,
   * account group, and account data is already stored to the database. It also
   * makes no assumptions about the transaction set. As this method represents
   * a complete database transaction, the caller should submit a list of
   * transactions that is complete with respect to the logical unit of work,
   * usually transactions for all the existing fiscal years.
   * 
   * @param transactions the set of transactions to store
   */
  public void storeTransactions(Set<Transaction> transactions);
}
