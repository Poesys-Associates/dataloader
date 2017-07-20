package com.poesys.accounting.dataloader.properties;


import java.io.Reader;


/**
 * Interface that allows a client to get the parameters of the data loader;
 * implementations may read properties from a file or provide properties
 * directly from the implementation for unit testing; the get-reader methods
 * provide an interface for unit testing without reading files
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
}
