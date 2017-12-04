/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.properties;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import com.poesys.accounting.dataloader.newaccounting.IDataAccessService;
import com.poesys.accounting.dataloader.newaccounting.IFiscalYearUpdater;
import com.poesys.accounting.dataloader.newaccounting.IStorageManager;
import com.poesys.accounting.dataloader.newaccounting.UnitTestFiscalYearUpdater;
import com.poesys.accounting.dataloader.newaccounting.UnitTestNoExceptionDataService;
import com.poesys.accounting.dataloader.newaccounting.UnitTestNoExceptionsStorageManager;


/**
 * An implementation of IParameters that returns the parameters for a unit test
 * without looking at a properties file or command line arguments; maintains
 * counts of the calls to each parameter getter for unit testing methods that
 * call the parameters, to make sure the method does make the required calls.
 * 
 * @author Robert J. Muller
 */
public class UnitTestParametersGroupsThreeYears extends
    AbstractStatementMaintainingParameters {
  private static final String LINE_RET = "\n";
  private int pathCalls = 0;
  private int entityCalls = 0;
  private int incomeSummaryCalls = 0;
  private int startCalls = 0;
  private int endCalls = 0;

  // unit test string writers
  private StringWriter balanceSheetWriter = null;
  private StringWriter incomeStatementWriter = null;

  // Data input
  private static final String DELIM = "\t";

  private static final Integer START_YEAR = 2014;
  private static final Integer END_YEAR = 2016;
  private static final Integer REC_YEAR = 2016;

  private static final String SINGLE_ENTITY_NAME = "John Q. Doe";

  private static final String CAP_ACCOUNT = "Personal Capital";
  private static final String DIST_ACCOUNT = "Distributions";
  private static final Double OWNERSHIP = 1.0D;

  private static final String GROUP_NAME_1 = "Cash";
  private static final String GROUP_NAME_2 = "Accounts Receivable";
  private static final String GROUP_NAME_3 = "Accounts Payable";
  private static final String GROUP_NAME_4 = "Capital";
  private static final String GROUP_NAME_5 = "Salary Income";
  private static final String GROUP_NAME_6 = "Household Expenses";
  private static final String GROUP_NAME_7 = "General Expenses";

  private static final Float START_1 = 100.00F;
  private static final Float END_1 = 109.99F;
  private static final Float START_2 = 110.00F;
  private static final Float END_2 = 119.99F;
  private static final Float START_3 = 220.00F;
  private static final Float END_3 = 229.99F;
  private static final Float START_4 = 300.00F;
  private static final Float END_4 = 309.99F;
  private static final Float START_5 = 400.00F;
  private static final Float END_5 = 409.99F;
  private static final Float START_6 = 700.00F;
  private static final Float END_6 = 709.99F;
  // range for range moving to different range
  private static final Float START_8 = 410.00F;
  private static final Float END_8 = 419.99F;

  private static final Float ACCOUNT_NUMBER_1 = 100.0F;
  private static final String ACCOUNT_NAME_1 =
    "Citicorp Checking (111222333444)";
  private static final String ACCOUNT_NAME_NEW = "Citicorp Checking";
  private static final Float ACCOUNT_NUMBER_2 = 110.0F;
  private static final String ACCOUNT_NAME_2 = "Accounts Receivable";
  private static final Float ACCOUNT_NUMBER_3 = 109.0F;
  private static final String ACCOUNT_NAME_3 = "Other Cash";
  private static final Float ACCOUNT_NUMBER_4 = 400.0F;
  private static final String ACCOUNT_NAME_4 = "Revenue";
  private static final Double AMOUNT = 100.00D;

  private static final Integer TRANS_ID_1 = 200;
  private static final Integer REC_ID = 100;
  private static final Integer REIM_ID = 400;

  private static final Double ALLOCATED_AMOUNT = 50.00D;

  private static final String CREDIT = "CR";
  private static final String DEBIT = "DR";

  private static final String FALSE = "N";

  /** description enclosed in quotes with trailing blanks */
  private static final String FORMATTED_DESC = "\"description              \"";

  /** date as Oracle-formatted string representation */
  private static final String FORMATTED_TRANS_DATE = "26-JUN-17";

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
  public String getIncomeSummaryAccountName() {
    return "Income Summary";
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
   * Get the count of getIncomeSummaryAccountName() calls.
   * 
   * @return a count
   */
  public int getIncomeSummaryCalls() {
    return incomeSummaryCalls;
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
  public Reader getCapitalEntityReader() {
    String input =
      SINGLE_ENTITY_NAME + DELIM + CAP_ACCOUNT + DELIM + DIST_ACCOUNT + DELIM + OWNERSHIP.toString();
    return new StringReader(input);
  }

  @Override
  public Reader getAccountGroupReader(Integer year) {
    String input = null;
    if (year.equals(START_YEAR)) {
      // @formatter:off
      input = 
        START_1.toString() + DELIM + END_1.toString() + DELIM + GROUP_NAME_1 + LINE_RET + 
        START_2.toString() + DELIM + END_2.toString() + DELIM + GROUP_NAME_2 + LINE_RET + 
        START_3.toString() + DELIM + END_3.toString() + DELIM + GROUP_NAME_3 + LINE_RET + 
        START_4.toString() + DELIM + END_4.toString() + DELIM + GROUP_NAME_4 + LINE_RET + 
        START_5.toString() + DELIM + END_5.toString() + DELIM + GROUP_NAME_5 + LINE_RET + 
        START_6.toString() + DELIM + END_6.toString() + DELIM + GROUP_NAME_6;
      // @formatter:on
    } else if (year.equals(START_YEAR + 1)) {
      // Change the name of group 6 to group 7, change range of group 5
      // @formatter:off
      input = 
        START_1.toString() + DELIM + END_1.toString() + DELIM + GROUP_NAME_1 + LINE_RET + 
        START_2.toString() + DELIM + END_2.toString() + DELIM + GROUP_NAME_2 + LINE_RET + 
        START_3.toString() + DELIM + END_3.toString() + DELIM + GROUP_NAME_3 + LINE_RET + 
        START_4.toString() + DELIM + END_4.toString() + DELIM + GROUP_NAME_4 + LINE_RET + 
        START_8.toString() + DELIM + END_8.toString() + DELIM + GROUP_NAME_5 + LINE_RET + 
        START_6.toString() + DELIM + END_6.toString() + DELIM + GROUP_NAME_7;
      // @formatter:on
    } else if (year.equals(END_YEAR)) {
      // Delete group 8
      // @formatter:off
      input = 
        START_1.toString() + DELIM + END_1.toString() + DELIM + GROUP_NAME_1 + LINE_RET + 
        START_2.toString() + DELIM + END_2.toString() + DELIM + GROUP_NAME_2 + LINE_RET + 
        START_3.toString() + DELIM + END_3.toString() + DELIM + GROUP_NAME_3 + LINE_RET + 
        START_4.toString() + DELIM + END_4.toString() + DELIM + GROUP_NAME_4 + LINE_RET + 
        START_6.toString() + DELIM + END_6.toString() + DELIM + GROUP_NAME_7;
      // @formatter:on
    }
    if (input == null) {
      throw new RuntimeException("could not get data for year " + year);
    }
    return new StringReader(input);
  }

  @Override
  public Reader getAccountMapReader(Integer year) {
    String input = ACCOUNT_NUMBER_1 + DELIM + ACCOUNT_NAME_NEW;
    return new StringReader(input);
  }

  @Override
  public Reader getAccountReader(Integer year) {
    String input =
      ACCOUNT_NUMBER_1 + DELIM + ACCOUNT_NAME_1 + DELIM + CREDIT + LINE_RET
          + ACCOUNT_NUMBER_2 + DELIM + ACCOUNT_NAME_2 + DELIM + CREDIT
          + LINE_RET + ACCOUNT_NUMBER_3 + DELIM + ACCOUNT_NAME_3 + DELIM
          + CREDIT + LINE_RET + ACCOUNT_NUMBER_4 + DELIM + ACCOUNT_NAME_4
          + DELIM + CREDIT;
    return new StringReader(input);
  }

  @Override
  public Reader getReimbursementReader(Integer year) {
    String input =
      REIM_ID + DELIM + REC_YEAR + DELIM + REC_ID + DELIM + ACCOUNT_NUMBER_2
          + DELIM + AMOUNT + DELIM + ALLOCATED_AMOUNT;
    return new StringReader(input);
  }

  @Override
  public Reader getBalanceReader(Integer year) {
    String input =
      ACCOUNT_NUMBER_3 + DELIM + FORMATTED_TRANS_DATE + DELIM + DEBIT + DELIM
          + AMOUNT;
    return new StringReader(input);
  }

  @Override
  public Reader getTransactionReader(Integer year) {
    String input =
      TRANS_ID_1 + DELIM + FORMATTED_DESC + DELIM + FORMATTED_TRANS_DATE
          + DELIM + FALSE;
    return new StringReader(input);
  }

  @Override
  public Reader getItemReader(Integer year) {
    String input =
      TRANS_ID_1 + DELIM + ACCOUNT_NUMBER_1 + DELIM + AMOUNT + DELIM + "DR"
          + DELIM + "N" + LINE_RET + TRANS_ID_1 + DELIM + ACCOUNT_NUMBER_4
          + DELIM + AMOUNT + DELIM + "CR" + DELIM + "N";
    return new StringReader(input);
  }

  @Override
  public void createWriters(Integer year) {
    incomeStatementWriter = new StringWriter();
    balanceSheetWriter = new StringWriter();
  }

  @Override
  public void closeWriters() {
    try {
      if (balanceSheetWriter != null) {
        balanceSheetWriter.close();
        balanceSheetWriter = null;
      }

      if (incomeStatementWriter != null) {
        incomeStatementWriter.close();
        incomeStatementWriter = null;
      }
    } catch (IOException e) {
      throw new RuntimeException("Exception closing writer", e);
    }
  }

  @Override
  public Writer getBalanceSheetWriter() {
    return balanceSheetWriter;
  }

  @Override
  public Writer getIncomeStatementWriter() {
    return incomeStatementWriter;
  }

  @Override
  public IFiscalYearUpdater getUpdater() {
    return new UnitTestFiscalYearUpdater();
  }

  @Override
  public IDataAccessService getDataAccessService() {
    return new UnitTestNoExceptionDataService();
  }

  @Override
  public IStorageManager getStorageManager() {
    return new UnitTestNoExceptionsStorageManager();
  }
}
