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
package com.poesys.accounting.dataloader;

import com.poesys.accounting.dataloader.newaccounting.*;
import com.poesys.accounting.dataloader.newaccounting.Statement.StatementType;
import com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder;
import com.poesys.accounting.dataloader.properties.IParameters;
import com.poesys.accounting.dataloader.properties.PropertyFileParameters;
import com.poesys.db.InvalidParametersException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * The main driver class for the Poesys Data Loader, which loads the old Poesys accounting data into
 * the new database.
 *
 * @author Robert J. Muller
 */
public class DataLoader implements IDirector {
  /** Logger for this class */
  private static final Logger logger = Logger.getLogger(DataLoader.class);

  /** the capital structure of the accounting system, containing the capital entities */
  private CapitalStructure capitalStructure = null;

  /** property file name, file should be in classpath */
  private static final String PROPERTY_FILE = "dataloader.properties";

  /** the BigDecimal scale constant for money amounts */
  private static final int SCALE = 2;

  // messages

  private static final String IO_ERROR = "IO exception writing statements";
  private static final String FATAL_LOADING_ERROR = "Fatal error loading accounting data";
  private static final String FATAL_BALANCE_ERROR = "Fatal error: statements don't balance";
  private static final String BALANCES_OK_MSG = "balances are zero and match for ";
  private static final String NON_ZERO_BALANCE_WARNING = "balances are not zero for ";
  private static final String MATCH_WARNING = "balances do not match for ";
  private static final String INVALID_YEAR_RANGE_ERROR =
    "Start year greater than end year in parameters";
  private static final String NULL_YEAR_ERROR = "Null start or end year in parameters";

  /**
   * Main entry point for Poesys Data Loader; exits with 0 status code if successful, 1 if not
   * (check the log for exception stack traces)
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    int status = 1;
    try {
      // Create the data loader.
      DataLoader loader = new DataLoader();
      // Create the production interface implementations for the loader.
      IParameters parameters = new PropertyFileParameters(PROPERTY_FILE);
      IBuilder builder = new OldDataBuilder(parameters);
      IStorageManager storageManager = parameters.getStorageManager();
      IDataAccessService dbService = parameters.getDataAccessService();

      loader.construct(parameters, builder, storageManager, dbService);

      // Everything is OK, return 0 return code.
      status = 0;
    } catch (FatalProgramException e) {
      // Log the thrown fatal error before exiting
      logger.fatal(e.getMessage(), e);
    } catch (Throwable e) {
      // Log fatal error before exiting.
      logger.fatal(FATAL_LOADING_ERROR, e);
    }
    System.exit(status);
  }

  @Override
  public void construct(IParameters parameters, IBuilder builder, IStorageManager storageManager,
                        IDataAccessService dbService) {
    builder.buildCapitalStructure();
    buildFiscalYears(builder, parameters);
    if (storageManager.validate(builder.getFiscalYears())) {
      storageManager.store(parameters.getEntity(), capitalStructure, builder.getFiscalYears(),
                           builder.getTransactions(), dbService);
    } else {
      logger.fatal(FATAL_BALANCE_ERROR);
      System.exit(1);
    }
  }

  /**
   * Iterate through the fiscal years from start to end, building each year.
   *
   * @param builder    the IBuilder instance to use to build the fiscal years
   * @param parameters the IParameters object containing program parameters
   */
  private void buildFiscalYears(IBuilder builder, IParameters parameters) {
    // Get the years.
    Integer start = parameters.getStartYear();
    Integer end = parameters.getEndYear();

    validateRange(start, end);

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

      writeStatements(parameters, builder.getFiscalYear());
    }
  }

  /**
   * Validate start and end years as a range of years; both years must be not null and the start
   * year cannot be greater than the end year.
   *
   * @param start the start year
   * @param end   the end year
   */
  private void validateRange(Integer start, Integer end) {
    // Validate the range.
    if (start == null || end == null) {
      throw new InvalidParametersException(NULL_YEAR_ERROR);
    }

    if (start.compareTo(end) > 0) {
      throw new InvalidParametersException(INVALID_YEAR_RANGE_ERROR + ": " + start + ", " + end);
    }
  }

  /**
   * Generate a balance sheet and income statement for a fiscal year and write those statements out
   * with the appropriate writers from the program parameters. Inform the user of the balance status
   * of the statements.
   *
   * @param parameters the program parameters
   * @param fiscalYear the fiscal year for which to generate statements
   */
  private void writeStatements(IParameters parameters, FiscalYear fiscalYear) {
    Statement balanceSheet =
      new Statement(fiscalYear, "Balance Sheet", StatementType.BALANCE_SHEET);
    BigDecimal balanceSheetBalance =
      balanceSheet.getBalance().setScale(SCALE, BigDecimal.ROUND_HALF_UP);
    logger.debug("Balance sheet balance for " + fiscalYear.getYear() + ": " + balanceSheetBalance);
    Statement incomeStatement =
      new Statement(fiscalYear, "Income Statement", StatementType.INCOME_STATEMENT);
    BigDecimal incomeStatementBalance =
      incomeStatement.getBalance().setScale(SCALE, BigDecimal.ROUND_HALF_UP);
    logger.debug(
      "Income statement balance for " + fiscalYear.getYear() + ": " + incomeStatementBalance);
    BigDecimal difference = incomeStatementBalance.subtract(balanceSheetBalance).setScale(SCALE,
                                                                                          BigDecimal.ROUND_HALF_UP);

    // Report balance status to the user as warning or information.
    if (difference.compareTo(BigDecimal.ZERO) != 0) {
      logger.warn(MATCH_WARNING + fiscalYear.getYear() + ": $" + balanceSheetBalance + " vs. $" +
                  incomeStatementBalance);
    } else if (!balanceSheetBalance.equals(
      BigDecimal.ZERO.setScale(SCALE, BigDecimal.ROUND_HALF_UP))) {
      logger.warn(
        NON_ZERO_BALANCE_WARNING + fiscalYear.getYear() + ": $" + balanceSheetBalance + " vs. $" +
        incomeStatementBalance);
    } else {
      logger.info(BALANCES_OK_MSG + fiscalYear.getYear());
    }

    writeStatementData(parameters, fiscalYear, balanceSheet, incomeStatement);
  }

  /**
   * Write out the files containing statements and statement data for a fiscal year.
   *
   * @param parameters      the parameters object giving writer access
   * @param fiscalYear      the fiscal year to write
   * @param balanceSheet    the balance sheet to write
   * @param incomeStatement the income statement to write
   */
  private void writeStatementData(IParameters parameters, FiscalYear fiscalYear, Statement
    balanceSheet, Statement incomeStatement) {
    try {
      parameters.createWriters(fiscalYear.getYear());

      parameters.getBalanceSheetWriter().write(balanceSheet.toData());
      parameters.getIncomeStatementWriter().write(incomeStatement.toData());
      parameters.getBalanceSheetDetailsWriter().write(balanceSheet.toDetailData());
      parameters.getIncomeStatementDetailsWriter().write(incomeStatement.toDetailData());
    } catch (IOException e) {
      logger.error(IO_ERROR, e);
      throw new RuntimeException(IO_ERROR, e);
    }
    finally {
      parameters.closeWriters();
    }
  }
}
