/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import com.poesys.db.InvalidParametersException;


/**
 * A data transfer object containing data about a category of accounts, such as
 * Cash or Accounts Receivable; each account has an associated group, but the
 * group does not track the accounts that it groups
 * 
 * @author Robert J. Muller
 */
public class AccountGroup {
  /** the group name */
  private final String name;

  // Messages

  /** null parameter to constructor */
  private static final String NULL_PARAMETER_ERROR =
    "AccountGroup parameters are required but one is null";

  /**
   * Create a AccountGroup object.
   * 
   * @param name the group name
   */
  public AccountGroup(String name) {
    if (name == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }
    this.name = name;
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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AccountGroup other = (AccountGroup)obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  /**
   * Get the group name.
   * 
   * @return a group name
   */
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "AccountGroup [name=" + name + "]";
  }
}
