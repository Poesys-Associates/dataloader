/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.connection.JdbcConnectionManager;


/**
 * CUT: AccountingDbService
 * 
 * Tests the production implementation of IDataAccessService with full
 * integration with Accounting/DB. The test constructs the full database,
 * validates creation, then deletes the database.
 * 
 * @author Robert J. Muller
 */
public class AccountingDbServiceIntegrationTest {
  /** logger for this class */
  private static final Logger logger =
    Logger.getLogger(AccountingDbServiceIntegrationTest.class);

  // Poesys/DB subsystems for direct connections
  private static final String ACCOUNT_SUBSYSTEM =
    "com.poesys.accounting.db.account";
  private static final String TRANSACTION_SUBSYSTEM =
    "com.poesys.accounting.db.transaction";

  /** test entity name */
  private static final String ENTITY_NAME = "Poesys Associates";

  // Fiscal year numbers
  private static final Integer YEAR1 = 2010;
  private static final Integer YEAR2 = 2011;
  private static final Integer YEAR3 = 2012;

  // Create standard account groups and accounts shared between all tests.
  private static final AccountGroup CASH_GROUP = new AccountGroup("Cash");
  private static final AccountGroup AR_GROUP =
    new AccountGroup("Accounts Receivable");
  private static final AccountGroup LIABILITY_GROUP =
    new AccountGroup("Credit Card");
  private static final AccountGroup EQUITY_GROUP =
    new AccountGroup("Personal Capital");
  private static final AccountGroup INCOME_GROUP = new AccountGroup("Salary");
  private static final AccountGroup EXPENSE_GROUP =
    new AccountGroup("Household");

  // Note: accounts can't be static or will get multiple items from different
  // tests
  private final Account cashAccount = new Account("Checking",
                                                  "Checking Account",
                                                  AccountType.ASSETS,
                                                  true,
                                                  false,
                                                  CASH_GROUP);
  private final Account arAccount = new Account("Receivables",
                                                "Accounts Receivable",
                                                Account.AccountType.ASSET,
                                                true,
                                                true,
                                                AR_GROUP);
  private final Account liabilityAcccount =
    new Account("Credit Card",
                "Credit Card",
                Account.AccountType.LIABILITY,
                false,
                false,
                LIABILITY_GROUP);
  private final Account equityAccount = new Account("Personal Capital",
                                                    "Personal Capital",
                                                    Account.AccountType.EQUITY,
                                                    false,
                                                    false,
                                                    EQUITY_GROUP);
  private final Account incomeAccount = new Account("Salary",
                                                    "Salary",
                                                    Account.AccountType.INCOME,
                                                    false,
                                                    false,
                                                    INCOME_GROUP);
  private final Account expenseAccount =
    new Account("Household",
                "Household Expenses",
                Account.AccountType.EXPENSE,
                true,
                false,
                EXPENSE_GROUP);

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountingDbService#storeEntity(java.lang.String, java.util.List)}
   * .
   */
  @Test
  public void testStoreEntity() {
    FiscalYear year1 = createFiscalYear(YEAR1);
    FiscalYear year2 = createFiscalYear(YEAR2);
    FiscalYear year3 = createFiscalYear(YEAR3);

    List<FiscalYear> years = new ArrayList<FiscalYear>();
    years.add(year1);
    years.add(year2);
    years.add(year3);

    try {
      IDataAccessService service = new AccountingDbService();
      service.storeEntity(ENTITY_NAME, years);
      assertTrue("account subsystem not stored correctly",
                 validateAccountSubsystem(ENTITY_NAME, years));
    } catch (java.lang.AssertionError e) {
      // passthrough to JUnit
      throw e;
    } catch (Throwable e) {
      logger.error("Exception storing entity", e);
      fail("exception storing entity");
    } finally {
      clearDatabase();
    }
  }

