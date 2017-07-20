/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.accounting.dataloader.newaccounting.AccountingDbService;
import com.poesys.accounting.dataloader.newaccounting.FiscalYear;
import com.poesys.accounting.dataloader.newaccounting.IDataAccessService;
import com.poesys.accounting.dataloader.newaccounting.IStorageManager;
import com.poesys.accounting.dataloader.newaccounting.StorageManager;
import com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder;
import com.poesys.accounting.dataloader.properties.IParameters;
import com.poesys.accounting.dataloader.properties.PropertyFileParameters;


/**
 * The main driver class for the Poesys Data Loader, which loads the old Poesys
 * accounting data into the new database.
 * 
 * @author Robert J. Muller
 */
public class DataLoader implements IDirector {
  /** Logger for this class */
  private static final Logger logger = Logger.getLogger(DataLoader.class);

  /** the set of fiscal year objects */
  private final List<FiscalYear> years = new ArrayList<FiscalYear>();

  /**
   * Main entry point for Poesys Data Loader; exits with 0 status code if
   * successful, 1 if not (check the log for exception stack traces)
   * 
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    int status = 1;
    try {
      // Create the data loader.
      DataLoader loader = new DataLoader();
      // Create the production interface implementations for the loader.
      IParameters parameters = new PropertyFileParameters();
      IBuilder builder = new OldDataBuilder(parameters);
      IStorageManager storageManager = new StorageManager();
      IDataAccessService dbService = new AccountingDbService();
      // Construct the accounting system.
      loader.construct(parameters, builder, storageManager, dbService);
      status = 0;
    } catch (Throwable e) {
      // Log fatal error before exiting.
      logger.fatal("Fatal error loading accounting data", e);
    }
    System.exit(status);
  }

  @Override
  public void construct(IParameters parameters, IBuilder builder,
                        IStorageManager storageManager,
                        IDataAccessService dbService) {
    buildFiscalYears(builder,
                     parameters.getStartYear(),
                     parameters.getEndYear());
    storageManager.validate(years);
    storageManager.store(parameters.getEntity(), years, dbService);
  }

  /**
   * Iterate through the fiscal years from start to end, building each year.
   * 
   * @param builder the IBuilder instance to use to build the fiscal years
   * @param start the first fiscal year in sequence
   * @param end the last fiscal year in sequence
   */
  private void buildFiscalYears(IBuilder builder, int start, int end) {
    for (int year = start; year <= end; year++) {
      // Build the current fiscal year, clearing data from previous year.
      builder.buildFiscalYear(year);

      // Go through the process steps in order.
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();

      // Build balances for first year only.
      if (year == start) {
        builder.buildBalances();
      }

      builder.buildTransactions();
      builder.buildReimbursements();

      // Add the new year to the list of years.
      years.add(builder.getFiscalYear());
    }
  }
}
