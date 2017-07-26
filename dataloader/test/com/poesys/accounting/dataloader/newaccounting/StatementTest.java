/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.poesys.accounting.dataloader.newaccounting.Account.AccountType;
import com.poesys.accounting.dataloader.newaccounting.Statement.StatementType;
import com.poesys.db.InvalidParametersException;


/**
 * CUT: Statement and Rollup
 * 
 * @author Robert J. Muller
 */
public class StatementTest {
  /** Logger for this class */
  private static final Logger logger = Logger.getLogger(StatementTest.class);

  // statement constants
  private static final String BALANCE_SHEET_NAME = "Balance Sheet";
  private static final String INCOME_STATEMENT_NAME = "Income Statement";

  // Date for all date values
  private static final Timestamp DATE =
    Timestamp.valueOf("2017-05-01 00:00:00");

  // flag value constants
  private static final Boolean CHECKED = Boolean.TRUE;
  private static final Boolean NOT_CHECKED = Boolean.FALSE;
  private static final Boolean BALANCE = Boolean.TRUE;
  private static final Boolean NOT_BALANCE = Boolean.FALSE;
  private static final Boolean DEBIT = Boolean.TRUE;
  private static final Boolean CREDIT = Boolean.FALSE;

  // account constants
  private static final String DESCRIPTION = "description";
  private static final String INCOME_ACCOUNT_NAME = "Salary";
  private static final String CHECKING_ACCOUNT_NAME = "Checking";
  private static final String EXPENSE_ACCOUNT_NAME = "Essential Expense";
  private static final String LIABILITY_ACCOUNT_NAME = "Credit Card";
  private static final String EQUITY_ACCOUNT_NAME = "Shared Capital";
  private static final Boolean DEBIT_DEFAULT = Boolean.TRUE;
  private static final Boolean CREDIT_DEFAULT = Boolean.FALSE;
  private static final Boolean NOT_RECEIVABLE = Boolean.FALSE;
  private static final String CASH_GROUP_NAME = "Cash";
  private static final AccountGroup CASH_GROUP =
    new AccountGroup(CASH_GROUP_NAME);
  private static final String INCOME_GROUP_NAME = "Earned Income";
  private static final AccountGroup INCOME_GROUP =
    new AccountGroup(INCOME_GROUP_NAME);
  private static final String EXPENSE_GROUP_NAME = "Household Expenses";
  private static final AccountGroup EXPENSE_GROUP =
    new AccountGroup(EXPENSE_GROUP_NAME);
  private static final String LIABILITY_GROUP_NAME = "Credit Accounts";
  private static final AccountGroup LIABILITY_GROUP =
    new AccountGroup(LIABILITY_GROUP_NAME);
  private static final String EQUITY_GROUP_NAME = "Personal Capital";
  private static final AccountGroup EQUITY_GROUP =
    new AccountGroup(EQUITY_GROUP_NAME);

  // Setup fiscal year and account objects for use in transactions.
  private static final Integer YEAR = 2017;

  // Setup accounts for each main account type.
  private final Account incomeAccount = new Account(INCOME_ACCOUNT_NAME,
                                                    DESCRIPTION,
                                                    AccountType.INCOME,
                                                    CREDIT_DEFAULT,
                                                    NOT_RECEIVABLE,
                                                    INCOME_GROUP);
  private final Account checkingAccount = new Account(CHECKING_ACCOUNT_NAME,
                                                      DESCRIPTION,
                                                      AccountType.ASSET,
                                                      DEBIT_DEFAULT,
                                                      NOT_RECEIVABLE,
                                                      CASH_GROUP);
  private final Account expenseAccount = new Account(EXPENSE_ACCOUNT_NAME,
                                                     DESCRIPTION,
                                                     AccountType.EXPENSE,
                                                     DEBIT_DEFAULT,
                                                     NOT_RECEIVABLE,
                                                     EXPENSE_GROUP);

  private final Account liabilityAccount = new Account(LIABILITY_ACCOUNT_NAME,
                                                       DESCRIPTION,
                                                       AccountType.LIABILITY,
                                                       !DEBIT_DEFAULT,
                                                       NOT_RECEIVABLE,
                                                       LIABILITY_GROUP);

