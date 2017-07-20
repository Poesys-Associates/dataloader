/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import static org.junit.Assert.fail;

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
import com.poesys.accounting.bs.account.AccountGroupDelegate;
import com.poesys.accounting.bs.account.BsAccount;
import com.poesys.accounting.bs.account.BsAccountGroup;
import com.poesys.accounting.bs.account.BsEntity;
import com.poesys.accounting.bs.account.BsFiscalYear;
import com.poesys.accounting.bs.account.BsFiscalYearAccount;
import com.poesys.accounting.bs.account.EntityDelegate;
import com.poesys.accounting.bs.account.FiscalYearDelegate;
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

  /** a map of item objects indexed by item, ar objects only for reimbursments */
  private final Map<Item, BsItem> arItems = new HashMap<Item, BsItem>();

  /**
   * Helper class for index to id map
   */
  private class OldId {
    public int year;
    public BigDecimal id;

    /**
     * Create an OldId indexing object with a fiscal year and transaction id.
     * 
     * @param year the fiscal year number
     * @param id the old transaction id number
     */
    public OldId(int year, BigDecimal id) {
      this.year = year;
      this.id = id;
    }

    @Override
    public String toString() {
      return "OldId [year=" + year + ", id=" + id.toBigInteger() + "]";
    }
  }

  @Override
  public void storeEntity(String entityName, List<FiscalYear> years) {
    FiscalYearDelegate yearDelegate =
      AccountDelegateFactory.getFiscalYearDelegate();
    EntityDelegate entityDelegate = AccountDelegateFactory.getEntityDelegate();
    AccountGroupDelegate accountGroupDelegate =
      AccountDelegateFactory.getAccountGroupDelegate();

    // Intermediate storage and lookup structures
    List<BsFiscalYear> yearList = new ArrayList<BsFiscalYear>();
    Map<AccountGroup, BsAccountGroup> groups =
      new HashMap<AccountGroup, BsAccountGroup>();

    entity = entityDelegate.createEntity(entityName);

    // Track order number of year starting at 1.
    int orderNumber = 1;
    for (FiscalYear year : years) {
      // Create the fiscal year and add to the entity.
      BsFiscalYear fiscalYear =
        yearDelegate.createFiscalYear(year.getYear(),
                                      year.getStart(),
                                      year.getEnd());
      fiscalYears.put(year.getYear(), fiscalYear);
      yearList.add(fiscalYear);

      // Create account groups and accounts.
      for (Account account : year.getAccounts()) {
        // Extract group and create if needed.
        BsAccountGroup storedGroup = groups.get(account.getGroup());
        if (storedGroup == null) {
          storedGroup =
            accountGroupDelegate.createAccountGroup(account.getGroup().getName());
          if (storedGroup == null) {
            throw new RuntimeException("Could not create stored group from group in account "
                                       + account);
          } else {
            groups.put(account.getGroup(), storedGroup);
          }
        }
        // Create account and add to fiscal year.
        BsAccount storedAccount =
          entityDelegate.createAccount(entity,
                                       entity.getEntityName(),
                                       account.getName(),
                                       account.getDescription(),
                                       account.isDebitDefault(),
                                       account.getAccountType().toString(),
                                       account.isReceivable(),
                                       true,
                                       storedGroup.getGroupName());
        try {
          entity.addAccountsAccount(storedAccount);
          storedAccount.setGroup(storedGroup);
          storedAccount.addYearsFiscalYear(fiscalYear);
          fiscalYear.addAccountsAccount(storedAccount);
          createFiscalYearAccountLinks(yearDelegate,
                                       storedAccount,
                                       orderNumber,
                                       fiscalYear);
          // Next year, increment order number
          orderNumber++;
        } catch (SQLException e) {
          throw new DelegateException("SQL exception storing account", e);
        }
        storedAccount.setGroup(storedGroup);

        // Add the account to the map for later lookups.
        accounts.put(account, storedAccount);
      }
    }

    // Store everything.
    yearDelegate.process(yearList);
    entityDelegate.process(entity);
  }

  /**
   * Create a set of links from an account to a set of fiscal years. The method
   * sets the link arrays to associate the links with the fiscal year and
   * account objects. It does not add the account to the year or the year to the
   * account; this results in an infinite loop.
   * 
   * @param delegate the fiscal year delegate that creates the links
   * @param account the linking account
   * @param orderNumber the order number of the account in the list of accounts
   *          associated with the fiscal year (the same for all years)
   * @param year the linked year
   */
  private void createFiscalYearAccountLinks(FiscalYearDelegate delegate,
                                            BsAccount account,
                                            Integer orderNumber,
                                            BsFiscalYear year) {
    BsFiscalYearAccount link =
      delegate.createFiscalYearAccount(account,
                                       year,
                                       account.getAccountName(),
                                       account.getEntityName(),
                                       year.getYear(),
                                       orderNumber);

    // Set the link variables.
    try {
      year.addFiscalYearAccountFiscalYearAccount(link);
      account.addFiscalYearAccountFiscalYearAccount(link);
    } catch (SQLException e) {
      String message = "SQL exception creating year-account links";
      logger.error(message, e);
      fail(message + ": " + e.getMessage());
    }
  }

  @Override
  public void storeTransactions(Set<Transaction> transactions) {
    /** a map of database transaction ids indexed by old transaction id */
    Map<OldId, BigDecimal> idMap = new HashMap<OldId, BigDecimal>();

    if (entity == null) {
      throw new RuntimeException("Entity is null, run storeEntity() first");
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
          stmt =
            connection.prepareStatement("INSERT INTO IdMap(year, oldId, newId) VALUES (?, ?, ?)");
          stmt.setInt(1, id.year);
          stmt.setBigDecimal(2, id.id);
          stmt.setBigDecimal(3, ids.get(id));
          stmt.execute();
        }
        connection.commit();
      } catch (SQLException | IOException e) {
        try {
          connection.rollback();
        } catch (SQLException e1) {
          // ignore
        }
        throw new RuntimeException("exception storing id map", e);
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
                receivablesObject.addReimbursement(reimbursementObject);
                receivablesObject.addReimbursingItem(reimbursingItemsObject);
                reimbursingItemsObject.addReimbursement(reimbursementObject);
                reimbursingItemsObject.addReceivableItem(receivablesObject);
              } catch (SQLException e) {
                throw new DelegateException("SQL exception creating reimbursement",
                                            e);
              }

            } else {
              throw new RuntimeException("couldn't resolve reimbursment objects for receivable "
                                         + item);
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
          throw new InvalidParametersException("unknown account in item: "
                                               + item);
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

    OldId oldId = new OldId(year, new BigDecimal(transaction.getId()));
    BigDecimal newId = new BigDecimal(transactionObject.getTransactionId());
    ids.put(oldId, newId);
  }
}
