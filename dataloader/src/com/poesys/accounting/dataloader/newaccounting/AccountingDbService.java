/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.poesys.accounting.bs.account.AccountDelegateFactory;
import com.poesys.accounting.bs.account.AccountTypeDelegate;
import com.poesys.accounting.bs.account.BsAccount;
import com.poesys.accounting.bs.account.BsAccountGroup;
import com.poesys.accounting.bs.account.BsAccountType;
import com.poesys.accounting.bs.account.BsCapitalAccount;
import com.poesys.accounting.bs.account.BsDistributionAccount;
import com.poesys.accounting.bs.account.BsEntity;
import com.poesys.accounting.bs.account.BsFiscalYear;
import com.poesys.accounting.bs.account.BsFiscalYearAccount;
import com.poesys.accounting.bs.account.BsSimpleAccount;
import com.poesys.accounting.bs.account.CapitalAccountDelegate;
import com.poesys.accounting.bs.account.DistributionAccountDelegate;
import com.poesys.accounting.bs.account.EntityDelegate;
import com.poesys.accounting.bs.account.FiscalYearDelegate;
import com.poesys.accounting.bs.account.SimpleAccountDelegate;
import com.poesys.accounting.bs.transaction.BsItem;
import com.poesys.accounting.bs.transaction.BsReimbursement;
import com.poesys.accounting.bs.transaction.BsTransaction;
import com.poesys.accounting.bs.transaction.TransactionDelegate;
import com.poesys.accounting.bs.transaction.TransactionDelegateFactory;
import com.poesys.accounting.dataloader.newaccounting.Item.Reimbursement;
import com.poesys.bs.delegate.DelegateException;
import com.poesys.db.InvalidParametersException;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.connection.JdbcConnectionManager;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;


/**
 * The production services for storing Accounting/DB data to the database given
 * a set of "newaccounting" objects
 * 
 * @author Robert J. Muller
 */
public class AccountingDbService implements IDataAccessService {
  /** logger for this class */
  private static final Logger logger =
    Logger.getLogger(AccountingDbService.class);

  /** SQL for ID map insert */
  private static final String ID_MAP_INSERT_SQL =
    "INSERT INTO IdMap(entityName, year, oldId, newId) VALUES (?, ?, ?, ?)";

  // messages
  private static final String NO_LINKS_IN_GROUP_ERROR =
    "no fiscal-year-account links in ";
  private static final String PERSISTED_GROUP_ERROR =
    "Could not create persisted group ";
  private static final String LINKING_SQL_ERROR =
    "SQL exception linking account to year and group: ";
  private static final String REIMBURSEMENT_SQL_ERROR =
    "SQL exception creating reimbursement";
  private static final String NULL_ENTITY_ERROR =
    "Entity is null, run storeEntity() first";
  private static final String ID_MAP_STORE_ERROR = "exception storing id map";
  private static final String NO_REIMBURSEMENTS_ERROR =
    "couldn't find reimbursment objects for receivable ";
  private static final String UNKNOWN_ACCOUNT_ERROR =
    "unknown account in item: ";

  /** the stored, existing entity created by storeEntity() */
  private BsEntity entity = null;
  /**
   * a map of database accounts created by storeEntity() indexed on
   * newaccounting account for lookup by storeTransctions
   */
  private final Map<Account, BsAccount> accounts =
    new HashMap<Account, BsAccount>();

  /**
   * a map of stored fiscal years created by storeEntity() indexed on the
   * integer year
   */
  private final Map<Integer, BsFiscalYear> fiscalYears =
    new HashMap<Integer, BsFiscalYear>();

  /** a map of item objects indexed by item, AR objects only for reimbursments */
  private final Map<Item, BsItem> arItems = new HashMap<Item, BsItem>();

  /** a map of account types indexed by type for lookup of database type object */
  private final Map<AccountType, BsAccountType> types =
    new HashMap<AccountType, BsAccountType>();

  /**
   * Create an AccountingDbService object.
   */
  public AccountingDbService() {
    refreshAccountTypeCache();
  }

  /**
   * Query the account types from the database to refresh the local cache of
   * account types keyed on new-accounting account type (an enum).
   */
  public void refreshAccountTypeCache() {
    AccountTypeDelegate typeDelegate =
      AccountDelegateFactory.getAccountTypeDelegate();
    // Clear the cache.
    types.clear();
    // Query the business objects from the database.
    List<BsAccountType> typeList = typeDelegate.getAllObjects(5);
    // Get the enum value for the type and map the type.
    for (BsAccountType type : typeList) {
      AccountType localType =
        AccountType.databaseValueOf(type.getAccountType());
      types.put(localType, type);
    }
  }

