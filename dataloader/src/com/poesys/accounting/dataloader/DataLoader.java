/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader;


import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.accounting.dataloader.newaccounting.AccountingDbService;
import com.poesys.accounting.dataloader.newaccounting.FiscalYear;
import com.poesys.accounting.dataloader.newaccounting.IDataAccessService;
import com.poesys.accounting.dataloader.newaccounting.IStorageManager;
import com.poesys.accounting.dataloader.newaccounting.NonStoringStorageManager;
import com.poesys.accounting.dataloader.newaccounting.Statement;
import com.poesys.accounting.dataloader.newaccounting.Statement.StatementType;
// TODO restore this import after system testing is complete
// import com.poesys.accounting.dataloader.newaccounting.StorageManager;
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

  // messages
  private static final String IO_ERROR = "IO exception writing statements";
  private static final String FATAL_LOADING_ERROR =
    "Fatal error loading accounting data";
  private static final String FATAL_BALANCE_ERROR =
    "Fatal error: statements don't balance";

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
      // IStorageManager storageManager = new StorageManager();
      // TODO: Use a non-storing storage manager for now until full system test
      // is complete, then uncomment above line and remove these two lines.
      IStorageManager storageManager = new NonStoringStorageManager();
      IDataAccessService dbService = new AccountingDbService();
      // Construct the accounting system.
      loader.construct(parameters, builder, storageManager, dbService);
      status = 0;
    } catch (Throwable e) {
      // Log fatal error before exiting.
      logger.fatal(FATAL_LOADING_ERROR, e);
    }
    System.exit(status);
  }

  @Override
  public void construct(IParameters parameters, IBuilder builder,
                        IStorageManager storageManager,
                        IDataAccessService dbService) {
    buildFiscalYears(builder, parameters);
    if (storageManager.validate(years)) {
      storageManager.store(parameters.getEntity(), years, dbService);
    } else {
      logger.fatal(FATAL_BALANCE_ERROR);
      System.exit(1);
    }
  }

  /**
   * Iterate through the fiscal years from start to end, building each year.
   * 
   * @param builder the IBuilder instance to use to build the fiscal years
   * @param parameters the IParameters object containing program parameters
   */
  private void buildFiscalYears(IBuilder builder, IParameters parameters) {
    for (int year = parameters.getStartYear(); year <= parameters.getEndYear(); year++) {
      // Build the current fiscal year, clearing data from previous year.
      builder.buildFiscalYear(year);

      // Go through the process steps in order.
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();

      // Build balances for first year only.
      if (year == parameters.getStartYear()) {
        builder.buildBalances();
      }

      builder.buildTransactions();
      builder.buildReimbursements();

      // Add the new year to the list of years.
      FiscalYear fiscalYear = builder.getFiscalYear();
      years.add(fiscalYear);

      // Write the statements.
      writeStatements(parameters, fiscalYear);
    }
  }

  /**
   * Generate a balance sheet and income statement for a fiscal year and write
   * those statements out with the appropriate writers from the program
   * parameters.
   * 
   * @param parameters the program parameters
   * @param fiscalYear the fiscal year for which to generate statements
   */
  private void writeStatements(IParameters parameters, FiscalYear fiscalYear) {
    Statement balanceSheet =
      new Statement(fiscalYear, "Balance Sheet", StatementType.BALANCE_SHEET);
    Statement incomeStatement =
      new Statement(fiscalYear,
                    "Income Statement",
                    StatementType.INCOME_STATEMENT);
    try {
      parameters.createWriters(fiscalYear.getYear());
      Writer balanceSheetWriter = parameters.getBalanceSheetWriter();
      Writer incomeStmtWriter = parameters.getIncomeStatementWriter();
      Writer balanceSheetDetailsWriter = parameters.getBalanceSheetDetailsWriter();
      Writer incomeStmtDetailsWriter = parameters.getIncomeStatementDetailsWriter();
      balanceSheetWriter.write(balanceSheet.toData());
      incomeStmtWriter.write(incomeStatement.toData());
      balanceSheetDetailsWriter.write(balanceSheet.toDetailData());
      incomeStmtDetailsWriter.write(incomeStatement.toDetailData());
    } catch (IOException e) {
      logger.error(IO_ERROR, e);
      throw new RuntimeException(IO_ERROR, e);
    } finally {
      parameters.closeWriters();
    }
  }
}
