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
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;

/**
 * CUT: AccountingDbService
 *
 * Tests the production implementation of IDataAccessService with full integration with
 * Accounting/DB. The test constructs the full database, validates creation, then deletes the
 * database.
 *
 * @author Robert J. Muller
 */
public class AccountingDbServiceIntegrationTest {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(AccountingDbServiceIntegrationTest.class);

  // Poesys/DB subsystems for direct connections
  private static final String ACCOUNT_SUBSYSTEM = "com.poesys.accounting.db.account";
  private static final String TRANSACTION_SUBSYSTEM = "com.poesys.accounting.db.transaction";

  /** test entity name */
  private static final String ENTITY_NAME = "Poesys Associates";

  // Fiscal year numbers
  private static final Integer YEAR1 = 2010;
  private static final Integer YEAR2 = 2011;
  private static final Integer YEAR3 = 2012;

  // Create standard account groups and accounts shared between all tests.
  private static final AccountGroup CASH_GROUP = new AccountGroup("Cash");
  private static final AccountGroup AR_GROUP = new AccountGroup("Accounts Receivable");
  private static final AccountGroup LIABILITY_GROUP = new AccountGroup("Credit Card");
  private static final AccountGroup EQUITY_GROUP = new AccountGroup("Personal Capital");
  private static final AccountGroup INCOME_GROUP = new AccountGroup("Salary");
  private static final AccountGroup EXPENSE_GROUP = new AccountGroup("Household");

  // Create test accounts for non-equity types.
  // Note: accounts can't be static or will get multiple items from different tests
  private final Account cashAccount =
    new Account("Checking", "Checking Account", AccountType.ASSETS, true, false);
  private final Account arAccount =
    new Account("Receivables", "Accounts Receivable", AccountType.ASSETS, true, true);
  private final Account liabilityAccount =
    new Account("Credit Card", "Credit Card", AccountType.LIABILITIES, false, false);
  private final Account incomeAccount =
    new Account("Salary", "Salary", AccountType.INCOME, false, false);
  private final Account expenseAccount =
    new Account("Household", "Household Expenses", AccountType.EXPENSES, true, false);

  // Account names
  private static final String CAP_ACCOUNT_1_NAME = "Partner 1 Capital";
  private static final String CAP_ACCOUNT_2_NAME = "Partner 2 Capital";
  private static final String DIST_ACCOUNT_1_NAME = "Partner 1 Distributions";
  private static final String DIST_ACCOUNT_2_NAME = "Partner 2 Distributions";

  // Create two capital entities (partners) with 50% ownership interests.
  private static final CapitalEntity partner1 =
    new CapitalEntity("Partner 1", CAP_ACCOUNT_1_NAME, DIST_ACCOUNT_1_NAME, new BigDecimal("0.50"));
  private static final CapitalEntity partner2 =
    new CapitalEntity("Partner 2", CAP_ACCOUNT_2_NAME, DIST_ACCOUNT_2_NAME, new BigDecimal("0.50"));
  private static final String incomeSummaryName = "Income Summary";