  /**
   *
   */
  @Override
  public void storeEntity(String entityName, List<FiscalYear> years) {
    FiscalYearDelegate yearDelegate =
      AccountDelegateFactory.getFiscalYearDelegate();
    EntityDelegate entityDelegate = AccountDelegateFactory.getEntityDelegate();

    // Intermediate storage and lookup structures
    List<BsFiscalYear> yearList = new ArrayList<BsFiscalYear>();

    // Refresh the account type object cache. This ensures that each access
    // type in the map contains the latest group information from the database.
    refreshAccountTypeCache();

    entity = entityDelegate.createEntity(entityName);

    // list of accumulated links to insert later
    List<BsFiscalYearAccount> links = new ArrayList<BsFiscalYearAccount>();

    for (FiscalYear year : years) {
      // Create the fiscal year and add to the entity.
      BsFiscalYear fiscalYear =
        yearDelegate.createFiscalYear(year.getYear(),
                                      year.getStart(),
                                      year.getEnd());
      fiscalYears.put(year.getYear(), fiscalYear);
      yearList.add(fiscalYear);

      // Create accounts.
      for (FiscalYearAccount link : year.getAccounts()) {
        // Extract group and create if needed.
        BsAccountGroup group = getPersistedGroup(link.getGroup());

        BsAccount account = createAccount(link.getAccount());

        // Create the fiscal-year-account link but don't add it to the linked
        // objects for persisting yet. This works around a bug with
        // nested-object processing, you need to store the objects before the
        // link. Instead, accumulate the links for later insert.
        links.add(yearDelegate.createFiscalYearAccount(account,
                                                       group,
                                                       fiscalYear,
                                                       account.getAccountName(),
                                                       account.getEntityName(),
                                                       fiscalYear.getYear(),
                                                       link.getAccountOrderNumber(),
                                                       link.getGroupOrderNumber(),
                                                       group.getAccountType(),
                                                       group.getGroupName(),
                                                       group.toDto()));

        // Add the account to the map for later lookups.
        accounts.put(link.getAccount(), account);
      }
    }

    // Store everything.
    yearDelegate.process(yearList);
    entityDelegate.process(entity);

    // Link accounts to fiscal years, associate groups.
    for (BsFiscalYearAccount link : links) {
      try {
        BsAccount account = link.getAccount();
        BsAccountGroup group = link.getGroup();
        BsFiscalYear year = link.getFiscalYear();

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
   * Get a persisted group object based on the new-accounting group object; this
   * looks up the group in the local account-type cache using the account type
   * associated with the specified group. The group must have at least one
   * fiscal year account link. If there is no persisted group with the same name
   * as the specified group, the method creates a new group and returns that;
   * note that the group is not yet persisted. The method adds the group to the
   * type in the type map cache.
   * 
   * @param group the group to look up
   * @return the persisted group corresponding to the specified group, or null
   *         if there is no persisted group
   */
  private BsAccountGroup getPersistedGroup(AccountGroup group) {
    AccountTypeDelegate typeDelegate =
      AccountDelegateFactory.getAccountTypeDelegate();
    BsAccountGroup returnGroup = null;

    // Make sure there is at least one fiscal-year-account link.
    if (group.getFiscalYearAccounts() == null
        || group.getFiscalYearAccounts().size() == 0) {
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
      returnGroup =
        typeDelegate.createAccountGroup(persistedType,
                                        persistedType.getAccountType(),
                                        group.getName());
      if (returnGroup == null) {
        throw new RuntimeException(PERSISTED_GROUP_ERROR + group);
      } else {
        // Add the new group to the persisted type to keep the cache consistent.
        persistedType.addGroupsAccountGroup(returnGroup);
      }
    }
    return returnGroup;
  }

  /**
   * Based on the presence of the capital entity in an input account, create the
   * persistent account business object of the appropriate subclass and return
   * it as a generic persistent account object. This is a Factory Method
   * pattern.
   * 
   * @param account the new-accounting-system account
   * @return the generic, persistent account object
   */
  private BsAccount createAccount(Account account) {
    BsAccount genericAccount = null;
    String entityName = entity.getEntityName();
    CapitalEntity capEntity = account.getCapitalEntity();
    if (capEntity != null
        && capEntity.getCapitalAccount().getName().equals(account.getName())) {
      // Capital entity associated and the account name is the entity capital
      // account name, so create a capital account.
      CapitalAccountDelegate accountDelegate =
        AccountDelegateFactory.getCapitalAccountDelegate();

      BsCapitalAccount capitalAccount =
        accountDelegate.createCapitalAccount(account.getName(),
                                             entityName,
                                             account.getDescription(),
                                             account.isDebitDefault(),
                                             true,
                                             capEntity.getOwnership().doubleValue(),
                                             capEntity.getName());
      genericAccount = new BsAccount(capitalAccount.toDto());
    } else if (capEntity != null
               && capEntity.getDistributionAccount().getName().equals(account.getName())) {
      // Capital entity associated and the account name is the entity
      // distribution account name, so create a distribution account.
      DistributionAccountDelegate accountDelegate =
        AccountDelegateFactory.getDistributionAccountDelegate();

      BsDistributionAccount capitalAccount =
        accountDelegate.createDistributionAccount(account.getName(),
                                                  entityName,
                                                  account.getDescription(),
                                                  account.isDebitDefault(),
                                                  true,
                                                  capEntity.getName());
      genericAccount = new BsAccount(capitalAccount.toDto());
    } else if (capEntity == null) {
      // Capital entity not associated, create a simple account.
      SimpleAccountDelegate accountDelegate =
        AccountDelegateFactory.getSimpleAccountDelegate();

      // Create simple account.
      BsSimpleAccount simpleAccount =
        accountDelegate.createSimpleAccount(account.getName(),
                                            entityName,
                                            account.getDescription(),
                                            account.isDebitDefault(),
                                            true,
                                            account.isReceivable());
      genericAccount = new BsAccount(simpleAccount.toDto());
    }

    // Add the generic account to the entity.
    entity.addAccountsAccount(genericAccount);
    return genericAccount;
  }

  @Override
  public void storeTransactions(Set<Transaction> transactions) {
    /** a map of database transaction ids indexed by old transaction id */
    Map<OldId, BigDecimal> idMap = new HashMap<OldId, BigDecimal>();

    if (entity == null) {
      throw new RuntimeException(NULL_ENTITY_ERROR);
    }

    logger.debug("Storing transactions");

    TransactionDelegate delegate =
      TransactionDelegateFactory.getTransactionDelegate();

    List<BsTransaction> transactionList =
      new ArrayList<BsTransaction>(transactions.size());

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
          JdbcConnectionManager.getConnection(DBMS.MYSQL,
                                              "com.poesys.accounting.db.transaction");
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
          connection.rollback();
        } catch (SQLException e1) {
          // ignore
        }
        throw new RuntimeException(ID_MAP_STORE_ERROR, e);
      } finally {
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
   * Link receivable items to reimbursing items based on the input transaction
   * items. Use the shared AR items map to look up the database item objects.
   * 
   * @param transactions set of transactions to link
   * @param delegate transaction delegate to use to create reimbursements
   */
  private void linkReimbursements(Set<Transaction> transactions,
                                  TransactionDelegate delegate) {
    // After processing all the transactions, take a pass through the
    // transactions again to create reimbursement links.
    for (Transaction transaction : transactions) {
      for (Item item : transaction.getItems()) {
        // Check only the receivables, not reimbursement items
        if (item.getAccount().isReceivable() && item.isDebit()
            && item.getReimbursements().size() > 0) {
          for (Reimbursement reimbursement : item.getReimbursements()) {
            BsItem receivablesObject = arItems.get(item);
            BsItem reimbursingItemsObject =
              arItems.get(reimbursement.getReimbursingItem());
            if (receivablesObject != null && reimbursingItemsObject != null) {
              BsReimbursement reimbursementObject =
                delegate.createReimbursement(receivablesObject,
                                             reimbursingItemsObject,
                                             receivablesObject.getOrderNumber(),
                                             reimbursingItemsObject.getOrderNumber(),
                                             receivablesObject.getTransactionId(),
                                             reimbursingItemsObject.getTransactionId(),
                                             reimbursement.getReimbursedAmount(),
                                             reimbursement.getAllocatedAmount());
              // Link everything up in memory.
              try {
                receivablesObject.addReimbursingItemsReimbursementReimbursement(reimbursementObject);
                receivablesObject.addReimbursingItemsItem(reimbursingItemsObject);
                reimbursingItemsObject.addReceivablesReimbursementReimbursement(reimbursementObject);
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
   * Create the transactions and items from the input set of transactions and
   * put them into a list of database transactions. This also fills in a map of
   * transaction ids indexed by old transaction id.
   * 
   * @param transactions the input list of newaccounting transactions
   * @param delegate the transaction delegate to use to create objects
   * @param transactionList the output list of database transactions
   * @param idMap the map into which to put the id mappings
   */
  private void createTransactions(Set<Transaction> transactions,
                                  TransactionDelegate delegate,
                                  List<BsTransaction> transactionList,
                                  Map<OldId, BigDecimal> idMap) {
    for (Transaction transaction : transactions) {
      // Create the transaction object.
      BsTransaction transactionObject =
        delegate.createTransaction(null,
                                   transaction.getDescription(),
                                   transaction.getDate(),
                                   transaction.isChecked(),
                                   transaction.isBalance());

      addIdsToMap(idMap, transaction, transactionObject);

      // Create the items. Order the items as they come from the transaction.
      int orderNumber = 1;
      for (Item item : transaction.getItems()) {
        BsItem itemObject =
          delegate.createItem(transactionObject,
                              transactionObject.getTransactionId(),
                              orderNumber,
                              item.getAmount(),
                              item.isDebit(),
                              item.isChecked(),
                              item.getAccount().getName(),
                              entity.getEntityName());
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
   * @param ids the map to which to add the mapping
   * @param transaction the transaction with the old id
   * @param transactionObject the transaction with the new id
   */
  private void addIdsToMap(Map<OldId, BigDecimal> ids, Transaction transaction,
                           BsTransaction transactionObject) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(transaction.getDate());
    int year = cal.get(Calendar.YEAR);

    OldId oldId =
      new OldId(entity.getEntityName(),
                year,
                new BigDecimal(transaction.getId()));
    BigDecimal newId = new BigDecimal(transactionObject.getTransactionId());
    ids.put(oldId, newId);
  }
}
