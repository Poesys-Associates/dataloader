/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.math.BigDecimal;


/**
 * Helper class used to create a temporary map of old transaction ids to new
 * transaction ids
 * 
 * @author Robert J. Muller
 */
public class OldId {
  /** the name of the accounting entity */
  private final String entityName;
  /** the fiscal year */
  private final Integer year;
  /** the old-accounting transaction id */
  private final BigDecimal id;

  // messages
  private static final String NULL_ID_ERROR = "null id for ID map entry";
  private static final String NULL_YEAR_ERROR = "null year for ID map entry";
  private static final String NULL_ENTITY_ERROR =
    "null entity name for ID map entry";

  /**
   * Create an OldId indexing object with an entity, a fiscal year, and a
   * transaction id.
   * 
   * @param entityName the name of the accounting entity with the ids
   * @param year the fiscal year number
   * @param id the old transaction id number
   */
  public OldId(String entityName, Integer year, BigDecimal id) {
    // Verify input existence
    if (entityName == null) {
      throw new RuntimeException(NULL_ENTITY_ERROR);
    }
    if (year == null) {
      throw new RuntimeException(NULL_YEAR_ERROR);
    }
    if (id == null) {
      throw new RuntimeException(NULL_ID_ERROR);
    }
    this.entityName = entityName;
    this.year = year;
    this.id = id;
  }

  /**
   * Get the entity name.
   * 
   * @return an entity name
   */
  String getEntityName() {
    return entityName;
  }

  /**
   * Get the year.
   * 
   * @return a year
   */
  Integer getYear() {
    return year;
  }

  /**
   * Get the id.
   * 
   * @return an id
   */
  BigDecimal getId() {
    return id;
  }

  @Override
  public String toString() {
    return "OldId [entityName=" + entityName + ", year=" + year + ", id="
           + id.toBigInteger() + "]";
  }
}
