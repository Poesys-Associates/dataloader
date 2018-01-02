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

import com.poesys.accounting.bs.account.*;
import com.poesys.accounting.bs.transaction.*;
import com.poesys.accounting.dataloader.newaccounting.Item.Reimbursement;
import com.poesys.accounting.db.account.AccountFactory;
import com.poesys.bs.delegate.DelegateException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.connection.JdbcConnectionManager;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * The production services for storing Accounting/DB data to the database given a set of
 * "newaccounting" objects
 *
 * @author Robert J. Muller
 */
public class AccountingDbService implements IDataAccessService {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(AccountingDbService.class);

  /** SQL for ID map insert */
  private static final String ID_MAP_INSERT_SQL =
    "INSERT INTO IdMap(entityName, year, oldId, newId) VALUES (?, ?, ?, ?)";

  // messages
  private static final String NO_LINKS_IN_GROUP_ERROR = "no fiscal-year-account links in ";
  private static final String LINKING_SQL_ERROR =
    "SQL exception linking account to year and group: ";
  private static final String REIMBURSEMENT_SQL_ERROR = "SQL exception creating reimbursement";
  private static final String NULL_ENTITY_ERROR = "Entity is null, run storeEntity() first";
  private static final String ID_MAP_STORE_ERROR = "exception storing id map";
  private static final String NO_REIMBURSEMENTS_ERROR =
    "couldn't find reimbursement objects for receivable ";
  private static final String UNKNOWN_ACCOUNT_ERROR = "unknown account in item: ";
  private static final String CAP_ENTITY_NULL_NO_ACCOUNTS_ERROR =
    "capital entity is null and has no associated accounts";
  private static final String CAP_ENTITY_CREATION_ERROR = "could not get or create capital entity ";
  private static final String NULL_PARAMETER_ERROR =
    "null account, entity name, or capital entity for creating capital account";
  private static final String MISSING_ACCOUNT_ERROR =
    "capital entity is missing associated capital account";
  private static final String YEAR_QUERY_ERROR = "fiscal year query failed for year ";
  private static final String GROUP_QUERY_ERROR = "group query failed for type ";
  private static final String NO_STRUCTURE_ERROR = "no capital structure for storing";
  private static final String FISCAL_YEAR_NOT_FOUND_ERROR = "Fiscal year not found: ";

  /** the primary key of the stored, existing entity created by storeEntity() */
  private IPrimaryKey entityKey = null;

  /**
   * a map of database accounts created by storeEntity() indexed on newaccounting account for lookup
   * by storeTransactions()
   */
  private final Map<Account, BsAccount> accounts = new HashMap<>();

  /** a map of item objects indexed by item, AR objects only for reimbursements */
  private final Map<Item, BsItem> arItems = new HashMap<>();

  /** a map of account types indexed by type for lookup of database type object */
  private final Map<AccountType, BsAccountType> types = new HashMap<>();

  /**
   * Create an AccountingDbService object.
   */
  public AccountingDbService() {
    refreshAccountTypeCache();
  }

  /**
   * Query the account types from the database to refresh the local cache of account types keyed on
   * new-accounting account type (an enum).
   */
  private void refreshAccountTypeCache() {
    AccountTypeDelegate delegate = AccountDelegateFactory.getAccountTypeDelegate();
    // Clear the cache.
    types.clear();
    // Query the business objects from the database.
    List<BsAccountType> typeList = delegate.getAllObjects(5);
    // Get the enum value for the type and map the type.
    for (BsAccountType type : typeList) {
      AccountType localType = AccountType.databaseValueOf(type.getAccountType());
      types.put(localType, type);
    }
  }

  @Override
  public void storeCapitalStructure(CapitalStructure structure) {
    CapitalEntityDelegate delegate = AccountDelegateFactory.getCapitalEntityDelegate();

    if (structure == null || structure.getEntities().isEmpty()) {
      throw new InvalidParametersException(NO_STRUCTURE_ERROR);
    }

    List<BsCapitalEntity> entities = new ArrayList<>(structure.getEntities().size());

    for (CapitalEntity entity : structure.getEntities()) {
      BsCapitalEntity persistedEntity = delegate.createCapitalEntity(entity.getName());
      entities.add(persistedEntity);
    }

    delegate.process(entities);
  }

