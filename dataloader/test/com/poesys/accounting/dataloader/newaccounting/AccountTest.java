/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.poesys.accounting.dataloader.newaccounting.Account.AccountType;
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
  private static final AccountType ACCOUNT_TYPE = AccountType.ASSET;
  private static final Boolean DEBIT_DEFAULT = Boolean.TRUE;
  private static final Boolean RECEIVABLE = Boolean.FALSE;
  private static final String GROUP_NAME = "Cash";
  private static final FiscalYear YEAR1 = new FiscalYear(2010);
  private static final FiscalYear YEAR2 = new FiscalYear(2011);
  private static final FiscalYear YEAR3 = new FiscalYear(2012);
  private static final String STRING_REP = "Account [name=" + NAME
                                           + ", description=" + DESCRIPTION
                                           + ", accountType=" + ACCOUNT_TYPE
                                           + ", debitDefault=" + DEBIT_DEFAULT
                                           + ", receivable=" + RECEIVABLE
                                           + ", group=" + "AccountGroup [name="
                                           + GROUP_NAME + "]" + "]";

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean, com.poesys.accounting.dataloader.newaccounting.AccountGroup)}
   * . Tests required name field in constructor.
   */
  public void testAccountRequiredName() {
    try {
      new Account(null,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
      fail("No exception on required name field");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean, com.poesys.accounting.dataloader.newaccounting.AccountGroup)}
   * . Tests required description field in constructor.
   */
  public void testAccountRequiredDescription() {
    try {
      new Account(NAME,
                  null,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
      fail("No exception on required name field");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean, com.poesys.accounting.dataloader.newaccounting.AccountGroup)}
   * . Tests required account type field in constructor.
   */
  public void testAccountRequiredAccountType() {
    try {
      new Account(NAME,
                  DESCRIPTION,
                  null,
                  DEBIT_DEFAULT,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
      fail("No exception on required name field");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean, com.poesys.accounting.dataloader.newaccounting.AccountGroup)}
   * . Tests required debit default field in constructor.
   */
  public void testAccountRequiredDebitDefault() {
    try {
      new Account(NAME,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  null,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
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
    AccountGroup group = new AccountGroup(GROUP_NAME);
    Account account =
      new Account(NAME, DESCRIPTION, type, DEBIT_DEFAULT, RECEIVABLE, group);
    account.addYear(YEAR1);
    account.addYear(YEAR2);
    account.addYear(YEAR3);
    assertTrue("Account constructor failed", account != null);
    assertTrue("name getter failed", NAME.equals(account.getName()));
    assertTrue("name getter failed",
               DESCRIPTION.equals(account.getDescription()));
    assertTrue("name getter failed", type.equals(account.getAccountType()));
    assertTrue("name getter failed",
               DEBIT_DEFAULT.equals(account.isDebitDefault()));
    assertTrue("name getter failed", RECEIVABLE.equals(account.isReceivable()));
    assertTrue("group getter failed: expected " + group + " but got "
                   + account.getGroup(),
               group.equals(account.getGroup()));
    assertTrue("account year list does not contain year " + YEAR1,
               account.getYears().contains(YEAR1));
    assertTrue("account year list does not contain year " + YEAR2,
               account.getYears().contains(YEAR2));
    assertTrue("account year list does not contain year " + YEAR3,
               account.getYears().contains(YEAR3));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean, com.poesys.accounting.dataloader.newaccounting.AccountGroup)}
   * . Tests Asset type.
   */
  @Test
  public void testAssetAccount() {
    testAccount(AccountType.ASSET);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean, com.poesys.accounting.dataloader.newaccounting.AccountGroup)}
   * . Tests Liability type.
   */
  @Test
  public void testLiabilityAccount() {
    testAccount(AccountType.LIABILITY);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean, com.poesys.accounting.dataloader.newaccounting.AccountGroup)}
   * . Tests Equity type.
   */
  @Test
  public void testEquityAccount() {
    testAccount(AccountType.EQUITY);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean, com.poesys.accounting.dataloader.newaccounting.AccountGroup)}
   * . Tests Income type.
   */
  @Test
  public void testIncomeAccount() {
    testAccount(AccountType.INCOME);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#Account(java.lang.String, java.lang.String, AccountType, java.lang.Boolean, java.lang.Boolean, com.poesys.accounting.dataloader.newaccounting.AccountGroup)}
   * . Tests Expense type.
   */
  @Test
  public void testExpenseAccount() {
    testAccount(AccountType.EXPENSE);
  }
  
  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Account#addYear(FiscalYear)}
   * . Tests null year parameter error.
   */
  @Test
  public void testNullAddYear() {
    AccountGroup group = new AccountGroup(GROUP_NAME);
    Account account =
      new Account(NAME, DESCRIPTION, AccountType.ASSET, DEBIT_DEFAULT, RECEIVABLE, group);
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
      new Account(NAME,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
    Account account2 =
      new Account(NAME,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
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
      new Account(NAME,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
    Account account2 =
      new Account(OTHER_NAME,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
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
      new Account(NAME,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
    Account account2 =
      new Account(NAME,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
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
      new Account(NAME,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
    Account account2 =
      new Account(OTHER_NAME,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
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
      new Account(NAME,
                  DESCRIPTION,
                  ACCOUNT_TYPE,
                  DEBIT_DEFAULT,
                  RECEIVABLE,
                  new AccountGroup(GROUP_NAME));
    assertTrue("String representation failed: " + account.toString(),
               STRING_REP.equals(account.toString()));
  }
}
