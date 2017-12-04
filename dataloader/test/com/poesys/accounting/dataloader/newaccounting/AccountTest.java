/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.poesys.accounting.dataloader.newaccounting.AccountType;
import com.poesys.db.InvalidParametersException;


/**
 * CUT: Account
 * 
 * @author Robert J. Muller
 */
public class AccountTest {

  private static final String NAME = "Cash";
  private static final String OTHER_NAME = "Accounts Receivable";
  private static final String DESCRIPTION = "A description";
  private static final AccountType ACCOUNT_TYPE = AccountType.ASSETS;
  private static final Boolean DEBIT_DEFAULT = Boolean.TRUE;
  private static final Boolean RECEIVABLE = Boolean.FALSE;
  private static final String GROUP_NAME = "Cash";
  private static final FiscalYear YEAR1 = new FiscalYear(2010);
  private static final FiscalYear YEAR2 = new FiscalYear(2011);
  private static final FiscalYear YEAR3 = new FiscalYear(2012);
  private static final String STRING_REP = "Account [name=" + NAME
                                           + ", description=" + DESCRIPTION
                                            + ", debitDefault=" + DEBIT_DEFAULT
                                           + ", receivable=" + RECEIVABLE
                                           + ", years=" + "[]" + "]";

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean)}
   * . Tests required name field in constructor.
   */
  public void testAccountRequiredName() {
    try {
      new Account(null, DESCRIPTION, ACCOUNT_TYPE, DEBIT_DEFAULT, RECEIVABLE);
      fail("No exception on required name field");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean)}
   * . Tests required description field in constructor.
   */
  public void testAccountRequiredDescription() {
    try {
      new Account(NAME, null, ACCOUNT_TYPE, DEBIT_DEFAULT, RECEIVABLE);
      fail("No exception on required name field");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean)}
   * . Tests required account type field in constructor.
   */
  public void testAccountRequiredAccountType() {
    try {
      new Account(NAME, DESCRIPTION, null, DEBIT_DEFAULT, RECEIVABLE);
      fail("No exception on required name field");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean)}
   * . Tests required debit default field in constructor.
   */
  public void testAccountRequiredDebitDefault() {
    try {
      new Account(NAME, DESCRIPTION, ACCOUNT_TYPE, null, RECEIVABLE);
      fail("No exception on required name field");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Shared test method for constructor and getters, varying the asset type.
   * Also tests addYear().
   * 
   * @param type the asset type to test
   */
  private void testAccount(AccountType type) {
    Account account =
      new Account(NAME, DESCRIPTION, type, DEBIT_DEFAULT, RECEIVABLE);
    AccountGroup group = new AccountGroup(GROUP_NAME);
    FiscalYearAccount link1 =
      new FiscalYearAccount(YEAR1, ACCOUNT_TYPE, group, 1, account, 1);
    FiscalYearAccount link2 =
      new FiscalYearAccount(YEAR2, ACCOUNT_TYPE, group, 1, account, 1);
    FiscalYearAccount link3 =
      new FiscalYearAccount(YEAR3, ACCOUNT_TYPE, group, 1, account, 1);
    account.addYear(link1);
    account.addYear(link2);
    account.addYear(link3);
    assertTrue("Account constructor failed", account != null);
    assertTrue("name getter failed", NAME.equals(account.getName()));
    assertTrue("description getter failed",
               DESCRIPTION.equals(account.getDescription()));
    assertTrue("debit-default flag getter failed",
               DEBIT_DEFAULT.equals(account.isDebitDefault()));
    assertTrue("receivable flag getter failed",
               RECEIVABLE.equals(account.isReceivable()));
    assertTrue("fiscal-year-account getter failed",
               account.getYears().size() > 0);
    boolean foundYear1 = false;
    boolean foundYear2 = false;
    boolean foundYear3 = false;
    for (FiscalYearAccount fya : account.getYears()) {
      if (fya.getFiscalYear().equals(YEAR1)) {
        foundYear1 = true;
      } else if (fya.getFiscalYear().equals(YEAR2)) {
        foundYear2 = true;
      } else if (fya.getFiscalYear().equals(YEAR3)) {
        foundYear3 = true;
      } else {
        fail("Found invalid linked fiscal year: " + fya.getFiscalYear());
      }
    }
    assertTrue("account year list does not contain year " + YEAR1, foundYear1);
    assertTrue("account year list does not contain year " + YEAR2, foundYear2);
    assertTrue("account year list does not contain year " + YEAR3, foundYear3);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean)}
   * . Tests Asset type.
   */
  @Test
  public void testAssetAccount() {
    testAccount(AccountType.ASSETS);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean)}
   * . Tests Liability type.
   */
  @Test
  public void testLiabilityAccount() {
    testAccount(AccountType.LIABILITIES);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean)}
   * . Tests Equity type.
   */
  @Test
  public void testEquityAccount() {
    testAccount(AccountType.EQUITY);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean)}
   * . Tests Income type.
   */
  @Test
  public void testIncomeAccount() {
    testAccount(AccountType.INCOME);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean)}
   * . Tests Expense type.
   */
  @Test
  public void testExpenseAccount() {
    testAccount(AccountType.EXPENSES);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#addYear(FiscalYearAccount)}
   * . Tests null year parameter error.
   */
  @Test
  public void testNullAddYear() {
    Account account =
      new Account(NAME,
                  DESCRIPTION,
                  AccountType.ASSETS,
                  DEBIT_DEFAULT,
                  RECEIVABLE);
    try {
      account.addYear(null);
      fail("did not throw invalid parameters error for null year added");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#hashCode()}.
   * Tests hashCode() for equality.
   */
  @Test
  public void testHashCodeEquality() {
    Account account1 =
      new Account(NAME, DESCRIPTION, ACCOUNT_TYPE, DEBIT_DEFAULT, RECEIVABLE);
    Account account2 =
      new Account(NAME, DESCRIPTION, ACCOUNT_TYPE, DEBIT_DEFAULT, RECEIVABLE);
    assertTrue("hash code not equal for same name",
               account1.hashCode() == account2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#hashCode()}.
   * Tests hashCode() for inequality.
   */
  @Test
  public void testHashCodeInequality() {
    Account account1 =
      new Account(NAME, DESCRIPTION, ACCOUNT_TYPE, DEBIT_DEFAULT, RECEIVABLE);
    Account account2 =
      new Account(OTHER_NAME,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE);
    assertTrue("hash code equal for different name",
               account1.hashCode() != account2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#equals(java.lang.Object)}
   * . Tests equals() for equality.
   */
  @Test
  public void testEqualsObject() {
    Account account1 =
      new Account(NAME, DESCRIPTION, ACCOUNT_TYPE, DEBIT_DEFAULT, RECEIVABLE);
    Account account2 =
      new Account(NAME, DESCRIPTION, ACCOUNT_TYPE, DEBIT_DEFAULT, RECEIVABLE);
    assertTrue("equals comparison not equal for same name",
               account1.equals(account2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#equals(java.lang.Object)}
   * . Tests equals for inequality.
   */
  @Test
  public void testNotEqualsObject() {
    Account account1 =
      new Account(NAME, DESCRIPTION, ACCOUNT_TYPE, DEBIT_DEFAULT, RECEIVABLE);
    Account account2 =
      new Account(OTHER_NAME,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE);
    assertTrue("equals comparison equal for different name",
               !account1.equals(account2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#toString()}.
   * Tests the string representation of the object.
   */
  @Test
  public void testToString() {
    Account account =
      new Account(NAME, DESCRIPTION, ACCOUNT_TYPE, DEBIT_DEFAULT, RECEIVABLE);
    assertTrue("String representation failed: " + account.toString()
                   + ", should be \"" + STRING_REP + "\"",
               STRING_REP.equals(account.toString()));
  }
}