  @Override
  public void storeFiscalYears(List<FiscalYear> years) {
    FiscalYearDelegate delegate = AccountDelegateFactory.getFiscalYearDelegate();
    List<BsFiscalYear> persistedYears = new ArrayList<>(years.size());
    for (FiscalYear year : years) {
      BsFiscalYear persistedYear =
        delegate.createFiscalYear(year.getYear(), year.getStart(), year.getEnd());
      persistedYears.add(persistedYear);
    }
    delegate.process(persistedYears);
  }

  @Override
  public void storeEntity(String entityName, List<FiscalYear> years) {
    AccountTypeDelegate typeDelegate = AccountDelegateFactory.getAccountTypeDelegate();
    FiscalYearDelegate yearDelegate = AccountDelegateFactory.getFiscalYearDelegate();
    EntityDelegate entityDelegate = AccountDelegateFactory.getEntityDelegate();

    // Intermediate storage and lookup structures
    List<BsFiscalYear> yearList = new ArrayList<>();

    // Refresh the account type object cache. This ensures that each access
    // type in the map contains the latest group information from the database.
    refreshAccountTypeCache();

    BsEntity entity = entityDelegate.createEntity(entityName);

    // list of accumulated links to insert later
    List<BsFiscalYearAccount> links = new ArrayList<>();

    for (FiscalYear year : years) {
      // Query the previously stored fiscal year object.
      IPrimaryKey key = AccountFactory.getFiscalYearPrimaryKey(year.getYear());
      BsFiscalYear fiscalYear = yearDelegate.getObject((NaturalPrimaryKey)key);
      if (fiscalYear == null) {
        throw new RuntimeException(FISCAL_YEAR_NOT_FOUND_ERROR + year.getYear());
      }
      yearList.add(fiscalYear);

      // Create accounts.
      for (FiscalYearAccount link : year.getAccounts()) {
        // Extract group and create if needed.
        BsAccountGroup group = getPersistedGroup(link.getGroup());

        // Extract account and create if needed.
        Account account = link.getAccount();
        BsAccount persistedAccount = accounts.get(account);
        if (persistedAccount == null) {
          persistedAccount = createAccount(entity, account);
          // Add the account to the map for later lookups.
          logger.debug("Adding account to map: " + account);
          accounts.put(account, persistedAccount);
        }

        // Create the fiscal-year-account link but don't add it to the linked
        // objects for persisting yet. This works around a bug with
        // nested-object processing, you need to store the objects before the
        // link. Instead, accumulate the links for later insert.
        links.add(
          yearDelegate.createFiscalYearAccount(persistedAccount, group, fiscalYear, persistedAccount.getAccountName(),
                                               persistedAccount.getEntityName(), fiscalYear.getYear(),
                                               link.getAccountOrderNumber(),
                                               link.getGroupOrderNumber(), group.getAccountType(),
                                               group.getGroupName(), group.toDto()));
      }
    }

    // Store everything.
    yearDelegate.process(yearList);
    entityDelegate.process(entity);
    List<BsAccountType> typeList = new ArrayList<>(types.values());
    typeDelegate.process(typeList);

    // Save the primary key for the persisted entity.
    entityKey = entity.getPrimaryKey();

    // Refresh the type map.
    refreshAccountTypeCache();

    storeFiscalYearAccountLinks(links);
  }