  // Create equity accounts.
  private final Account partner1CapitalAccount =
    new Account(CAP_ACCOUNT_1_NAME, "Capital account for Partner 1", AccountType.EQUITY, false,
                false);
  private final Account partner2CapitalAccount =
    new Account(CAP_ACCOUNT_2_NAME, "Capital account for Partner 2", AccountType.EQUITY, false,
                false);
  private final Account partner1DistributionAccount =
    new Account(DIST_ACCOUNT_1_NAME, "Distribution account for Partner 1", AccountType.EQUITY,
                false, false);
  private final Account partner2DistributionAccount =
    new Account(DIST_ACCOUNT_2_NAME, "Distribution account for Partner 2", AccountType.EQUITY,
                false, false);

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .AccountingDbService#storeEntity(java.lang.String, * java.util.List)} .
   */
  @Test
  public void testStoreCapitalStructure() {
    CapitalStructure capitalStructure = createCapitalStructure();

    try {
      clearDatabase();
      logger.info("Storing account subsystem: capital structure");
      IDataAccessService service = new AccountingDbService();
      service.storeCapitalStructure(capitalStructure);
      assertTrue("capital structure not stored correctly",
                 validateCapitalStructure(capitalStructure));
    } catch (java.lang.AssertionError e) {
      // pass through to JUnit
      throw e;
    } catch (Throwable e) {
      logger.error("Exception storing entity", e);
      fail("exception storing entity");
    }
    finally {
      clearDatabase();
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .AccountingDbService#storeEntity(java.lang.String, * java.util.List)} .
   */
  @Test
  public void testStoreEntity() {
    CapitalStructure capitalStructure = createCapitalStructure();

    FiscalYear year1 = new FiscalYear(YEAR1);
    FiscalYear year2 = new FiscalYear(YEAR2);
    FiscalYear year3 = new FiscalYear(YEAR3);

    List<FiscalYear> years = new ArrayList<>();
    years.add(year1);
    years.add(year2);
    years.add(year3);

    try {
      clearDatabase();
      logger.info("Storing account subsystem: entity and dependents");
      IDataAccessService service = new AccountingDbService();
      logger.info("Storing capital structure");
      service.storeCapitalStructure(capitalStructure);
      assertTrue("capital structure not stored correctly",
                 validateCapitalStructure(capitalStructure));
      logger.info("Storing fiscal years");
      service.storeFiscalYears(years);
      logger.info("Creating accounts");
      createAccounts(years);
      logger.info("Storing entity");
      service.storeEntity(ENTITY_NAME, years);
      validateAccountSubsystem(years);
    } catch (java.lang.AssertionError e) {
      // pass through to JUnit
      throw e;
    } catch (Throwable e) {
      logger.error("Exception storing entity", e);
      fail("exception storing entity");
    }
    finally {
      clearDatabase();
    }
  }

  /**
   * Test method for {@link com.poesys .accounting.dataloader.newaccounting
   * .AccountingDbService#storeEntity(java.lang.String, * java.util.List)} . This is a debugging
   * test that stores one year and only stores the capital accounts. Use it to debug the storing of
   * the account inheritance hierarchy.
   */
  @Test
  public void testStoreCapitalAccount() {
    CapitalStructure capitalStructure = createCapitalStructure();

    FiscalYear year1 = createLimitedFiscalYear(YEAR1);

    List<FiscalYear> years = new ArrayList<>();
    years.add(year1);

    try {
      clearDatabase();
      logger.info("Storing account subsystem: entity and dependents");
      IDataAccessService service = new AccountingDbService();
      service.storeCapitalStructure(capitalStructure);
      assertTrue("capital structure not stored correctly",
                 validateCapitalStructure(capitalStructure));
      createAccounts(years);
      service.storeFiscalYears(years);
      service.storeEntity(ENTITY_NAME, years);
      assertTrue("account subsystem invalid", validateAccountSubsystem(years));
    } catch (java.lang.AssertionError e) {
      // pass through to JUnit
      throw e;
    } catch (Throwable e) {
      logger.error("Exception storing entity", e);
      fail("exception storing entity");
    }
    finally {
      clearDatabase();
    }
  }

  /**
   * Create the capital structure by adding 2 partner capital entities.
   *
   * @return a capital structure
   */
  private CapitalStructure createCapitalStructure() {
    CapitalStructure capitalStructure = new CapitalStructure(incomeSummaryName);
    List<CapitalEntity> entities = new ArrayList<>(2);
    entities.add(partner1);
    entities.add(partner2);
    capitalStructure.addEntities(entities);
    return capitalStructure;
  }

  /**
   * Validate the capital structure against the database.
   *
   * @param capitalStructure the structure to validate
   * @return true if the structure is valid, false if not
   */
  private boolean validateCapitalStructure(CapitalStructure capitalStructure) {
    boolean status = false;

    Connection connection = null;
    PreparedStatement stmt = null;

    try {
      connection = JdbcConnectionManager.getConnection(DBMS.MYSQL, ACCOUNT_SUBSYSTEM);

      for (CapitalEntity entity : capitalStructure.getEntities()) {
        stmt = connection.prepareStatement(
          "SELECT 1 FROM CapitalEntity WHERE capitalEntityName " + "= ?");
        stmt.setString(1, entity.getName());
        ResultSet rs = stmt.executeQuery();
        assertTrue("no capital entity " + entity.getName(), rs.next());
      }

      // Set the status to true to complete validation.
      status = true;
    } catch (SQLException | IOException e) {
      logger.error("SQL exception or IO error validating accounts", e);
      try {
        if (connection != null) {
          connection.rollback();
        }
      } catch (SQLException e1) {
        // ignore exception
      }
    }
    finally {
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
   * Validate the entity, accounts, account groups, and fiscal years stored by the storeEntity()
   * method against the database.
   *
   * @param years the list of fiscal years to validate
   * @return true if the database state is valid, false if not
   */
  private boolean validateAccountSubsystem(List<FiscalYear> years) {
    boolean status = false;

    Connection connection = null;
    PreparedStatement stmt = null;

    try {
      connection = JdbcConnectionManager.getConnection(DBMS.MYSQL, ACCOUNT_SUBSYSTEM);

      // Verify entity
      stmt = connection.prepareStatement("SELECT 1 FROM Entity WHERE entityName = ?");
      stmt.setString(1, ENTITY_NAME);
      ResultSet rs = stmt.executeQuery();
      assertTrue("no entity", rs.next());
      stmt.close();

      // Verify fiscal years and accounts, first year only as it contains all
      // the accounts
      for (FiscalYear year : years) {
        // Verify the year by year number.
        stmt = connection.prepareStatement("SELECT 1 FROM FiscalYear where year = ?");
        stmt.setInt(1, year.getYear());
        rs = stmt.executeQuery();
        assertTrue("no year " + year.getYear(), rs.next());
        stmt.close();

        // Verify the account groups.
        // Verify the account group as well.
        stmt = connection.prepareStatement("SELECT 1 FROM AccountGroup WHERE groupName = ?");
        verifyGroup(stmt, CASH_GROUP);
        verifyGroup(stmt, AR_GROUP);
        verifyGroup(stmt, LIABILITY_GROUP);
        verifyGroup(stmt, EQUITY_GROUP);
        verifyGroup(stmt, INCOME_GROUP);
        verifyGroup(stmt, EXPENSE_GROUP);

        // For the first year only, verify the accounts and capital entities.
        if (YEAR1.equals(year.getYear())) {
          validateAccounts(connection, year);
          validateCapitalAccounts(connection, year);
          validateDistributionAccounts(connection, year);

          // Close statement in finally clause.
        }
      }

      // Set the status to true to complete validation.
      status = true;
    } catch (SQLException | IOException e) {
      logger.error("SQL exception or IO error validating accounts", e);
      try {
        if (connection != null) {
          connection.rollback();
        }
      } catch (SQLException e1) {
        // ignore exception
      }
    }
    finally {
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
   * Validate the capital accounts for a single year given a JDBC connection.
   *
   * @param connection the JDBC connection
   * @param year       the fiscal year to validate
   */
  private void validateCapitalAccounts(Connection connection, FiscalYear year) {
    PreparedStatement stmt = null;
    ResultSet rs;// Query the account with account name and entity name
    try {
      // @formatter:off
      stmt = connection.prepareStatement(
        "SELECT 1 " + "" +
              "FROM CapitalAccount ca JOIN " +
                   "Account a ON a.entityName = ca.entityName AND " +
                                "a.accountName = ca.accountName JOIN " +
                   "CapitalEntity e ON ca.capitalEntityName = e.capitalEntityName " +
             "WHERE a.accountName = ?");
      // @formatter:on
      boolean foundAccount = false;
      for (FiscalYearAccount fya : year.getAccounts()) {
        Account account = fya.getAccount();
        CapitalEntity entity = account.getCapitalEntity();
        if (entity != null && entity.getCapitalAccountName().equals(account.getName())) {
          foundAccount = true;
          // This account is a capital account, query it.
          stmt.setString(1, account.getName());
          rs = stmt.executeQuery();
          // Get the single returned row to validate the result.
          assertTrue("no account " + account, rs.next());
          rs.close();
        }
      }
      if (!foundAccount) {
        fail("no distribution account associated with capital entity");
      }
    } catch (SQLException e) {
      fail("SQL Exception " + e.getMessage());
    }
    finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }
  }

  /**
   * Validate the distribution accounts for a single year given a JDBC connection.
   *
   * @param connection the JDBC connection
   * @param year       the fiscal year to validate
   */
  private void validateDistributionAccounts(Connection connection, FiscalYear year) {
    PreparedStatement stmt = null;
    ResultSet rs;// Query the account with account name and entity name
    try {
      // @formatter:off
      stmt = connection.prepareStatement(
        "SELECT 1 " + "" +
              "FROM DistributionAccount da JOIN " +
                   "Account a ON a.entityName = da.entityName AND " +
                                "a.accountName = da.accountName JOIN " +
                   "CapitalEntity e ON da.capitalEntityName = e.capitalEntityName " +
             "WHERE a.accountName = ?");
      // @formatter:on
      boolean foundAccount = false;
      for (FiscalYearAccount fya : year.getAccounts()) {
        Account account = fya.getAccount();
        CapitalEntity entity = account.getCapitalEntity();
        if (entity != null) {
          Account distributionAccount = entity.getDistributionAccount();
          if (distributionAccount != null && entity.getDistributionAccount().equals(account)) {
            foundAccount = true;
            // This account is a distribution account, query it.
            stmt.setString(1, account.getName());
            rs = stmt.executeQuery();
            // Get the single returned row to validate the result.
            assertTrue("no account " + account, rs.next());
            rs.close();
          }
        }
      }
      if (!foundAccount) {
        fail("no distribution account associated with capital entity");
      }
    } catch (SQLException e) {
      fail("SQL Exception " + e.getMessage());
    }
    finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }
  }

  /**
   * Validate the BsAccount objects (superclass objects) for all accounts in a year.
   *
   * @param connection the JDBC connection
   * @param year       the fiscal year to validate
   */
  private void validateAccounts(Connection connection, FiscalYear year) {
    PreparedStatement stmt = null;
    ResultSet rs;// Query the account with account name and entity name
    try {
      stmt = connection.prepareStatement("SELECT 1 FROM Account where " + "accountName = ?");
      for (FiscalYearAccount fya : year.getAccounts()) {
        Account account = fya.getAccount();
        stmt.setString(1, account.getName());
        rs = stmt.executeQuery();
        assertTrue("no account " + account, rs.next());
        rs.close();
      }
    } catch (SQLException e) {
      fail("SQL Exception " + e.getMessage());
    }
    finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }
  }

  /**
   * Verify an accounting group using a previously created JDBC statement.
   *
   * @param stmt  the JDBC statement with an open query with one parameter, the group name
   * @param group the account group to verify
   * @throws SQLException when there is a problem executing the query
   */
  private void verifyGroup(PreparedStatement stmt, AccountGroup group) throws SQLException {
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
    CapitalStructure capitalStructure = createCapitalStructure();

    FiscalYear year1 = new FiscalYear(YEAR1);
    FiscalYear year2 = new FiscalYear(YEAR2);
    FiscalYear year3 = new FiscalYear(YEAR3);

    List<FiscalYear> years = new ArrayList<>();
    years.add(year1);
    years.add(year2);
    years.add(year3);

    Set<Transaction> transactions = createFiscalYearTransactions(years);

    try {
      clearDatabase();
      logger.info("Storing transaction subsystem: entity, transactions");
      IDataAccessService service = new AccountingDbService();
      service.storeCapitalStructure(capitalStructure);
      service.storeFiscalYears(years);
      createAccounts(years);
      service.storeEntity(ENTITY_NAME, years);
      service.storeTransactions(transactions);
      assertTrue("transaction subsystem not stored correctly",
                 validateTransactionSubsystem(transactions));
    } catch (java.lang.AssertionError e) {
      // pass through to JUnit
      throw e;
    } catch (Throwable e) {
      logger.error("exception storing entity and transactions", e);
      fail("exception storing entity and transactions");
    }
    finally {
      clearDatabase();
    }
  }

  /**
   * Validate the transactions, items, and reimbursements stored by the storeEntity() method against
   * the database. Use the IdMap table to look up the new transaction id based on the old one. This
   * validates the IdMap table as well.
   *
   * @param transactions the set of transactions to validate
   * @return true if the database state is valid, false if not
   */
  private boolean validateTransactionSubsystem(Set<Transaction> transactions) {
    boolean status = false;

    Connection connection = null;
    PreparedStatement stmt = null;

    try {
      connection = JdbcConnectionManager.getConnection(DBMS.MYSQL, TRANSACTION_SUBSYSTEM);
      if (connection == null) {
        fail("no JDBC connection");
      }

      // Verify entity
      stmt = connection.prepareStatement("SELECT 1 FROM Entity WHERE entityName = ?");
      stmt.setString(1, ENTITY_NAME);
      ResultSet rs = stmt.executeQuery();
      assertTrue("no entity", rs.next());
      stmt.close();

      // Verify transactions, items, and reimbursements, all in same fiscal year
      for (Transaction transaction : transactions) {
        // Get the year.
        Integer year = transaction.getYear();
        // Verify transaction and id map by querying items using the old id and the fiscal year.
        stmt = connection.prepareStatement(
          "SELECT count(*) FROM Transaction t JOIN IdMap i ON t.transactionId = i.newId JOIN " +
          "Item it ON t.transactionId = it.transactionId WHERE oldId = ? AND YEAR" +
          "(transactionDate) = ?");
        stmt.setBigDecimal(1, new BigDecimal(transaction.getId()));
        stmt.setInt(2, transaction.getYear());
        rs = stmt.executeQuery();
        if (rs.next()) {
          int count = rs.getInt(1);
          // Verify items by count.
          assertTrue(
            "Wrong number of items: " + count + ", should be " + transaction.getItems().size() +
            ": " + transaction, count == transaction.getItems().size());
        }
        stmt.close();

        // For receivable transaction, verify Reimbursement
        for (Item item : transaction.getItems()) {
          if (item.getAccount().isReceivable() && item.isDebit() && !transaction.isBalance()) {
            // receivable item in non-balance transaction, check for reimbursement based on old id
            // @formatter:off
            stmt = connection.prepareStatement(
              "SELECT count(*) FROM Reimbursement r JOIN IdMap i ON r.receivablesTransactionId = i.newId JOIN Transaction t ON r.receivablesTransactionId = t.transactionId JOIN " +
                "FiscalYear y ON t.transactionDate BETWEEN y.startDate AND y.endDate WHERE i" +
                ".oldId = ? AND y.year = ?");
            // @formatter:on
            stmt.setBigDecimal(1, new BigDecimal(item.getTransaction().getId()));
            stmt.setInt(2, year);
            rs = stmt.executeQuery();
            if (rs.next()) {
              int count = rs.getInt(1);
              assertTrue("no receivable reimbursement found", count == 1);
            }
            stmt.close();
          } else if (item.getAccount().isReceivable() && !item.isDebit() &&
                     !transaction.isBalance()) {
            // reimbursing item in non-balance transaction, check for reimbursement based on old id
            // @formatter:off
            stmt = connection.prepareStatement(
              "SELECT count(*) FROM Reimbursement r JOIN IdMap i ON r.reimbursingItemsTransactionId = i.newId JOIN " +
                "Transaction t ON r.reimbursingItemsTransactionId = t.transactionId JOIN FiscalYear y ON t.transactionDate BETWEEN y.startDate AND y.endDate " +
                "WHERE i.oldId = ? AND y.year = ?");
            // @formatter:on
            stmt.setBigDecimal(1, new BigDecimal(item.getTransaction().getId()));
            stmt.setInt(2, year);
            rs = stmt.executeQuery();
            if (rs.next()) {
              int count = rs.getInt(1);
              assertTrue("no reimbursement found for reimbursing item", count == 1);
            }
            stmt.close();
          }
        }
      }

      // Set the status to true to complete validation.
      status = true;
    } catch (SQLException | IOException e) {
      logger.error("SQL exception or IO error validating transactions", e);
      try {
        if (connection != null) {
          connection.rollback();
        }
      } catch (SQLException e1) {
        // ignore exception
      }
    }
    finally {
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
   * Helper method to clear caches for any objects used in the test
   *
   * @param subsystem the subsystem to clear
   */
  private void clearSubsystemCaches(String subsystem) {
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    if (manager != null) {
      manager.clearAllCaches();
    }
  }

  /**
   * Remove any data currently in the database and set sequences to 1.
   */
  private void clearDatabase() {
    // Clear subsystems in dependency order: transaction then account
    clearTransactionSubsystem();
    // Clear Java caches for subsystem to refresh on the next test
    clearSubsystemCaches("com.poesys.accounting.db.transaction");
    clearAccountSubsystem();
    clearSubsystemCaches("com.poesys.accounting.db.account");
  }

  /**
   * Clear the tables in the Account subsystem (Entity, FiscalYear, AccountGroup). This method also
   * clears the tables Account and FiscalYearAccount through cascaded deletes. Call the
   * clearTransactionSubsystem() method before calling this one to remove dependencies from Item to
   * Account.
   */
  private void clearAccountSubsystem() {
    Connection connection = null;
    java.sql.Statement stmt = null;

    try {
      connection = JdbcConnectionManager.getConnection(DBMS.MYSQL, ACCOUNT_SUBSYSTEM);
      stmt = connection.createStatement();
      stmt.execute("DELETE FROM Entity");
      stmt.execute("DELETE FROM FiscalYear");
      stmt.execute("DELETE FROM AccountGroup");
      stmt.execute("DELETE FROM CapitalEntity");
      connection.commit();

      // Clear Poesys/DB caches
    } catch (SQLException | IOException e) {
      logger.error("exception clearing database", e);
      if (connection != null) {
        try {
          connection.rollback();
        } catch (SQLException e1) {
          // ignore exception
        }
      }
    }
    finally {
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
   * Clear the tables in the Transaction subsystem (Transaction). Call this before calling
   * clearAccountSubsystem(). Also deletes Item, IdMap, and Reimbursement tables through cascaded
   * deletes. Also updates the transaction sequence to start at 1 again.
   */
  private void clearTransactionSubsystem() {
    Connection connection = null;
    java.sql.Statement stmt = null;

    try {
      connection = JdbcConnectionManager.getConnection(DBMS.MYSQL, TRANSACTION_SUBSYSTEM);
      stmt = connection.createStatement();
      stmt.execute("DELETE FROM IdMap");
      stmt.execute("DELETE FROM Transaction");
      stmt.execute("UPDATE Sequence SET sequence = 1 WHERE name = 'transactionId'");
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
    }
    finally {
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
   * Create the test accounts for a list of fiscal years with a set of standard accounts and account
   * groups. This method creates the new-accounting accounts and groups required without any
   * transactions.
   *
   * @param fiscalYears a list of persisted, new-accounting fiscal year objects
   */
  private void createAccounts(List<FiscalYear> fiscalYears) {
    for (FiscalYear fiscalYear : fiscalYears) {
      // Link the accounts to the fiscal year.
      linkAccountToYear(fiscalYear, AccountType.ASSETS, CASH_GROUP, cashAccount, 1, 1);
      linkAccountToYear(fiscalYear, AccountType.ASSETS, AR_GROUP, arAccount, 2, 1);
      linkAccountToYear(fiscalYear, AccountType.LIABILITIES, LIABILITY_GROUP, liabilityAccount, 1,
                        1);
      linkAccountToYear(fiscalYear, AccountType.INCOME, INCOME_GROUP, incomeAccount, 1, 1);
      linkAccountToYear(fiscalYear, AccountType.EXPENSES, EXPENSE_GROUP, expenseAccount, 1, 1);

      // Add the capital entities to the equity accounts and vice versa, then link them.
      partner1CapitalAccount.setCapitalEntity(partner1);
      partner1.setCapitalAccount(partner1CapitalAccount);
      partner1DistributionAccount.setCapitalEntity(partner1);
      partner1.setDistributionAccount(partner1DistributionAccount);
      partner2CapitalAccount.setCapitalEntity(partner2);
      partner2.setCapitalAccount(partner2CapitalAccount);
      partner2DistributionAccount.setCapitalEntity(partner2);
      partner2.setDistributionAccount(partner2DistributionAccount);

      linkAccountToYear(fiscalYear, AccountType.EQUITY, EQUITY_GROUP, partner1CapitalAccount, 1, 1);
      linkAccountToYear(fiscalYear, AccountType.EQUITY, EQUITY_GROUP, partner1DistributionAccount,
                        1, 2);
      linkAccountToYear(fiscalYear, AccountType.EQUITY, EQUITY_GROUP, partner2CapitalAccount, 1, 3);
      linkAccountToYear(fiscalYear, AccountType.EQUITY, EQUITY_GROUP, partner2DistributionAccount,
                        1, 4);
    }
  }

  /**
   * Create a fiscal year with a set of capital accounts and the EQUITY account group. This method
   * creates the fiscal year without any transactions.
   *
   * @param year the integer year
   * @return the fiscal year with nested accounts and groups
   */
  private FiscalYear createLimitedFiscalYear(Integer year) {
    FiscalYear fiscalYear = new FiscalYear(year);
    assertTrue("wrong year created: " + fiscalYear.getYear(), fiscalYear.getYear().equals(year));
    // Link the accounts to the fiscal year.
    linkAccountToYear(fiscalYear, AccountType.ASSETS, CASH_GROUP, cashAccount, 1, 1);
    linkAccountToYear(fiscalYear, AccountType.ASSETS, AR_GROUP, arAccount, 2, 1);
    linkAccountToYear(fiscalYear, AccountType.LIABILITIES, LIABILITY_GROUP, liabilityAccount, 1, 1);
    linkAccountToYear(fiscalYear, AccountType.INCOME, INCOME_GROUP, incomeAccount, 1, 1);
    linkAccountToYear(fiscalYear, AccountType.EXPENSES, EXPENSE_GROUP, expenseAccount, 1, 1);

    // Add the capital entities to the equity accounts, then link them.
    partner1CapitalAccount.setCapitalEntity(partner1);
    partner1DistributionAccount.setCapitalEntity(partner1);
    partner2CapitalAccount.setCapitalEntity(partner2);
    partner2DistributionAccount.setCapitalEntity(partner2);

    linkAccountToYear(fiscalYear, AccountType.EQUITY, EQUITY_GROUP, partner1CapitalAccount, 1, 1);
    linkAccountToYear(fiscalYear, AccountType.EQUITY, EQUITY_GROUP, partner1DistributionAccount, 1,
                      2);
    linkAccountToYear(fiscalYear, AccountType.EQUITY, EQUITY_GROUP, partner2CapitalAccount, 1, 3);
    linkAccountToYear(fiscalYear, AccountType.EQUITY, EQUITY_GROUP, partner2DistributionAccount, 1,
                      4);

    return fiscalYear;
  }

  /**
   * Link the account to a fiscal year, associating both with a group.
   *
   * @param fiscalYear         the fiscal year to link
   * @param type               the type of the account (ASSETS, LIABILITIES, and so on)
   * @param group              the account group to associate with the link
   * @param account            the account to link
   * @param groupOrderNumber   the order of the group in the type
   * @param accountOrderNumber the order of the account in the group
   */
  private void linkAccountToYear(FiscalYear fiscalYear, AccountType type, AccountGroup group,
                                 Account account, Integer groupOrderNumber, Integer
                                   accountOrderNumber) {
    FiscalYearAccount fya =
      new FiscalYearAccount(fiscalYear, type, group, groupOrderNumber, account, accountOrderNumber);
    fiscalYear.addAccount(fya);
    group.addLink(fya);
    account.addYear(fya);
  }

  /**
   * Create a set of transactions for a list of fiscal years, creating the same transactions in each
   * year. This builds a reasonable set of annual transactions that exercises a range of accounts in
   * the system for each year, simulating a real accounting system.
   *
   * @param years the list of years
   * @return the set of generated transactions
   */
  private Set<Transaction> createFiscalYearTransactions(List<FiscalYear> years) {
    Set<Transaction> transactions = new HashSet<>();
    BigInteger id = BigInteger.ONE;
    Transaction transaction;

    boolean firstYear = true;

    for (FiscalYear fiscalYear : years) {
      // Create a set of transactions that looks like a complete year's accounts for each year.

      // Initialize first id for the year to be 1.
      BigInteger nextId = id.add(BigInteger.ONE);

      // Balance transactions for the asset, liability, and equity accounts. First year only.
      // These accounts sum to zero (debits negative, credits positive).

      if (firstYear) {
        // Cash balance
        transaction = new Transaction(id, "Cash Balance", fiscalYear.getStart(), true, true);
        transaction.addItem(100.00D, cashAccount, true, false);
        transactions.add(transaction);

        // Accounts Receivable balance
        transaction = new Transaction(nextId, "AR Balance", fiscalYear.getStart(), true, true);
        transaction.addItem(0.00D, arAccount, true, false);
        assertTrue("transaction invalid: " + transaction, transaction.isValid());
        transactions.add(transaction);

        // Liability balance
        nextId = nextId.add(BigInteger.ONE);
        transaction =
          new Transaction(nextId, "Liability Balance", fiscalYear.getStart(), false, true);
        transaction.addItem(50.00D, liabilityAccount, false, false);
        assertTrue("transaction invalid: " + transaction, transaction.isValid());
        transactions.add(transaction);

        // Partner 1 capital balance
        nextId = nextId.add(BigInteger.ONE);
        transaction =
          new Transaction(nextId, "Partner 1 Capital Balance", fiscalYear.getStart(), false, true);
        transaction.addItem(25.00D, partner1CapitalAccount, false, false);
        assertTrue("transaction invalid: " + transaction, transaction.isValid());
        transactions.add(transaction);

        // Partner 2 capital balance
        nextId = nextId.add(BigInteger.ONE);
        transaction =
          new Transaction(nextId, "Partner 2 Capital Balance", fiscalYear.getStart(), false, true);
        transaction.addItem(25.00D, partner2CapitalAccount, false, false);
        assertTrue("transaction invalid: " + transaction, transaction.isValid());
        transactions.add(transaction);
      }

      // Transactions for the year

      // Income-Cash
      nextId = nextId.add(BigInteger.ONE);
      transaction =
        new Transaction(nextId, "Income-Cash Transaction", fiscalYear.getStart(), false, false);
      transaction.addItem(50.00D, incomeAccount, false, false);
      transaction.addItem(50.00D, cashAccount, true, false);
      assertTrue("transaction invalid: " + transaction, transaction.isValid());
      transactions.add(transaction);

      // Income-AR (Receivable)
      nextId = nextId.add(BigInteger.ONE);
      transaction =
        new Transaction(nextId, "Income-Receivable Transaction", fiscalYear.getStart(), false,
                        false);

      transaction.addItem(50.00D, incomeAccount, false, false);
      transaction.addItem(50.00D, arAccount, true, false);
      assertTrue("transaction invalid: " + transaction, transaction.isValid());
      transactions.add(transaction);

      // Save the receivable item for later reimbursement.
      Item receivableItem = null;
      for (Item item : transaction.getItems()) {
        if (item.getAccount().isReceivable()) {
          receivableItem = item;
          break;
        }
      }
      assertTrue("no receivable item for receivable transaction", receivableItem != null);

      // AR-Cash (Reimbursement of Receivable)
      nextId = nextId.add(BigInteger.ONE);
      transaction =
        new Transaction(nextId, "Reimbursement Transaction", fiscalYear.getStart(), false, false);
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

      assertTrue("no reimbursing item for reimbursement transaction", reimbursingItem != null);

      // Reimburse the receivable item.
      receivableItem.reimburse(reimbursingItem, 50.00D, 0.00D);

      assertTrue("transaction invalid: " + transaction, transaction.isValid());
      transactions.add(transaction);

      // Cash-Expense
      nextId = nextId.add(BigInteger.ONE);
      transaction =
        new Transaction(nextId, "Cash-Expense Transaction", fiscalYear.getStart(), false, false);
      transaction.addItem(25.00D, cashAccount, false, false);
      transaction.addItem(25.00D, expenseAccount, true, false);
      assertTrue("transaction invalid: " + transaction, transaction.isValid());
      transactions.add(transaction);

      // Credit-Expense
      nextId = nextId.add(BigInteger.ONE);
      transaction =
        new Transaction(nextId, "Credit-Expense Transaction", fiscalYear.getStart(), false, false);
      transaction.addItem(30.00D, liabilityAccount, false, false);
      transaction.addItem(30.00D, expenseAccount, true, false);
      assertTrue("transaction invalid: " + transaction, transaction.isValid());
      transactions.add(transaction);

      // Capital-Cash
      nextId = nextId.add(BigInteger.ONE);
      transaction =
        new Transaction(nextId, "Capital-Cash Transaction for Partner 1", fiscalYear.getStart(),
                        false, false);
      transaction.addItem(100.00D, cashAccount, true, false);
      transaction.addItem(100.00D, partner1CapitalAccount, false, false);
      assertTrue("transaction invalid: " + transaction, transaction.isValid());
      transactions.add(transaction);

      // Set first-year flag off.
      firstYear = false;
    }

    return transactions;
  }
}
