/*
 * Copyright (c) 2018 Poesys Associates. All rights reserved.
 *
 * This file is part of Poesys/Dataloader.
 *
 * Poesys/Dataloader is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Poesys/Dataloader is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Poesys/Dataloader. If not, see <http://www.gnu.org/licenses/>.
 */
package com.poesys.accounting.dataloader.oldaccounting;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Test;

import com.poesys.db.InvalidParametersException;

/**
 * CUT: Reimbursement
 *
 * @author Robert J. Muller
 */
public class ReimbursementTest {
  private static final Float ACCOUNT_NUMBER = 110.0F;
  private static final Integer REC_YEAR = 2016;
  private static final Integer REC_ID = 100;
  private static final Integer REIM_YEAR = 2017;
  private static final Integer REIM_YEAR_2 = 2018;
  private static final Integer REIM_ID = 400;
  private static final Integer REIM_ID_2 = 500;
  private static final Double AMOUNT = 100.00D;
  private static final Double ZERO_AMOUNT = 0.00D;
  private static final Double ALLOCATED_AMOUNT = 50.00D;

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Reimbursement#Reimbursement(java.lang.Float, * java.lang.Integer, java.lang.Integer, java
   * .lang.Integer, java.lang.Integer, java.lang.Double, * java.lang.Double)} . Tests constructor
   * and getters.
   */
  @Test
  public void testReimbursement() {
    Reimbursement reimbursement =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);

    assertTrue("wrong account number: " + reimbursement.getAccountNumber(),
               reimbursement.getAccountNumber().equals(ACCOUNT_NUMBER));
    assertTrue("wrong receivable year: " + reimbursement.getReceivableYear(),
               reimbursement.getReceivableYear().equals(REC_YEAR));
    assertTrue("wrong receivable transaction id: " + reimbursement.getReceivableTransactionId(),
               reimbursement.getReceivableTransactionId().equals(REC_ID));
    assertTrue("wrong reimbursement year: " + reimbursement.getReimbursementYear(),
               reimbursement.getReimbursementYear().equals(REIM_YEAR));
    assertTrue(
      "wrong reimbursement transaction id: " + reimbursement.getReimbursementTransactionId(),
      reimbursement.getReimbursementTransactionId().equals(REIM_ID));
    assertTrue("wrong reimbursed amount: " + reimbursement.getReimbursedAmount(),
               reimbursement.getReimbursedAmount().equals(AMOUNT));
    assertTrue("wrong allocated amount: " + reimbursement.getAllocatedAmount(),
               reimbursement.getAllocatedAmount().equals(ALLOCATED_AMOUNT));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Reimbursement#Reimbursement(java.lang.Float, * java.lang.Integer, java.lang.Integer, java
   * .lang.Integer, java.lang.Integer, java.lang.Double, * java.lang.Double)} . Tests constructor
   * with null account number
   */
  @Test
  public void testReimbursementNullAccountNumber() {
    try {
      new Reimbursement(null, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, AMOUNT, ALLOCATED_AMOUNT);
      fail("null account number did not throw invalid parameter exception");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Reimbursement#Reimbursement(java.lang.Float, * java.lang.Integer, java.lang.Integer, java
   * .lang.Integer, java.lang.Integer, java.lang.Double, * java.lang.Double)} . Tests constructor
   * with null receivable year
   */
  @Test
  public void testReimbursementNullReceivableYear() {
    try {
      new Reimbursement(ACCOUNT_NUMBER, null, REC_ID, REIM_YEAR, REIM_ID, AMOUNT, ALLOCATED_AMOUNT);
      fail("null receivable year did not throw invalid parameter exception");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Reimbursement#Reimbursement(java.lang.Float, * java.lang.Integer, java.lang.Integer, java
   * .lang.Integer, java.lang.Integer, java.lang.Double, * java.lang.Double)} . Tests constructor
   * with null receivable transaction id
   */
  @Test
  public void testReimbursementNullReceivableTransactionId() {
    try {
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, null, REIM_YEAR, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);
      fail("null receivable transaction id did not throw invalid parameter exception");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Reimbursement#Reimbursement(java.lang.Float, * java.lang.Integer, java.lang.Integer, java
   * .lang.Integer, java.lang.Integer, java.lang.Double, * java.lang.Double)} . Tests constructor
   * with null reimbursement year
   */
  @Test
  public void testReimbursementNullReimbursementYear() {
    try {
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, null, REIM_ID, AMOUNT, ALLOCATED_AMOUNT);
      fail("null reimbursement year did not throw invalid parameter exception");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Reimbursement#Reimbursement(java.lang.Float, * java.lang.Integer, java.lang.Integer, java
   * .lang.Integer, java.lang.Integer, java.lang.Double, * java.lang.Double)} . Tests constructor
   * with null reimbursement transaction id
   */
  @Test
  public void testReimbursementNullReimbursementTransactionId() {
    try {
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, null, AMOUNT,
                        ALLOCATED_AMOUNT);
      fail("null reimbursement transaction id did not throw invalid parameter exception");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Reimbursement#Reimbursement(java.lang.Float, * java.lang.Integer, java.lang.Integer, java
   * .lang.Integer, java.lang.Integer, java.lang.Double, * java.lang.Double)} . Tests constructor
   * with null reimbursement amount
   */
  @Test
  public void testReimbursementNullAmount() {
    try {
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, null,
                        ALLOCATED_AMOUNT);
      fail("null reimbursement amount did not throw invalid parameter exception");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Reimbursement#Reimbursement(java.lang.Float, * java.lang.Integer, java.lang.Integer, java
   * .lang.Integer, java.lang.Integer, java.lang.Double, * java.lang.Double)} . Tests constructor
   * with null allocated amount, should default to zero
   */
  @Test
  public void testReimbursementNullAllocatedAmount() {
    Reimbursement reimbursement =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, AMOUNT, null);
    assertTrue(
      "wrong allocated amount, expected default zero: " + reimbursement.getAllocatedAmount(),
      reimbursement.getAllocatedAmount().equals(0.00D));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Reimbursement#Reimbursement(java.lang.Integer, * java.io.BufferedReader)} . Tests reader constructor and getters.
   */
  @Test
  public void testReimbursementReaderValid() {
    String input =
      REIM_ID + "\t" + REC_YEAR + "\t" + REC_ID + "\t" + ACCOUNT_NUMBER + "\t" + AMOUNT + "\t" +
      ALLOCATED_AMOUNT;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    Reimbursement reimbursement = new Reimbursement(REIM_YEAR, reader);
    assertTrue("wrong account number: " + reimbursement.getAccountNumber(),
               reimbursement.getAccountNumber().equals(ACCOUNT_NUMBER));
    assertTrue("wrong receivable year: " + reimbursement.getReceivableYear(),
               reimbursement.getReceivableYear().equals(REC_YEAR));
    assertTrue("wrong receivable transaction id: " + reimbursement.getReceivableTransactionId(),
               reimbursement.getReceivableTransactionId().equals(REC_ID));
    assertTrue("wrong reimbursement year: " + reimbursement.getReimbursementYear(),
               reimbursement.getReimbursementYear().equals(REIM_YEAR));
    assertTrue(
      "wrong reimbursement transaction id: " + reimbursement.getReimbursementTransactionId(),
      reimbursement.getReimbursementTransactionId().equals(REIM_ID));
    assertTrue("wrong reimbursed amount: " + reimbursement.getReimbursedAmount(),
               reimbursement.getReimbursedAmount().equals(AMOUNT));
    assertTrue("wrong allocated amount: " + reimbursement.getAllocatedAmount(),
               reimbursement.getAllocatedAmount().equals(ALLOCATED_AMOUNT));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Reimbursement#Reimbursement(java.lang.Float, * java.lang.Integer, java.lang.Integer, java
   * .lang.Integer, java.lang.Integer, java.lang.Double, * java.lang.Double)} . Tests reader
   * constructor and getters when reimbursement is zero and
   * allocation is positive.
   */
  @Test
  public void testReimbursementReaderValidAllocationOnly() {
    String input =
      REIM_ID + "\t" + REC_YEAR + "\t" + REC_ID + "\t" + ACCOUNT_NUMBER + "\t" + ZERO_AMOUNT +
      "\t" + ALLOCATED_AMOUNT;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    Reimbursement reimbursement = new Reimbursement(REIM_YEAR, reader);

    assertTrue("wrong account number: " + reimbursement.getAccountNumber(),
               reimbursement.getAccountNumber().equals(ACCOUNT_NUMBER));
    assertTrue("wrong receivable year: " + reimbursement.getReceivableYear(),
               reimbursement.getReceivableYear().equals(REC_YEAR));
    assertTrue("wrong receivable transaction id: " + reimbursement.getReceivableTransactionId(),
               reimbursement.getReceivableTransactionId().equals(REC_ID));
    assertTrue("wrong reimbursement year: " + reimbursement.getReimbursementYear(),
               reimbursement.getReimbursementYear().equals(REIM_YEAR));
    assertTrue(
      "wrong reimbursement transaction id: " + reimbursement.getReimbursementTransactionId(),
      reimbursement.getReimbursementTransactionId().equals(REIM_ID));
    assertTrue("wrong reimbursed amount, should be zero: " + reimbursement.getReimbursedAmount(),
               reimbursement.getReimbursedAmount().equals(ZERO_AMOUNT));
    assertTrue("wrong allocated amount: " + reimbursement.getAllocatedAmount(),
               reimbursement.getAllocatedAmount().equals(ALLOCATED_AMOUNT));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Reimbursement#Reimbursement(java.lang.Integer, * java.io.BufferedReader)} . Tests reader constructor with null year.
   */
  @Test
  public void testReimbursementReaderNullYear() {
    String input =
      REIM_ID + "\t" + REC_YEAR + "\t" + REC_ID + "\t" + ACCOUNT_NUMBER + "\t" + AMOUNT + "\t" +
      ALLOCATED_AMOUNT;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    try {
      new Reimbursement(null, reader);
      fail("No exception for null fiscal year");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting.Reimbursement#hashCode()}
   * . Tests hash codes for the same object
   */
  @Test
  public void testHashCodeEquality() {
    Reimbursement reimbursement1 =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);

    Reimbursement reimbursement2 =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);

    assertTrue("equal reimbursements but different hash codes",
               reimbursement1.hashCode() == reimbursement2.hashCode());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting.Reimbursement#hashCode()}
   * . Tests hash codes for objects that differ by reimbursement year
   */
  @Test
  public void testHashCodeInequalityYear() {
    Reimbursement reimbursement1 =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);

    Reimbursement reimbursement2 =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR_2, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);

    assertTrue("different reimbursements by year but same hash codes",
               reimbursement1.hashCode() != reimbursement2.hashCode());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting.Reimbursement#hashCode()}
   * . Tests hash codes for objects that differ by reimbursement transaction id
   */
  @Test
  public void testHashCodeInequalityId() {
    Reimbursement reimbursement1 =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);

    Reimbursement reimbursement2 =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID_2, AMOUNT,
                        ALLOCATED_AMOUNT);

    assertTrue("different reimbursements by id but same hash codes",
               reimbursement1.hashCode() != reimbursement2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Reimbursement#equals(java.lang.Object)}
   * . Tests same objects for equals()
   */
  @Test
  public void testEqualsObjectEquality() {
    Reimbursement reimbursement1 =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);

    Reimbursement reimbursement2 =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);

    assertTrue("equal reimbursements but not equal", reimbursement1.equals(reimbursement2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Reimbursement#equals(java.lang.Object)}
   * . Tests objects that differ by year for equals()
   */
  @Test
  public void testEqualsObjectInequalityYear() {
    Reimbursement reimbursement1 =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);

    Reimbursement reimbursement2 =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR_2, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);

    assertTrue("different reimbursements but equal", !reimbursement1.equals(reimbursement2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Reimbursement#equals(java.lang.Object)}
   * . Tests objects that differ by reimbursement transaction id for equals()
   */
  @Test
  public void testEqualsObjectInequalityId() {
    Reimbursement reimbursement1 =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);

    Reimbursement reimbursement2 =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID_2, AMOUNT,
                        ALLOCATED_AMOUNT);

    assertTrue("different reimbursements but equal", !reimbursement1.equals(reimbursement2));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting.Reimbursement#toString()}
   * .
   */
  @Test
  public void testToString() {
    Reimbursement reimbursement =
      new Reimbursement(ACCOUNT_NUMBER, REC_YEAR, REC_ID, REIM_YEAR, REIM_ID, AMOUNT,
                        ALLOCATED_AMOUNT);

    assertTrue("wrong string representation: " + reimbursement, reimbursement.toString().equals(
      "Reimbursement [accountNumber=110.0, receivableYear=2016, receivableTransactionId=100, " +
      "reimbursementYear=2017, reimbursementTransactionId=400, reimbursedAmount=100.0, " +
      "allocatedAmount=50.0]"));
  }
}