  private void storeFiscalYearAccountLinks(List<BsFiscalYearAccount> links) {
    FiscalYearDelegate yearDelegate = AccountDelegateFactory.getFiscalYearDelegate();
    EntityDelegate entityDelegate = AccountDelegateFactory.getEntityDelegate();

    // Query the entity.
    BsEntity entity = entityDelegate.getObject((NaturalPrimaryKey)entityKey);
    if (entity == null) {
      throw new RuntimeException(CAP_ENTITY_CREATION_ERROR);
    }

    // Link accounts to fiscal years, associate groups.
    for (BsFiscalYearAccount link : links) {
      try {
        // Recreate the link to get the committed objects from the cache.
        BsAccount account = getAccountByEntityAndName(entity, link.getAccountName());
        BsAccountGroup group =
          getGroupByTypeAndName(link.getGroup().getType(), link.getGroupName());
        if (group == null) {
          throw new RuntimeException(
            GROUP_QUERY_ERROR + link.getGroup().getType() + " group name " + link.getGroupName());
        }
        IPrimaryKey key = link.getFiscalYear().getPrimaryKey();
        BsFiscalYear year = yearDelegate.getObject((NaturalPrimaryKey)key);
        if (year == null) {
          throw new RuntimeException(YEAR_QUERY_ERROR + link.getFiscalYear().getYear());
        }

        link = yearDelegate.createFiscalYearAccount(account, group, year, account.getAccountName(),
                                                    entity.getEntityName(), year.getYear(),
                                                    link.getAccountOrderNumber(),
                                                    link.getGroupOrderNumber(),
                                                    link.getAccountType(), link.getGroupName(),
                                                    group.toDto());

        account.addFiscalYearAccountFiscalYearAccount(link);
        group.addAccountsFiscalYearAccount(link);
        year.addFiscalYearAccountFiscalYearAccount(link);
      } catch (SQLException e) {
        String message = LINKING_SQL_ERROR + link.getPrimaryKey();
        logger.error(message, e);
        throw new RuntimeException(message, e);
      }
    }

    // Store the entity again, this time storing the links.
    entityDelegate.process(entity);
  }

  /**
   * Get a persisted account business object by name from an entity.
   *
   * @param entity the entity that owns the accounts
   * @param name   the name of the account to find
   * @return the account
   */
  private BsAccount getAccountByEntityAndName(BsEntity entity, String name) {
    BsAccount persistedAccount = null;

    for (BsAccount account : entity.getAccounts()) {
      if (account.getAccountName().equals(name)) {
        persistedAccount = account;
        break;
      }
    }
    return persistedAccount;
  }

  /**
   * Get a persisted group business object by name from a type.
   *
   * @param type the account type business object
   * @param name the group name
   * @return the group with the specified name belonging to the type
   */
  private BsAccountGroup getGroupByTypeAndName(BsAccountType type, String name) {
    AccountTypeDelegate delegate = AccountDelegateFactory.getAccountTypeDelegate();
    BsAccountGroup persistedGroup = null;

    // Re-query the type to make sure we've got the latest cached version.
    type = delegate.getObject((NaturalPrimaryKey)type.getPrimaryKey());

    for (BsAccountGroup group : type.getGroups()) {
      if (group.getGroupName().equals(name)) {
        persistedGroup = group;
        break;
      }
    }

    return persistedGroup;
  }

  /**
   * Get a persisted group object based on the new-accounting group object; this looks up the group
   * in the local account-type cache using the account type associated with the specified group. The
   * group must have at least one fiscal year account link. If there is no persisted group with the
   * same name as the specified group, the method creates a new group and returns that; note that
   * the group is not yet persisted. The method adds the group to the type in the type map cache.
   *
   * @param group the group to look up
   * @return the persisted group corresponding to the specified group, or null if there is no
   * persisted group
   */
  private BsAccountGroup getPersistedGroup(AccountGroup group) {
    AccountTypeDelegate typeDelegate = AccountDelegateFactory.getAccountTypeDelegate();
    BsAccountGroup returnGroup = null;

    // Make sure there is at least one fiscal-year-account link.
    if (group.getFiscalYearAccounts() == null || group.getFiscalYearAccounts().size() == 0) {
      throw new RuntimeException(NO_LINKS_IN_GROUP_ERROR + group);
    }

    // Assume group doesn't change type (a constraint), so get the first link
    // from the fiscal-year-account links for the group and take that type.
    AccountType type = group.getFiscalYearAccounts().get(0).getAccountType();

    // Get the persisted type from the types map.
    BsAccountType persistedType = types.get(type);

    // Iterate through the type's groups to see if the group is already there.
    for (BsAccountGroup persistedGroup : persistedType.getGroups()) {
      if (persistedGroup.getGroupName().equals(group.getName())) {
        // Found the named group, set the return and break out of the loop.
        returnGroup = persistedGroup;
        break;
      }
    }
    if (returnGroup == null) {
      returnGroup = typeDelegate.createAccountGroup(persistedType, persistedType.getAccountType(),
                                                    group.getName());
      returnGroup.setType(persistedType);
      // Add the new group to the persisted type to keep the cache consistent.
      persistedType.addGroupsAccountGroup(returnGroup);
    }
    return returnGroup;
  }

