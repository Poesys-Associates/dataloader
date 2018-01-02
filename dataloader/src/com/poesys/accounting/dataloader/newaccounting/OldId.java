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

import java.math.BigDecimal;

/**
 * Helper class used to create a temporary map of old transaction ids to new transaction ids
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
  private static final String NULL_ENTITY_ERROR = "null entity name for ID map entry";

  /**
   * Create an OldId indexing object with an entity, a fiscal year, and a transaction id.
   *
   * @param entityName the name of the accounting entity with the ids
   * @param year       the fiscal year number
   * @param id         the old transaction id number
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
    return "OldId [entityName=" + entityName + ", year=" + year + ", id=" + id.toBigInteger() + "]";
  }
}
