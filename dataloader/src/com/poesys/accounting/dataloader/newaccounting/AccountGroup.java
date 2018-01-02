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

import java.util.ArrayList;
import java.util.List;

import com.poesys.db.InvalidParametersException;

/**
 * A data transfer object containing data about a category of accounts, such as Cash or Accounts
 * Receivable; each account has an associated group, but the group does not track the accounts that
 * it groups
 *
 * @author Robert J. Muller
 */
public class AccountGroup {
  /** the group name */
  private final String name;
  /** links between accounts and fiscal years that associate with this group */
  private final List<FiscalYearAccount> fiscalYearAccounts = new ArrayList<>();

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
    result = prime * result + name.hashCode();
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
    return this.hashCode() == obj.hashCode();
  }

  /**
   * Get the group name.
   *
   * @return a group name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the fiscalYearAccounts.
   *
   * @return a fiscalYearAccounts
   */
  public List<FiscalYearAccount> getFiscalYearAccounts() {
    return fiscalYearAccounts;
  }

  /**
   * Add a fiscal year account object that associates this group to an account-fiscal-year link.
   *
   * @param fiscalYearAccount the link to associate to this group
   */
  public void addLink(FiscalYearAccount fiscalYearAccount) {
    fiscalYearAccounts.add(fiscalYearAccount);
  }

  @Override
  public String toString() {
    return "AccountGroup [name=" + name + "]";
  }
}
