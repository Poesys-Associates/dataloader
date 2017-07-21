/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.properties;


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;


/**
 * <p>
 * CUT: DataLoaderProperties
 * </p>
 * <p>
 * The various file-oriented tests require the existence of the various files as
 * defined in dataloader.properties but do not depend on specific data values in
 * the files.
 * </p>
 * 
 * @author Robert J. Muller
 */
public class PropertyFileParametersTest {
  private static final Integer YEAR = 1996;

  /**
   * Tests the properties read from a file statically at startup; requires that
   * a properties file exists on the classpath with the name
   * "dataloader.properties"; the test just verifies value existence, it does
   * not test for specific values; tests the four getters for path, start year,
   * end year, and entity name.
   */
  @Test
  public void testProperties() {
    PropertyFileParameters parameters = new PropertyFileParameters();

    assertTrue("No path found", parameters.getPath() != null);
    assertTrue("No start year found", parameters.getStartYear() != null);
    assertTrue("No end year found", parameters.getEndYear() != null);
    assertTrue("No entity name found", parameters.getEntity() != null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getAccountGroupReader(java.lang.Integer)}
   * .
   * 
   * @throws IOException when reader.close fails
   */
  @Test
  public void testGetAccountGroupReader() throws IOException {
    PropertyFileParameters parameters = new PropertyFileParameters();
    Reader reader = parameters.getAccountGroupReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getAccountMapReader(java.lang.Integer)}
   * .
   * 
   * @throws IOException when reader.close fails
   */
  @Test
  public void testGetAccountMapReader() throws IOException {
    PropertyFileParameters parameters = new PropertyFileParameters();
    Reader reader = parameters.getAccountMapReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getAccountReader(java.lang.Integer)}
   * .
   * 
   * @throws IOException when reader.close fails
   */
  @Test
  public void testGetAccountReader() throws IOException {
    PropertyFileParameters parameters = new PropertyFileParameters();
    Reader reader = parameters.getAccountReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getReimbursementReader(java.lang.Integer)}
   * .
   * 
   * @throws IOException when reader.close fails
   */
  @Test
  public void testGetReimbursementReader() throws IOException {
    PropertyFileParameters parameters = new PropertyFileParameters();
    Reader reader = parameters.getReimbursementReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getBalanceReader(java.lang.Integer)}
   * .
   * 
   * @throws IOException when reader.close fails
   */
  @Test
  public void testGetBalanceReader() throws IOException {
    PropertyFileParameters parameters = new PropertyFileParameters();
    Reader reader = parameters.getBalanceReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getTransactionReader(java.lang.Integer)}
   * .
   * 
   * @throws IOException when reader.close fails
   */
  @Test
  public void testGetTransactionReader() throws IOException {
    PropertyFileParameters parameters = new PropertyFileParameters();
    Reader reader = parameters.getTransactionReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getItemReader(java.lang.Integer)}
   * .
   * 
   * @throws IOException when reader.close fails
   */
  @Test
  public void testGetItemReader() throws IOException {
    PropertyFileParameters parameters = new PropertyFileParameters();
    Reader reader = parameters.getItemReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#createWriters(java.lang.Integer)}
   * . Also tests writer getters when writer is properly set.
   * 
   * @throws IOException when closing the writer throws an exception
   */
  @Test
  public void testCreateWriters() throws IOException {
    PropertyFileParameters parameters = new PropertyFileParameters();
    parameters.createWriters(YEAR);
    Writer balanceSheetWriter = parameters.getBalanceSheetWriter();
    assertTrue("balance sheet writer not created", balanceSheetWriter != null);
    balanceSheetWriter.close();
    Writer incomeStatementWriter = parameters.getIncomeStatementWriter();
    assertTrue("income statement writer not created",
               incomeStatementWriter != null);
    incomeStatementWriter.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#closeWriters()}
   * .
   */
  @Test
  public void testCloseWriters() {
    PropertyFileParameters parameters = new PropertyFileParameters();
    parameters.createWriters(YEAR);
    parameters.closeWriters();
    Writer balanceSheetWriter = parameters.getBalanceSheetWriter();
    assertTrue("balance sheet writer not closed", balanceSheetWriter == null);
    Writer incomeStatementWriter = parameters.getIncomeStatementWriter();
    assertTrue("income statement writer not closed",
               incomeStatementWriter == null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getBalanceSheetWriter()}
   * . Tests writer getter when writers are not set
   */
  @Test
  public void testGetBalanceSheetWriterNotSet() {
    PropertyFileParameters parameters = new PropertyFileParameters();
    Writer balanceSheetWriter = parameters.getBalanceSheetWriter();
    assertTrue("balance sheet writer not closed", balanceSheetWriter == null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getIncomeStatementWriter()}
   * . Tests writer getter when writers are not set
   */
  @Test
  public void testGetIncomeStatementWriterNotSet() {
    PropertyFileParameters parameters = new PropertyFileParameters();
    Writer incomeStatementWriter = parameters.getIncomeStatementWriter();
    assertTrue("income statement writer not closed",
               incomeStatementWriter == null);
  }
}