  private final Account equityAccount = new Account(EQUITY_ACCOUNT_NAME,
                                                    DESCRIPTION,
                                                    AccountType.EQUITY,
                                                    !DEBIT_DEFAULT,
                                                    NOT_RECEIVABLE,
                                                    EQUITY_GROUP);

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Statement#Statement(com.poesys.accounting.dataloader.newaccounting.FiscalYear, java.lang.String, StatementType)}
   * . Tests constructor and year, name, and type getters
   */
  @Test
  public void testStatement() {
    FiscalYear year = new FiscalYear(YEAR);
    Statement statement =
      new Statement(year, BALANCE_SHEET_NAME, StatementType.BALANCE_SHEET);
    assertTrue(statement != null);
    assertTrue("Wrong year " + statement.getYear(),
               statement.getYear().getYear() == YEAR);
    assertTrue("Wrong name " + statement.getYear(),
               statement.getName().equals(BALANCE_SHEET_NAME));
    assertTrue("Wrong type " + statement.getYear(),
               statement.getType().equals(StatementType.BALANCE_SHEET));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Statement#Statement(com.poesys.accounting.dataloader.newaccounting.FiscalYear, java.lang.String, StatementType)}
   * . Tests null value for year, name, and type
   */
  @Test
  public void testInvalidStatementWithNulls() {
    FiscalYear year = new FiscalYear(YEAR);
    try {
      new Statement(null, BALANCE_SHEET_NAME, StatementType.BALANCE_SHEET);
      fail("null year supplied to statement but no exception");
    } catch (InvalidParametersException e) {
      // success
    }
    try {
      new Statement(year, null, StatementType.BALANCE_SHEET);
      fail("null name supplied to statement but no exception");
    } catch (InvalidParametersException e) {
      // success
    }
    try {
      new Statement(year, BALANCE_SHEET_NAME, null);
      fail("null type supplied to statement but no exception");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Create a Statement object against a specific account.
   * 
   * @param year the fiscal year for the statement
   * @param account the account
   * @return a new Statement object
   */
  private Statement createStatementWithRollup(FiscalYear year, Account account) {
    // Add account to fiscal year.
    year.addAccount(account);
    Statement statement =
      new Statement(year, BALANCE_SHEET_NAME, StatementType.BALANCE_SHEET);
    assertTrue("Did not construct statement", statement != null);
    return statement;
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Statement#getRollups()}
   * .
   */
  @Test
  public void testGetRollup() {
    FiscalYear year = new FiscalYear(YEAR);
    Statement statement = createStatementWithRollup(year, incomeAccount);
    Rollup addedRollup = statement.getRollups().get(incomeAccount);
    assertTrue("Income account rollup not found", addedRollup != null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Statement#getBalance()}
   * . Tests balance sheet balance when there is just one year.
   */
  @Test
  public void testGetBalanceForBalanceSheetSingleYear() {
    FiscalYear year = new FiscalYear(YEAR);
    createFiscalYearForBalance(year, getNextId(null));
    Statement statement =
      new Statement(year, BALANCE_SHEET_NAME, StatementType.BALANCE_SHEET);
    BigDecimal balance = statement.getBalance();
    for (Rollup rollup : statement.getRollups().values()) {
      switch (rollup.getAccount().getAccountType()) {
      case ASSET:
      case LIABILITY:
      case EQUITY:
        logger.info(rollup.getAccount().getName() + " = " + rollup.getTotal());
        break;
      default:
        // Don't print.
      }
    }
    logger.info("Balance: " + balance);
    BigDecimal checkBalance = new BigDecimal("25.00").setScale(2);
    assertTrue("Balance is not $25 (credit) for Balance Sheet: " + balance,
               balance.compareTo(checkBalance) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Statement#getBalance()}
   * . Tests balance sheet balance when there are multiple years set up sharing
   * the accounts that list the transaction items.
   */
  @Test
  public void testGetBalanceForBalanceSheetMultipleYears() {
    int yearNumber = YEAR;
    FiscalYear year1 = new FiscalYear(yearNumber);
    createFiscalYearForBalance(year1, getNextId(null));
    yearNumber++;
    FiscalYear year2 = new FiscalYear(yearNumber);
    createFiscalYearForBalance(year2, getNextId(year1));
    yearNumber++;
    FiscalYear year3 = new FiscalYear(yearNumber);
    createFiscalYearForBalance(year3, getNextId(year2));
    Statement statement =
      new Statement(year2, BALANCE_SHEET_NAME, StatementType.BALANCE_SHEET);
    BigDecimal balance = statement.getBalance();
    for (Rollup rollup : statement.getRollups().values()) {
      switch (rollup.getAccount().getAccountType()) {
      case ASSET:
      case LIABILITY:
      case EQUITY:
        logger.info(rollup.getAccount().getName() + " = " + rollup.getTotal());
        break;
      default:
        // Don't print.
      }
    }
    logger.info("Balance: " + balance);
    BigDecimal checkBalance = new BigDecimal("25.00").setScale(2);
    assertTrue("Balance is not $25 (credit) for Balance Sheet: " + balance,
               balance.compareTo(checkBalance) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Statement#getBalance()}
   * . Tests income statement balance.
   */
  @Test
  public void testGetBalanceForIncomeStatement() {
    FiscalYear year = new FiscalYear(YEAR);
    createFiscalYearForBalance(year, getNextId(null));
    Statement statement =
      new Statement(year, INCOME_STATEMENT_NAME, StatementType.INCOME_STATEMENT);
    BigDecimal balance = statement.getBalance();
    for (Rollup rollup : statement.getRollups().values()) {
      switch (rollup.getAccount().getAccountType()) {
      case ASSET:
      case LIABILITY:
      case EQUITY:
        logger.info(rollup.getAccount().getName() + " = " + rollup.getTotal());
        break;
      default:
        // Don't print.
      }
    }
    logger.info("Balance: " + balance);
    BigDecimal checkBalance = new BigDecimal("-25.00").setScale(2);
    assertTrue("Balance is not $25 (debit) for Balance Sheet: " + balance,
               balance.compareTo(checkBalance) == 0);
  }

  /**
   * Get the next id to use given the transactions in a year. If the year is
   * null, return the default initial id, ONE.
   * 
   * @param year optional fisal year
   * @return the next id
   */
  private BigInteger getNextId(FiscalYear year) {
    BigInteger nextId = BigInteger.ZERO;
    if (year != null) {
      for (Transaction transaction : year.getTransactions()) {
        BigInteger id = transaction.getId();
        if (id.compareTo(nextId) > 0) {
          nextId = id;
        }
      }
    }
    nextId = nextId.add(BigInteger.ONE);
    logger.debug("Next transaction id = " + nextId);
    return nextId;
  }

  /**
   * Create a set of transactions in the fiscal year that can be used to get a
   * balance for a particular kind of statement. The transactions are against
   * the full range of accounts so the set can form the basis of either a
   * balance sheet or an income statement. There are also initial balances for
   * balance sheet accounts (asset, liability, equity).
   * 
   * @param year the fiscal year in which to create transactions and accounts
   * @param nextId the next transaction id to use
   */
  private void createFiscalYearForBalance(FiscalYear year, BigInteger nextId) {
    // Add the five accounts to the fiscal year.
    year.addAccount(checkingAccount);
    year.addAccount(liabilityAccount);
    year.addAccount(equityAccount);
    year.addAccount(incomeAccount);
    year.addAccount(expenseAccount);

    Transaction transaction = null;

    // Get the transaction date to use for all transactions.
    Timestamp date = year.getStart();

    // Create balance transactions for asset, liability, and equity accounts.
    // Sum of debits and credits in balance transaction items should be zero.
    transaction =
      new Transaction(nextId, DESCRIPTION, date, NOT_CHECKED, BALANCE);
    transaction.addItem(100.00D, checkingAccount, DEBIT, CHECKED);
    year.addTransaction(transaction);

    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId, DESCRIPTION, date, NOT_CHECKED, BALANCE);
    transaction.addItem(20.00D, liabilityAccount, CREDIT, CHECKED);
    year.addTransaction(transaction);

    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId, DESCRIPTION, date, NOT_CHECKED, BALANCE);
    transaction.addItem(80.00D, equityAccount, CREDIT, CHECKED);
    year.addTransaction(transaction);

    // Create two-item transactions against all five accounts.

    // Income transaction
    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId, DESCRIPTION, date, NOT_CHECKED, NOT_BALANCE);
    transaction.addItem(100.00D, checkingAccount, DEBIT, CHECKED);
    transaction.addItem(100.00D, incomeAccount, CREDIT, CHECKED);
    assertTrue("income transaction is not valid", transaction.isValid());
    year.addTransaction(transaction);

    // Expense transaction
    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId, DESCRIPTION, date, NOT_CHECKED, NOT_BALANCE);
    transaction.addItem(50.00D, expenseAccount, DEBIT, CHECKED);
    transaction.addItem(50.00D, checkingAccount, CREDIT, CHECKED);
    assertTrue("expense transaction is not valid", transaction.isValid());
    year.addTransaction(transaction);

    // Liability transaction (credit card transaction)
    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId, DESCRIPTION, date, NOT_CHECKED, NOT_BALANCE);
    transaction.addItem(75.00D, expenseAccount, DEBIT, CHECKED);
    transaction.addItem(75.00D, liabilityAccount, CREDIT, CHECKED);
    assertTrue("liability transaction is not valid", transaction.isValid());
    year.addTransaction(transaction);

    // Equity transaction (capital infusion)
    nextId = nextId.add(BigInteger.ONE);
    transaction =
      new Transaction(nextId, DESCRIPTION, date, NOT_CHECKED, NOT_BALANCE);
    transaction.addItem(2000.00D, checkingAccount, DEBIT, CHECKED);
    transaction.addItem(2000.00D, equityAccount, CREDIT, CHECKED);
    assertTrue("equity transaction is not valid", transaction.isValid());
    year.addTransaction(transaction);
  }

  /**
   * Test rollup hash code equality
   */
  @Test
  public void testRollupHashCodeEquality() {
    FiscalYear year = new FiscalYear(YEAR);
    Statement statement1 = createStatementWithRollup(year, incomeAccount);
    Rollup rollup1 = statement1.getRollups().get(incomeAccount);
    Rollup rollup2 = statement1.getRollups().get(incomeAccount);
    assertTrue("Same rollup, but has codes are different",
               rollup1.hashCode() == rollup2.hashCode());
  }

  /**
   * Test rollup equals() equality.
   */
  @Test
  public void testRollupEqualsEquality() {
    FiscalYear year = new FiscalYear(YEAR);
    Statement statement1 = createStatementWithRollup(year, incomeAccount);
    Rollup rollup1 = statement1.getRollups().get(incomeAccount);
    Rollup rollup2 = statement1.getRollups().get(incomeAccount);
    assertTrue("Same rollup but equals() is false", rollup1.equals(rollup2));
  }

  /**
   * Test rollup hash code inequality
   */
  @Test
  public void testRollupHashCodeInequality() {
    FiscalYear year1 = new FiscalYear(YEAR);
    FiscalYear year2 = new FiscalYear(YEAR);
    Statement statement1 = createStatementWithRollup(year1, incomeAccount);
    Statement statement2 = createStatementWithRollup(year2, incomeAccount);
    Rollup rollup1 = statement1.getRollups().get(incomeAccount);
    Rollup rollup2 = statement2.getRollups().get(incomeAccount);
    assertTrue("Different rollups but hash codes are equal",
               rollup1.hashCode() != rollup2.hashCode());
  }

  /**
   * Test rollup equals() inequality.
   */
  @Test
  public void testRollupEqualsInequality() {
    FiscalYear year1 = new FiscalYear(YEAR);
    FiscalYear year2 = new FiscalYear(YEAR);
    Statement statement1 = createStatementWithRollup(year1, incomeAccount);
    Statement statement2 = createStatementWithRollup(year2, incomeAccount);
    Rollup rollup1 = statement1.getRollups().get(incomeAccount);
    Rollup rollup2 = statement2.getRollups().get(incomeAccount);
    assertTrue("Different rollups but equals() is true",
               !rollup1.equals(rollup2));
  }

  /**
   * Test Statement.toString().
   */
  @Test
  public void testStatementToString() {
    FiscalYear year = new FiscalYear(YEAR);
    Statement statement = createStatementWithRollup(year, incomeAccount);
    assertTrue("statement string rep failed: " + statement,
               "Statement [year=FiscalYear [year=2017, start=2017-01-01 00:00:00.0, end=2017-12-31 23:59:59.0], name=Balance Sheet, type=BALANCE_SHEET]".equals(statement.toString()));
  }

  /**
   * Test Statement.toData().
   */
  @Test
  public void testStatementToData() {
    FiscalYear year = new FiscalYear(YEAR);
    createFiscalYearForBalance(year, getNextId(null));
    Statement statement =
      new Statement(year, BALANCE_SHEET_NAME, StatementType.BALANCE_SHEET);
    String data = statement.toData();
    assertTrue("data set incorrect for statement: " + data,
               "Liability\tCredit Accounts\tCredit Card\t95.00\nEquity\tPersonal Capital\tShared Capital\t2080.00\nAsset\tCash\tChecking\t-2150.00".equals(data));
  }

  /**
   * Test Statement.toDetailData().
   */
  @Test
  public void testStatementToDetailData() {
    FiscalYear year = new FiscalYear(YEAR);
    createFiscalYearForBalance(year, getNextId(null));
    Statement statement =
      new Statement(year, BALANCE_SHEET_NAME, StatementType.BALANCE_SHEET);
    String data = statement.toDetailData();
    assertTrue("detail data set incorrect for statement: " + data,
               "Liability\tCredit Accounts\tCredit Card\t6\t01-Jan-17\t75.00\nLiability\tCredit Accounts\tCredit Card\t2\t01-Jan-17\t20.00\nEquity\tPersonal Capital\tShared Capital\t7\t01-Jan-17\t2000.00\nEquity\tPersonal Capital\tShared Capital\t3\t01-Jan-17\t80.00\nAsset\tCash\tChecking\t5\t01-Jan-17\t50.00\nAsset\tCash\tChecking\t7\t01-Jan-17\t-2000.00\nAsset\tCash\tChecking\t1\t01-Jan-17\t-100.00\nAsset\tCash\tChecking\t4\t01-Jan-17\t-100.00".equals(data));
    
    
  }

  /**
   * Test Rollup.toString().
   */
  @Test
  public void testRollupToString() {
    FiscalYear year = new FiscalYear(YEAR);
    Statement statement = createStatementWithRollup(year, incomeAccount);
    Rollup rollup = statement.getRollups().get(incomeAccount);
    assertTrue("rollup string rep failed: " + rollup,
               "Rollup [statement=Statement [year=FiscalYear [year=2017, start=2017-01-01 00:00:00.0, end=2017-12-31 23:59:59.0], name=Balance Sheet, type=BALANCE_SHEET], account=Account [name=Salary, description=description, accountType=Income, debitDefault=false, receivable=false, group=AccountGroup [name=Earned Income]], total=0.00]".equals(rollup.toString()));
  }

  /**
   * Test Rollup.toData().
   */
  @Test
  public void testRollupToData() {
    FiscalYear year = new FiscalYear(YEAR);
    Statement statement = createStatementWithRollup(year, incomeAccount);
    Rollup rollup = statement.getRollups().get(incomeAccount);
    assertTrue("rollup data string rep failed: " + rollup.toData(),
               rollup.toData().equals("Income\tEarned Income\tSalary\t0.00"));
  }

  /**
   * Create a transaction with two items, a credit item and a debit item, with
   * the same dollar amount.
   * 
   * @param id the unique identifier for the transaction
   * @param amount the dollar amount of the transaction
   * @param debitAccount the account for the debit item
   * @param creditAccount the account for the credit item
   * @return the transaction with two items
   */
  private Transaction createTransactionWithItems(BigInteger id, Double amount,
                                                 Account debitAccount,
                                                 Account creditAccount) {
    Transaction transaction =
      new Transaction(id, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    assertTrue("transaction constructor failed", transaction != null);
    Item debitItem =
      transaction.addItem(amount, debitAccount, DEBIT, NOT_CHECKED);

    assertTrue("debit item constructor failed", debitItem != null);
    Item creditItem =
      transaction.addItem(amount, creditAccount, CREDIT, NOT_CHECKED);
    assertTrue("credit item constructor failed", creditItem != null);
    return transaction;
  }

  /**
   * Test Rollup.getTotal(). Test scenario with multiple items against the
   * account, one debit and one credit, and at least one item not against the
   * account.
   */
  @Test
  public void testRollupGetTotalMultipleItems() {
    FiscalYear year = new FiscalYear(YEAR);
    Statement statement = createStatementWithRollup(year, checkingAccount);
    Rollup rollup = statement.getRollups().get(checkingAccount);
    // Get $100 into checking account as income.
    Transaction transaction1 =
      createTransactionWithItems(new BigInteger("1"),
                                 100.00D,
                                 checkingAccount,
                                 incomeAccount);
    // Take $50 from checking account and expense.
    Transaction transaction2 =
      createTransactionWithItems(new BigInteger("2"),
                                 50.00D,
                                 expenseAccount,
                                 checkingAccount);
    year.addTransaction(transaction1);
    year.addTransaction(transaction2);
    BigDecimal total = new BigDecimal(-50.00).setScale(2);
    assertTrue("rollup total for checking is wrong, should be " + total + ": "
               + rollup.getTotal(), rollup.getTotal().compareTo(total) == 0);
  }

  /**
   * Test Rollup.getTotal(). Test scenario with single item against the account,
   * one debit.
   */
  @Test
  public void testRollupGetTotalSingleItem() {
    FiscalYear year = new FiscalYear(YEAR);
    Statement statement = createStatementWithRollup(year, checkingAccount);
    Rollup rollup = statement.getRollups().get(checkingAccount);
    // Get $100 into checking account as income.
    Transaction transaction1 =
      createTransactionWithItems(new BigInteger("1"),
                                 100.00D,
                                 checkingAccount,
                                 incomeAccount);
    year.addTransaction(transaction1);
    BigDecimal total = new BigDecimal(-100.00).setScale(2);
    assertTrue("rollup total for checking is wrong, should be " + total + ": "
               + rollup.getTotal(), rollup.getTotal().compareTo(total) == 0);
  }

  /**
   * Test Rollup.getTotal() with no transactions from FiscalYear.
   */
  @Test
  public void testRollupGetTotalNoTransactions() {
    FiscalYear year = new FiscalYear(YEAR);
    Statement statement = createStatementWithRollup(year, checkingAccount);
    Rollup rollup = statement.getRollups().get(checkingAccount);
    BigDecimal checkTotal = BigDecimal.ZERO.setScale(2);
    assertTrue("rollup total for checking is wrong, should be " + checkTotal
                   + ": " + rollup.getTotal(),
               rollup.getTotal().compareTo(checkTotal) == 0);
  }

  /**
   * Test Rollup.getTotal() with no items against account to roll up.
   */
  @Test
  public void testRollupGetTotalNoAccountItems() {
    FiscalYear year = new FiscalYear(YEAR);
    Statement statement = createStatementWithRollup(year, checkingAccount);
    Rollup rollup = statement.getRollups().get(checkingAccount);
    Transaction transaction =
      new Transaction(new BigInteger("1"),
                      DESCRIPTION,
                      DATE,
                      NOT_CHECKED,
                      NOT_BALANCE);
    assertTrue("transaction constructor failed", transaction != null);
    // No items added
    year.addTransaction(transaction);
    BigDecimal checkTotal = BigDecimal.ZERO.setScale(2);
    assertTrue("rollup total for checking is wrong, should be " + checkTotal
                   + ": " + rollup.getTotal(),
               rollup.getTotal().compareTo(checkTotal) == 0);
  }
}
