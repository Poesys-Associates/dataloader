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
package com.poesys.accounting.dataloader.properties;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.poesys.accounting.dataloader.FatalProgramException;
import com.poesys.accounting.dataloader.newaccounting.AccountingDbService;
import com.poesys.accounting.dataloader.newaccounting.DoNothingDataAccessService;
import com.poesys.accounting.dataloader.newaccounting.IDataAccessService;
import com.poesys.accounting.dataloader.newaccounting.IFiscalYearUpdater;
import com.poesys.accounting.dataloader.newaccounting.IStorageManager;
import com.poesys.accounting.dataloader.newaccounting.NonStoringStorageManager;
import com.poesys.accounting.dataloader.newaccounting.PoesysFiscalYearUpdater;
import com.poesys.accounting.dataloader.newaccounting.RjmMlsFiscalYearUpdater;
import com.poesys.accounting.dataloader.newaccounting.UnitTestFiscalYearUpdater;
import com.poesys.accounting.dataloader.newaccounting.StorageManager;
import com.poesys.db.InvalidParametersException;

/**
 * Implementation of the IParameters interface that provides the parameter values from a property
 * file on the class path; construct an object of this class to access the properties currently in
 * the file
 *
 * @author Robert J. Muller
 */
public class PropertyFileParameters extends AbstractStatementMaintainingParameters {

  /** logger for this class */
  private static final Logger logger = Logger.getLogger(PropertyFileParameters.class);

  /** IO error loading file */
  private static final String PROPERTIES_FILE_ERROR = "IO Error loading properties file ";
  /** IO error closing file */
  private static final String PROPERTIES_FILE_CLOSE_ERROR = "IO Error closing properties file ";

  /** properties object initialized from properties file */
  private static final Properties properties = new Properties();

  // keys in properties file
  private static final String PATH_KEY = "path";
  private static final String START_KEY = "start";
  private static final String END_KEY = "end";
  private static final String ENTITY = "entity";
  private static final String INCOME_SUMMARY_ACCOUNT_NAME = "income_summary_account_name";

  private static final String PATH_DELIMITER = "/";

  // keys in properties file for accounting system filenames
  private static final String CAPITAL_ENTITY_FILE = "capital_entity_file";
  private static final String ACCOUNT_GROUP_FILE = "account_group_file";
  private static final String ACCOUNT_MAP_FILE = "account_map_file";
  private static final String ACCOUNT_FILE = "account_file";
  private static final String BALANCE_FILE = "balance_file";
  private static final String REIM_FILE = "reimbursement_file";
  private static final String TRANSACTION_FILE = "transaction_file";
  private static final String ITEM_FILE = "item_file";

  // keys in properties file for output accounting statement filenames
  private static final String BALANCE_SHEET_FILE = "balance_sheet_file";
  private static final String INCOME_STMT_FILE = "income_statement_file";
  private static final String BALANCE_SHEET_DETAILS_FILE = "balance_sheet_details_file";
  private static final String INCOME_STMT_DETAILS_FILE = "income_statement_details_file";

  // plug-in class specifications
  /** keyword for updater */
  private static final String UPDATER = "updater";
  /** keyword for data access service */
  private static final String DATA_ACCESS_SERVICE = "data_access_service";
  /** keyword for storage manager */
  private static final String STORAGE_MGR = "storage_manager";

  // messages
  private static final String FILE_NOT_FOUND = "file not found: ";
  private static final String NULL_PARAMETERS = "null file parameters";

