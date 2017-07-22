/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.properties;


import java.io.Reader;
import java.io.Writer;


/**
 * An implementation of IParameters that returns the parameters for a unit test
 * without looking at a properties file or command line arguments; maintains
 * counts of the calls to each parameter getter
 * 
 * @author Robert J. Muller
 */
public class UnitTestParametersInvalidPath extends
    AbstractStatementMaintainingParameters implements IParameters {
  private int pathCalls = 0;
  private int entityCalls = 0;
  private int startCalls = 0;
  private int endCalls = 0;

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

  // messages
  private static final String FILE_NOT_FOUND = "file not found: ";

  @Override
  public String getPath() {
    pathCalls++;
    return "/Users/muller/devaccounting/dataloader/data";
  }

  @Override
  public String getEntity() {
    entityCalls++;
    return "Poesys Associates";
  }

  @Override
  public Integer getStartYear() {
    startCalls++;
    return 2015;
  }

  @Override
  public Integer getEndYear() {
    endCalls++;
    return 2017;
  }

  /**
   * Get the count of getPath() calls.
   * 
   * @return a count
   */
  public int getPathCalls() {
    return pathCalls;
  }

  /**
   * Get the count of getEntity() calls.
   * 
   * @return a count
   */
  public int getEntityCalls() {
    return entityCalls;
  }

  /**
   * Get the count of getStartYear() calls.
   * 
   * @return a count
   */
  public int getStartCalls() {
    return startCalls;
  }

  /**
   * Get the count of getEndYear() calls.
   * 
   * @return a count
   */
  public int getEndCalls() {
    return endCalls;
  }

  @Override
  public Reader getAccountGroupReader(Integer year) {
    throw new RuntimeException(FILE_NOT_FOUND + ACCOUNT_GROUP_FILE);
  }

  @Override
  public Reader getAccountReader(Integer year) {
    throw new RuntimeException(FILE_NOT_FOUND + ACCOUNT_FILE);
  }

  @Override
  public Reader getReimbursementReader(Integer year) {
    throw new RuntimeException(FILE_NOT_FOUND + REIM_FILE);
  }

  @Override
  public Reader getBalanceReader(Integer year) {
    throw new RuntimeException(FILE_NOT_FOUND + BALANCE_FILE);
  }

  @Override
  public Reader getTransactionReader(Integer year) {
    throw new RuntimeException(FILE_NOT_FOUND + TRANSACTION_FILE);
  }

  @Override
  public Reader getItemReader(Integer year) {
    throw new RuntimeException(FILE_NOT_FOUND + ITEM_FILE);
  }

  @Override
  public Reader getAccountMapReader(Integer year) {
    throw new RuntimeException(FILE_NOT_FOUND + ACCOUNT_MAP_FILE);
  }

  @Override
  public void createWriters(Integer year) {
    throw new RuntimeException(FILE_NOT_FOUND + BALANCE_SHEET_FILE);
  }

  @Override
  public void closeWriters() {
    // nothing to do, never fails    
  }

  @Override
  public Writer getBalanceSheetWriter() {
    // fails by returning null
    return null;
  }

  @Override
  public Writer getIncomeStatementWriter() {
    // fails by returning null
    return null;
  }
}
