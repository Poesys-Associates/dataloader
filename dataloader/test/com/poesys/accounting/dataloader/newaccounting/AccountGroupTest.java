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

import com.poesys.db.InvalidParametersException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * CUT: AccountGroup
 *
 * @author Robert J. Muller
 */
public class AccountGroupTest {
  private static final String NAME = "Cash";
  private static final String OTHER_NAME = "Accounts Receivable";
  private static final String STRING_REP = "AccountGroup [name=" + NAME + "]";

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#AccountGroup(java.lang.String)}
   * . Tests constructor and getters.
   */
  @Test
  public void testAccountGroup() {
    AccountGroup group = new AccountGroup(NAME);
    assertTrue("name getter failed", NAME.equals(group.getName()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#AccountGroup(java.lang.String)}
   * . Tests constructor for required name exception.
   */
  @Test
  public void testAccountGroupRequiredName() {
    try {
      new AccountGroup(null);
      fail("No exception for null name in account group");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception for null name in account group");
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#hashCode()}
   * . Tests for equality of account with same field values.
   */
  @Test
  public void testHashCodeEquality() {
    AccountGroup group = new AccountGroup(NAME);
    AccountGroup otherGroup = new AccountGroup(NAME);
    assertTrue("hash equality failed", group.hashCode() == otherGroup.hashCode());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#hashCode()}
   * . Tests for inequality of account with different name.
   */
  @Test
  public void testHashCodeInequality() {
    AccountGroup group = new AccountGroup(NAME);
    AccountGroup otherGroup = new AccountGroup(OTHER_NAME);
    assertTrue("hash inequality failed", group.hashCode() != otherGroup.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#equals(java.lang.Object)}
   * . Tests whether equals() works for objects defined with same key.
   */
  @Test
  public void testEqualsObject() {
    AccountGroup group = new AccountGroup(NAME);
    AccountGroup otherGroup = new AccountGroup(NAME);
    assertTrue("equals failed with same name", group.equals(otherGroup));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#equals(java.lang.Object)}
   * . Tests whether equals() works for objects defined with different keys.
   */
  @Test
  public void testEqualsDifferentObject() {
    AccountGroup group = new AccountGroup(NAME);
    AccountGroup otherGroup = new AccountGroup("NOT NAME");
    assertTrue("equals failed with different name", !group.equals(otherGroup));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#equals(java.lang.Object)}
   * . Tests whether equals() works for comparing the same object.
   */
  @Test
  public void testEqualsSameObject() {
    AccountGroup group = new AccountGroup(NAME);
    assertTrue("equals failed with same object", group.equals(group));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#equals(java.lang.Object)}
   * . Tests whether equals() works for comparing to a null object.
   */
  @Test
  public void testEqualsNullObject() {
    AccountGroup group = new AccountGroup(NAME);
    assertTrue("equals failed with null", !group.equals(null));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#equals(java.lang.Object)}
   * . Tests whether equals() works for comparing to a different class.
   */
  @Test
  public void testEqualsDifferentClass() {
    AccountGroup group = new AccountGroup(NAME);
    assertTrue("equals failed with null", !group.equals(new AccountTest()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#equals(java.lang.Object)}
   * . Tests whether equals works for objects defined with different names.
   */
  @Test
  public void testNotEqualsObject() {
    AccountGroup group = new AccountGroup(NAME);
    AccountGroup otherGroup = new AccountGroup(OTHER_NAME);
    assertTrue("not equals failed", !group.equals(otherGroup));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#addLink(FiscalYearAccount)}
   * and {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#getFiscalYearAccounts().
   */
  @Test
  public void testFiscalYearAccounts() {
    FiscalYear year = new FiscalYear(2017);
    AccountType type = AccountType.ASSETS;
    AccountGroup group = new AccountGroup(NAME);
    Account account = new Account("name", "description", type, true, false);
    FiscalYearAccount fya = new FiscalYearAccount(year, type, group, 1, account, 1);
    group.addLink(fya);
    List<FiscalYearAccount> list = group.getFiscalYearAccounts();
    assertTrue("no link added", list.size() > 0);
    FiscalYearAccount fya2 = list.get(0);
    assertTrue("wrong link added", fya.equals(fya2));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#toString()}
   * .
   */
  @Test
  public void testToString() {
    AccountGroup group = new AccountGroup(NAME);
    assertTrue("group string representation failed", STRING_REP.equals(group.toString()));
  }
}
