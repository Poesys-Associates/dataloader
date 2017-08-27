/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Set;

import org.junit.Test;

import com.poesys.accounting.dataloader.newaccounting.Account.AccountType;
import com.poesys.db.InvalidParametersException;


/**
 * CUT: FiscalYear
 * 
 * @author Robert J. Muller
 */
public class FiscalYearTest {
  private static final Integer YEAR = 2017;
  private static final Integer PRIOR_YEAR = 2016;
  private static final Integer LATER_YEAR = 2018;

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#FiscalYear(java.lang.Integer)}
   * . Tests constructor and getters.
   */
  @Test
  public void testFiscalYear() {
    FiscalYear year = new FiscalYear(YEAR);
    assertTrue("no fiscal year created", year != null);
    assertTrue("wrong year", year.getYear() == YEAR);
    assertTrue("no start date", year.getStart() != null);
    assertTrue("no end date", year.getEnd() != null);
    Timestamp start = Timestamp.valueOf(YEAR + "-01-01 00:00:00");
    Timestamp end = Timestamp.valueOf(YEAR + "-12-31 23:59:59");
    assertTrue("wrong start date", start.equals(year.getStart()));
    assertTrue("wrong end date", end.equals(year.getEnd()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#hashCode()}
   * .
   */
  @Test
  public void testHashCodeEquality() {
    FiscalYear year1 = new FiscalYear(YEAR);
    FiscalYear year2 = new FiscalYear(YEAR);
    assertTrue("same year but different hash code",
               year1.hashCode() == year2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#hashCode()}
   * .
   */
  @Test
  public void testHashCodeInequality() {
    FiscalYear year1 = new FiscalYear(YEAR);
    FiscalYear year2 = new FiscalYear(PRIOR_YEAR);
    assertTrue("different year but same hash code",
               year1.hashCode() != year2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#equals(Object obj)}
   * .
   */
  @Test
  public void testEqualsEquality() {
    FiscalYear year1 = new FiscalYear(YEAR);
    FiscalYear year2 = new FiscalYear(YEAR);
    assertTrue("same year but equals not true", year1.equals(year2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#equals(Object obj)}
   * .
   */
  @Test
  public void testEqualsInequality() {
    FiscalYear year1 = new FiscalYear(YEAR);
    FiscalYear year2 = new FiscalYear(PRIOR_YEAR);
    assertTrue("different year but equals true", !year1.equals(year2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#getTransactions()}
   * . Tests addTransaction and getTransactions.
   */
  @Test
  public void testGetTransactions() {
    FiscalYear year = new FiscalYear(YEAR);
    assertTrue("no transactions set", year.getTransactions() != null);
    Timestamp date = Timestamp.valueOf("2017-01-01 00:00:00");
    Transaction transaction =
      new Transaction(new BigInteger("1"), "description", date, false, false);
    year.addTransaction(transaction);
    Set<Transaction> set = year.getTransactions();
    boolean found = false;
    for (Transaction trans : set) {
      if (trans.equals(transaction)) {
        found = true;
      }
    }
    assertTrue("Did not find added transaction", found);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#isIn(java.sql.Timestamp)}
   * .
   */
  @Test
  public void testIsIn() {
    FiscalYear year = new FiscalYear(YEAR);
    Timestamp date = Timestamp.valueOf("2017-05-01 00:00:00");
    assertTrue("date in year but isIn is false", year.isIn(date));
    date = Timestamp.valueOf("2016-05-01 00:00:00");
    assertTrue("date not in year but isIn is true", !year.isIn(date));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#isInYearOrPriorYear(java.sql.Timestamp)}
   * .
   */
  @Test
  public void testIsInYearOrPriorYearSameYear() {
    FiscalYear year = new FiscalYear(YEAR);
    Timestamp date = Timestamp.valueOf(YEAR + "-05-01 00:00:00");
    assertTrue("date in year but isInYearOrPriorYear is false", year.isInYearOrPriorYear(date));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#isInYearOrPriorYear(java.sql.Timestamp)}
   * .
   */
  @Test
  public void testIsInYearOrPriorYearPriorYear() {
    FiscalYear year = new FiscalYear(YEAR);
    Timestamp date = Timestamp.valueOf(PRIOR_YEAR + "-05-01 00:00:00");
    assertTrue("date in prior year but isInYearOrPriorYear is false", year.isInYearOrPriorYear(date));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#isInYearOrPriorYear(java.sql.Timestamp)}
   * .
   */
  @Test
  public void testIsInYearOrPriorYearLaterYear() {
    FiscalYear year = new FiscalYear(YEAR);
    Timestamp date = Timestamp.valueOf(LATER_YEAR + "-05-01 00:00:00");
    assertTrue("date in later year but isInYearOrPriorYear is true", !year.isInYearOrPriorYear(date));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#toString()}
   * .
   */
  @Test
  public void testToString() {
    FiscalYear year = new FiscalYear(YEAR);
    assertTrue("toString not correct: " + year.toString(),
               "FiscalYear [year=2017, start=2017-01-01 00:00:00.0, end=2017-12-31 23:59:59.0]".equals(year.toString()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#addAccount(com.poesys.accounting.dataloader.newaccounting.Account)}
   * . Tests addAccount() and getAccount().
   */
  @Test
  public void testAddAccount() {
    FiscalYear year = new FiscalYear(YEAR);
    assertTrue("no transactions set", year.getTransactions() != null);
    AccountGroup group = new AccountGroup("Income");
    Account account =
      new Account("Income", "Income", AccountType.INCOME, true, false, group);
    year.addAccount(account);
    Set<Account> set = year.getAccounts();
    assertTrue("no set of accounts", set != null);
    boolean found = false;
    for (Account acct : set) {
      if (acct.equals(account)) {
        found = true;
      }
    }
    assertTrue("account not found", found);

    // Verify that account has fiscal year in list of fiscal years.
    found = false;
    for (FiscalYear accountYear : account.getYears()) {
      if (year.equals(accountYear)) {
        found = true;
        break;
      }
    }
    assertTrue("fiscal year not in account year list: " + year, found);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#addAccount(com.poesys.accounting.dataloader.newaccounting.Account)}
   * . Tests addAccount() with null account parameter.
   */
  @Test
  public void testAddAccountNullAccount() {
    FiscalYear year = new FiscalYear(YEAR);
    assertTrue("no transactions set", year.getTransactions() != null);
    try {
      year.addAccount(null);
      fail("did not throw invalid parameters exception with null account added");
    } catch (InvalidParametersException e) {
      // success
    }
 }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#addAccount(com.poesys.accounting.dataloader.newaccounting.Account)}
   * . Tests addAccount() when account is already in account year list.
   */
  @Test
  public void testAddAccountAlreadyInList() {
    FiscalYear year = new FiscalYear(YEAR);
    assertTrue("no transactions set", year.getTransactions() != null);
    AccountGroup group = new AccountGroup("Income");
    Account account =
      new Account("Income", "Income", AccountType.INCOME, true, false, group);
    year.addAccount(account);

    // now add the account again and check year list to make sure there's only
    // one instance of the account in the list.
    year.addAccount(account);
    int found = 0;
    for (FiscalYear accountYear : account.getYears()) {
      if (year.equals(accountYear)) {
        found++;
      }
    }
    assertTrue("fiscal year count not correct for " + year + ": " + found,
               found == 1);

  }

}
