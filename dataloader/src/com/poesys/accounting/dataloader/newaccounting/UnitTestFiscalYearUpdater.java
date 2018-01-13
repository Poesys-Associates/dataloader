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

import com.poesys.accounting.dataloader.IBuilder;

import java.util.Set;

/**
 * Implementation of the IFiscalYearUpdater interface that updates fiscal years for the Robert J.
 * Muller and Mary L. Swanson accounting system. The update method in this case produces the capital
 * closing transaction (income to capital) for the system.
 *
 * @author Robert J. Muller
 */
public class UnitTestFiscalYearUpdater implements IFiscalYearUpdater {
  @Override
  public void update(FiscalYear fiscalYear, Set<Transaction> transactions, Set<Transaction> currentTransactions, IBuilder builder) {
    // Does nothing for unit test
  }
}
