/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import java.io.BufferedReader;

import com.poesys.db.InvalidParametersException;


/**
 * Represents one of potentially several entities that participate in the
 * capital structure of the accounting entity. This data comes from a single
 * file in the top directory for the accounting entity and applies to all years.
 * The entity has a required capital account and an optional distribution
 * account, and the data supplies the unique names for these accounts, which
 * must correspond to the names in the account.txt or account_map.txt files. The
 * ownership percentage specifies the allocation of income and distributions to
 * the entity; for example, an equal partnership of two people would have 50%
 * ownership for each entity, giving each entity half the income and half the
 * distribution amount.
 * 
 * @author Robert J. Muller
 */
public class CapitalEntity extends AbstractReaderDto {

  /** name of the capital account for the entity */
  private String capitalAccountName;
  /** name of the optional distribution account for the entity */
  private String distributionAccountName;
  /** decimal percentage ownership of the accounting entity (e.g., .5 for 50%) */
  private Double ownership;

  /**
   * Create a CapitalEntity object.
   * 
   * @param capitalAccountName the name of the capital account for the entity
   * @param distributionAccountName the name of the distribution account for the
   *          entity
   * @param ownership the decimal percentage ownership of the accounting entity
   *          (for example, .50 = 50% ownership for an equal partner); default
   *          is 1 (100%)
   */
  public CapitalEntity(String capitalAccountName,
                       String distributionAccountName,
                       Double ownership) {
    if (capitalAccountName == null || capitalAccountName.isEmpty()) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
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
   * @return a deciaml percentage (for example, .50 for 50%)
   */
  public Double getOwnership() {
    return ownership;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
      prime * result
          + ((capitalAccountName == null) ? 0 : capitalAccountName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CapitalEntity other = (CapitalEntity)obj;
    if (capitalAccountName == null) {
      if (other.capitalAccountName != null)
        return false;
    } else if (!capitalAccountName.equals(other.capitalAccountName))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "CapitalEntity [capitalAccountName=" + capitalAccountName
           + ", distributionAccountName=" + distributionAccountName
           + ", ownership=" + ownership + "]";
  }

  @Override
  protected void init(String[] fields) {
    capitalAccountName = fields[0].trim();
    if (capitalAccountName == null || capitalAccountName.isEmpty()) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    distributionAccountName = fields[1].trim();
    if (fields[2].isEmpty()) {
      ownership = 1.0D;
    } else {
      ownership = new Double(fields[2]);
    }
  }

  @Override
  protected int numberOfFields() {
    return 3;
  }
}