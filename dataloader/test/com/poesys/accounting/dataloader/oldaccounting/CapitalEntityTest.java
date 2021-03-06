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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.Test;

import com.poesys.db.InvalidParametersException;

/**
 * CUT: CapitalEntity
 *
 * @author Robert J. Muller
 */
public class CapitalEntityTest {
  private static final String NAME = "Personal Accounts";
  private static final String CAP_ENTITY_1 = "Partner 1";
  private static final String CAP_ACCOUNT_1 = "Partner 1 Capital";
  private static final String DIST_ACCOUNT_1 = "Partner 1 Distributions";
  private static final Double OWNERSHIP_1 = 1.0D;
  private static final String CAP_ENTITY_2 = "Partner 2";
  private static final String CAP_ACCOUNT_2 = "Partner 2 Capital";
  private static final String DIST_ACCOUNT_2 = "Partner 2 Distributions";
  private static final Double OWNERSHIP_2 = 0.5D;

  private static final String DELIM = "\t";

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .CapitalEntity#CapitalEntity(java.lang.String, * java.lang.String, java.lang.String, java
   * .lang.Double)} . Tests constructor and getters.
   */
  @Test
  public void testCapitalEntityStringStringDouble() {
    CapitalEntity entity =
      new CapitalEntity(CAP_ENTITY_1, CAP_ACCOUNT_1, DIST_ACCOUNT_1, OWNERSHIP_1);
    assertTrue("no entity created", entity != null);
    assertTrue("wrong capital entity name: " + entity.getName(),
               entity.getName().equals(CAP_ENTITY_1));
    assertTrue("wrong capital account name: " + entity.getCapitalAccountName(),
               entity.getCapitalAccountName().equals(CAP_ACCOUNT_1));
    assertTrue("wrong distribution account name: " + entity.getDistributionAccountName(),
               entity.getDistributionAccountName().equals(DIST_ACCOUNT_1));
    assertTrue("wrong ownership pctg: " + entity.getOwnership(),
               entity.getOwnership().equals(OWNERSHIP_1));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .CapitalEntity#CapitalEntity(java.lang.String, * java.lang.String, java.lang.String, java
   * .lang.Double)} . Tests constructor with null capital
   * account name.
   */
  @Test
  public void testCapitalEntityStringStringDoubleNullCap() {
    try {
      new CapitalEntity(NAME, null, DIST_ACCOUNT_1, OWNERSHIP_1);
      fail("missing capital entity name did not throw exception");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .CapitalEntity#CapitalEntity(java.lang.String, * java.lang.String, java.lang.String, java
   * .lang.Double)} . Tests constructor with empty capital
   * account name.
   */
  @Test
  public void testCapitalEntityStringStringDoubleEmptyCap() {
    try {
      new CapitalEntity(NAME, "", DIST_ACCOUNT_1, OWNERSHIP_1);
      fail("empty capital entity name did not throw exception");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting
   * .CapitalEntity#CapitalEntity(java.lang.String, * java.lang.String, java.lang.String, java
   * .lang.Double)} . Tests constructor with null
   * ownership.
   */
  @Test
  public void testCapitalEntityStringStringDoubleNullOwnership() {
    CapitalEntity entity = new CapitalEntity(NAME, CAP_ACCOUNT_1, DIST_ACCOUNT_1, null);
    assertTrue("wrong default ownership pctg: " + entity.getOwnership(),
               entity.getOwnership().equals(1.0D));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.CapitalEntity#CapitalEntity(java.io.BufferedReader)}
   * . Tests constructor and getters.
   */
  @Test
  public void testCapitalEntityValidBufferedReader() {
    String input = CAP_ENTITY_1 + DELIM + CAP_ACCOUNT_1 + DELIM + DIST_ACCOUNT_1 + DELIM +
                   OWNERSHIP_1.toString();
    BufferedReader reader = new BufferedReader(new StringReader(input));
    CapitalEntity entity = new CapitalEntity(reader);
    assertTrue("no entity created", entity != null);
    assertTrue("wrong capital account name: " + entity.getCapitalAccountName(),
               entity.getCapitalAccountName().equals(CAP_ACCOUNT_1));
    assertTrue("wrong distribution account name: " + entity.getDistributionAccountName(),
               entity.getDistributionAccountName().equals(DIST_ACCOUNT_1));
    assertTrue("wrong ownership pctg: " + entity.getOwnership(),
               entity.getOwnership().equals(OWNERSHIP_1));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.CapitalEntity#CapitalEntity(java.io.BufferedReader)}
   * . Tests constructor with null capital account name.
   */
  @Test
  public void testCapitalEntityValidBufferedReaderNullCap() {
    String input = DELIM + CAP_ACCOUNT_1 + DELIM + DIST_ACCOUNT_1 + OWNERSHIP_1.toString();
    BufferedReader reader = new BufferedReader(new StringReader(input));
    try {
      new CapitalEntity(reader);
      fail("missing capital entity name did not throw exception");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.CapitalEntity#CapitalEntity(java.io.BufferedReader)}
   * . Tests constructor with null ownership.
   */
  @Test
  public void testCapitalEntityValidBufferedReaderNullOwnership() {
    String input = CAP_ACCOUNT_1 + DELIM + CAP_ACCOUNT_1 + DELIM + DIST_ACCOUNT_1 + DELIM;
    BufferedReader reader = new BufferedReader(new StringReader(input));
    CapitalEntity entity = new CapitalEntity(reader);
    assertTrue("wrong default ownership pctg: " + entity.getOwnership(),
               entity.getOwnership().equals(1.0D));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting.CapitalEntity#hashCode()}
   * .
   */
  @Test
  public void testHashCodeEquality() {
    CapitalEntity entity1 =
      new CapitalEntity(CAP_ENTITY_1, CAP_ACCOUNT_1, DIST_ACCOUNT_1, OWNERSHIP_1);
    CapitalEntity entity2 =
      new CapitalEntity(CAP_ENTITY_1, CAP_ACCOUNT_1, DIST_ACCOUNT_1, OWNERSHIP_1);
    assertTrue("same objects do not have same hash code", entity1.hashCode() == entity2.hashCode());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting.CapitalEntity#hashCode()}
   * .
   */
  @Test
  public void testHashCodeInequality() {
    CapitalEntity entity1 =
      new CapitalEntity(CAP_ENTITY_1, CAP_ACCOUNT_1, DIST_ACCOUNT_1, OWNERSHIP_1);
    CapitalEntity entity2 =
      new CapitalEntity(CAP_ENTITY_2, CAP_ACCOUNT_2, DIST_ACCOUNT_2, OWNERSHIP_2);
    assertTrue("different objects have same hash code", entity1.hashCode() != entity2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.CapitalEntity#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsEquality() {
    CapitalEntity entity1 =
      new CapitalEntity(CAP_ENTITY_1, CAP_ACCOUNT_1, DIST_ACCOUNT_1, OWNERSHIP_1);
    CapitalEntity entity2 =
      new CapitalEntity(CAP_ENTITY_1, CAP_ACCOUNT_1, DIST_ACCOUNT_1, OWNERSHIP_1);
    assertTrue("same objects are not equal", entity1.equals(entity2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.oldaccounting.CapitalEntity#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testEqualsInquality() {
    CapitalEntity entity1 =
      new CapitalEntity(CAP_ENTITY_1, CAP_ACCOUNT_1, DIST_ACCOUNT_1, OWNERSHIP_1);
    CapitalEntity entity2 =
      new CapitalEntity(CAP_ENTITY_2, CAP_ACCOUNT_2, DIST_ACCOUNT_2, OWNERSHIP_2);
    assertTrue("different objects are equal", !entity1.equals(entity2));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.oldaccounting.CapitalEntity#toString()}
   * .
   */
  @Test
  public void testToString() {
    CapitalEntity entity1 = new CapitalEntity(NAME, CAP_ACCOUNT_1, DIST_ACCOUNT_1, OWNERSHIP_1);
    assertTrue("string rep not correct: " + entity1.toString(),
               ("CapitalEntity [name=Personal Accounts, capitalAccountName=Partner 1 Capital, " +
                "distributionAccountName=Partner 1 Distributions, ownership=1.0]").equals(
                 entity1.toString()));
  }
}
