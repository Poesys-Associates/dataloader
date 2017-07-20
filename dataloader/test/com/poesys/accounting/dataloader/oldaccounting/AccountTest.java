package com.poesys.accounting.dataloader.oldaccounting;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Test;

import com.poesys.accounting.dataloader.newaccounting.Account.AccountType;
import com.poesys.db.InvalidParametersException;


/**
 * <p>
 * CUT: Account DTO
 * </p>
 * <p>
 * Note: getItems() method is tested in the TransactionTest class because it
 * requires extensive transaction and item creation already done in that class.
 * </p>
 * 
 * @author Robert J. Muller
 */
public class AccountTest {

  private static final Integer YEAR = 2017;
  private static final Float ACCOUNT = 109.0F;
  private static final Float RECEIVABLE_ACCOUNT = 110.0F;
  private static final String NAME = "Cash";
  private static final String NAME2 = "Accounts Receivable";
  private static final Boolean DEFAULT_DEBIT = Boolean.TRUE;
  private static final String STRING_REP =
    "Account [year=2017, name=Cash, accountNumber=109.0, defaultDebit=true, getAccountType()=Asset]";

  /**
   * Test constructor and getters (getAccount is separate from this test)
   */
  @Test
  public void testAccount() {
    Account account = new Account(YEAR, ACCOUNT, NAME, DEFAULT_DEBIT);

    assertTrue("constructor failed", account != null);
    assertTrue("year getter failed", YEAR.equals(account.getYear()));
    assertTrue("account number getter failed",
               ACCOUNT.equals(account.getAccountNumber()));
    assertTrue("name getter failed", NAME.equals(account.getName()));
    assertTrue("default debit getter failed",
               DEFAULT_DEBIT.equals(account.getDefaultDebit()));
  }

  /**
   * Test constructor required year exception
   */
  @Test
  public void testAccountRequiredYear() {
    try {
      new Account(null, ACCOUNT, NAME, DEFAULT_DEBIT);
      fail("No exception for null year");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception for null year");
    }
  }

  /**
   * Test constructor required account number exception
   */
  @Test
  public void testAccountRequiredAccountNumber() {
    try {
      new Account(YEAR, null, NAME, DEFAULT_DEBIT);
      fail("No exception for null account number");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception for null account");
    }
  }

  /**
   * Test constructor required name exception
   */
  @Test
  public void testAccountRequiredName() {
    try {
      new Account(YEAR, ACCOUNT, null, DEFAULT_DEBIT);
      fail("No exception for null name");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception for null name");
    }
  }

  /**
   * Test constructor required default debit exception
   */
  @Test
  public void testAccountRequiredDefaultDebit() {
    try {
      new Account(YEAR, ACCOUNT, NAME, null);
      fail("No exception for null year");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception for null year");
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Account#Account(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor and getters. Also tests multiple-line input.
   */
  @Test
  public void testAccountReader() {
    Float accountNumber = 100.00F;
    String accountName = "Citicorp Checking (111222333444)";
    String input =
      accountNumber + "\t" + accountName + "\t" + "CR" + "\n" + "101" + "\t"
          + "Schwab One (111222333444) Money Market Fund" + "\t" + "CR" + "\n"
          + "109" + "\t" + "Cash" + "\t" + "CR";
    BufferedReader reader = new BufferedReader(new StringReader(input));
    Account account = new Account(YEAR, reader);

    assertTrue("year getter failed", YEAR.equals(account.getYear()));
    assertTrue("account number getter failed",
               accountNumber.equals(account.getAccountNumber()));
    assertTrue("account name getter failed",
               accountName.equals(account.getName()));
    assertTrue("default debit getter failed",
               Boolean.FALSE.equals(account.getDefaultDebit()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Account#Account(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor and getters.
   */
  @Test
  public void testAccountReaderNullYear() {
    Float accountNumber = 100.00F;
    String input =
      accountNumber + "\t" + "Citicorp Checking (937-4002518)" + "\t" + "CR"
          + "\n" + "101" + "\t" + "Schwab One (6522-1969) Money Market Fund"
          + "\t" + "CR" + "\n" + "109" + "\t" + "Cash" + "\t" + "CR";
    BufferedReader reader = new BufferedReader(new StringReader(input));
    try {
      new Account(null, reader);
      fail("No exception for null fiscal year");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null fiscal year: " + e.getMessage());
    }
  }

  /**
   * Test hash code equality
   */
  @Test
  public void testHashCodeEquality() {
    Account account = new Account(YEAR, ACCOUNT, NAME, DEFAULT_DEBIT);
    int hash = account.hashCode();

    Account account2 = new Account(YEAR, ACCOUNT, NAME, DEFAULT_DEBIT);

    assertTrue("Hash codes for same values don't match: " + hash + ", "
               + account2.hashCode(), hash == account2.hashCode());
  }

  /**
   * Test hash code inequality
   */
  @Test
  public void testHashCodeInequality() {
    Account account = new Account(YEAR, ACCOUNT, NAME, DEFAULT_DEBIT);
    int hash = account.hashCode();

    Account account2 =
      new Account(YEAR, RECEIVABLE_ACCOUNT, NAME2, DEFAULT_DEBIT);

    assertTrue("Hash codes for different values match: " + hash + ", "
               + account2.hashCode(), hash != account2.hashCode());
  }

  /**
   * Test equals equality
   */
  @Test
  public void testEqualsObjectEquality() {
    Account account = new Account(YEAR, ACCOUNT, NAME, DEFAULT_DEBIT);

    Account account2 = new Account(YEAR, ACCOUNT, NAME, DEFAULT_DEBIT);

    assertTrue("Equals comparison failed", account.equals(account2));
  }

  /**
   * Test equals inequality
   */
  @Test
  public void testEqualsObjectInequality() {
    Account account = new Account(YEAR, ACCOUNT, NAME, DEFAULT_DEBIT);

    Account account2 =
      new Account(YEAR, RECEIVABLE_ACCOUNT, NAME2, DEFAULT_DEBIT);

    assertTrue("Not-Equals comparison failed", !account.equals(account2));
  }

  /**
   * Test string representation
   */
  @Test
  public void testToString() {
    Account account = new Account(YEAR, ACCOUNT, NAME, DEFAULT_DEBIT);

    assertTrue("String representation failed: " + account + ", expecting "
               + STRING_REP, STRING_REP.equals(account.toString()));
  }

  /**
   * Test the getAccountType() method for all account types.
   */
  @Test
  public void testGetAccountType() {
    validateAccountType(101F, AccountType.ASSET);
    validateAccountType(220.1F, AccountType.LIABILITY);
    validateAccountType(310.2F, AccountType.EQUITY);
    validateAccountType(402F, AccountType.INCOME);
    validateAccountType(599F, AccountType.INCOME);
    validateAccountType(620F, AccountType.EXPENSE);
    validateAccountType(620F, AccountType.EXPENSE);
    validateAccountType(5F, null);
    validateAccountType(900F, null);
  }

  /**
   * Create an account with a specified account number, get the account type,
   * and validate that type against a specified, expected type.
   * @param number the account number
   * @param type the account type
   */
  private void validateAccountType(Float number, AccountType type) {
    Account account = new Account(YEAR, number, NAME, DEFAULT_DEBIT);
    if (type == null) {
      assertTrue("expecting null but got type " + account.getAccountType(),
                 account.getAccountType() == null);
    } else {
      assertTrue("expecting " + type.toString() + " but got "
                     + account.getAccountType(),
                 account.getAccountType().equals(type));
    }
  }
}
