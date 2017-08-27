/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import com.poesys.accounting.dataloader.IBuilder;
import com.poesys.accounting.dataloader.properties.IParameters;


/**
 * Implementation of the IFiscalYearUpdater interface that updates fiscal years
 * for the Robert J. Muller and Mary L. Swanson accounting system. The update
 * method in this case produces the capital closing transaction (income to
 * capital) for the system.
 * 
 * @author Robert J. Muller
 */
public class RjmMlsFiscalYearUpdater implements IFiscalYearUpdater {
  @Override
  public void update(FiscalYear fiscalYear, IBuilder builder,
                     IParameters parameters) {
    // Update the fiscal year with closing transactions.

    CapitalStructure capStruct = builder.getCapitalStructure();

    // Add a transaction to transfer net income to capital.
    Transaction capitalTransaction =
      capStruct.getIncomeToCapitalTransaction(fiscalYear, builder);
    fiscalYear.addTransaction(capitalTransaction);
  }
}
