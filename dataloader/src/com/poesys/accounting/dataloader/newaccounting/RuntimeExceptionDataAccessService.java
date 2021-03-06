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
 * Unit test implementation of the IDataAccessService interface that throws runtime exceptions from
 * the methods.
 *
 * @author Robert J. Muller
 */
public class RuntimeExceptionDataAccessService implements IDataAccessService {

  @Override
  public void storeCapitalStructure(CapitalStructure structure) {
    throw new RuntimeException("storeCapitalStructure() runtime exception");
  }

  @Override
  public void storeFiscalYears(List<FiscalYear> years) {
    throw new RuntimeException("storeFiscalYears() runtime exception");
  }

  @Override
  public void storeEntity(String entityName, List<FiscalYear> years) {
    throw new RuntimeException("storeEntity() runtime exception");
  }

  @Override
  public void storeTransactions(Set<Transaction> transactions) {
    throw new RuntimeException("storeTransactions() runtime exception");
  }
}
