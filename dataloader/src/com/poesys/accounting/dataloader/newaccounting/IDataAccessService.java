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
   * <p>
   * Create and store Accounting/DB objects to the database for a list of
   * account groups. This list corresponds to the account groups for all fiscal
   * years, ordered within asset types. For example, all "Assets" groups come
   * together and are ordered within that type. The key for an account group in
   * the database is the combination of the account type and the order number,
   * which orders a set of account groups within the type regardless of whether
   * the group appears in a particular fiscal year. There is also a unique
   * constraint on the combination of accountType and group name. The account
   * types are a fixed set of types already in the database; they do not change.
   * </p>
   * <p>
   * Note that accounts link to groups in the database through the
   * FiscalYearAccount link, which in turn orders the accounts within the fiscal
   * year. The new-accounting system guarantees that these two orderings are
   * consistent; fiscal years always order accounts to preserve group order.
   * </p>
   * <p>
   * <strong>WARNING: run this method before running storeEntity(), as that
   * method depends on having the proper account groups created. The method will
   * remove any existing account groups before storing the passed list.</strong>
   * </p>
   * 
   * @param groups the list of groups to create and store
   */
  public void storeAccountGroups(List<AccountGroup> groups);

  /**
   * <p>
   * Create and store Accounting/DB objects to the database for an entity and a
   * set of fiscal years for that entity.
   * </p>
   * <p>
   * <strong>WARNING: run this method before running storeTransactions(), as
   * that method depends on having the fiscal years, entity, account groups, and
   * accounts created.</strong>
   * </p>
   * 
   * @param entityName the name of the accounting entity to store
   * @param years the list of fiscal years for the entity (order is important)
   */
  public void storeEntity(String entityName, List<FiscalYear> years);

  /**
   * Create and store Accounting/DB transaction-related objects to the database
   * for a set of transactions. This method assumes that entity, fiscal year,
   * account group, and account data is already stored to the database. It also
   * makes no assumptions about the transaction set. As this method represents a
   * complete database transaction, the caller should submit a list of
   * transactions that is complete with respect to the logical unit of work,
   * usually transactions for all the existing fiscal years.
   * 
   * @param transactions the set of transactions to store
   */
  public void storeTransactions(Set<Transaction> transactions);
}
