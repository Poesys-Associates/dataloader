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
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;

import com.poesys.db.InvalidParametersException;

/**
 * @author Robert J. Muller
 */
public class BalanceTest {
  private static final Integer YEAR = 2017;
  private static final Float ACCOUNT1 = 109.0F;
  private static final Float ACCOUNT2 = 110.0F;
  private static final String STRING_REP =
    "Balance [year=2017, accountNumber=109.0, amount=100.0, debit=true]";
  private static final Double AMOUNT = 100.00D;
  private static final Boolean DEBIT = Boolean.TRUE;

  /**
   * Create a timestamp for the balance date.
   *
   * @return a timestamp
   */
  private Timestamp getDate() {
    Timestamp date = null;
    try {
      String format = "dd-MMM-yy";
      SimpleDateFormat formatter = new SimpleDateFormat(format);
      date = new Timestamp(formatter.parse("1-JAN-2017").getTime());
    } catch (ParseException e) {
      fail("Could not format date 1-JAN-2017");
    }
    return date;
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Balance#Balance(java.lang.Integer, * java.lang.Float, java.sql.Timestamp, java.lang.Double,
   * java.lang.Boolean)} . Tests constructor
   * and getters.
   */
  @Test
  public void testBalance() {
    Balance balance = new Balance(YEAR, ACCOUNT1, getDate(), AMOUNT, DEBIT);

    assertTrue("account not correct: " + balance.getAccountNumber() + ", but expected " + ACCOUNT1,
               balance.getAccountNumber().equals(ACCOUNT1));
    assertTrue("amount not correct: " + balance.getAmount(), balance.getAmount().equals(AMOUNT));
    assertTrue("debit flag not correct: " + balance.isDebit(), balance.isDebit());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Balance#Balance(java.lang.Integer, * java.lang.Float, java.sql.Timestamp, java.lang.Double,
   * java.lang.Boolean)} . Tests reader
   * constructor and getters.
   */
  @Test
  public void testBalanceReaderValid() {
    String input = ACCOUNT1 + "\t" + "1-JAN-2017" + "\t" + "DR" + "\t" + AMOUNT;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    Balance balance = new Balance(YEAR, reader);

    assertTrue("account not correct: " + balance.getAccountNumber() + ", but expected " + ACCOUNT1,
               balance.getAccountNumber().equals(ACCOUNT1));
    assertTrue("amount not correct: " + balance.getAmount(), balance.getAmount().equals(AMOUNT));
    assertTrue("debit flag not correct: " + balance.isDebit(), balance.isDebit());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Balance#Balance(java.lang.Integer, * java.lang.Float, java.sql.Timestamp, java.lang.Double,
   * java.lang.Boolean)} . Tests constructor
   * with null year.
   */
  @Test
  public void testBalanceNullYear() {
    try {
      new Balance(null, ACCOUNT1, getDate(), AMOUNT, DEBIT);
      fail("No exception for null year");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception for null year");
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Balance#Balance(java.lang.Integer, * java.lang.Float, java.sql.Timestamp, java.lang.Double,
   * java.lang.Boolean)} . Tests constructor
   * with null account number.
   */
  @Test
  public void testBalanceNullAccountNumber() {
    try {
      new Balance(YEAR, null, getDate(), AMOUNT, DEBIT);
      fail("No exception for null account number");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception for null account number");
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Balance#Balance(java.lang.Integer, * java.lang.Float, java.sql.Timestamp, java.lang.Double,
   * java.lang.Boolean)} . Tests constructor
   * with null amount.
   */
  @Test
  public void testBalanceNullAmount() {
    try {
      new Balance(YEAR, ACCOUNT1, getDate(), null, DEBIT);
      fail("No exception for null amount");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception for null amount");
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Balance#Balance(java.lang.Integer, * java.lang.Float, java.sql.Timestamp, java.lang.Double,
   * java.lang.Boolean)} . Tests constructor
   * with null balance date.
   */
  @Test
  public void testBalanceNullBalanceDate() {
    try {
      new Balance(YEAR, ACCOUNT1, null, AMOUNT, DEBIT);
      fail("No exception for null balance date");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception for null amount");
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .Balance#Balance(java.lang.Integer, * java.lang.Float, java.sql.Timestamp, java.lang.Double,
   * java.lang.Boolean)} . Tests constructor
   * with null amount.
   */
  @Test
  public void testBalanceNullDebit() {
    try {
      new Balance(YEAR, ACCOUNT1, getDate(), AMOUNT, null);
      fail("No exception for null debit flag");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception for null debit flag");
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting.Balance#hashCode()}.
   */
  @Test
  public void testHashCodeEquality() {
    Balance balance = new Balance(YEAR, ACCOUNT1, getDate(), AMOUNT, DEBIT);
    Balance balance2 = new Balance(YEAR, ACCOUNT1, getDate(), AMOUNT, DEBIT);
    assertTrue("Equal balances have different hash codes",
               balance.hashCode() == balance2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Balance#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsObjectEquality() {
    Balance balance = new Balance(YEAR, ACCOUNT1, getDate(), AMOUNT, DEBIT);
    Balance balance2 = new Balance(YEAR, ACCOUNT1, getDate(), AMOUNT, DEBIT);
    assertTrue("Equal balances test as not equal", balance.equals(balance2));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting.Balance#hashCode()}.
   */
  @Test
  public void testHashCodeInequality() {
    Balance balance = new Balance(YEAR, ACCOUNT1, getDate(), AMOUNT, DEBIT);
    Balance balance2 = new Balance(YEAR, ACCOUNT2, getDate(), AMOUNT, DEBIT);
    assertTrue("Different balances have the sname hash code",
               balance.hashCode() != balance2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.Balance#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsObjectInequality() {
    Balance balance = new Balance(YEAR, ACCOUNT1, getDate(), AMOUNT, DEBIT);
    Balance balance2 = new Balance(YEAR, ACCOUNT2, getDate(), AMOUNT, DEBIT);
    assertTrue("Different balances test as equal", !balance.equals(balance2));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting.Balance#toString()}.
   */
  @Test
  public void testToString() {
    Balance balance = new Balance(YEAR, ACCOUNT1, getDate(), AMOUNT, DEBIT);
    assertTrue("String representation not correct: " + balance,
               balance.toString().equals(STRING_REP));
  }
}
