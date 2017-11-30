/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

import com.poesys.accounting.dataloader.newaccounting.AccountType;
import com.poesys.db.InvalidParametersException;


/**
 * CUT: AccountGroup
 * 
 * @author Robert J. Muller
 */
public class AccountGroupTest {
  private static final Integer YEAR = 2017;
  private static final String NAME = "Cash";
  private static final Float START = 100.00F;
  private static final Float END = 109.99F;
  private static final String STRING_REP =
    "AccountGroup [year=2017, name=Cash, start=100.0, end=109.99, orderNumber=null, getAccountType()=Assets]";

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.lang.String, java.lang.Float, java.lang.Float)}
   * . Tests field constructor and getters.
   */
  @Test
  public void testAccountGroup() {
    AccountGroup group = new AccountGroup(YEAR, NAME, START, END);
    assertTrue("year getter failed", YEAR.equals(group.getYear()));
    assertTrue("name getter failed", NAME.equals(group.getName()));
    assertTrue("start account getter failed", START.equals(group.getStart()));
    assertTrue("end account getter failed", END.equals(group.getEnd()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#equals(Object)}
   * . Tests equality comparison.
   */
  @Test
  public void testEqualsTrue() {
    AccountGroup group1 = new AccountGroup(YEAR, NAME, START, END);
    AccountGroup group2 = new AccountGroup(YEAR, NAME, START, END);
    assertTrue("equality comparison failed", group1.equals(group2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#equals(Object)}
   * . Tests equality comparison when intervals differ.
   */
  @Test
  public void testEqualsTrueDifferentEnd() {
    AccountGroup group1 = new AccountGroup(YEAR, NAME, START, END);
    AccountGroup group2 = new AccountGroup(YEAR, NAME, START, 108.00F);
    assertTrue("equality comparison failed", group1.equals(group2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#equals(Object)}
   * . Tests equality comparison when years differ.
   */
  @Test
  public void testEqualsFalseYear() {
    AccountGroup group1 = new AccountGroup(YEAR, NAME, START, END);
    AccountGroup group2 = new AccountGroup(YEAR+1, NAME, START, END);
    assertTrue("non-equality comparison failed", !group1.equals(group2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#equals(Object)}
   * . Tests equality comparison when names differ.
   */
  @Test
  public void testEqualsFalseName() {
    AccountGroup group1 = new AccountGroup(YEAR, NAME, START, END);
    AccountGroup group2 = new AccountGroup(YEAR, NAME+"aaa", START, END);
    assertTrue("non-equality comparison failed", !group1.equals(group2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#equals(Object)}
   * . Tests equality comparison when names differ.
   */
  @Test
  public void testEqualsFalseType() {
    AccountGroup group1 = new AccountGroup(YEAR, NAME, 200.00F, 209.99F);
    AccountGroup group2 = new AccountGroup(YEAR, NAME, START, END);
    assertTrue("non-equality comparison failed", !group1.equals(group2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#setOrderNumber(java.lang.Integer)}
   * . Tests getter/setter for order numbers.
   */
  @Test
  public void testGroupOrderNumber() {
    AccountGroup group = new AccountGroup(YEAR, NAME, START, END);
    group.setOrderNumber(1);
    Integer orderNumber = group.getOrderNumber();
    assertTrue("order number setter/getter failed", orderNumber == 1);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.lang.String, java.lang.Float, java.lang.Float)}
   * . Tests constructor required year exception.
   */
  @Test
  public void testAccountGroupRequiredYear() {
    try {
      new AccountGroup(null, NAME, START, END);
      fail("No exception for null year");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null year");
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.lang.String, java.lang.Float, java.lang.Float)}
   * . Tests constructor required name exception.
   */
  @Test
  public void testAccountGroupRequiredName() {
    try {
      new AccountGroup(YEAR, null, START, END);
      fail("No exception for null name");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null name");
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.lang.String, java.lang.Float, java.lang.Float)}
   * . Tests constructor required start exception.
   */
  @Test
  public void testAccountGroupRequiredStart() {
    try {
      new AccountGroup(YEAR, NAME, null, END);
      fail("No exception for null start account");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null start account");
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.lang.String, java.lang.Float, java.lang.Float)}
   * . Tests constructor required end exception.
   */
  @Test
  public void testAccountGroupRequiredEnd() {
    try {
      new AccountGroup(YEAR, NAME, null, END);
      fail("No exception for null end account");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null end account");
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor and getters.
   */
  @Test
  public void testAccountGroupReaderValid() {
    String input = START.toString() + "\t" + END.toString() + "\t" + NAME;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    AccountGroup group = new AccountGroup(YEAR, reader);
    assertTrue("year getter failed", YEAR.equals(group.getYear()));
    assertTrue("name getter failed", NAME.equals(group.getName()));
    assertTrue("start account getter failed", START.equals(group.getStart()));
    assertTrue("end account getter failed", END.equals(group.getEnd()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor required year.
   */
  @Test
  public void testAccountGroupReaderNullYear() {
    String input = START.toString() + "\t" + END.toString() + "\t" + NAME;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    try {
      new AccountGroup(null, reader);
      fail("No exception for null fiscal year");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null fiscal year: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor required reader.
   */
  @Test
  public void testAccountGroupReaderNullReader() {
    try {
      new AccountGroup(YEAR, null);
      fail("No exception for null reader");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null reader: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor reader end-of-stream.
   */
  @Test
  public void testAccountGroupReaderEndOfStream() {
    String input = START.toString() + "\t" + END.toString() + "\t" + NAME;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    try {
      // Read the line before passing it to the constructor to move the reader
      // to the end of the input stream.
      reader.readLine();
      new AccountGroup(YEAR, reader);
      fail("No exception for end of stream");
    } catch (EndOfStream e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for end of stream: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor invalid fields error.
   */
  @Test
  public void testAccountGroupReaderInvalidFields() {
    // input stream missing third field NAME
    String input = START.toString() + "\t" + END.toString();
    BufferedReader reader = new BufferedReader(new StringReader(input));
    try {
      new AccountGroup(YEAR, reader);
      fail("No exception for end of stream");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for end of stream: " + e.getMessage());
    }
  }

  /**
   * Internal subclass for testing that throws IO exception from readLine()
   */
  public class IoExceptionBufferedReader extends BufferedReader {

    /**
     * Create an IoExceptionBufferedReader object.
     * 
     * @param in the input reader to buffer
     */
    public IoExceptionBufferedReader(Reader in) {
      super(in);
    }

    @Override
    public String readLine() throws IOException {
      throw new IOException("IO Exception in readLine()");
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#AccountGroup(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor I/O exception.
   */
  @Test
  public void testAccountGroupReaderIoException() {
    // input stream missing third field NAME
    String input = START.toString() + "\t" + END.toString();
    BufferedReader reader =
      new IoExceptionBufferedReader(new StringReader(input));
    try {
      new AccountGroup(YEAR, reader);
      fail("No IO exception");
    } catch (RuntimeException e) {
      if (!e.getMessage().equals("I/O exception reading account group")) {
        fail("Wrong runtime exception, expecting I/O message: "
             + e.getMessage());
      }
    } catch (Exception e) {
      fail("Wrong exception for end of stream: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#getAccountType()}
   * . Tests valid getAccountType() with an asset group.
   */
  @Test
  public void testGetAccountTypeAssets() {
    AccountGroup group = new AccountGroup(YEAR, NAME, START, END);
    assertTrue("assets group returns other type: " + group.getAccountType(),
               group.getAccountType().equals(AccountType.ASSETS));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#getAccountType()}
   * . Tests valid getAccountType() with a liabilities group.
   */
  @Test
  public void testGetAccountTypeLiabilities() {
    AccountGroup group = new AccountGroup(YEAR, NAME, 200.00F, 299.99F);
    assertTrue("liabilities group returns other type: "
                   + group.getAccountType(),
               group.getAccountType().equals(AccountType.LIABILITIES));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#getAccountType()}
   * . Tests valid getAccountType() with an asset group.
   */
  @Test
  public void testGetAccountTypeEquity() {
    AccountGroup group = new AccountGroup(YEAR, NAME, 300.00F, 399.99F);
    assertTrue("equity group returns other type: " + group.getAccountType(),
               group.getAccountType().equals(AccountType.EQUITY));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#getAccountType()}
   * . Tests valid getAccountType() with a liabilities group.
   */
  @Test
  public void testGetAccountTypeIncome() {
    AccountGroup group = new AccountGroup(YEAR, NAME, 400.00F, 599.99F);
    assertTrue("income group returns other type: " + group.getAccountType(),
               group.getAccountType().equals(AccountType.INCOME));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#getAccountType()}
   * . Tests valid getAccountType() with a liabilities group.
   */
  @Test
  public void testGetAccountTypeExpenses() {
    AccountGroup group = new AccountGroup(YEAR, NAME, 600.00F, 899.99F);
    assertTrue("expenses group returns other type: " + group.getAccountType(),
               group.getAccountType().equals(AccountType.EXPENSES));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#getAccountType()}
   * . Tests valid getAccountType() with a liabilities group.
   */
  @Test
  public void testGetAccountTypeUnknown() {
    AccountGroup group = new AccountGroup(YEAR, NAME, 900.00F, 999.99F);
    assertTrue("invalid group returns type rather than null: "
                   + group.getAccountType(),
               group.getAccountType() == null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#contains(Integer, Float)}
   * . Tests contains method where a group contains the account.
   */
  @Test
  public void testContainsTrue() {
    String input = START.toString() + "\t" + END.toString() + "\t" + NAME;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    AccountGroup group = new AccountGroup(YEAR, reader);
    assertTrue("group does not contain account 105 for year " + YEAR + ": "
               + group, group.contains(YEAR, 105.00F));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#contains(Integer, Float)}
   * . Tests contains method where a group does not contain the account because
   * of the fiscal year being different.
   */
  @Test
  public void testContainsFalseYear() {
    String input = START.toString() + "\t" + END.toString() + "\t" + NAME;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    AccountGroup group = new AccountGroup(YEAR, reader);
    assertTrue("group contains account 105 for year " + (YEAR + 2) + ": "
                   + group + " but should not because the year is different",
               !group.contains(YEAR + 2, 105.00F));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#contains(Integer, Float)}
   * . Tests contains method where a group does not contain the account because
   * of the account being out of range.
   */
  @Test
  public void testContainsFalseAccount() {
    String input = START.toString() + "\t" + END.toString() + "\t" + NAME;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    AccountGroup group = new AccountGroup(YEAR, reader);
    assertTrue("group contains account 205 for year " + YEAR + ": " + group
                   + " but should not because the account is out of range",
               !group.contains(YEAR, 205.00F));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#compareTo(AccountGroup)}
   * . Tests compareTo for same start.
   * 
   * @throws IOException when the reader can't be closed
   */
  @Test
  public void testCompareToEquals() throws IOException {
    String input = START.toString() + "\t" + END.toString() + "\t" + NAME;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    AccountGroup group1 = new AccountGroup(YEAR, reader);
    reader.close();
    reader = new BufferedReader(new StringReader(input));
    AccountGroup group2 = new AccountGroup(YEAR, reader);
    assertTrue("same start but compareTo not zero: " + group1.compareTo(group2),
               group1.compareTo(group2) == 0);
    assertTrue("same start but compareTo and equals don't return equivalently: "
                   + group1.compareTo(group2),
               group1.equals(group2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#compareTo(AccountGroup)}
   * . Tests compareTo for lower start.
   * 
   * @throws IOException when the reader can't be closed
   */
  @Test
  public void testCompareToLessThan() throws IOException {
    String input1 = START.toString() + "\t" + END.toString() + "\t" + NAME;
    BufferedReader reader = new BufferedReader(new StringReader(input1));
    AccountGroup group1 = new AccountGroup(YEAR, reader);
    String input2 = "200.00\t299.99\t" + NAME;
    reader.close();
    reader = new BufferedReader(new StringReader(input2));
    AccountGroup group2 = new AccountGroup(YEAR, reader);
    assertTrue("lower start but compareTo not negative: "
                   + group1.compareTo(group2),
               group1.compareTo(group2) < 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#compareTo(AccountGroup)}
   * . Tests compareTo for higher start.
   * 
   * @throws IOException when the reader can't be closed
   */
  @Test
  public void testCompareToGreaterThan() throws IOException {
    String input1 = START.toString() + "\t" + END.toString() + "\t" + NAME;
    BufferedReader reader = new BufferedReader(new StringReader(input1));
    AccountGroup group2 = new AccountGroup(YEAR, reader);
    String input2 = "200.00\t299.99\t" + NAME;
    reader.close();
    reader = new BufferedReader(new StringReader(input2));
    AccountGroup group1 = new AccountGroup(YEAR, reader);
    assertTrue("higher start but compareTo not positive: "
                   + group1.compareTo(group2),
               group1.compareTo(group2) > 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountGroup#toString()}
   * . Tests string representation.
   */
  @Test
  public void testToString() {
    AbstractReaderDto group = new AccountGroup(YEAR, NAME, START, END);
    assertTrue("String representation incorrect: " + group.toString()
                   + ", expecting " + STRING_REP,
               STRING_REP.equals(group.toString()));
  }
}
