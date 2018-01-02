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

import java.io.BufferedReader;

import com.poesys.db.InvalidParametersException;

/**
 * Represents one of potentially several entities that participate in the capital structure of the
 * accounting entity. This data comes from a single file in the top directory for the accounting
 * entity and applies to all years. The entity has a required capital account and an optional
 * distribution account, and the data supplies the unique names for these accounts, which must
 * correspond to the names in the account.txt or account_map.txt files. The ownership percentage
 * specifies the allocation of income and distributions to the entity; for example, an equal
 * partnership of two people would have 50% ownership for each entity, giving each entity half the
 * income and half the distribution amount.
 *
 * @author Robert J. Muller
 */
public class CapitalEntity extends AbstractReaderDto {
  /** name of the capital entity */
  private String name;
  /** name of the capital account for the entity */
  private String capitalAccountName;
  /** name of the optional distribution account for the entity */
  private String distributionAccountName;
  /** decimal percentage ownership of the accounting entity (e.g., .5 for 50%) */
  private Double ownership;

  /**
   * Create a CapitalEntity object.
   *
   * @param name                    the name of the capital entity
   * @param capitalAccountName      the name of the capital account for the entity
   * @param distributionAccountName the name of the distribution account for the entity
   * @param ownership               the decimal percentage ownership of the accounting entity (for
   *                                example, .50 = 50% ownership for an equal partner); default is 1
   *                                (100%)
   */
  public CapitalEntity(String name, String capitalAccountName, String distributionAccountName,
                       Double ownership) {
    if (capitalAccountName == null || capitalAccountName.isEmpty()) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.name = name;
    this.capitalAccountName = capitalAccountName;
    this.distributionAccountName = distributionAccountName;
    this.ownership = ownership != null ? ownership : 1.00D;
  }

  /**
   * Create a CapitalEntity object using a Reader.
   *
   * @param reader the reader
   */
  public CapitalEntity(BufferedReader reader) {
    super(reader);
  }

  /**
   * Get the name.
   *
   * @return a name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name.
   *
   * @param name a name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the capital account mame.
   *
   * @return a name
   */
  public String getCapitalAccountName() {
    return capitalAccountName;
  }

  /**
   * Get the distribution account name.
   *
   * @return a name
   */
  public String getDistributionAccountName() {
    return distributionAccountName;
  }

  /**
   * Get the ownership percentage.
   *
   * @return a decimal percentage (for example, .50 for 50%)
   */
  public Double getOwnership() {
    return ownership;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CapitalEntity other = (CapitalEntity)obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "CapitalEntity [name=" + name + ", capitalAccountName=" + capitalAccountName +
           ", distributionAccountName=" + distributionAccountName + ", ownership=" + ownership +
           "]";
  }

  @Override
  protected void init(String[] fields) {
    name = fields[0].trim();
    capitalAccountName = fields[1].trim();
    distributionAccountName = fields[2].trim();
    if (fields[3].isEmpty()) {
      ownership = 1.0D;
    } else {
      ownership = new Double(fields[3]);
    }
  }

  @Override
  protected int numberOfFields() {
    return 4;
  }
}
