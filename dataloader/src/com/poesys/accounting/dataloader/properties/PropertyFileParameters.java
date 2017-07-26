/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
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

import com.poesys.db.InvalidParametersException;


/**
 * Implementation of the IParameters interface that provides the parameter
 * values from a property file on the class path; construct an object of this
 * class to access the properties currently in the file
 * 
 * @author Robert J. Muller
 */
public class PropertyFileParameters extends
    AbstractStatementMaintainingParameters {

  /** logger for this class */
  private static final Logger logger =
    Logger.getLogger(PropertyFileParameters.class);

  /** IO error loading file */
  private static final String PROPERTIES_FILE_ERROR =
    "IO Error loading properties file ";
  /** IO error closing file */
  private static final String PROPERTIES_FILE_CLOSE_ERROR =
    "IO Error closing properties file ";

  /** name of the properties file in the classpath */
  private static final String PROP_FILE = "dataloader.properties";

  /** properties object initialized from properties file */
  private static final Properties properties = new Properties();

  // keys in properties file
  private static final String PATH_KEY = "path";
  private static final String START_KEY = "start";
  private static final String END_KEY = "end";
  private static final String ENTITY = "entity";

  private static final String PATH_DELIMITER = "/";

  // keys in properties file for accounting system filenames
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
  private static final String BALANCE_SHEET_DETAILS_FILE =
    "balance_sheet_details_file";
  private static final String INCOME_STMT_DETAILS_FILE =
    "income_statement_details_file";

  // messages
  private static final String FILE_NOT_FOUND = "file not found: ";
  private static final String NULL_PARAMETERS = "null file parameters";

  /**
   * Create a PropertyFileParameters object. This constructor reads the
   * properties file and sets the internal properties from the file as read-only
   * (final) values.
   */
  public PropertyFileParameters() {
    InputStream stream = null;
    try {
      stream =
        PropertyFileParameters.class.getClassLoader().getResourceAsStream(PROP_FILE);
      properties.load(stream);
    } catch (IOException e) {
      logger.fatal(PROPERTIES_FILE_ERROR + PROP_FILE, e);
      // Fatal error, exit program
      System.exit(-1);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException e) {
          logger.fatal(PROPERTIES_FILE_CLOSE_ERROR + PROP_FILE, e);
          System.exit(-1);
        }
      }
    }
  }

  @Override
  public String getEntity() {
    return properties.getProperty(ENTITY);
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
   * Get the Reader that reads data from the specified file. The filename is
   * required.
   * 
   * @param filename the fully qualified filename for the file
   * @return a Reader pointing at the file data
   */
  private Reader getReader(String filename) {
    Reader r = null;

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
   * Get the Writer that writes data to the specified file. The filename is
   * required.
   * 
   * @param filename the fully qualified filename for the file
   * @return a Writer pointing at the file for appending
   */
  private Writer getWriter(String filename) {
    Writer w = null;

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
   * Get the fully qualified filename based on the fiscal year number and the
   * file name; this appends the file name from the properties file to the path
   * and year, producing the fully qualified name. The path property, the year,
   * and the filename are all required for the function to succeed.
   * 
   * @param year the fiscal year number (directory name)
   * @param filename the simple file name
   * @return the fully qualified file name
   */
  private String getFullyQualifiedFilename(Integer year, String filename) {
    String path = getPath();
    if (path == null || path.isEmpty() || year == null || filename == null
        || filename.isEmpty()) {
      throw new InvalidParametersException(NULL_PARAMETERS);
    }

    StringBuilder builder = new StringBuilder(getPath());
    builder.append(year.toString());
    builder.append(PATH_DELIMITER);
    builder.append(properties.getProperty(filename));
    return builder.toString();
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
    balanceSheetWriter =
      getWriter(getFullyQualifiedFilename(year, BALANCE_SHEET_FILE));
    incomeStatementWriter =
      getWriter(getFullyQualifiedFilename(year, INCOME_STMT_FILE));
    balanceSheetDetailsWriter =
      getWriter(getFullyQualifiedFilename(year, BALANCE_SHEET_DETAILS_FILE));
    incomeStatementDetailsWriter =
      getWriter(getFullyQualifiedFilename(year, INCOME_STMT_DETAILS_FILE));
  }
}
