/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader;


import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.poesys.accounting.dataloader.newaccounting.IDataAccessService;
import com.poesys.accounting.dataloader.newaccounting.IFiscalYearUpdater;
import com.poesys.accounting.dataloader.newaccounting.IStorageManager;
import com.poesys.accounting.dataloader.properties.UnitTestParametersCapitalOneEntityOneYearNoDistribution;


/**
 * CUT: DataLoader
 * 
 * @author Robert J. Muller
 */
public class DataLoaderTest {
  /** number of fiscal years in the test load */
  private static int NUMBER_OF_YEARS = 1;

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.DataLoader#construct(com.poesys.accounting.dataloader.properties.IParameters, com.poesys.accounting.dataloader.IBuilder, com.poesys.accounting.dataloader.newaccounting.IStorageManager, com.poesys.accounting.dataloader.newaccounting.IDataAccessService, IFiscalYearUpdater)}
   * .
   */
  @Test
  public void testConstruct() {
    // Create the data loader.
    DataLoader loader = new DataLoader();
    // Create the production interface implementations for the loader.
    UnitTestParametersCapitalOneEntityOneYearNoDistribution parameters =
      new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    UnitTestNoExceptionsBuilder builder = new UnitTestNoExceptionsBuilder();
    IStorageManager storageManager = parameters.getStorageManager();
    IDataAccessService dbService = parameters.getDataAccessService();
    IFiscalYearUpdater updater = parameters.getUpdater();

    // Construct the accounting system.
    loader.construct(parameters, builder, storageManager, dbService, updater);

    // Test the calls to the interface implementations

    // There should be no calls to get the path for this unit test.
    assertTrue("path parameter retrieved", parameters.getPathCalls() == 0);

    // There should be one call to get the entity name.
    assertTrue("entity parameter not retrieved",
               parameters.getEntityCalls() == 1);

    // The number of start-end calls depends on the number of fiscal years
    // processed plus one startup call.
    assertTrue("start parameter not retrieved: " + parameters.getStartCalls(),
               parameters.getStartCalls() == NUMBER_OF_YEARS + 1);
    assertTrue("end parameter not retrieved",
               parameters.getEndCalls() == NUMBER_OF_YEARS + 1);

    // test has NUMBER_OF_YEARS fiscal years, so each builder method is called
    // NUMBER_OF_YEARS times
    assertTrue("buildYear() not called: " + builder.getYearCalls(),
               builder.getYearCalls() == NUMBER_OF_YEARS);
    assertTrue("buildGroups() not called",
               builder.getGroupCalls() == NUMBER_OF_YEARS);
    assertTrue("buildAccountMap() not called",
               builder.getAccountMapCalls() == NUMBER_OF_YEARS);
    assertTrue("buildAccounts() not called",
               builder.getAccountCalls() == NUMBER_OF_YEARS);
    // buildBalances should be called only for first fiscal year
    assertTrue("buildBalances() not called: " + builder.getBalanceCalls(),
               builder.getBalanceCalls() == 1);
    assertTrue("buildTransactions() not called",
               builder.getTransactionCalls() == NUMBER_OF_YEARS);
    assertTrue("buildReimbursements() not called",
               builder.getReimbursementCalls() == NUMBER_OF_YEARS);

    // Validate the statements.
    for (int year = parameters.getStartYear(); year <= parameters.getEndYear(); year++) {
      String balanceSheetDataSet = parameters.getBalanceSheetData(year);
      assertTrue("balance sheet null for year " + year,
                 balanceSheetDataSet != null);
      String incomeStatementDataSet = parameters.getIncomeStatementData(year);
      assertTrue("income statement null for year " + year,
                 incomeStatementDataSet != null);
    }
  }
}
