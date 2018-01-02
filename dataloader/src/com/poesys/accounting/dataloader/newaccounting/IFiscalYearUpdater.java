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
 * Interface for Strategy pattern that updates a FiscalYear with additional elements based on the
 * build process implemented by IBuilder implementations; specific implementations apply to the
 * different entities, each of which may have a unique situation to handle
 *
 * @author Robert J. Muller
 */
public interface IFiscalYearUpdater {
  /**
   * Update a fiscal year with additional constructs based on the built content of the fiscal year.
   *
   * @param fiscalYear   the fiscal year to update
   * @param transactions the set of transactions to update
   * @param builder      the Builder containing built elements of the accounting system that the
   *                     Updater will use to update the fiscal year
   */
  void update(FiscalYear fiscalYear, Set<Transaction> transactions, IBuilder builder);
}
