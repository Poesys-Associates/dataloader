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
package com.poesys.accounting.dataloader.newaccounting;


/* Copyright (c) 2017 Poesys Associates. All rights reserved. */

import com.poesys.db.InvalidParametersException;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * CUT: FiscalYear
 *
 * @author Robert J. Muller
 */
public class FiscalYearTest {
  private static final Logger logger = Logger.getLogger(FiscalYearTest.class);
  private static final Integer YEAR = 2017;
  private static final Integer PRIOR_YEAR = 2016;
  private static final Integer LATER_YEAR = 2018;

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#FiscalYear(java.lang.Integer)}
   * . Tests constructor and getters.
   */
  @Test
  public void testFiscalYear() {
    FiscalYear year = new FiscalYear(YEAR);
    assertTrue("wrong year", Objects.equals(year.getYear(), YEAR));
    assertTrue("no start date", year.getStart() != null);
    assertTrue("no end date", year.getEnd() != null);
    Timestamp start = Timestamp.valueOf(YEAR + "-01-01 00:00:00");
    Timestamp end = Timestamp.valueOf(YEAR + "-12-31 23:59:59");
    assertTrue("wrong start date", start.equals(year.getStart()));
    assertTrue("wrong end date", end.equals(year.getEnd()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#FiscalYear(java.lang.Integer)}
   * . Tests exception from constructor with null date.
   */
  @Test
  public void testFiscalYearNullYear() {
    try {
      new FiscalYear(null);
      fail("null input year but no exception");
    } catch (InvalidParametersException e) {
      assertTrue("null input year with wrong message: " + e.getMessage(),
                 e.getMessage().equals("FiscalYear parameters are required but one is null"));
    } catch (Exception e) {
      fail("Wrong exception from null input year: " + e.getMessage());
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#hashCode()} .
   */
  @Test
  public void testHashCodeEquality() {
    FiscalYear year1 = new FiscalYear(YEAR);
    FiscalYear year2 = new FiscalYear(YEAR);
    assertTrue("same year but different hash code", year1.hashCode() == year2.hashCode());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#hashCode()} .
   */
  @Test
  public void testHashCodeInequality() {
    FiscalYear year1 = new FiscalYear(YEAR);
    FiscalYear year2 = new FiscalYear(PRIOR_YEAR);
    assertTrue("different year but same hash code", year1.hashCode() != year2.hashCode());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#equals(Object
   * obj)} .
   */
  @Test
  public void testEqualsEquality() {
    FiscalYear year1 = new FiscalYear(YEAR);
    FiscalYear year2 = new FiscalYear(YEAR);
    assertTrue("same year but equals not true", year1.equals(year2));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#equals(Object
   * obj)} .
   */
  @Test
  public void testEqualsInequality() {
    FiscalYear year1 = new FiscalYear(YEAR);
    FiscalYear year2 = new FiscalYear(PRIOR_YEAR);
    assertTrue("different year but equals true", !year1.equals(year2));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#equals(Object
   * obj)} . Tests comparison to same object being true.
   */
  @Test
  public void testEqualsSameObject() {
    FiscalYear year1 = new FiscalYear(YEAR);
    //noinspection EqualsWithItself
    assertTrue("same fiscal year object but equals false", year1.equals(year1));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#equals(Object
   * obj)} . Tests null comparison object being false.
   */
  @Test
  public void testEqualsNullObject() {
    FiscalYear year1 = new FiscalYear(YEAR);
    assertTrue("null year but equals true", !year1.equals(null));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#equals(Object
   * obj)} . Tests comparison to different class object being false.
   */
  @Test
  public void testEqualsDifferentClass() {
    FiscalYear year1 = new FiscalYear(YEAR);
    assertTrue("different class but equal", !year1.equals(new Integer(2017)));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#isIn(java.sql.Timestamp)}
   * .
   */
  @Test
  public void testIsIn() {
    FiscalYear year = new FiscalYear(YEAR);
    Timestamp date = Timestamp.valueOf("2017-05-01 00:00:00");
    assertTrue("date in year but isIn is false", year.isIn(date));
    date = Timestamp.valueOf("2016-05-01 00:00:00");
    assertTrue("date not in year but isIn is true", !year.isIn(date));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#isIn(java.sql.Timestamp)}
   * . Tests exception from null input date.
   */
  @Test
  public void testIsInNull() {
    FiscalYear year = new FiscalYear(YEAR);
    try {
      year.isIn(null);
      fail("no exception from null input date to isIn()");
    } catch (InvalidParametersException e) {
      // success
      assertTrue("null input date but wrong exception: " + e.getMessage(),
                 e.getMessage().equals("date for comparison is null but is required"));
    } catch (Exception e) {
      fail("null input date but wrong exception: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#isInYearOrPriorYear(java.sql.Timestamp)}
   * . Tests where input date is in year.
   */
  @Test
  public void testIsInYearOrPriorYearSameYear() {
    FiscalYear year = new FiscalYear(YEAR);
    Timestamp date = Timestamp.valueOf(YEAR + "-05-01 00:00:00");
    assertTrue("date in year but isInYearOrPriorYear is false", year.isInYearOrPriorYear(date));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#isInYearOrPriorYear(java.sql.Timestamp)}
   * . Tests where input date is in previous year.
   */
  @Test
  public void testIsInYearOrPriorYearPriorYear() {
    FiscalYear year = new FiscalYear(YEAR);
    Timestamp date = Timestamp.valueOf(PRIOR_YEAR + "-05-01 00:00:00");
    assertTrue("date in prior year but isInYearOrPriorYear is false",
               year.isInYearOrPriorYear(date));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#isInYearOrPriorYear(java.sql.Timestamp)}
   * . Tests where input date is in later year.
   */
  @Test
  public void testIsInYearOrPriorYearLaterYear() {
    FiscalYear year = new FiscalYear(YEAR);
    Timestamp date = Timestamp.valueOf(LATER_YEAR + "-05-01 00:00:00");
    assertTrue("date in later year but isInYearOrPriorYear is true",
               !year.isInYearOrPriorYear(date));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#isInYearOrPriorYear(java.sql.Timestamp)}
   * . Tests exception from null comparison year.
   */
  @Test
  public void testIsInYearOrPriorYearNullYear() {
    FiscalYear year = new FiscalYear(YEAR);
    try {
      year.isInYearOrPriorYear(null);
      fail("null date but no exception");
    } catch (InvalidParametersException e) {
      assertTrue(e.getMessage().equals("date for comparison is null but is required"));
    } catch (Exception e) {
      fail("Wrong exception from null input date: " + e.getMessage());
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#toString()} .
   */
  @Test
  public void testToString() {
    FiscalYear year = new FiscalYear(YEAR);
    assertTrue("toString not correct: " + year.toString(),
               "FiscalYear [year=2017, start=2017-01-01 00:00:00.0, end=2017-12-31 23:59:59.0]"
                 .equals(
                 year.toString()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#addAccount(com.poesys.accounting.dataloader.newaccounting.FiscalYearAccount)}
   * . Tests addAccount() and getAccount().
   */
  @Test
  public void testAddAccount() {
    FiscalYear year = new FiscalYear(YEAR);
    AccountGroup group = new AccountGroup("Income");
    Account account = new Account("Income", "Income", AccountType.INCOME, true, false);
    FiscalYearAccount fya = new FiscalYearAccount(year, AccountType.INCOME, group, 1, account, 1);
    account.addYear(fya);
    year.addAccount(fya);
    List<FiscalYearAccount> list = year.getAccounts();
    assertTrue("no set of accounts", list != null);
    boolean found = false;
    for (FiscalYearAccount acct : list) {
      if (acct.equals(fya)) {
        found = true;
      }
    }
    assertTrue("account not found", found);

    // Verify that account has fiscal year in list of fiscal years.
    found = false;
    for (FiscalYearAccount accountYear : account.getYears()) {
      if (year.equals(accountYear.getFiscalYear())) {
        found = true;
        break;
      }
    }
    assertTrue("fiscal year not in account year list: " + year, found);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#addAccount(com.poesys.accounting.dataloader.newaccounting.FiscalYearAccount)}
   * . Tests addAccount() with null account parameter.
   */
  @Test
  public void testAddAccountNullAccount() {
    FiscalYear year = new FiscalYear(YEAR);
    try {
      year.addAccount(null);
      fail("did not throw invalid parameters exception with null account added");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#getNextId()}
   * . Tests the trio of methods for ids with a standard scenario: set id, get id, get next id.
   */
  @Test
  public void testNextId() {
    BigInteger lastId = new BigInteger("100");
    BigInteger nextId = lastId.add(BigInteger.ONE);
    FiscalYear year = new FiscalYear(YEAR);
    year.setLastId(lastId);
    assertTrue("year does not have correct id: " + year.getId(), lastId.equals(year.getId()));
    assertTrue("did not get correct next id", nextId.equals(year.getNextId()));
    assertTrue("year id not set to correct next id", year.getId().equals(nextId));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#getId()}
   * . Tests the methods getID and getNextId for ids with a default scenario: get id, get next id.
   */
  @Test
  public void testNextDefaultId() {
    BigInteger lastId = BigInteger.ZERO;
    BigInteger nextId = BigInteger.ONE;
    FiscalYear year = new FiscalYear(YEAR);
    assertTrue("year does not have correct id zero: " + year.getId(), lastId.equals(year.getId()));
    assertTrue("did not get correct next id one", nextId.equals(year.getNextId()));
    assertTrue("year id not set to correct next id", year.getId().equals(nextId));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.FiscalYear#setLastId(BigInteger)}
   * . Tests the methods getID and getNextId for ids with a default scenario: get id, get next id.
   */
  @Test
  public void testSetNextNullId() {
    FiscalYear year = new FiscalYear(YEAR);
    try {
      year.setLastId(null);
      fail("setLastId() with null id did not throw exception");
    } catch (InvalidParametersException e) {
      // success
      assertTrue("wrong exception: " + e.getMessage(),
                 e.getMessage().equals("no last id supplied but one is required"));
    } catch (NullPointerException e) {
      String message = "setLastId() with null id threw null pointer exception";
      logger.error(message, e);
      fail(message);
    } catch (Exception e) {
      fail("setLastId() with null id threw wrong exception: " + e.getMessage());
      throw e;
    }
  }
}
