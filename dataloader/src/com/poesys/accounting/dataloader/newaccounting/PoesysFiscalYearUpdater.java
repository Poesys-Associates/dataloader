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
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Set;

/**
 * Implementation of the IFiscalYearUpdater interface that updates fiscal years for the Poesys
 * accounting system. The update method in this case produces the capital closing transactions
 * (income to capital, distributions from capital) for the system.
 *
 * @author Robert J. Muller
 */
public class PoesysFiscalYearUpdater implements IFiscalYearUpdater {
  private static final Logger logger = Logger.getLogger(PoesysFiscalYearUpdater.class);

  @Override
  public void update(FiscalYear fiscalYear, Set<Transaction> transactions, Set<Transaction> currentTransactions, IBuilder builder) {
    // Update the fiscal year with closing transactions.

    CapitalStructure capStruct = builder.getCapitalStructure();

    // Add a transaction to transfer net income to capital.
    Transaction capitalTransaction = capStruct.getIncomeToCapitalTransaction(fiscalYear, builder);
    transactions.add(capitalTransaction);

    // Add transactions to close the distributions accounts by removing the distributions from the capital accounts.
    List<Transaction> distTransactions = capStruct.getDistributionTransactions(fiscalYear, builder);
    if (!distTransactions.isEmpty()) {
      logger.debug("Adding distribution adjustment transactions: " + distTransactions);
      transactions.addAll(distTransactions);
    }

    // Ensure that the capital accounts are nearly equal. Create a balance sheet
    // and a distributor.
    Transaction adjustingTransaction = capStruct.getCapitalAdjustmentTransaction(builder);
    if (adjustingTransaction != null) {
      transactions.add(adjustingTransaction);
      logger.debug("Adding capital adjustment transactions: " + distTransactions);
    }
  }
}
