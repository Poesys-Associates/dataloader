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
 * CUT: Receivable
 * 
 * @author Robert J. Muller
 */
public class ReceivableTest {

  private static final Integer YEAR = 2017;
  private static final Integer YEAR_2 = 2016;
  private static final Float ACCOUNT_NUMBER = 110.0F;
  private static final Integer TRANS_ID_1 = 200;
  private static final Integer TRANS_ID_2 = 210;
  private static final Double AMOUNT = 100.00D;

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#Receivable(java.lang.Integer, java.lang.Float, java.lang.Integer, java.lang.Double)}
   * . Tests constructor and getters.
   */
  @Test
  public void testReceivable() {
    Receivable receivable =
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);

    assertTrue("wrong account number " + receivable.getAccountNumber(),
               receivable.getAccountNumber().equals(ACCOUNT_NUMBER));
    assertTrue("wrong amount " + receivable.getAmount(),
               receivable.getAmount().equals(-AMOUNT));
    assertTrue("wrong transaction id " + receivable.getTransactionId(),
               receivable.getTransactionId().equals(TRANS_ID_1));
    assertTrue("wrong year " + receivable.getYear(),
               receivable.getYear().equals(YEAR));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#Receivable(java.lang.Integer, java.lang.Float, java.lang.Integer, java.lang.Double)}
   * Test null year argument.
   */
  @Test
  public void testReceivableNullYear() {
    try {
      new Receivable(null, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);
      fail("no InvalidParameterException on null year");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#Receivable(java.lang.Integer, java.lang.Float, java.lang.Integer, java.lang.Double)}
   * Test null account-number argument.
   */
  @Test
  public void testReceivableNullAccountNumber() {
    try {
      new Receivable(YEAR, null, TRANS_ID_1, AMOUNT);
      fail("no InvalidParameterException on null account number");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#Receivable(java.lang.Integer, java.lang.Float, java.lang.Integer, java.lang.Double)}
   * Test null account-number argument.
   */
  @Test
  public void testReceivableInvalidAccountNumber() {
    try {
      new Receivable(YEAR, 200.0F, TRANS_ID_1, AMOUNT);
      fail("no RuntimeException on invalid account number");
    } catch (RuntimeException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#Receivable(java.lang.Integer, java.lang.Float, java.lang.Integer, java.lang.Double)}
   * Test null transaction-id argument.
   */
  @Test
  public void testReceivableNullTransactionId() {
    try {
      new Receivable(YEAR, ACCOUNT_NUMBER, null, AMOUNT);
      fail("no InvalidParameterException on null transaction id");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#Receivable(java.lang.Integer, java.lang.Float, java.lang.Integer, java.lang.Double)}
   * Test null amount argument.
   */
  @Test
  public void testReceivableNullAmount() {
    try {
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_1, null);
      fail("no InvalidParameterException on null transaction id");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#Receivable(java.lang.Integer, java.lang.Float, java.lang.Integer, java.lang.Double)}
   * . Tests constructor and getters.
   */
  @Test
  public void testReceivableReader() {
    String input =
      YEAR + "\t" + TRANS_ID_1 + "\t" + ACCOUNT_NUMBER + "\t" + AMOUNT;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    Receivable receivable = new Receivable(reader);

    assertTrue("wrong year " + receivable.getYear(),
               receivable.getYear().equals(YEAR));
    assertTrue("wrong transaction id " + receivable.getTransactionId(),
               receivable.getTransactionId().equals(TRANS_ID_1));
    assertTrue("wrong account number " + receivable.getAccountNumber(),
               receivable.getAccountNumber().equals(ACCOUNT_NUMBER));
    assertTrue("wrong amount " + receivable.getAmount(),
               receivable.getAmount().equals(-AMOUNT));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#hashCode()}
   * . Tests has codes for the same object.
   */
  @Test
  public void testHashCodeEquality() {
    Receivable receivable1 =
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);
    Receivable receivable2 =
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);

    assertTrue("equal objects have different hash codes",
               receivable1.hashCode() == receivable2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#hashCode()}
   * . Tests hash codes for objects that differ by year.
   */
  @Test
  public void testHashCodeInequalityYear() {
    Receivable receivable1 =
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);
    Receivable receivable2 =
      new Receivable(YEAR_2, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);
    assertTrue("different objects have equal hash codes",
               receivable1.hashCode() != receivable2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#hashCode()}
   * . Tests hash codes for objects that differ by transaction id.
   */
  @Test
  public void testHashCodeInequalityId() {
    Receivable receivable1 =
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);
    Receivable receivable2 =
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_2, AMOUNT);
    assertTrue("different objects have equal hash codes",
               receivable1.hashCode() != receivable2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#equals(java.lang.Object)}
   * . Tests equals() for the same object.
   */
  @Test
  public void testEqualsObjectEquality() {
    Receivable receivable1 =
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);
    Receivable receivable2 =
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);

    assertTrue("equal objects get false equals()",
               receivable1.hashCode() == receivable2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#equals(java.lang.Object)}
   * . Tests equals() for objects that differ by year.
   */
  @Test
  public void testEqualsObjectInequalityYear() {
    Receivable receivable1 =
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);
    Receivable receivable2 =
      new Receivable(YEAR_2, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);

    assertTrue("different objects get true equals()",
               receivable1.hashCode() != receivable2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#equals(java.lang.Object)}
   * . Tests equals() for objects that differ by transaction id.
   */
  @Test
  public void testEqualsObjectInequality() {
    Receivable receivable1 =
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);
    Receivable receivable2 =
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_2, AMOUNT);

    assertTrue("different objects get true equals()",
               receivable1.hashCode() != receivable2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Receivable#toString()}
   * .
   */
  @Test
  public void testToString() {
    Receivable receivable =
      new Receivable(YEAR, ACCOUNT_NUMBER, TRANS_ID_1, AMOUNT);
    assertTrue("receivable string representation wrong: " + receivable,
               receivable.toString().equals("Receivable [year=2017, accountNumber=110.0, transactionId=200, amount=-100.0]"));
  }
}