  /**
   * Based on the presence of the capital entity in an input account, create the persistent account
   * business object of the appropriate subclass belonging to a newly created entity business object
   * and return it as a generic persistent account object. This is a Factory Method pattern.
   *
   * @param entity  the newly created and not-yet-persisted entity business object
   * @param account the new-accounting-system account
   * @return the generic, persistent account object
   */
  private BsAccount createAccount(BsEntity entity, Account account) {
    BsAccount genericAccount;

    String entityName = entity.getEntityName();
    CapitalEntity capEntity = account.getCapitalEntity();
    if (capEntity != null) {
      // Associate the account with the capital entity.
      genericAccount = createCapitalEntityAccount(account, entityName, capEntity);
    } else {
      // Capital entity not associated, create a simple account.
      SimpleAccountDelegate accountDelegate = AccountDelegateFactory.getSimpleAccountDelegate();

      // Create simple account.
      BsSimpleAccount simpleAccount =
        accountDelegate.createSimpleAccount(account.getName(), entityName, account.getDescription(),
                                            account.isDebitDefault(), true, account.isReceivable());
      genericAccount = new BsAccount(simpleAccount.toDto());
    }

    // Add the generic account to the entity.
    genericAccount.setEntity(entity);
    entity.addAccountsAccount(genericAccount);
    return genericAccount;
  }

  /**
   * Create an account that associates with a capital entity. This can be a capital account or a
   * distribution account. The method will create a persisted capital entity if one doesn't already
   * exist. The method returns a superclass business object for the account.
   *
   * @param account    the account to create
   * @param entityName the name of the entity that owns the account
   * @param capEntity  the capital entity to associate with the account
   * @return the generic account
   */
  private BsAccount createCapitalEntityAccount(Account account, String entityName, CapitalEntity
    capEntity) {
    BsAccount genericAccount;

    // All the parameters must be not null.
    if ((account == null || entityName == null || capEntity == null)) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }

    String capAccount = capEntity.getCapitalAccountName();
    String distAccount = capEntity.getDistributionAccountName();

    if (capAccount == null) {
      throw new RuntimeException(MISSING_ACCOUNT_ERROR);
    }

    BsCapitalEntity persistedCapitalEntity = getPersistedCapitalEntity(capEntity);

