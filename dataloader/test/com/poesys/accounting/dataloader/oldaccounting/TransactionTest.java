/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;

import com.poesys.db.InvalidParametersException;


/**
 * CUT: Transaction
 * 
 * @author Robert J. Muller
 */
public class TransactionTest {
  // Oracle-formatted transaction date
  private static final String FORMAT = "dd-MMM-yy";
  private static final SimpleDateFormat formatter =
    new SimpleDateFormat(FORMAT);
  private static final Integer YEAR_1 = 2016;
  private static final Integer YEAR_2 = 2017;
  private static final Integer TRANS_ID_1 = 10;
  private static final Integer TRANS_ID_2 = 20;
  /** date as Oracle-formatted string representation */
  private static final String FORMATTED_TRANS_DATE = "26-JUN-17";
  /** date in wrong format */
  private static final String BAD_FORMATTED_TRANS_DATE = "6/26/2017";
  private static final String DESC = "description";
  /** description enclosed in quotes with trailing blanks */
  private static final String FORMATTED_DESC = "\"description              \"";
  private static final Boolean NOT_CHECKED = Boolean.FALSE;
  private static final String STRING_NOT_CHECKED = "N";
  private static Timestamp TRANS_DATE = null;

  // Set up the transaction date static constant in a block to handle exceptions
  static {
    try {
      TRANS_DATE =
        new Timestamp(formatter.parse(FORMATTED_TRANS_DATE).getTime());
    } catch (ParseException e) {
      fail("couldn't parse formatted date " + FORMATTED_TRANS_DATE);
    }
  }

