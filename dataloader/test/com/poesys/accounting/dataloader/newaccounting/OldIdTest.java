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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;

import com.poesys.db.StringUtilities;

/**
 * CUT: OldId
 *
 * @author Robert J. Muller
 */
public class OldIdTest {
  private static final String NAME = StringUtilities.generateString(50);
  private static final Integer YEAR = 2017;
  private static final BigDecimal ID = BigDecimal.ONE;

  /**
   * Test constructor and getters
   */
  @Test
  public void testOldId() {
    OldId oldId = new OldId(NAME, YEAR, ID);
    assertTrue("could not create old id", oldId != null);
    assertTrue("wrong name: " + oldId.getEntityName(), NAME.equals(oldId.getEntityName()));
    assertTrue("wrong year: " + oldId.getYear(), YEAR.equals(oldId.getYear()));
    assertTrue("wrong id: " + oldId.getId(), ID.equals(oldId.getId()));
  }

  /**
   * Test constructor error on null entity name
   */
  @Test
  public void testOldIdNoEntityName() {
    try {
      new OldId(null, YEAR, ID);
      fail("null entity name does not throw exception");
    } catch (RuntimeException e) {
      // success
    }
  }

  /**
   * Test constructor error on null year
   */
  @Test
  public void testOldIdNoYear() {
    try {
      new OldId(NAME, null, ID);
      fail("null year does not throw exception");
    } catch (RuntimeException e) {
      // success
    }
  }

  /**
   * Test constructor error on null id
   */
  @Test
  public void testOldIdNoId() {
    try {
      new OldId(NAME, YEAR, null);
      fail("null id does not throw exception");
    } catch (RuntimeException e) {
      // success
    }
  }

  /**
   * Test toString()
   */
  @Test
  public void testToString() {
    OldId oldId = new OldId(NAME, YEAR, ID);
    String stringRep = "OldId [entityName=" + NAME + ", year=" + YEAR + ", id=" + ID + "]";
    assertTrue("wrong string representation: " + oldId + ", should be \"" + stringRep + "\"",
               oldId.toString().equals(stringRep));
  }
}
