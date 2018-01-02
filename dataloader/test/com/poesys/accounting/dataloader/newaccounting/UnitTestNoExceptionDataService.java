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
 * Implementation of the IDataAccessService interface for unit testing; throws no exceptions and
 * tracks calls to the methods.
 *
 * @author Robert J. Muller
 */
public class UnitTestNoExceptionDataService implements IDataAccessService {
  private int storeCapitalStructureCalls = 0;
  private int storeFiscalYearsCalls = 0;
  private int storeEntityCalls = 0;
  private int storeTransactionCalls = 0;

  @Override
  public void storeCapitalStructure(CapitalStructure structure) {
    storeCapitalStructureCalls++;
  }

  @Override
  public void storeFiscalYears(List<FiscalYear> years) {
    storeFiscalYearsCalls++;
  }

  @Override
  public void storeEntity(String entityName, List<FiscalYear> years) {
    storeEntityCalls++;
  }

  @Override
  public void storeTransactions(Set<Transaction> transactions) {
    storeTransactionCalls++;
  }

  /**
   * Get the count of storeCapitalStructure calls.
   *
   * @return a count
   */
  public int getStoreCapitalStructureCalls() {
    return storeCapitalStructureCalls;
  }

  /**
   * Get the count of storeEntity() calls.
   *
   * @return a count
   */
  public int getStoreEntityCalls() {
    return storeEntityCalls;
  }

  /**
   * Get the count of storeTransaction() calls.
   *
   * @return a count
   */
  public int getStoreTransactionCalls() {
    return storeTransactionCalls;
  }

  /**
   * Get the count of storeFiscalYears calls.
   *
   * @return a count
   */
  public int getStoreFiscalYearsCalls() {
    return storeFiscalYearsCalls;
  }
}