  /**
   * Validate the entity, accounts, account groups, and fiscal years stored by
   * the storeEntity() method against the database.
   * 
   * @param entityName the name of the entity to validate
   * @param years the list of fiscal years to validate
   * 
   * @return true if the database state is valid, false if not
   */
  private boolean validateAccountSubsystem(String entityName,
                                           List<FiscalYear> years) {
    boolean status = false;

    Connection connection = null;
    PreparedStatement stmt = null;

    try {
      connection =
        JdbcConnectionManager.getConnection(DBMS.MYSQL, ACCOUNT_SUBSYSTEM);

      // Verify entity
      stmt =
        connection.prepareStatement("SELECT 1 FROM Entity WHERE entityName = ?");
      stmt.setString(1, ENTITY_NAME);
      ResultSet rs = stmt.executeQuery();
      assertTrue("no entity", rs.next());
      stmt.close();

      // Verify fiscal years and accounts, first year only as it contains all
      // the accounts
      for (FiscalYear year : years) {
        // Verify the year by year number.
        stmt =
          connection.prepareStatement("SELECT 1 FROM FiscalYear where year = ?");
        stmt.setInt(1, year.getYear());
        rs = stmt.executeQuery();
        assertTrue("no year " + year.getYear(), rs.next());
        stmt.close();

        // Verify the account groups.
        // Verify the account group as well.
        stmt =
          connection.prepareStatement("SELECT 1 FROM AccountGroup WHERE groupName = ?");
        verifyGroup(stmt, CASH_GROUP);
        verifyGroup(stmt, AR_GROUP);
        verifyGroup(stmt, LIABILITY_GROUP);
        verifyGroup(stmt, EQUITY_GROUP);
        verifyGroup(stmt, INCOME_GROUP);
        verifyGroup(stmt, EXPENSE_GROUP);

        // For the first year only, verify the accounts.
        if (year.getYear() == YEAR1) {
          // Query the account with account name and entity name
          stmt =
            connection.prepareStatement("SELECT 1 FROM Account where accountName = ?");
          for (Account account : year.getAccounts()) {
            stmt.setString(1, account.getName());
            rs = stmt.executeQuery();
            assertTrue("no account " + account, rs.next());
            rs.close();
          }
          // Close statement in finally clause.
        }
      }

      // Set the status to true to complete valiation.
      status = true;
    } catch (SQLException | IOException e) {
      logger.error("SQL exception or IO error validating accounts", e);
      try {
        connection.rollback();
      } catch (SQLException e1) {
        // ignore exception
      }
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // ignore exception
        }
      }
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          // ignore exception
        }
      }
    }
    return status;
  }

  /**
   * @param stmt the JDBC statement with an open query with one parameter, the
   *          group name
   * @param group the account group to verify
   * 
   * @throws SQLException when there is a problem executing the query
   */
  private void verifyGroup(PreparedStatement stmt, AccountGroup group)
      throws SQLException {
    ResultSet rs;
    stmt.setString(1, group.getName());
    rs = stmt.executeQuery();
    assertTrue("no account group " + group.getName(), rs.next());
    rs.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountingDbService#storeTransactions(java.util.Set)}
   * . Depends on storeEntity(), tested separately.
   */
  @Test
  public void testStoreTransactions() {
    BigInteger nextId = BigInteger.ONE;
    FiscalYear year1 = createFiscalYearTransactions(YEAR1, nextId, true);
    nextId = getNextId(year1);
    FiscalYear year2 = createFiscalYearTransactions(YEAR2, nextId, true);
    nextId = getNextId(year2);
    FiscalYear year3 = createFiscalYearTransactions(YEAR3, nextId, true);
    Set<Transaction> transactions = new HashSet<Transaction>();

    List<FiscalYear> years = new ArrayList<FiscalYear>();
    years.add(year1);
    years.add(year2);
    years.add(year3);

    try {
      clearDatabase();
      IDataAccessService service = new AccountingDbService();
      service.storeEntity(ENTITY_NAME, years);
      // Build a complete set of transactions for all the years.
      for (FiscalYear year : years) {
        transactions.addAll(year.getTransactions());
      }
      // Store all the transactions in one committed transaction.
      service.storeTransactions(transactions);
      assertTrue("transaction subsystem not stored correctly",
                 validateTransactionSubsystem(years));
    } catch (java.lang.AssertionError e) {
      // passthrough to JUnit
      throw e;
    } catch (Throwable e) {
      logger.error("exception storing entity and transactions", e);
      fail("exception storing entity and transactions");
    } finally {
      clearDatabase();
    }
  }

  /**
   * Validate the transactions, items, and reimbursements stored by the
   * storeEntity() method against the database. Use the IdMap table to look up
   * the new transaction id based on the old one. This validates the IdMap table
   * as well.
   * 
   * @param years the list of fiscal years to validate
   * 
   * @return true if the database state is valid, false if not
   */
  private boolean validateTransactionSubsystem(List<FiscalYear> years) {
    boolean status = false;

    Connection connection = null;
    PreparedStatement stmt = null;

    try {
      connection =
        JdbcConnectionManager.getConnection(DBMS.MYSQL, TRANSACTION_SUBSYSTEM);

      // Verify entity
      stmt =
        connection.prepareStatement("SELECT 1 FROM Entity WHERE entityName = ?");
      stmt.setString(1, ENTITY_NAME);
      ResultSet rs = stmt.executeQuery();
      assertTrue("no entity", rs.next());
      stmt.close();

      // Verify transactions, items, and reimbursements
      for (FiscalYear year : years) {
        for (Transaction transaction : year.getTransactions()) {
          // Verify transaction and id map by id retrieved from IdMap table.
          stmt =
            connection.prepareStatement("SELECT count(*) FROM Transaction t JOIN IdMap i ON t.transactionId = i.newId JOIN Item it ON t.transactionId = it.transactionId WHERE oldId = ?");
          stmt.setBigDecimal(1, new BigDecimal(transaction.getId()));
          rs = stmt.executeQuery();
          if (rs.next()) {
            int count = rs.getInt(1);
            // Verify items by count.
            assertTrue("Wrong number of items: " + count,
                       count == transaction.getItems().size());
          }
          stmt.close();

          // For receivable transaction, verify Reimbursement
          for (Item item : transaction.getItems()) {
            if (item.getAccount().isReceivable() && item.isDebit()
                && !transaction.isBalance()) {
              // receivable item in non-balance transaction, check for
              // reimbursement based on old id
              stmt =
                connection.prepareStatement("SELECT count(*) FROM Reimbursement r JOIN IdMap i ON r.receivablesTransactionId = i.newId JOIN Transaction t ON r.receivablesTransactionId = t.transactionId JOIN FiscalYear y ON t.transactionDate BETWEEN y.startDate AND y.endDate WHERE oldId = ? AND y.year = ?");
              stmt.setBigDecimal(1,
                                 new BigDecimal(item.getTransaction().getId()));
              stmt.setInt(2, year.getYear());
              rs = stmt.executeQuery();
              if (rs.next()) {
                int count = rs.getInt(1);
                assertTrue("no receivable reimbursement found", count == 1);
              }
              stmt.close();
            } else if (item.getAccount().isReceivable() && !item.isDebit()
                       && !transaction.isBalance()) {
              // reimbursing item in non-balance transaction, check for
              // reimbursement based on old id
              stmt =
                connection.prepareStatement("SELECT count(*) FROM Reimbursement r JOIN IdMap i ON r.reimbursingItemsTransactionId = i.newId JOIN Transaction t ON r.reimbursingItemsTransactionId = t.transactionId JOIN FiscalYear y ON t.transactionDate BETWEEN y.startDate AND y.endDate WHERE oldId = ? AND y.year = ?");
              stmt.setBigDecimal(1,
                                 new BigDecimal(item.getTransaction().getId()));
              stmt.setInt(2, year.getYear());
              rs = stmt.executeQuery();
              if (rs.next()) {
                int count = rs.getInt(1);
                assertTrue("no reimbursement found for reimbursing item",
                           count == 1);
              }
              stmt.close();
            }
          }
        }
      }

      // Set the status to true to complete validation.
      status = true;
    } catch (SQLException | IOException e) {
      logger.error("SQL exception or IO error validating transactions", e);
      try {
        connection.rollback();
      } catch (SQLException e1) {
        // ignore exception
      }
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // ignore exception
        }
      }
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          // ignore exception
        }
      }
    }
    return status;
  }

  /**
   * Remove any data currently in the database and set sequences to 1.
   */
  private void clearDatabase() {
    // Clear subsystems in dependency order: transaction then account
    clearTransactionSubsystem();
    clearAccountSubsystem();
  }

  /**
   * Clear the tables in the Account subsystem (Entity, FiscalYear,
   * AccountGroup). This method also clears the tables Account and
   * FiscalYearAccount through cascaded deletes. Call the
   * clearTransactionSubsystem() method before calling this one to remove
   * dependencies from Item to Account.
   */
  private void clearAccountSubsystem() {
    Connection connection = null;
    java.sql.Statement stmt = null;

    try {
      connection =
        JdbcConnectionManager.getConnection(DBMS.MYSQL, ACCOUNT_SUBSYSTEM);
      stmt = connection.createStatement();
      stmt.execute("DELETE FROM Entity");
      stmt.execute("DELETE FROM FiscalYear");
      stmt.execute("DELETE FROM AccountGroup");
      connection.commit();
    } catch (SQLException | IOException e) {
      logger.error("exception clearing database", e);
      if (connection != null) {
        try {
          connection.rollback();
        } catch (SQLException e1) {
          // ignore exception
        }
      }
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // ignore exception
        }
      }
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          // ignore exception
        }
      }
    }
  }

  /**
   * Clear the tables in the Transaction subsystem (Transaction). Call this
   * before calling clearAccountSubsystem(). Also deletes Item, IdMap, and
   * Reimbursement tables through cascaded deletes.
   */
  private void clearTransactionSubsystem() {
    Connection connection = null;
    java.sql.Statement stmt = null;

    try {
      connection =
        JdbcConnectionManager.getConnection(DBMS.MYSQL, TRANSACTION_SUBSYSTEM);
      stmt = connection.createStatement();
      stmt.execute("DELETE FROM Transaction");
      connection.commit();
    } catch (SQLException | IOException e) {
      logger.error("exception clearing database", e);
      if (connection != null) {
        try {
          connection.rollback();
        } catch (SQLException e1) {
          // ignore exception
        }
      }
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // ignore exception
        }
      }
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          // ignore exception
        }
      }
    }
  }

  /**
   * Create a fiscal year with a set of standard accounts and account groups.
   * This method creates the fiscal year without any transactions.
   * 
   * @param year the integer year
   * @return the fiscal year with nested accounts and groups
   */
  private FiscalYear createFiscalYear(Integer year) {
    FiscalYear fiscalYear = new FiscalYear(year);
    assertTrue("could not create fiscal year " + year, fiscalYear != null);
    assertTrue("wrong year created: " + fiscalYear.getYear(),
               fiscalYear.getYear() == year);
    // Add the accounts to the fiscal year.
    fiscalYear.addAccount(cashAccount);
    fiscalYear.addAccount(arAccount);
    fiscalYear.addAccount(liabilityAcccount);
    fiscalYear.addAccount(equityAccount);
    fiscalYear.addAccount(incomeAccount);
    fiscalYear.addAccount(expenseAccount);

    return fiscalYear;
  }

  /**
   * Create a fiscal year with a set of standard accounts and standard
   * transactions against the various kinds of accounts.
   * 
   * @param year the year number
   * @param id the first integer id for transactions
   * @param valid whether the year has valid balances
   * @return the fiscal year with nested accounts and transaction
   */
  private FiscalYear createFiscalYearTransactions(Integer year, BigInteger id,
                                                  boolean valid) {
    FiscalYear fiscalYear = createFiscalYear(year);

    // Create a set of transactions that looks like a complete year's accounts.

    // Balance transactions for the asset, liability, and equity accounts.
    // These accounts sum to zero (debits negative, credits positive).

    // Cash balance
    Transaction transaction =
      new Transaction(id, "Cash Balance", fiscalYear.getStart(), true, true);
    transaction.addItem(100.00D, cashAccount, true, false);
    fiscalYear.addTransaction(transaction);

    // Accounts Receivable balance
    BigInteger nextId = id.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId, "AR Balance", fiscalYear.getStart(), true, true);
    transaction.addItem(0.00D, arAccount, true, false);
    assertTrue("transaction invalid: " + transaction, transaction.isValid());
    fiscalYear.addTransaction(transaction);

    // Liability balance
    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId,
                      "Liability Balance",
                      fiscalYear.getStart(),
                      false,
                      true);
    transaction.addItem(50.00D, liabilityAcccount, false, false);
    assertTrue("transaction invalid: " + transaction, transaction.isValid());
    fiscalYear.addTransaction(transaction);

    // Equity balance
    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId,
                      "Equity Balance",
                      fiscalYear.getStart(),
                      false,
                      true);
    // Set the equity balance to a valid value or invalid value depending on the
    // value of the valid flag.
    Double value = valid ? 50.00D : 60.00D;
    transaction.addItem(value, equityAccount, false, false);
    assertTrue("transaction invalid: " + transaction, transaction.isValid());
    fiscalYear.addTransaction(transaction);

    // Transactions for the year

    // Income-Cash
    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId,
                      "Income-Cash Transaction",
                      fiscalYear.getStart(),
                      false,
                      false);
    transaction.addItem(50.00D, incomeAccount, false, false);
    transaction.addItem(50.00D, cashAccount, true, false);
    assertTrue("transaction invalid: " + transaction, transaction.isValid());
    fiscalYear.addTransaction(transaction);

    // Income-AR (Receivable)
    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId,
                      "Income-Receivable Transaction",
                      fiscalYear.getStart(),
                      false,
                      false);
    transaction.addItem(50.00D, incomeAccount, false, false);
    transaction.addItem(50.00D, arAccount, true, false);
    assertTrue("transaction invalid: " + transaction, transaction.isValid());
    fiscalYear.addTransaction(transaction);

    // Save the receivable item for later reimbursement.
    Item receivableItem = null;
    for (Item item : transaction.getItems()) {
      if (item.getAccount().isReceivable()) {
        receivableItem = item;
        break;
      }
    }
    assertTrue("no receivable item for receivable transaction",
               receivableItem != null);

    // AR-Cash (Reimbursement of Receivable)
    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId,
                      "Reimbursement Transaction",
                      fiscalYear.getStart(),
                      false,
                      false);
    transaction.addItem(50.00D, cashAccount, true, false);
    transaction.addItem(50.00D, arAccount, false, false);

    // Save the reimbursing item for later reimbursement.
    Item reimbursingItem = null;
    for (Item item : transaction.getItems()) {
      if (item.getAccount().isReceivable()) {
        reimbursingItem = item;
        break;
      }
    }

    assertTrue("no reimbursing item for reimbursement transaction",
               reimbursingItem != null);

    // Reimburse the receivable item.
    receivableItem.reimburse(reimbursingItem, 50.00D, 0.00D);

    assertTrue("transaction invalid: " + transaction, transaction.isValid());
    fiscalYear.addTransaction(transaction);

    // Cash-Expense
    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId,
                      "Cash-Expense Transaction",
                      fiscalYear.getStart(),
                      false,
                      false);
    transaction.addItem(25.00D, cashAccount, false, false);
    transaction.addItem(25.00D, expenseAccount, true, false);
    assertTrue("transaction invalid: " + transaction, transaction.isValid());
    fiscalYear.addTransaction(transaction);

    // Credit-Expense
    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId,
                      "Credit-Expense Transaction",
                      fiscalYear.getStart(),
                      false,
                      false);
    transaction.addItem(30.00D, liabilityAcccount, false, false);
    transaction.addItem(30.00D, expenseAccount, true, false);
    assertTrue("transaction invalid: " + transaction, transaction.isValid());
    fiscalYear.addTransaction(transaction);

    // Capital-Cash
    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId,
                      "Capital-Cash Transaction",
                      fiscalYear.getStart(),
                      false,
                      false);
    transaction.addItem(100.00D, cashAccount, true, false);
    transaction.addItem(100.00D, equityAccount, false, false);
    assertTrue("transaction invalid: " + transaction, transaction.isValid());
    fiscalYear.addTransaction(transaction);

    return fiscalYear;
  }

  /**
   * Get the next transaction id to use given the transactions in a fiscal year.
   * 
   * @param year the fiscal year to analyze
   * @return the next id
   */
  private BigInteger getNextId(FiscalYear year) {
    BigInteger id = BigInteger.ONE;
    if (year != null) {
      for (Transaction transaction : year.getTransactions()) {
        BigInteger newId = transaction.getId();
        // exchange ids when new id is greater than id
        if (newId != null && newId.compareTo(id) > 0) {
          id = newId;
        }
      }
    }
    id = id.add(BigInteger.ONE);
    return id;
  }
}
