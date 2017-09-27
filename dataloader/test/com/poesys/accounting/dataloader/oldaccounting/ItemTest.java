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
 * CUT: Item
 * 
 * @author Robert J. Muller
 */
public class ItemTest {
  private static final Integer YEAR_1 = 2016;
  private static final Integer YEAR_2 = 2017;
  private static final Integer TRANS_ID_1 = 10;
  private static final Integer TRANS_ID_2 = 20;
  private static final Float ACCOUNT_NO_1 = 100.00F;
  private static final Float ACCOUNT_NO_2 = 200.00F;
  private static final Double AMOUNT = 1024.56D;
  private static final Boolean DEBIT = Boolean.TRUE;
  private static final Boolean NOT_CHECKED = Boolean.FALSE;

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#Item(java.lang.Integer, java.lang.Integer, java.lang.Float, java.lang.Double, java.lang.Boolean, java.lang.Boolean)}
   * . Tests field constructor and getters.
   */
  @Test
  public void testItemValid() {
    Item item =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    assertTrue("wrong year: " + item.getYear(), YEAR_1.equals(item.getYear()));
    assertTrue("wrong id: " + item.getTransactionId(),
               TRANS_ID_1.equals(item.getTransactionId()));
    assertTrue("wrong account number: " + item.getAccountNumber(),
               ACCOUNT_NO_1.equals(item.getAccountNumber()));
    assertTrue("wrong amount: " + item.getAmount(),
               AMOUNT.equals(item.getAmount()));
    assertTrue("wrong debit flag: " + item.isDebit(),
               DEBIT.equals(item.isDebit()));
    assertTrue("wrong checked flag: " + item.isChecked(),
               NOT_CHECKED.equals(item.isChecked()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#Item(java.lang.Integer, java.lang.Integer, java.lang.Float, java.lang.Double, java.lang.Boolean, java.lang.Boolean)}
   * . Tests field constructor with null year.
   */
  @Test
  public void testItemNullYear() {
    try {
      new Item(null, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
      fail("No exception for null fiscal year");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null fiscal year: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#Item(java.lang.Integer, java.lang.Integer, java.lang.Float, java.lang.Double, java.lang.Boolean, java.lang.Boolean)}
   * . Tests field constructor with null transaction id.
   */
  @Test
  public void testItemNullTransactionId() {
    try {
      new Item(YEAR_1, null, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
      fail("No exception for null transaction id");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null transaction id: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#Item(java.lang.Integer, java.lang.Integer, java.lang.Float, java.lang.Double, java.lang.Boolean, java.lang.Boolean)}
   * . Tests field constructor with null account number.
   */
  @Test
  public void testItemNullAccountNumber() {
    try {
      new Item(YEAR_1, TRANS_ID_1, null, AMOUNT, DEBIT, NOT_CHECKED);
      fail("No exception for null account number");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null account number: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#Item(java.lang.Integer, java.lang.Integer, java.lang.Float, java.lang.Double, java.lang.Boolean, java.lang.Boolean)}
   * . Tests field constructor with null amount.
   */
  @Test
  public void testItemNullAmount() {
    try {
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, null, DEBIT, NOT_CHECKED);
      fail("No exception for null amount");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null amount: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#Item(java.lang.Integer, java.lang.Integer, java.lang.Float, java.lang.Double, java.lang.Boolean, java.lang.Boolean)}
   * . Tests field constructor with null debit flag.
   */
  @Test
  public void testItemNullDebitFlag() {
    try {
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, null, NOT_CHECKED);
      fail("No exception for null debit flag");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null debit flag: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#Item(java.lang.Integer, java.lang.Integer, java.lang.Float, java.lang.Double, java.lang.Boolean, java.lang.Boolean)}
   * . Tests field constructor with null checked flag.
   */
  @Test
  public void testItemNullCheckedFlag() {
    try {
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, null);
      fail("No exception for null checked flag");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null checked flag: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#Item(java.lang.Integer, java.io.BufferedReader)}
   * . Tests buffered reader constructor and getters
   */
  @Test
  public void testItemIntegerBufferedReader() {
    String input =
      TRANS_ID_1 + "\t" + ACCOUNT_NO_1 + "\t" + AMOUNT + "\t" + "DR" + "\t"
          + "N";
    BufferedReader reader = new BufferedReader(new StringReader(input));
    Item item = new Item(YEAR_1, reader);
    assertTrue("wrong year: " + item.getYear(), YEAR_1.equals(item.getYear()));
    assertTrue("wrong id: " + item.getTransactionId(),
               TRANS_ID_1.equals(item.getTransactionId()));
    assertTrue("wrong account number: " + item.getAccountNumber(),
               ACCOUNT_NO_1.equals(item.getAccountNumber()));
    assertTrue("wrong amount: " + item.getAmount(),
               AMOUNT.equals(item.getAmount()));
    assertTrue("wrong debit flag: " + item.isDebit(),
               DEBIT.equals(item.isDebit()));
    assertTrue("wrong checked flag: " + item.isChecked(),
               NOT_CHECKED.equals(item.isChecked()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#Item(java.lang.Integer, java.io.BufferedReader)}
   * . Tests buffered reader constructor with null year
   */
  @Test
  public void testItemIntegerBufferedReaderNullYear() {
    String input =
      TRANS_ID_1 + "\t" + ACCOUNT_NO_1 + "\t" + AMOUNT + "\t" + "DR" + "\t"
          + "N";
    BufferedReader reader = new BufferedReader(new StringReader(input));

    try {
      new Item(null, reader);
      fail("No exception for null fiscal year");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null fiscal year: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#Item(java.lang.Integer, java.io.BufferedReader)}
   * . Tests buffered reader constructor with null transaction id
   */
  @Test
  public void testItemIntegerBufferedReaderNullId() {
    String input =
      "\t" + ACCOUNT_NO_1 + "\t" + AMOUNT + "\t" + "DR" + "\t"
          + "N";
    BufferedReader reader = new BufferedReader(new StringReader(input));

    try {
      new Item(YEAR_1, reader);
      fail("No exception for null item transaction id");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null item transaction id: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#Item(java.lang.Integer, java.io.BufferedReader)}
   * . Tests buffered reader constructor with null account number
   */
  @Test
  public void testItemIntegerBufferedReaderNullAccountNumber() {
    String input =
        TRANS_ID_1 + "\t" + "\t" + AMOUNT + "\t" + "DR" + "\t"
          + "N";
    BufferedReader reader = new BufferedReader(new StringReader(input));

    try {
      new Item(YEAR_1, reader);
      fail("No exception for null item account");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null item account: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#Item(java.lang.Integer, java.io.BufferedReader)}
   * . Tests buffered reader constructor with null amount
   */
  @Test
  public void testItemIntegerBufferedReaderNullAmount() {
    String input =
        TRANS_ID_1 + "\t" + ACCOUNT_NO_1  + "\t" + "\t" + "DR" + "\t"
          + "N";
    BufferedReader reader = new BufferedReader(new StringReader(input));

    try {
      new Item(YEAR_1, reader);
      fail("No exception for null item amount");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null item amount: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#hashCode()}.
   * Tests hashCode on same objects
   */
  @Test
  public void testHashCodeEquality() {
    Item item1 =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    Item item2 =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    assertTrue("same item but different hash codes",
               item1.hashCode() == item2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#hashCode()}.
   * Tests hashCode() on objects differing by year
   */
  @Test
  public void testHashCodeInequalityYear() {
    Item item1 =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    Item item2 =
      new Item(YEAR_2, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    assertTrue("different item on year but same hash codes",
               item1.hashCode() != item2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#hashCode()}.
   * Tests hashCode() on objects differing by transaction id
   */
  @Test
  public void testHashCodeInequalityTransactionId() {
    Item item1 =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    Item item2 =
      new Item(YEAR_1, TRANS_ID_2, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    assertTrue("different item on transaction id but same hash codes",
               item1.hashCode() != item2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#hashCode()}.
   * Tests hashCode() on objects differing by account
   */
  @Test
  public void testHashCodeInequalityAccount() {
    Item item1 =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    Item item2 =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_2, AMOUNT, DEBIT, NOT_CHECKED);
    assertTrue("different item on account number but same hash codes",
               item1.hashCode() != item2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#equals(java.lang.Object)}
   * . Tests equals() on same objects
   */
  @Test
  public void testEqualsObjectEquality() {
    Item item1 =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    Item item2 =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    assertTrue("same item but objects not equal", item1.equals(item2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#equals(java.lang.Object)}
   * . Tests equals() on objects differing by year
   */
  @Test
  public void testEqualsObjectInequalityYear() {
    Item item1 =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    Item item2 =
      new Item(YEAR_2, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    assertTrue("different item on year but equal objects", !item1.equals(item2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#equals(java.lang.Object)}
   * . Tests equals() on objects differing by transaction id
   */
  @Test
  public void testEqualsObjectInequalityTransactionId() {
    Item item1 =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    Item item2 =
      new Item(YEAR_1, TRANS_ID_2, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    assertTrue("different item on transaction id but equal objects",
               !item1.equals(item2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#equals(java.lang.Object)}
   * . Tests equals() on objects differing by account number
   */
  @Test
  public void testEqualsObjectInequalityAccount() {
    Item item1 =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    Item item2 =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_2, AMOUNT, DEBIT, NOT_CHECKED);
    assertTrue("different item on account number but equal objects",
               !item1.equals(item2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Item#toString()}.
   */
  @Test
  public void testToString() {
    Item item =
      new Item(YEAR_1, TRANS_ID_1, ACCOUNT_NO_1, AMOUNT, DEBIT, NOT_CHECKED);
    assertTrue("string representation wrong: " + item.toString(),
               "Item [year=2016, transactionId=10, accountNumber=100.0, amount=1024.56, debit=true, checked=false]".equals(item.toString()));

  }
}
