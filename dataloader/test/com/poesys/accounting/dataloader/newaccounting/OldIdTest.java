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
    assertTrue("wrong name: " + oldId.getEntityName(),
               NAME.equals(oldId.getEntityName()));
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
    String stringRep =
      "OldId [entityName=" + NAME + ", year=" + YEAR + ", id=" + ID + "]";
    assertTrue("wrong string representation: " + oldId + ", should be \""
               + stringRep + "\"", oldId.toString().equals(stringRep));
  }
}
