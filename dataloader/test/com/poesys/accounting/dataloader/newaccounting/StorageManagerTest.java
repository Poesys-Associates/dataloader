/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


/**
 * 
 * @author Robert J. Muller
 */
public class StorageManagerTest {

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
                                                  Account.AccountType.ASSET,
                                                  true,
                                                  false,
                                                  CASH_GROUP);
  private final Account arAccount = new Account("Receivables",
                                                "Accounts Receivable",
                                                Account.AccountType.ASSET,
                                                true,
                                                false,
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
   * {@link com.poesys.accounting.dataloader.newaccounting.StorageManager#validate(java.util.List)}
   * . Test valid set of years.
   */
  @Test
  public void testValidateValidSingleYear() {
    IStorageManager manager = new StorageManager();
    FiscalYear year = createFiscalYear(2010, getNextId(null), true);
    List<FiscalYear> years = new ArrayList<FiscalYear>(1);
    years.add(year);
    assertTrue("single year not valid: " + year, manager.validate(years));
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

  /**
   * Create a fiscal year with a set of standard accounts and standard
   * transactions against the various kinds of accounts.
   * 
   * @param year the year number
   * @param id the first integer id for transactions
   * @param valid whether the year has valid balances
   * @return the fiscal year with nested accounts and transaction
   */
  private FiscalYear createFiscalYear(Integer year, BigInteger id, boolean valid) {
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

    // Income-AR
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
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.StorageManager#validate(java.util.List)}
   * . Test valid set of years.
   */
  @Test
  public void testValidateValidMultipleYears() {
    IStorageManager manager = new StorageManager();
    FiscalYear year1 = createFiscalYear(2010, getNextId(null), true);
    FiscalYear year2 = createFiscalYear(2011, getNextId(year1), true);
    FiscalYear year3 = createFiscalYear(2012, getNextId(year2), true);
    List<FiscalYear> years = new ArrayList<FiscalYear>(3);
    years.add(year1);
    years.add(year2);
    years.add(year3);
    assertTrue("multiple years not valid", manager.validate(years));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.StorageManager#validate(java.util.List)}
   * . Test invalid set of years (not balanced).
   */
  @Test
  public void testValidateInvalid() {
    IStorageManager manager = new StorageManager();
    FiscalYear year = createFiscalYear(2010, getNextId(null), false);
    List<FiscalYear> years = new ArrayList<FiscalYear>(1);
    years.add(year);
    assertTrue("invalid year valid: " + year, !manager.validate(years));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.StorageManager#validate(java.util.List)}
   * . Test valid set of years.
   */
  @Test
  public void testValidateNullYears() {
    IStorageManager manager = new StorageManager();
    try {
      manager.validate(null);
      fail("null year to validate did not throw exception");
    } catch (RuntimeException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.StorageManager#validate(java.util.List)}
   * . Test valid set of years.
   */
  @Test
  public void testValidateZeroYears() {
    IStorageManager manager = new StorageManager();
    try {
      manager.validate(new ArrayList<FiscalYear>());
      fail("null year to validate did not throw exception");
    } catch (RuntimeException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.StorageManager#store(java.lang.String, java.util.List, com.poesys.accounting.dataloader.newaccounting.IDataAccessService)}
   * .
   */
  @Test
  public void testStore() {
    IStorageManager manager = new StorageManager();
    try {
      FiscalYear year = createFiscalYear(2010, getNextId(null), true);
      List<FiscalYear> years = new ArrayList<FiscalYear>(1);
      years.add(year);
      manager.store("Poesys Associates",
                    years,
                    new DoNothingDataAccessService());
    } catch (Throwable e) {
      fail("Exception calling store()");
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.StorageManager#store(java.lang.String, java.util.List, com.poesys.accounting.dataloader.newaccounting.IDataAccessService)}
   * . Tests storage runtime exception handling.
   */
  @Test
  public void testStoreRuntimeException() {
    IStorageManager manager = new StorageManager();
    try {
      FiscalYear year = createFiscalYear(2010, getNextId(null), true);
      List<FiscalYear> years = new ArrayList<FiscalYear>(1);
      years.add(year);
      manager.store("Poesys Associates",
                    years,
                    new RuntimeExceptionDataAccessService());
      fail("no exception calling store()");
    } catch (Throwable e) {
      // success, check message
      assertTrue("wrong message from store runtime exception: "
                     + e.getMessage(),
                 "exception in fiscal year storage operation".equals(e.getMessage()));
      assertTrue("no causing exception for store runtime exception",
                 e.getCause() != null);
    }
  }
}