  /**
   * } Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Transaction#Transaction(java.lang.Integer, java.lang.Integer, java.sql.Timestamp, java.lang.String, java.lang.Boolean)}
   * . Tests field constructor and getters.
   */
  @Test
  public void testTransactionIntegerIntegerTimestampStringBoolean() {
    Transaction transaction =
      new Transaction(YEAR_1, TRANS_ID_1, TRANS_DATE, DESC, NOT_CHECKED);
    assertTrue("wrong year: " + transaction.getYear(),
               YEAR_1.equals(transaction.getYear()));
    assertTrue("wrong transaction id: " + transaction.getTransactionId(),
               TRANS_ID_1.equals(transaction.getTransactionId()));
    assertTrue("wrong transaction date: " + transaction.getTransactionDate(),
               TRANS_DATE.equals(transaction.getTransactionDate()));
    assertTrue("wrong description: " + transaction.getDescription(),
               DESC.equals(transaction.getDescription()));
    assertTrue("wrong checked flag: " + transaction.getYear(),
               NOT_CHECKED.equals(transaction.isChecked()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Transaction#Transaction(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor and getters.
   */
  @Test
  public void testTransactionIntegerBufferedReader() {
    String input =
      TRANS_ID_1 + "\t" + FORMATTED_DESC + "\t" + FORMATTED_TRANS_DATE + "\t"
          + STRING_NOT_CHECKED;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    Transaction transaction = new Transaction(YEAR_1, reader);
    assertTrue("wrong year: " + transaction.getYear(),
               YEAR_1.equals(transaction.getYear()));
    assertTrue("wrong transaction id: " + transaction.getTransactionId(),
               TRANS_ID_1.equals(transaction.getTransactionId()));
    assertTrue("wrong transaction date: " + transaction.getTransactionDate(),
               TRANS_DATE.equals(transaction.getTransactionDate()));
    assertTrue("wrong description: " + transaction.getDescription(),
               DESC.equals(transaction.getDescription()));
    assertTrue("wrong checked flag: " + transaction.getYear(),
               NOT_CHECKED.equals(transaction.isChecked()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Transaction#Transaction(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor with null year.
   */
  @Test
  public void testTransactionIntegerBufferedReaderNullYear() {
    String input =
      TRANS_ID_1 + "\t" + FORMATTED_DESC + "\t" + FORMATTED_TRANS_DATE + "\t"
          + STRING_NOT_CHECKED;
    BufferedReader reader = new BufferedReader(new StringReader(input));

    try {
      new Transaction(null, reader);
      fail("No exception for null fiscal year");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for null fiscal year: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Transaction#Transaction(java.lang.Integer, java.io.BufferedReader)}
   * . Tests reader constructor with date in wrong format.
   */
  @Test
  public void testTransactionIntegerBufferedReaderBadDate() {
    String input =
      TRANS_ID_1 + "\t" + FORMATTED_DESC + "\t" + BAD_FORMATTED_TRANS_DATE
          + "\t" + STRING_NOT_CHECKED;
    BufferedReader reader = new BufferedReader(new StringReader(input));

    try {
      new Transaction(YEAR_1, reader);
      fail("No exception for badly formatted date");
    } catch (InvalidParametersException e) {
      // success
    } catch (Throwable e) {
      fail("Wrong throwable for badly formatted date: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Transaction#hashCode()}
   * . Tests equal objects with same year and transaction id
   */
  @Test
  public void testHashCodeEqualityYear() {
    Transaction transaction1 =
      new Transaction(YEAR_1, TRANS_ID_1, TRANS_DATE, DESC, NOT_CHECKED);
    Transaction transaction2 =
      new Transaction(YEAR_1, TRANS_ID_1, TRANS_DATE, DESC, NOT_CHECKED);
    assertTrue("same object but different hash codes",
               transaction1.hashCode() == transaction2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Transaction#hashCode()}
   * . Tests different year objects.
   */
  @Test
  public void testHashCodeInequalityYear() {
    Transaction transaction1 =
      new Transaction(YEAR_1, TRANS_ID_1, TRANS_DATE, DESC, NOT_CHECKED);
    Transaction transaction2 =
      new Transaction(YEAR_2, TRANS_ID_1, TRANS_DATE, DESC, NOT_CHECKED);
    assertTrue("same object but different hash codes",
               transaction1.hashCode() != transaction2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Transaction#hashCode()}
   * . Tests different id objects.
   */
  @Test
  public void testHashCodeInequalityId() {
    Transaction transaction1 =
      new Transaction(YEAR_1, TRANS_ID_1, TRANS_DATE, DESC, NOT_CHECKED);
    Transaction transaction2 =
      new Transaction(YEAR_1, TRANS_ID_2, TRANS_DATE, DESC, NOT_CHECKED);
    assertTrue("same object but different hash codes",
               transaction1.hashCode() != transaction2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Transaction#equals(java.lang.Object)}
   * . Tests same object for equals()
   */
  @Test
  public void testEqualsObjectEquality() {
    Transaction transaction1 =
      new Transaction(YEAR_1, TRANS_ID_1, TRANS_DATE, DESC, NOT_CHECKED);
    Transaction transaction2 =
      new Transaction(YEAR_1, TRANS_ID_1, TRANS_DATE, DESC, NOT_CHECKED);
    assertTrue("same object but not equal", transaction1.equals(transaction2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Transaction#equals(java.lang.Object)}
   * . Tests object differing on year for equals()
   */
  @Test
  public void testEqualsObjectInequalityYear() {
    Transaction transaction1 =
      new Transaction(YEAR_1, TRANS_ID_1, TRANS_DATE, DESC, NOT_CHECKED);
    Transaction transaction2 =
      new Transaction(YEAR_2, TRANS_ID_1, TRANS_DATE, DESC, NOT_CHECKED);
    assertTrue("different object but equal", !transaction1.equals(transaction2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Transaction#equals(java.lang.Object)}
   * . Tests object differing on id for equals()
   */
  @Test
  public void testEqualsObjectInequalityId() {
    Transaction transaction1 =
      new Transaction(YEAR_1, TRANS_ID_1, TRANS_DATE, DESC, NOT_CHECKED);
    Transaction transaction2 =
      new Transaction(YEAR_1, TRANS_ID_2, TRANS_DATE, DESC, NOT_CHECKED);
    assertTrue("different object but equal", !transaction1.equals(transaction2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Transaction#toString()}
   * .
   */
  @Test
  public void testToString() {
    Transaction transaction1 =
      new Transaction(YEAR_1, TRANS_ID_1, TRANS_DATE, DESC, NOT_CHECKED);
    assertTrue("string representation wrong: " + transaction1,
               "Transaction [year=2016, transactionId=10, transactionDate=2017-06-26 00:00:00.0, description=description]".equals(transaction1.toString()));
  }
}
