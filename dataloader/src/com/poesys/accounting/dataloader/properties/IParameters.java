package com.poesys.accounting.dataloader.properties;


import java.io.Reader;
import java.io.Writer;


/**
 * <p>
 * Interface that allows a client to get the parameters of the data loader;
 * implementations may read properties from a file or provide properties
 * directly from the implementation for unit testing; the get-reader and
 * get-writer methods implement Reader and Writer opening and filename handling
 * but also permit other implementations for unit testing
 * </p>
 * Properties you define in the accounting.properties file:
 * <ul>
 * <li>entity</li>
 * <li>path</li>
 * <li>start</li>
 * <li>end</li>
 * <li>account_group_file</li>
 * <li>account_map_file</li>
 * <li>account_file</li>
 * <li>reimbursement_file</li>
 * <li>balance_file</li>
 * <li>transaction_file</li>
 * <li>item_file</li>
 * <li>balance_sheet_file</li>
 * <li>income_statement_file</li>
 * </ul>
 * 
 * @author Robert J. Muller
 */
public interface IParameters {
  /**
   * Get the file path to the input data files; this path points to a directory
   * that contains a set of fiscal year subdirectories, each of which contains
   * the standard set of accounting data files
   * 
   * @return the file path
   */
  String getPath();

  /**
   * Get the name of the accounting entity to load.
   * 
   * @return the entity name
   */
  String getEntity();

  /**
   * Get the year number of the first fiscal year in the sequence; this must
   * correspond to one year subdirectory in the file path; the data loader will
   * load balances from this year.
   * 
   * @return the first fiscal year of the sequence
   */
  Integer getStartYear();

  /**
   * Get the year number of the last fiscal year to load; the loader will load
   * all years from the start year to the end year.
   * 
   * @return the last fiscal year of the sequence
   */
  Integer getEndYear();

  /**
   * Get a reader for the account group data.
   * 
   * @param year the current fiscal year number
   * 
   * @return a reader
   */
  Reader getAccountGroupReader(Integer year);

  /**
   * Get a reader for the account-map data.
   * 
   * @param year the current fiscal year number
   * 
   * @return a reader
   */
  Reader getAccountMapReader(Integer year);

  /**
   * Get a reader for the account data.
   * 
   * @param year the current fiscal year number
   * 
   * @return a reader
   */
  Reader getAccountReader(Integer year);

  /**
   * Get a reader for the reimbursement data.
   * 
   * @param year the current fiscal year number
   * 
   * @return a reader
   */
  Reader getReimbursementReader(Integer year);

  /**
   * Get a reader for the balance data.
   * 
   * @param year the current fiscal year number
   * 
   * @return a reader
   */
  Reader getBalanceReader(Integer year);

  /**
   * Get a reader for the transaction data.
   * 
   * @param year the current fiscal year number
   * 
   * @return a reader
   */
  Reader getTransactionReader(Integer year);

  /**
   * Get a reader for the item data.
   * 
   * @param year the current fiscal year number
   * 
   * @return a reader
   */
  Reader getItemReader(Integer year);

  /**
   * Create the several supported writers for the specified year. You can then
   * call the getters for the writers and get the same writer repeatedly to
   * allow appending of data and to allow getting a complete String version for
   * display in unit tests. Creating the writers closes any existing writers,
   * clearing the resources associated with them. To use the writers, you need
   * to create the writer and use it to completion before creating another set
   * of writers. Usually, this happens in an iteration through years.
   * 
   * @param year the current fiscal year number
   */
  void createWriters(Integer year);

  /**
   * Close the several supported writers if they are open. Call this method to
   * close writers explicitly rather than by creating new writers through the
   * createWriters() method.
   */
  void closeWriters();

  /**
   * Get a writer for the balance sheet data.
   * 
   * @return a writer
   */
  Writer getBalanceSheetWriter();

  /**
   * Get a writer for the income statement data.
   * 
   * @return a writer
   */
  Writer getIncomeStatementWriter();

  /**
   * Get the balance sheet data for unit test validation.
   * 
   * @param year the year of the balance sheet to get
   * 
   * @return the balance sheet data set as a String or null if there is no such
   *         data set
   */
  String getBalanceSheetData(int year);

  /**
   * Get the income statement data for unit test validation.
   * 
   * @param year the year of the income statement to get
   * 
   * @return the income statement data set as a String or null if there is no
   *         such data set
   */
  String getIncomeStatementData(int year);

  /**
   * Get a writer for the balance sheet details data (includes items).
   * 
   * @return a writer
   */
  Writer getBalanceSheetDetailsWriter();

  /**
   * Get a writer for the income statement details data (includes items).
   * 
   * @return a writer
   */
  Writer getIncomeStatementDetailsWriter();

  /**
   * Get the balance sheet details data for unit test validation.
   * 
   * @param year the year of the balance sheet to get
   * 
   * @return the balance sheet details data set as a String or null if there is
   *         no such data set
   */
  String getBalanceSheetDetailsData(int year);

  /**
   * Get the income statement details data for unit test validation.
   * 
   * @param year the year of the income statement to get
   * 
   * @return the income statement details data set as a String or null if there
   *         is no such data set
   */
  String getIncomeStatementDetailsData(int year);
}
