/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.poesys.accounting.dataloader.IBuilder;
import com.poesys.accounting.dataloader.newaccounting.Account;
import com.poesys.accounting.dataloader.newaccounting.AccountGroup;
import com.poesys.accounting.dataloader.newaccounting.FiscalYear;
import com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder.IBuildStrategy;
import com.poesys.accounting.dataloader.properties.IParameters;
import com.poesys.accounting.dataloader.properties.UnitTestParametersInvalidPath;
import com.poesys.accounting.dataloader.properties.UnitTestParametersInvalidTransaction;
import com.poesys.accounting.dataloader.properties.UnitTestParametersNoExceptions;
import com.poesys.accounting.dataloader.properties.UnitTestParametersReimbursementDifferentYear;
import com.poesys.accounting.dataloader.properties.UnitTestParametersReimbursementSameYear;
import com.poesys.db.InvalidParametersException;


/**
 * CUT: OldDataBuilder implementation of IBuilder interface (integration test,
 * uses 3-year test data set under data directory)
 * 
 * @author Robert J. Muller
 */
public class OldDataBuilderTest {

  private static final Integer YEAR = 2014;

  private static final String INVALID_TRANSACTIONS_ERROR =
    "invalid transactions";

  // Strategy implementations for readFile unit tests

  /**
   * Implementation of IBuildStrategy that does nothing (valid test case)
   */
  private class ValidUnitTestStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      // Does nothing, returns EndOfStream
      throw new EndOfStream();
    }
  }

  /**
   * Implementation of IBuildStrategy that has infinite loop due to no
   * EndOfStream exception being thrown
   */
  private class StrategyInfiniteLoop implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      // Does nothing.
    }
  }

  /** null parameter error from readFile parameter */
  private static final String NULL_PARAMETER_ERROR =
    "parameters required but are null";

  /** wrong number of or invalid type of fields */
  protected static final String INVALID_FIELDS_ERROR =
    "input data has wrong number of fields: ";

  /**
   * Implementation of IBuildStrategy that throws am invalid-parameters
   * exception
   */
  private class StrategyWithInvalidFieldsException implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      throw new InvalidParametersException(INVALID_FIELDS_ERROR);
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#OldDataBuilder(IParameters)}
   * . Tests constructor and groups/accounts getters.
   */
  @Test
  public void testOldDataBuilder() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    String path = builder.getPath();
    assertTrue("no path string assigned", path != null);
    assertTrue("wrong path string", builder.getPath().equalsIgnoreCase(path));
    Set<AccountGroup> createdGroups = builder.getAccountGroups();
    assertTrue("no groups variable set in constructor", createdGroups != null);
    assertTrue("invalid groups variable", createdGroups.isEmpty());
    Set<Account> createdAccounts = builder.getAccounts();
    assertTrue("no accounts variable set in constructor", createdGroups != null);
    assertTrue("invalid accounts variable", createdAccounts.isEmpty());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#OldDataBuilder(IParameters)}
   * . Tests constructor and groups/accounts getters.
   */
  @Test
  public void testOldDataBuilderMultipleYears() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    Set<FiscalYear> years = new HashSet<FiscalYear>();
    for (int i = 0; i < 3; i++) {
      builder.buildFiscalYear(YEAR + i);
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();
      if (i == 0) {
        builder.buildBalances();
      }
      builder.buildTransactions();
      years.add(builder.getFiscalYear());
    }
    assertTrue("wrong number of years built: " + years.size(),
               years.size() == 3);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#buildFiscalYear(java.lang.Integer)}
   * . Tests build method and year getter
   */
  @Test
  public void testBuildFiscalYear() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    FiscalYear year = builder.getFiscalYear();
    assertTrue("no fiscal year built", year != null);
    assertTrue("wrong fiscal year built: " + year.getYear(),
               YEAR.equals(year.getYear()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#readFile(Reader, OldDataBuilder.IBuildStrategy)}
   * . Tests building with valid setup resulting in EndOfStream exception
   */
  @Test
  public void testReadFile() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    // Use an account group reader for the test, data doesn't matter
    BufferedReader reader =
      new BufferedReader(parameters.getAccountGroupReader(YEAR));
    // Use test strategy that does nothing
    IBuildStrategy strategy = new ValidUnitTestStrategy();
    builder.readFile(reader, strategy);
    // success
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#readFile(Reader, OldDataBuilder.IBuildStrategy)}
   * . Tests building with setup resulting in no EndOfStream exception and hence
   * an infinite loop possibility constrained by a for-loop limit
   */
  @Test
  public void testReadFileNoInfiniteReader() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    // Use an account group reader for the test, data doesn't matter
    BufferedReader reader =
      new BufferedReader(parameters.getAccountGroupReader(YEAR));
    // Use test strategy that does nothing
    IBuildStrategy strategy = new StrategyInfiniteLoop();
    builder.readFile(reader, strategy);
    // success
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#readFile(Reader, OldDataBuilder.IBuildStrategy)}
   * . Tests building with invalid parameter exception thrown because of null
   * reader
   */
  @Test
  public void testReadFileInvalidParametersNullReader() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    try {
      // Make the reader null, causing exception
      BufferedReader reader = null;
      // Use test strategy that does nothing
      IBuildStrategy strategy = new StrategyWithInvalidFieldsException();
      builder.readFile(reader, strategy);
      fail("no invalid-parameters exception from null reader");
    } catch (InvalidParametersException e) {
      assertTrue("wrong exception: " + e.getMessage(),
                 e.getMessage().contains(NULL_PARAMETER_ERROR));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#readFile(Reader, OldDataBuilder.IBuildStrategy)}
   * . Tests building with invalid parameter exception thrown because of null
   * strategy
   */
  @Test
  public void testReadFileInvalidParametersNullStrategy() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    try {
      // Use an account group reader for the test, data doesn't matter
      BufferedReader reader =
        new BufferedReader(parameters.getAccountGroupReader(YEAR));
      // Make the strategy null, causing exception
      IBuildStrategy strategy = null;
      builder.readFile(reader, strategy);
      fail("no invalid-parameters exception from null reader");
    } catch (InvalidParametersException e) {
      assertTrue("wrong exception: " + e.getMessage(),
                 e.getMessage().contains(NULL_PARAMETER_ERROR));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#readFile(Reader, OldDataBuilder.IBuildStrategy)}
   * . Tests building with invalid parameter exception thrown because of invalid
   * data structure in input data
   */
  @Test
  public void testReadFileInvalidParameters() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    try {
      // Use an account group reader for the test, data doesn't matter
      BufferedReader reader =
        new BufferedReader(parameters.getAccountGroupReader(YEAR));
      // Use test strategy that does nothing
      IBuildStrategy strategy = new StrategyWithInvalidFieldsException();
      builder.readFile(reader, strategy);
      fail("no invalid-parameters exception from invalid path");
    } catch (InvalidParametersException e) {
      assertTrue("wrong exception: " + e.getMessage(),
                 e.getMessage().contains(INVALID_FIELDS_ERROR));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#buildAccountGroups()}
   * . Tests building with invalid path, which throws a runtime exception; tests
   * the internal getReader() method, no need to test different file names
   */
  @Test
  public void testBuildInvalidPath() {
    IParameters parameters = new UnitTestParametersInvalidPath();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    try {
      builder.buildAccountGroups();
      fail("no runtime exception from invalid path");
    } catch (RuntimeException e) {
      assertTrue("wrong exception: " + e.getMessage(),
                 e.getMessage().contains("file not found"));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#buildAccountGroups()}
   * . Tests building one year and one set of groups, group getter
   */
  @Test
  public void testBuildAccountGroupsSingleYear() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    builder.buildAccountGroups();
    Set<AccountGroup> createdGroups = builder.getAccountGroups();
    assertTrue("no groups variable set in constructor", createdGroups != null);
    assertTrue("invalid groups variable", !createdGroups.isEmpty());
    assertTrue("wrong number of groups created: " + createdGroups.size(),
               createdGroups.size() == 3);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#buildAccountGroups()}
   * . Tests building multiple years and shared group set
   */
  @Test
  public void testBuildAccountGroupsMultipleYears() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);

    for (int i = 0; i < 3; i++) {
      builder.buildFiscalYear(YEAR + i);
      builder.buildAccountGroups();
      Set<AccountGroup> createdGroups = builder.getAccountGroups();
      assertTrue("no groups variable set in constructor", createdGroups != null);
      assertTrue("invalid groups variable", !createdGroups.isEmpty());
      assertTrue("wrong number of groups created: " + createdGroups.size(),
                 createdGroups.size() == 3);
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#buildAccounts()}
   * . Tests building one year and one year's accounts with groups. Tests
   * account set getter.
   */
  @Test
  public void testBuildAccountsSingleYear() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    Set<Account> createdAccounts = builder.getAccounts();
    assertTrue("no accounts variable set in constructor",
               createdAccounts != null);
    assertTrue("invalid accounts variable", !createdAccounts.isEmpty());
    assertTrue("wrong number of accounts created: " + createdAccounts.size(),
               createdAccounts.size() == 4);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#buildAccounts()}
   * . Tests building multiple years and multiple year's accounts with groups.
   * Tests the shared-set processing for accounts and groups by adding same
   * accounts and groups for each year, resulting in same accounts as for a
   * single year.
   */
  @Test
  public void testBuildAccountsMultipleYears() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);

    for (int i = 0; i < 3; i++) {
      builder.buildFiscalYear(YEAR + i);
      assertTrue("did not build fiscal year " + (YEAR + i),
                 builder.getFiscalYear() != null);
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();
      Set<Account> createdAccounts = builder.getAccounts();
      assertTrue("no accounts variable set in constructor",
                 createdAccounts != null);
      assertTrue("invalid accounts variable", !createdAccounts.isEmpty());
      assertTrue("wrong number of accounts created: " + createdAccounts.size(),
                 createdAccounts.size() == 4);
    }
  }

  private static final String GROUP_LOOKUP_ERROR =
    "cannot find group for account ";

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#buildAccounts()}
   * .
   */
  @Test
  public void testBuildAccountsGroupError() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    // Don't build account groups, so group map will be null.
    builder.buildAccountMap();
    try {
      builder.buildAccounts();
      fail("Did not throw exception on not finding account group");
    } catch (Throwable e) {
      assertTrue("wrong exception: " + e.getMessage(),
                 e.getMessage().contains(GROUP_LOOKUP_ERROR));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#buildBalances()}
   * .
   */
  @Test
  public void testBuildBalances() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    Set<Balance> balanceDataSet = ((OldDataBuilder)builder).getBalanceDataSet();
    assertTrue("no balances created",
               balanceDataSet != null && !balanceDataSet.isEmpty());
    assertTrue("wrong number of balances created: " + balanceDataSet.size(),
               balanceDataSet.size() == 1);
    // One balance, extract and compare to account number.
    for (Balance balance : balanceDataSet) {
      assertTrue("wrong balance created: " + 109.0F,
                 balance.getAccountNumber().equals(109.0F));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#buildTransactions()}
   * .
   */
  @Test
  public void testBuildTransactions() {
    IParameters parameters = new UnitTestParametersNoExceptions();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    Set<com.poesys.accounting.dataloader.newaccounting.Transaction> transactions =
      year.getTransactions();

    assertTrue("wrong number of transactions created: " + transactions.size(),
               transactions.size() == 2);

    for (com.poesys.accounting.dataloader.newaccounting.Transaction transaction : transactions) {
      if (transaction.isBalance()) {
        assertTrue("no items in balance transaction",
                   !transaction.getItems().isEmpty());
        for (com.poesys.accounting.dataloader.newaccounting.Item item : transaction.getItems()) {
          // only one item for balance
          assertTrue("wrong balance account " + item.getAccount(),
                     item.getAccount().getName().equals("Other Cash"));
          assertTrue("wrong balance amount " + item.getAmount(),
                     item.getAmount().equals(100.00D));
        }
      } else {
        assertTrue("no items in regular transaction",
                   !transaction.getItems().isEmpty());
        assertTrue("wrong number of items in regular transaction, expecting 2",
                   transaction.getItems().size() == 2);
        for (com.poesys.accounting.dataloader.newaccounting.Item item : transaction.getItems()) {
          // 2 items, accounts Citicorp Checking and Revenue DR and CR,
          // respectively
          assertTrue("wrong amount for item: " + item.getAmount(),
                     item.getAmount().equals(100.00D));
          assertTrue("wrong checked flag, should be false", !item.isChecked());
          if (item.getAccount().getName().equals("Revenue")) {
            assertTrue("revenue item should be credit but is debit",
                       !item.isDebit());
          } else if (item.getAccount().getName().equals("Citicorp Checking")) {
            assertTrue("revenue item should be debit but is credit",
                       item.isDebit());
          } else {
            fail("wrong account for item: " + item.getAccount());
          }
        }
      }
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#buildTransactions()}
   * . Tests transaction data that doesn't balance throws exception
   */
  @Test
  public void testBuildInvalidTransaction() {
    IParameters parameters = new UnitTestParametersInvalidTransaction();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(YEAR);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    try {
      builder.buildTransactions();
      fail("no exception from invalid transaction");
    } catch (RuntimeException e) {
      assertTrue("wrong exception",
                 INVALID_TRANSACTIONS_ERROR.equals(e.getMessage()));
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#buildTransactions()}
   * . Tests the structure of a complete receivable/reimbursement series of
   * transactions in the same fiscal year
   */
  @Test
  public void testBuildReimbursementSameYear() {
    IParameters parameters = new UnitTestParametersReimbursementSameYear();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    // Use year set in parameters.
    Integer yearNumber = parameters.getStartYear();
    builder.buildFiscalYear(yearNumber);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();
    builder.buildReimbursements();

    FiscalYear year = builder.getFiscalYear();
    Set<com.poesys.accounting.dataloader.newaccounting.Transaction> transactions =
      year.getTransactions();

    assertTrue("wrong number of transactions created: " + transactions.size(),
               transactions.size() == 3);

    // Set up the two receivable-account items for comparison.
    com.poesys.accounting.dataloader.newaccounting.Item receivable = null;
    com.poesys.accounting.dataloader.newaccounting.Item reimbursement = null;

    for (com.poesys.accounting.dataloader.newaccounting.Transaction transaction : transactions) {
      if (transaction.isBalance()) {
        assertTrue("no items in balance transaction",
                   !transaction.getItems().isEmpty());
        for (com.poesys.accounting.dataloader.newaccounting.Item item : transaction.getItems()) {
          // only one item for balance
          assertTrue("wrong balance account " + item.getAccount(),
                     item.getAccount().getName().equals("Other Cash"));
          assertTrue("wrong balance amount " + item.getAmount(),
                     item.getAmount().equals(100.00D));
        }
      } else {
        assertTrue("no items in regular transaction",
                   !transaction.getItems().isEmpty());
        assertTrue("wrong number of items in regular transaction, expecting 2",
                   transaction.getItems().size() == 2);
        // 2 transactions, a receivable and a reimbursement; the receivable has
        // a receivable account debit and a revenue account credit, while the
        // reimbursement has a receivable account credit and a checking account
        // debit. To test, first extract the two items, then iterate through the
        // items again and check the links between the two extracted items.

        for (com.poesys.accounting.dataloader.newaccounting.Item item : transaction.getItems()) {
          assertTrue("wrong amount for item: " + item.getAmount(),
                     item.getAmount().equals(100.00D));
          assertTrue("wrong checked flag, should be false", !item.isChecked());
          if (item.getAccount().getName().equals("Revenue")) {
            assertTrue("revenue item should be credit but is debit",
                       !item.isDebit());
          } else if (item.getAccount().getName().equals("Citicorp Checking")) {
            assertTrue("revenue item should be debit but is credit",
                       item.isDebit());
          } else if (!item.getAccount().getName().equals("Accounts Receivable")) {
            fail("wrong account for item: " + item.getAccount());
          } else {
            // receivable can be either debit or credit

            // Extract the two items for later comparison.
            if (item.isDebit()) {
              receivable = item;
            } else {
              reimbursement = item;
            }
          }
        }
      }
    }

    assertTrue("could not extract receivable item", receivable != null);
    assertTrue("could not extract reimbursement item", reimbursement != null);

    Set<com.poesys.accounting.dataloader.newaccounting.Item.Reimbursement> reimbursements =
      null;

    for (com.poesys.accounting.dataloader.newaccounting.Transaction transaction : transactions) {
      for (com.poesys.accounting.dataloader.newaccounting.Item item : transaction.getItems()) {
        if (item.getAccount().getName().equals("Accounts Receivable")
            && item.isDebit()) {
          // receivable item, extract reimbursements and test
          reimbursements = item.getReimbursements();
          assertTrue("no reimbursements for receivable",
                     reimbursements != null && reimbursements.size() == 1);
          for (com.poesys.accounting.dataloader.newaccounting.Item.Reimbursement reimbursement1 : reimbursements) {
            assertTrue("wrong reimbursement item for receivable",
                       reimbursement.equals(reimbursement1.getReimbursingItem()));
            assertTrue("wrong receivable item for receivable",
                       receivable.equals(reimbursement1.getReceivable()));
            assertTrue("wrong reimbursed amount for receivable reimbursement",
                       reimbursement1.getReimbursedAmount().equals(100.00D));
            assertTrue("wrong allocated amount for receivable reimbursement",
                       reimbursement1.getAllocatedAmount().equals(0.00D));
          }
        } else if (item.getAccount().getName().equals("Accounts Receivable")
                   && !item.isDebit()) {
          // reimbursement item, extract reimbursements and test
          reimbursements = item.getReimbursements();
          assertTrue("no reimbursements for reimbursement",
                     reimbursements != null && reimbursements.size() == 1);
          for (com.poesys.accounting.dataloader.newaccounting.Item.Reimbursement reimbursement2 : reimbursements) {
            assertTrue("wrong reimbursement item for reimbursement",
                       reimbursement.equals(reimbursement2.getReimbursingItem()));
            assertTrue("wrong receivable item for reimbursement",
                       receivable.equals(reimbursement2.getReceivable()));
            assertTrue("wrong reimbursed amount for reimbursement",
                       reimbursement2.getReimbursedAmount().equals(100.00D));
            assertTrue("wrong allocated amount for reimbursement",
                       reimbursement2.getAllocatedAmount().equals(0.00D));
          }
        }
      }
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder#buildTransactions()}
   * . Tests the structure of a complete receivable/reimbursement series of
   * transactions in an earlier fiscal year
   */
  @Test
  public void testBuildReimbursementDifferentYear() {
    List<FiscalYear> fiscalYears = new ArrayList<FiscalYear>(2);
    com.poesys.accounting.dataloader.newaccounting.Account receivableAccount =
      null;

    UnitTestParametersReimbursementDifferentYear parameters =
      new UnitTestParametersReimbursementDifferentYear();

    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);

    for (int i = parameters.getStartYear(); i <= parameters.getEndYear(); i++) {
      // Use year set in parameters.
      parameters.setCurrentYear(i);
      builder.buildFiscalYear(i);
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();
      builder.buildBalances();
      builder.buildTransactions();
      builder.buildReimbursements();

      FiscalYear year = builder.getFiscalYear();
      Set<com.poesys.accounting.dataloader.newaccounting.Transaction> transactions =
        year.getTransactions();

      assertTrue("wrong number of transactions created in "
                     + transactions.size(),
                 transactions.size() == 2);

      fiscalYears.add(year);
    }

    assertTrue("wrong number of fiscal years: " + fiscalYears.size(),
               fiscalYears.size() == 2);

    // Extract the receivable account for later use.
    for (com.poesys.accounting.dataloader.newaccounting.Account account : builder.getAccounts()) {
      if (account.isReceivable()) {
        receivableAccount = account;
        break;
      }
    }

    // Set up the two receivable-account items for comparison.
    com.poesys.accounting.dataloader.newaccounting.Item receivable = null;
    com.poesys.accounting.dataloader.newaccounting.Item reimbursement = null;

    // Get the receivable item from year 0 (2016).
    for (com.poesys.accounting.dataloader.newaccounting.Transaction transaction : fiscalYears.get(0).getTransactions()) {
      com.poesys.accounting.dataloader.newaccounting.Item item =
        transaction.getItem(receivableAccount);
      if (item != null) {
        receivable = item;
        break;
      }
    }

    // Get the reimbursing item from year 1 (2017).
    for (com.poesys.accounting.dataloader.newaccounting.Transaction transaction : fiscalYears.get(1).getTransactions()) {
      com.poesys.accounting.dataloader.newaccounting.Item item =
        transaction.getItem(receivableAccount);
      if (item != null) {
        reimbursement = item;
        break;
      }
    }

    assertTrue("could not extract receivable item", receivable != null);
    assertTrue("could not extract reimbursement item", reimbursement != null);

    Set<com.poesys.accounting.dataloader.newaccounting.Item.Reimbursement> reimbursements =
      receivable.getReimbursements();
    assertTrue("no reimbursements for receivable",
               reimbursements != null && reimbursements.size() == 1);

    for (com.poesys.accounting.dataloader.newaccounting.Item.Reimbursement reimbursement1 : reimbursements) {
      assertTrue("wrong reimbursement item for receivable",
                 reimbursement.equals(reimbursement1.getReimbursingItem()));
      assertTrue("wrong receivable item for receivable",
                 receivable.equals(reimbursement1.getReceivable()));
      assertTrue("wrong reimbursed amount for receivable reimbursement",
                 reimbursement1.getReimbursedAmount().equals(100.00D));
      assertTrue("wrong allocated amount for receivable reimbursement",
                 reimbursement1.getAllocatedAmount().equals(0.00D));
    }

    reimbursements = reimbursement.getReimbursements();
    assertTrue("no reimbursements for reimbursement",
               reimbursements != null && reimbursements.size() == 1);

    for (com.poesys.accounting.dataloader.newaccounting.Item.Reimbursement reimbursement2 : reimbursements) {
      assertTrue("wrong reimbursement item for reimbursement",
                 reimbursement.equals(reimbursement2.getReimbursingItem()));
      assertTrue("wrong receivable item for reimbursement",
                 receivable.equals(reimbursement2.getReceivable()));
      assertTrue("wrong reimbursed amount for reimbursement",
                 reimbursement2.getReimbursedAmount().equals(100.00D));
      assertTrue("wrong allocated amount for reimbursement",
                 reimbursement2.getAllocatedAmount().equals(0.00D));
    }
  }
}
