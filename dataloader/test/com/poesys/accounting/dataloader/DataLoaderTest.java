/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader;


import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.poesys.accounting.dataloader.newaccounting.UnitTestNoExceptionDataService;
import com.poesys.accounting.dataloader.newaccounting.UnitTestNoExceptionsStorageManager;
import com.poesys.accounting.dataloader.properties.UnitTestParametersNoExceptions;


/**
 * CUT: DataLoader
 * 
 * @author Robert J. Muller
 */
public class DataLoaderTest {

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.DataLoader#construct(com.poesys.accounting.dataloader.properties.IParameters, com.poesys.accounting.dataloader.IBuilder, com.poesys.accounting.dataloader.newaccounting.IStorageManager, com.poesys.accounting.dataloader.newaccounting.IDataAccessService)}
   * .
   */
  @Test
  public void testConstruct() {
    // Create the data loader.
    DataLoader loader = new DataLoader();
    // Create the production interface implementations for the loader.
    UnitTestParametersNoExceptions parameters = new UnitTestParametersNoExceptions();
    UnitTestNoExceptionsBuilder builder = new UnitTestNoExceptionsBuilder();
    UnitTestNoExceptionsStorageManager storageManager =
      new UnitTestNoExceptionsStorageManager();
    UnitTestNoExceptionDataService dbService =
      new UnitTestNoExceptionDataService();
    // Construct the accounting system.
    loader.construct(parameters, builder, storageManager, dbService);

    // Test the calls to the interface implementations

    // There should be no calls to get the path for this unit test.
    assertTrue("path parameter retrieved", parameters.getPathCalls() == 0);

    assertTrue("entity parameter not retrieved",
               parameters.getEntityCalls() == 1);
    assertTrue("start parameter not retrieved", parameters.getStartCalls() == 1);
    assertTrue("end parameter not retrieved", parameters.getEndCalls() == 1);

    // test has 3 fiscal years, so each builder method is called 3 times
    assertTrue("buildYear() not called: " + builder.getYearCalls(), builder.getYearCalls() == 3);
    assertTrue("buildGroups() not called", builder.getGroupCalls() == 3);
    assertTrue("buildAccountMap() not called", builder.getAccountMapCalls() == 3);
    assertTrue("buildAccounts() not called", builder.getAccountCalls() == 3);
    // buildBalances should be called only for first fiscal year
    assertTrue("buildBalances() not called: " + builder.getBalanceCalls(), builder.getBalanceCalls() == 1);
    assertTrue("buildTransactions() not called",
               builder.getTransactionCalls() == 3);
    assertTrue("buildReimbursements() not called",
               builder.getReimbursementCalls() == 3);
  }
}
