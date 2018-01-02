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

import java.util.List;
import java.util.Set;

/**
 * Implementation of the IStorageManager interface for unit testing; throws no exceptions and tracks
 * calls to the methods.
 *
 * @author Robert J. Muller
 */
public class UnitTestNoExceptionsStorageManager implements IStorageManager {
  private int validateCalls = 0;
  private int storeCalls = 0;

  @Override
  public Boolean validate(List<FiscalYear> years) {
    validateCalls++;
    return Boolean.TRUE;
  }

  @Override
  public void store(String entityName, CapitalStructure structure, List<FiscalYear> years,
                    Set<Transaction> transactions, IDataAccessService storageService) {
    storeCalls++;
  }

  /**
   * Get the count of the calls to validate().
   *
   * @return a count
   */
  public int getValidateCalls() {
    return validateCalls;
  }

  /**
   * Get the count of the calls to store().
   *
   * @return a count
   */
  public int getStoreCalls() {
    return storeCalls;
  }
}