  /**
   * Create a PropertyFileParameters object. This constructor reads the properties file and sets the
   * internal properties from the file as read-only (final) values. The name of the property file
   * should correspond to a file on the classpath. Supplying the name as a parameter lets you create
   * separate property files for unit tests.
   *
   * @param propertyFileName the name of the property file to use
   * @throws FatalProgramException when there is a fatal error reading the properties file
   */
  public PropertyFileParameters(String propertyFileName) throws FatalProgramException {
    InputStream stream = null;
    try {
      stream = PropertyFileParameters.class.getClassLoader().getResourceAsStream(propertyFileName);
      if (stream != null) {
        properties.load(stream);
      } else {
        throw new IOException("cannot open stream for properties file " + propertyFileName);
      }
    } catch (IOException e) {
      logger.fatal(PROPERTIES_FILE_ERROR + propertyFileName, e);
      throw new FatalProgramException(e.getMessage(), e);
    }
    finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException e) {
          logger.fatal(PROPERTIES_FILE_CLOSE_ERROR + propertyFileName, e);
          // ignore exception
        }
      }
    }
  }

  @Override
  public String getEntity() {
    return properties.getProperty(ENTITY);
  }

  @Override
  public String getIncomeSummaryAccountName() {
    return properties.getProperty(INCOME_SUMMARY_ACCOUNT_NAME);
  }

  @Override
  public String getPath() {
    return properties.getProperty(PATH_KEY);
  }

  @Override
  public Integer getStartYear() {
    return new Integer(properties.getProperty(START_KEY));
  }

  @Override
  public Integer getEndYear() {
    return new Integer(properties.getProperty(END_KEY));
  }

  /**
   * Get the Reader that reads data from the specified file. The filename is required.
   *
   * @param filename the fully qualified filename for the file
   * @return a Reader pointing at the file data
   */
  private Reader getReader(String filename) {
    Reader r;

    if (filename == null || filename.isEmpty()) {
      throw new InvalidParametersException(NULL_PARAMETERS);
    }

    try {
      r = new FileReader(filename);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(FILE_NOT_FOUND + filename);
    }

    return r;
  }

  /**
   * Get the Writer that writes data to the specified file. The filename is required.
   *
   * @param filename the fully qualified filename for the file
   * @return a Writer pointing at the file for appending
   */
  private Writer getWriter(String filename) {
    Writer w;

    if (filename == null || filename.isEmpty()) {
      throw new InvalidParametersException(NULL_PARAMETERS);
    }

    try {
      w = new FileWriter(filename);
    } catch (IOException e) {
      throw new RuntimeException(FILE_NOT_FOUND + filename);
    }

    return w;
  }

  /**
   * Get the fully qualified filename based on the fiscal year number and the file name; this
   * appends the file name from the properties file to the path and year, producing the fully
   * qualified name. The path property, the year, and the filename are all required for the function
   * to succeed.
   *
   * @param year     the fiscal year number (directory name) (required)
   * @param filename the simple file name (required)
   * @return the fully qualified file name
   */
  private String getFullyQualifiedFilename(Integer year, String filename) {
    String path = getPath();
    if (path == null || path.isEmpty() || year == null || filename == null || filename.isEmpty()) {
      throw new InvalidParametersException(NULL_PARAMETERS);
    }

    StringBuilder builder = new StringBuilder(getPath());
    builder.append(year.toString());
    builder.append(PATH_DELIMITER);
    builder.append(properties.getProperty(filename));
    return builder.toString();
  }

  /**
   * Get the fully qualified filename for a file at the top level of the accounting entity; this
   * appends the file name from the properties file to the path, producing the fully qualified name.
   * The path property is required for this method to succeed.
   *
   * @param filename the simple file name (required)
   * @return the fully qualified file name
   */
  private String getEntityFilename(String filename) {
    String path = getPath();
    if (path == null || path.isEmpty()) {
      throw new InvalidParametersException(NULL_PARAMETERS);
    }

    StringBuilder builder = new StringBuilder(getPath());
    builder.append(properties.getProperty(filename));
    return builder.toString();
  }

  @Override
  public Reader getCapitalEntityReader() {
    return getReader(getEntityFilename(CAPITAL_ENTITY_FILE));
  }

  @Override
  public Reader getAccountGroupReader(Integer year) {
    return getReader(getFullyQualifiedFilename(year, ACCOUNT_GROUP_FILE));
  }

  @Override
  public Reader getAccountMapReader(Integer year) {
    return getReader(getFullyQualifiedFilename(year, ACCOUNT_MAP_FILE));
  }

  @Override
  public Reader getAccountReader(Integer year) {
    return getReader(getFullyQualifiedFilename(year, ACCOUNT_FILE));
  }

  @Override
  public Reader getReimbursementReader(Integer year) {
    return getReader(getFullyQualifiedFilename(year, REIM_FILE));
  }

  @Override
  public Reader getBalanceReader(Integer year) {
    return getReader(getFullyQualifiedFilename(year, BALANCE_FILE));
  }

  @Override
  public Reader getTransactionReader(Integer year) {
    return getReader(getFullyQualifiedFilename(year, TRANSACTION_FILE));
  }

  @Override
  public Reader getItemReader(Integer year) {
    return getReader(getFullyQualifiedFilename(year, ITEM_FILE));
  }

  @Override
  public void createWriters(Integer year) {
    closeWriters();
    this.year = year;
    balanceSheetWriter = getWriter(getFullyQualifiedFilename(year, BALANCE_SHEET_FILE));
    incomeStatementWriter = getWriter(getFullyQualifiedFilename(year, INCOME_STMT_FILE));
    balanceSheetDetailsWriter =
      getWriter(getFullyQualifiedFilename(year, BALANCE_SHEET_DETAILS_FILE));
    incomeStatementDetailsWriter =
      getWriter(getFullyQualifiedFilename(year, INCOME_STMT_DETAILS_FILE));
  }

  @Override
  public IFiscalYearUpdater getUpdater() {
    IFiscalYearUpdater updater;
    String plugin = properties.getProperty(UPDATER);
    switch (plugin) {
      case "RjmMlsFiscalYearUpdater":
        updater = new RjmMlsFiscalYearUpdater();
        break;
      case "PoesysFiscalYearUpdater":
        updater = new PoesysFiscalYearUpdater();
        break;
      case "UnitTestFiscalYearUpdater":
        updater = new UnitTestFiscalYearUpdater();
        break;
      default:
        logger.warn("updater parameter value not supported: " + plugin);
        updater = new UnitTestFiscalYearUpdater();
    }
    return updater;
  }

  @Override
  public IDataAccessService getDataAccessService() {
    IDataAccessService service;
    String plugin = properties.getProperty(DATA_ACCESS_SERVICE);
    switch (plugin) {
      case "DoNothingDataAccessService":
        service = new DoNothingDataAccessService();
        break;
      case "AccountingDbService":
        service = new AccountingDbService();
        break;
      default:
        logger.warn("data_access_service parameter value not supported: " + plugin);
        service = new DoNothingDataAccessService();
    }
    return service;
  }

  @Override
  public IStorageManager getStorageManager() {
    IStorageManager manager;
    String plugin = properties.getProperty(STORAGE_MGR);
    switch (plugin) {
      case "NonStoringStorageManager":
        manager = new NonStoringStorageManager();
        break;
      case "StorageManager":
        manager = new StorageManager();
        break;
      default:
        logger.warn("storage_manager parameter value not supported: " + plugin);
        manager = new NonStoringStorageManager();
    }
    return manager;
  }
}
