/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.poesys.db.InvalidParametersException;


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
    assertTrue("group constructor failed", group != null);
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
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#hashCode()}
   * . Tests for equality of account with same field values.
   */
  @Test
  public void testHashCodeEquality() {
    AccountGroup group = new AccountGroup(NAME);
    AccountGroup otherGroup = new AccountGroup(NAME);
    assertTrue("hash equality failed",
               group.hashCode() == otherGroup.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#hashCode()}
   * . Tests for inequality of account with different name.
   */
  @Test
  public void testHashCodeInequality() {
    AccountGroup group = new AccountGroup(NAME);
    AccountGroup otherGroup = new AccountGroup(OTHER_NAME);
    assertTrue("hash inequality failed",
               group.hashCode() != otherGroup.hashCode());
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
    assertTrue("equals failed", group.equals(otherGroup));
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
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountGroup#toString()}
   * .
   */
  @Test
  public void testToString() {
    AccountGroup group = new AccountGroup(NAME);
    assertTrue("group string representation failed",
               STRING_REP.equals(group.toString()));
  }
}
