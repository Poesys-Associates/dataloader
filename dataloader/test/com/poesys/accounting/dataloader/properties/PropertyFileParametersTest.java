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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Test;

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

/**
 * <p> CUT: DataLoaderProperties </p> <p> The various file-oriented tests require the existence of
 * the various files as defined in dataloader.properties but do not depend on specific data values
 * in the files. The dataloader.properties file should be set up as the unit tests require when
 * running this specific unit test. </p>
 *
 * @author Robert J. Muller
 */
public class PropertyFileParametersTest {
  /** year to use in all unit tests */
  private static final Integer YEAR = 1996;

  /** name of the general unit test properties file on classpath */
  private static final String PROPFILE1 = "unit_test.properties";
  private static final String PROPFILE2 = "unit_test_2.properties";
  private static final String PROPFILE3 = "unit_test_3.properties";

  /**
   * Tests the properties read from a file statically at startup; requires that a properties file
   * exists on the classpath with the name "dataloader.properties"; the test just verifies value
   * existence, it does not test for specific values; tests the four getters for path, start year,
   * end year, entity name, and income summary account name.
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testProperties() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);

    assertTrue("No path found", parameters.getPath() != null);
    assertTrue("No start year found", parameters.getStartYear() != null);
    assertTrue("No end year found", parameters.getEndYear() != null);
    assertTrue("No entity name found", parameters.getEntity() != null);
    assertTrue("No income summary account name found",
               parameters.getIncomeSummaryAccountName() != null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getCapitalEntityReader()}
   * .
   *
   * @throws IOException           when reader.close fails
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetCapitalEntityReader() throws IOException, FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    Reader reader = parameters.getCapitalEntityReader();
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getAccountGroupReader(java.lang.Integer)}
   * .
   *
   * @throws IOException           when reader.close fails
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetAccountGroupReader() throws IOException, FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    Reader reader = parameters.getAccountGroupReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getAccountMapReader(java.lang.Integer)}
   * .
   *
   * @throws IOException           when reader.close fails
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetAccountMapReader() throws IOException, FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    Reader reader = parameters.getAccountMapReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getAccountReader(java.lang.Integer)}
   * .
   *
   * @throws IOException           when reader.close fails
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetAccountReader() throws IOException, FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    Reader reader = parameters.getAccountReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getReimbursementReader(java.lang.Integer)}
   * .
   *
   * @throws IOException           when reader.close fails
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetReimbursementReader() throws IOException, FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    Reader reader = parameters.getReimbursementReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getBalanceReader(java.lang.Integer)}
   * .
   *
   * @throws IOException           when reader.close fails
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetBalanceReader() throws IOException, FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    Reader reader = parameters.getBalanceReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getTransactionReader(java.lang.Integer)}
   * .
   *
   * @throws IOException           when reader.close fails
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetTransactionReader() throws IOException, FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    Reader reader = parameters.getTransactionReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getItemReader(java.lang.Integer)}
   * .
   *
   * @throws IOException           when reader.close fails
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetItemReader() throws IOException, FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    Reader reader = parameters.getItemReader(YEAR);
    reader.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#createWriters(java.lang.Integer)}
   * . Also tests writer getters when writer is properly set.
   *
   * @throws IOException           when closing the writer throws an exception
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testCreateWriters() throws IOException, FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    parameters.createWriters(YEAR);
    Writer balanceSheetWriter = parameters.getBalanceSheetWriter();
    assertTrue("balance sheet writer not created", balanceSheetWriter != null);
    balanceSheetWriter.close();
    Writer incomeStatementWriter = parameters.getIncomeStatementWriter();
    assertTrue("income statement writer not created", incomeStatementWriter != null);
    incomeStatementWriter.close();
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#closeWriters()}
   * .
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testCloseWriters() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    parameters.createWriters(YEAR);
    parameters.closeWriters();
    Writer balanceSheetWriter = parameters.getBalanceSheetWriter();
    assertTrue("balance sheet writer not closed", balanceSheetWriter == null);
    Writer incomeStatementWriter = parameters.getIncomeStatementWriter();
    assertTrue("income statement writer not closed", incomeStatementWriter == null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getBalanceSheetWriter()}
   * . Tests writer getter when writers are not set
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetBalanceSheetWriterNotSet() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    Writer balanceSheetWriter = parameters.getBalanceSheetWriter();
    assertTrue("balance sheet writer not closed", balanceSheetWriter == null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getIncomeStatementWriter()}
   * . Tests writer getter when writers are not set
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetIncomeStatementWriterNotSet() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    Writer incomeStatementWriter = parameters.getIncomeStatementWriter();
    assertTrue("income statement writer not closed", incomeStatementWriter == null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getUpdater()}
   * . Tests updater factory method based on RjmMls updater in properties file
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetUpdaterRjmMls() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    IFiscalYearUpdater updater = parameters.getUpdater();
    assertTrue("no updater created", updater != null);
    assertTrue("wrong updater created", updater instanceof RjmMlsFiscalYearUpdater);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getUpdater()}
   * . Tests updater factory method based on Poesys updater in properties file
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetUpdaterPoesys() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE2);
    IFiscalYearUpdater updater = parameters.getUpdater();
    assertTrue("no updater created", updater != null);
    assertTrue("wrong updater created", updater instanceof PoesysFiscalYearUpdater);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getUpdater()}
   * . Tests updater factory method based on unknown updater in properties file
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetUpdaterDefault() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE3);
    IFiscalYearUpdater updater = parameters.getUpdater();
    assertTrue("no updater created", updater != null);
    assertTrue("wrong default updater created", updater instanceof UnitTestFiscalYearUpdater);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getUpdater()}
   * . Tests data access service factory method based on DoNothing service in properties file
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetDataAccessServiceDoNothing() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    IDataAccessService service = parameters.getDataAccessService();
    assertTrue("no service created", service != null);
    assertTrue("wrong service created", service instanceof DoNothingDataAccessService);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getUpdater()}
   * . Tests data access service factory method based on AccountingDb service in properties file
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetDataAccessServiceAccountingDb() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE2);
    IDataAccessService service = parameters.getDataAccessService();
    assertTrue("no service created", service != null);
    assertTrue("wrong service created", service instanceof AccountingDbService);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getUpdater()}
   * . Tests data access service factory method based on unknown service in properties file
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetDataAccessServiceDefault() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE3);
    IDataAccessService service = parameters.getDataAccessService();
    assertTrue("no service created", service != null);
    assertTrue("wrong service created", service instanceof DoNothingDataAccessService);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getUpdater()}
   * . Tests storage manager factory method based on DoNothing manager in properties file
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetStorageManagerDoNothing() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE1);
    IStorageManager manager = parameters.getStorageManager();
    assertTrue("no storage manager created", manager != null);
    assertTrue("wrong manager created", manager instanceof NonStoringStorageManager);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getUpdater()}
   * . Tests storage manager factory method based on actual storage manager in properties file
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetStorageManager() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE2);
    IStorageManager manager = parameters.getStorageManager();
    assertTrue("no storage manager created", manager != null);
    assertTrue("wrong manager created", manager instanceof StorageManager);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.properties.PropertyFileParameters#getUpdater()}
   * . Tests storage manager factory method based on unknown manager in properties file
   *
   * @throws FatalProgramException when the property file is wrong
   */
  @Test
  public void testGetStorageManagerDefault() throws FatalProgramException {
    PropertyFileParameters parameters = new PropertyFileParameters(PROPFILE3);
    IStorageManager manager = parameters.getStorageManager();
    assertTrue("no storage manager created", manager != null);
    assertTrue("wrong manager created", manager instanceof NonStoringStorageManager);
  }
}