    if (capAccount.equals(account.getName())) {
      // Capital entity associated and the account name is the entity capital
      // account name, so create a capital account.
      CapitalAccountDelegate accountDelegate = AccountDelegateFactory.getCapitalAccountDelegate();

      BsCapitalAccount capitalAccount =
        accountDelegate.createCapitalAccount(account.getName(), entityName,
                                             account.getDescription(), account.isDebitDefault(),
                                             true, capEntity.getOwnership().doubleValue(),
                                             capEntity.getName());
      capitalAccount.setCapitalEntity(persistedCapitalEntity);
      persistedCapitalEntity.addCapitalAccountCapitalAccount(capitalAccount);
      genericAccount = new BsAccount(capitalAccount.toDto());
    } else if (distAccount != null && distAccount.equals(account.getName())) {
      // Capital entity associated, distribution account exists, and the account name is the entity
      // distribution account name, so create a distribution account.
      DistributionAccountDelegate accountDelegate =
        AccountDelegateFactory.getDistributionAccountDelegate();

      BsDistributionAccount distributionAccount =
        accountDelegate.createDistributionAccount(account.getName(), entityName,
                                                  account.getDescription(),
                                                  account.isDebitDefault(), true,
                                                  capEntity.getName());
      distributionAccount.setCapitalEntity(persistedCapitalEntity);
      persistedCapitalEntity.addDistributionAccountDistributionAccount(distributionAccount);
      genericAccount = new BsAccount(distributionAccount.toDto());
    } else {
      // no relevant account in capital entity
      throw new RuntimeException(CAP_ENTITY_NULL_NO_ACCOUNTS_ERROR);
    }
    return genericAccount;
  }

  /**
   * Get a persisted capital entity business object (either existing or new) corresponding to a
   * new-accounting capital entity.
   *
   * @param capEntity the capital entity
   * @return the persisted capital entity business object
   */
  private BsCapitalEntity getPersistedCapitalEntity(CapitalEntity capEntity) {
    // First, get the persisted capital entity or create a new one.
    CapitalEntityDelegate ceDelegate = AccountDelegateFactory.getCapitalEntityDelegate();
    IPrimaryKey key = AccountFactory.getCapitalEntityPrimaryKey(capEntity.getName());
    BsCapitalEntity persistedCapitalEntity = ceDelegate.getObject((NaturalPrimaryKey)key);
    if (persistedCapitalEntity == null) {
      // no entity yet, create a new one.
      persistedCapitalEntity = ceDelegate.createCapitalEntity(capEntity.getName());
      if (persistedCapitalEntity == null) {
        throw new RuntimeException(CAP_ENTITY_CREATION_ERROR + capEntity.getName());
      }
    }
    return persistedCapitalEntity;
  }

  @Override
  public void storeTransactions(Set<Transaction> transactions) {
    // a map of database transaction ids indexed by old transaction id
    Map<OldId, BigDecimal> idMap = new HashMap<>();

    // Query the entity.
    EntityDelegate entityDelegate = AccountDelegateFactory.getEntityDelegate();
    BsEntity entity = entityDelegate.getObject((NaturalPrimaryKey)entityKey);

    if (entity == null) {
      throw new RuntimeException(NULL_ENTITY_ERROR);
    }

    logger.debug("Storing transactions");

    TransactionDelegate delegate = TransactionDelegateFactory.getTransactionDelegate();

    List<BsTransaction> transactionList = new ArrayList<>(transactions.size());

    createTransactions(transactions, delegate, transactionList, idMap);
    linkReimbursements(transactions, delegate);

    // Everything is now in the transactions, so store them.
    delegate.process(transactionList);
    // Now store the id map, which uses a separate connection; store the
    // transactions first, or the foreign key constraint will fail.
    storeIds(idMap);
  }

  /**
   * Store the map of transaction ids indexed by old transaction id.
   *
   * @param ids the map of old ids to new ids
   */
  private void storeIds(Map<OldId, BigDecimal> ids) {
    Connection connection = null;
    PreparedStatement stmt = null;

    if (ids != null && ids.size() > 0) {
      try {
        connection =
          JdbcConnectionManager.getConnection(DBMS.MYSQL, "com.poesys.accounting.db.transaction");
        assert connection != null;
        for (OldId id : ids.keySet()) {
          // Verify existence of required fields.
          if (id.getEntityName() == null) {
            throw new RuntimeException("no entity name for ID map entry: " + id);
          }
          if (id.getId() == null) {
            throw new RuntimeException("no id for ID map entry: " + id);
          }
          stmt = connection.prepareStatement(ID_MAP_INSERT_SQL);
          stmt.setString(1, id.getEntityName());
          stmt.setInt(2, id.getYear());
          stmt.setBigDecimal(3, id.getId());
          stmt.setBigDecimal(4, ids.get(id));
          stmt.execute();
        }
        connection.commit();
      } catch (SQLException | IOException e) {
        try {
          assert connection != null;
          connection.rollback();
        } catch (SQLException e1) {
          // ignore
        }
        throw new RuntimeException(ID_MAP_STORE_ERROR, e);
      }
      finally {
        if (stmt != null) {
          try {
            stmt.close();
          } catch (SQLException e) {
            // ignore
          }
        }
        if (connection != null) {
          try {
            connection.close();
          } catch (SQLException e) {
            // ignore
          }
        }
      }
    }
  }

  /**
   * Link receivable items to reimbursing items based on the input transaction items. Use the shared
   * AR items map to look up the database item objects.
   *
   * @param transactions set of transactions to link
   * @param delegate     transaction delegate to use to create reimbursements
   */
  private void linkReimbursements(Set<Transaction> transactions, TransactionDelegate delegate) {
    // After processing all the transactions, take a pass through the
    // transactions again to create reimbursement links.
    for (Transaction transaction : transactions) {
      for (Item item : transaction.getItems()) {
        // Check only the receivables, not reimbursement items
        if (item.getAccount().isReceivable() && item.isDebit() &&
            item.getReimbursements().size() > 0) {
          for (Reimbursement reimbursement : item.getReimbursements()) {
            BsItem receivablesObject = arItems.get(item);
            BsItem reimbursingItemsObject = arItems.get(reimbursement.getReimbursingItem());
            if (receivablesObject != null && reimbursingItemsObject != null) {
              BsReimbursement reimbursementObject =
                delegate.createReimbursement(receivablesObject, reimbursingItemsObject,
                                             receivablesObject.getOrderNumber(),
                                             reimbursingItemsObject.getOrderNumber(),
                                             receivablesObject.getTransactionId(),
                                             reimbursingItemsObject.getTransactionId(),
                                             reimbursement.getReimbursedAmount(),
                                             reimbursement.getAllocatedAmount());
              // Link everything up in memory.
              try {
                receivablesObject.addReimbursingItemsReimbursementReimbursement(
                  reimbursementObject);
                receivablesObject.addReimbursingItemsItem(reimbursingItemsObject);
                reimbursingItemsObject.addReceivablesReimbursementReimbursement(
                  reimbursementObject);
                reimbursingItemsObject.addReceivablesItem(receivablesObject);
              } catch (SQLException e) {
                throw new DelegateException(REIMBURSEMENT_SQL_ERROR, e);
              }
            } else {
              throw new RuntimeException(NO_REIMBURSEMENTS_ERROR + item);
            }
          }
        }
      }
    }
  }

  /**
   * Create the transactions and items from the input set of transactions and put them into a list
   * of database transactions. This also fills in a map of transaction ids indexed by old
   * transaction id.
   *
   * @param transactions    the input list of newaccounting transactions
   * @param delegate        the transaction delegate to use to create objects
   * @param transactionList the output list of database transactions
   * @param idMap           the map into which to put the id mappings
   */
  private void createTransactions(Set<Transaction> transactions, TransactionDelegate delegate,
                                  List<BsTransaction> transactionList, Map<OldId, BigDecimal>
                                    idMap) {
    // Query the entity.
    EntityDelegate entityDelegate = AccountDelegateFactory.getEntityDelegate();

    BsEntity entity = entityDelegate.getObject((NaturalPrimaryKey)entityKey);
    for (Transaction transaction : transactions) {
      // Create the transaction object.
      BsTransaction transactionObject =
        delegate.createTransaction(null, transaction.getDescription(), transaction.getDate(),
                                   transaction.isChecked(), transaction.isBalance());

      addIdsToMap(idMap, entity.getEntityName(), transaction, transactionObject);

      // Create the items. Order the items as they come from the transaction.
      int orderNumber = 1;
      for (Item item : transaction.getItems()) {
        BsItem itemObject =
          delegate.createItem(transactionObject, transactionObject.getTransactionId(), orderNumber,
                              item.getAmount(), item.isDebit(), item.isChecked(),
                              item.getAccount().getName(), entity.getEntityName());
        orderNumber++;

        // Set the account from the map of stored accounts created in
        // storeEntity(), add the item to the transaction, and add the
        // item to the account.
        BsAccount storedAccount = accounts.get(item.getAccount());
        if (storedAccount == null) {
          throw new InvalidParametersException(UNKNOWN_ACCOUNT_ERROR + item);
        }
        itemObject.setAccount(storedAccount);
        transactionObject.addItemsItem(itemObject);
        storedAccount.addItemsItem(itemObject);

        // Add AR item to item map for later lookup.
        if (item.getAccount().isReceivable()) {
          arItems.put(item, itemObject);
        }

        // Add the transaction to the accumulating list of transactions.
        transactionList.add(transactionObject);
      }
    }
  }

  /**
   * Add an old-id-to-new-id mapping to a map.
   *
   * @param ids               the map to which to add the mapping
   * @param entityName        the name of the entity owning the id
   * @param transaction       the transaction with the old id
   * @param transactionObject the transaction with the new id
   */
  private void addIdsToMap(Map<OldId, BigDecimal> ids, String entityName, Transaction
    transaction, BsTransaction transactionObject) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(transaction.getDate());
    int year = cal.get(Calendar.YEAR);

    OldId oldId = new OldId(entityName, year, new BigDecimal(transaction.getId()));
    BigDecimal newId = new BigDecimal(transactionObject.getTransactionId());
    ids.put(oldId, newId);
  }
}
