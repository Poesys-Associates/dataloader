/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Test;

import com.poesys.db.InvalidParametersException;


/**
 * CUT: AccountMap
 * 
 * @author Robert J. Muller
 */
public class AccountMapTest {
  private static final Float ACCOUNT_NUMBER_1 = 100.3F;
  private static final String NAME_1 = "Checking";
  private static final Float ACCOUNT_NUMBER_2 = 220.1F;
  private static final String NAME_2 = "Amazon Liability";
  private static final Float ACCOUNT_NUMBER_3 = 634.0F;
  private static final String NAME_3 = "Nonprofit Donations";
  private static final Object STRING_REP_1 = "AccountMap [accountNumber=100.3, name=Checking]";

  private static final String DELIMITER = "\t";
  private static final String LINE_DELIMETER = "\n";

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountMap#AccountMap(java.lang.Float, java.lang.String)}
   * . Tests constructor and getters
   */
  @Test
  public void testAccountMapFloatString() {
    AccountMap map = new AccountMap(ACCOUNT_NUMBER_1, NAME_1);
    assertTrue("no account map created", map != null);
    assertTrue("wrong number: " + map.getAccountNumber(),
               map.getAccountNumber().equals(ACCOUNT_NUMBER_1));
    assertTrue("wrong name: " + map.getName(), map.getName().equals(NAME_1));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountMap#AccountMap(java.lang.Float, java.lang.String)}
   * . Tests constructor for required account number
   */
  @Test
  public void testAccountMapRequiredNumber() {
    try {
      new AccountMap(null, NAME_1);
      fail("Did not throw invalid parameters exception for null account number");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountMap#AccountMap(java.lang.Float, java.lang.String)}
   * . Tests constructor for required account name
   */
  @Test
  public void testAccountMapRequiredName() {
    try {
      new AccountMap(ACCOUNT_NUMBER_1, null);
      fail("Did not throw invalid parameters exception for null account name");
    } catch (InvalidParametersException e) {
      // succcess
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountMap#AccountMap(java.io.BufferedReader)}
   * . Tests reader constructor and getters for 2 rows of data
   */
  @Test
  public void testAccountMapBufferedReader() {
    String input =
      ACCOUNT_NUMBER_1 + DELIMITER + NAME_1 + LINE_DELIMETER + ACCOUNT_NUMBER_2
          + DELIMITER + NAME_2 + LINE_DELIMETER + ACCOUNT_NUMBER_3 + DELIMITER
          + NAME_3;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    AccountMap map = new AccountMap(reader);
    assertTrue("no account map 1 created", map != null);
    assertTrue("wrong number: " + map.getAccountNumber(),
               map.getAccountNumber().equals(ACCOUNT_NUMBER_1));
    assertTrue("wrong name: " + map.getName(), map.getName().equals(NAME_1));
    map = new AccountMap(reader);
    assertTrue("no account map 2 created", map != null);
    assertTrue("wrong number: " + map.getAccountNumber(),
               map.getAccountNumber().equals(ACCOUNT_NUMBER_2));
    assertTrue("wrong name: " + map.getName(), map.getName().equals(NAME_2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountMap#AccountMap(java.io.BufferedReader)}
   * . Tests reader constructor and getters for null account number
   */
  @Test
  public void testAccountMapBufferedReaderNullNumber() {
    String input =
      DELIMITER + NAME_1 + LINE_DELIMETER + ACCOUNT_NUMBER_2 + DELIMITER
          + NAME_2 + LINE_DELIMETER + ACCOUNT_NUMBER_3 + DELIMITER + NAME_3;
    try {
      BufferedReader reader = new BufferedReader(new StringReader(input));
      new AccountMap(reader);
      fail("did not throw invalid parameters exception for null account number");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountMap#AccountMap(java.io.BufferedReader)}
   * . Tests reader constructor and getters for null account name
   */
  @Test
  public void testAccountMapBufferedReaderNullName() {
    String input =
      ACCOUNT_NUMBER_1 + DELIMITER + LINE_DELIMETER + ACCOUNT_NUMBER_2
          + DELIMITER + NAME_2 + LINE_DELIMETER + ACCOUNT_NUMBER_3 + DELIMITER
          + NAME_3;
    try {
      BufferedReader reader = new BufferedReader(new StringReader(input));
      new AccountMap(reader);
      fail("did not throw invalid parameters exception for null account name");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountMap#hashCode()}
   * .
   */
  @Test
  public void testHashCodeEquality() {
    AccountMap map1 = new AccountMap(ACCOUNT_NUMBER_1, NAME_1);
    AccountMap map2 = new AccountMap(ACCOUNT_NUMBER_1, NAME_1);
    assertTrue("equal account map but hash codes aren't equal",
               map1.hashCode() == map2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountMap#hashCode()}
   * .
   */
  @Test
  public void testHashCodeInequality() {
    AccountMap map1 = new AccountMap(ACCOUNT_NUMBER_1, NAME_1);
    AccountMap map2 = new AccountMap(ACCOUNT_NUMBER_2, NAME_2);
    assertTrue("different account map but hash codes are the same",
               map1.hashCode() != map2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountMap#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsObjectEquality() {
    AccountMap map1 = new AccountMap(ACCOUNT_NUMBER_1, NAME_1);
    AccountMap map2 = new AccountMap(ACCOUNT_NUMBER_1, NAME_1);
    assertTrue("equal account maps that don't compare as equal",
               map1.equals(map2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountMap#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsObjectInequality() {
    AccountMap map1 = new AccountMap(ACCOUNT_NUMBER_1, NAME_1);
    AccountMap map2 = new AccountMap(ACCOUNT_NUMBER_2, NAME_2);
    assertTrue("different account maps that compare as equal",
               !map1.equals(map2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.AccountMap#toString()}
   * .
   */
  @Test
  public void testToString() {
    AccountMap map = new AccountMap(ACCOUNT_NUMBER_1, NAME_1);
    assertTrue("String representation failed: " + map + ", expecting "
               + STRING_REP_1, STRING_REP_1.equals(map.toString()));
  }
}
