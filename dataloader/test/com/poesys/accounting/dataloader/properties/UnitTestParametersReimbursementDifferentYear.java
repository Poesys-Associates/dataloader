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
import com.poesys.accounting.dataloader.newaccounting.RjmMlsFiscalYearUpdater;
import com.poesys.accounting.dataloader.newaccounting.UnitTestNoExceptionDataService;
import com.poesys.accounting.dataloader.newaccounting.UnitTestNoExceptionsStorageManager;


/**
 * An implementation of IParameters that returns the parameters for a unit test
 * without looking at a properties file or command line arguments; maintains
 * counts of the calls to each parameter getter for unit testing methods that
 * call the parameters, to make sure the method does make the required calls.
 * This particular version creates a receivable transaction and a later
 * reimbursement for that receivable (2 transactions and a reimbursement link),
 * with the two transactions in different fiscal years. This requires two
 * separate parameter classes, one for the first year (this one) and one for the
 * second, reimbursing year (UnitTestParametersReimbursementDifferentYear2).
 * 
 * @author Robert J. Muller
 */
public class UnitTestParametersReimbursementDifferentYear extends
    AbstractStatementMaintainingParameters {
  private static final String LINE_RET = "\n";
  private int pathCalls = 0;
  private int entityCalls = 0;
  private int incomeSummaryCalls = 0;
  private int startCalls = 0;
  private int endCalls = 0;

  private static final int YEAR_1 = 2016;
  private static final int YEAR_2 = 2017;

  /**
   * special tracking variable for this implementation to allow conditionals on
   * current year
   */
  private int currentYear = YEAR_1;

  // Data input
  private static final String DELIM = "\t";

  private static final String CAP_ACCOUNT = "Personal Capital";
  private static final String DIST_ACCOUNT = "Distributions";
  private static final Double OWNERSHIP = 1.0D;

  private static final String GROUP_NAME_1 = "Cash";
  private static final String GROUP_NAME_2 = "Accounts Receivable";
  private static final String GROUP_NAME_3 = "Income";

  private static final Float START_1 = 100.00F;
  private static final Float END_1 = 109.99F;
  private static final Float START_2 = 110.00F;
  private static final Float END_2 = 119.99F;
  private static final Float START_3 = 400.00F;
  private static final Float END_3 = 409.99F;

  private static final Float CHECKING_ACCOUNT = 100.0F;
  private static final String CHECKING_ACCOUNT_NAME =
    "Citicorp Checking (111222333444)";
  private static final String NEW_CHECKING_ACCOUNT_NAME = "Citicorp Checking";
  private static final Float RECEIVABLE_ACCOUNT = 110.0F;
  private static final String RECEIVABLE_ACCOUNT_NAME = "Accounts Receivable";
  private static final Float CASH_ACCOUNT = 109.0F;
  private static final String CASH_ACCOUNT_NAME = "Other Cash";
  private static final Float REVENUE_ACCOUNT = 400.0F;
  private static final String REVENUE_ACCOUNT_NAME = "Revenue";
  private static final Double AMOUNT = 100.00D;
  private static final Double ALLOCATED_AMOUNT = 0.00D;

  private static final Integer RECEIVABLE_TRANS_ID = 200;
  private static final Integer REIMBURSEMENT_TRANS_ID = 400;

  private static final String CREDIT = "CR";
  private static final String DEBIT = "DR";

  private static final String FALSE = "N";

  /** description for receivable enclosed in quotes with trailing blanks */
  private static final String RECEIVABLE_DESC =
    "\"receivable income              \"";

  /** receivable date as Oracle-formatted string representation */
  private static final String RECEIVABLE_DATE = "26-JUN-16";

  /** description for reimbursement enclosed in quotes with trailing blanks */
  private static final String REIMBURSEMENT_DESC =
    "\"reimbursement              \"";

  /** reimbursement date as Oracle-formatted string representation */
  private static final String REIMBURSEMENT_DATE = "20-JUL-17";
  /** year in which receivable is reimbursed */
  private static final Integer RECEIVABLE_YEAR = YEAR_1;

  // writers
  private Writer balanceSheetWriter = null;
  private Writer incomeStatementWriter = null;

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
    return 2016;
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
    String input = CAP_ACCOUNT + DELIM + DIST_ACCOUNT + DELIM + OWNERSHIP.toString();
    return new StringReader(input);
  }

  @Override
  public Reader getAccountGroupReader(Integer year) {
    String input =
      START_1.toString() + DELIM + END_1.toString() + DELIM + GROUP_NAME_1
          + LINE_RET + START_2.toString() + DELIM + END_2.toString() + DELIM
          + GROUP_NAME_2 + LINE_RET + START_3.toString() + DELIM
          + END_3.toString() + DELIM + GROUP_NAME_3;
    return new StringReader(input);
  }

  @Override
  public Reader getAccountMapReader(Integer year) {
    String input = CHECKING_ACCOUNT + DELIM + NEW_CHECKING_ACCOUNT_NAME;
    return new StringReader(input);
  }

  @Override
  public Reader getAccountReader(Integer year) {
    String input =
      CHECKING_ACCOUNT + DELIM + CHECKING_ACCOUNT_NAME + DELIM + CREDIT
          + LINE_RET + RECEIVABLE_ACCOUNT + DELIM + RECEIVABLE_ACCOUNT_NAME
          + DELIM + CREDIT + LINE_RET + CASH_ACCOUNT + DELIM
          + CASH_ACCOUNT_NAME + DELIM + CREDIT + LINE_RET + REVENUE_ACCOUNT
          + DELIM + REVENUE_ACCOUNT_NAME + DELIM + CREDIT;
    return new StringReader(input);
  }

  @Override
  public Reader getReimbursementReader(Integer year) {
    // Reimbursement in second year
    String input = "";
    if (currentYear == YEAR_2) {
      input =
        REIMBURSEMENT_TRANS_ID + DELIM + RECEIVABLE_YEAR + DELIM
            + RECEIVABLE_TRANS_ID + DELIM + RECEIVABLE_ACCOUNT + DELIM + AMOUNT
            + DELIM + ALLOCATED_AMOUNT;
    }
    return new StringReader(input);
  }

  @Override
  public Reader getBalanceReader(Integer year) {
    String input =
      CASH_ACCOUNT + DELIM + RECEIVABLE_DATE + DELIM + DEBIT + DELIM + AMOUNT;
    return new StringReader(input);
  }

  @Override
  public Reader getTransactionReader(Integer year) {
    // Receivable transaction in first year, reimbursable in second
    String input = null;
    if (currentYear == YEAR_1) {
      input =
        RECEIVABLE_TRANS_ID + DELIM + RECEIVABLE_DESC + DELIM + RECEIVABLE_DATE
            + DELIM + FALSE;
    } else {
      input =
        REIMBURSEMENT_TRANS_ID + DELIM + REIMBURSEMENT_DESC + DELIM
            + REIMBURSEMENT_DATE + DELIM + FALSE;
    }
    return new StringReader(input);
  }

  @Override
  public Reader getItemReader(Integer year) {
    // Receivable items in first year, reimbursable in second
    String input = null;
    if (currentYear == 2016) {
      input =
        RECEIVABLE_TRANS_ID + DELIM + RECEIVABLE_ACCOUNT + DELIM + AMOUNT
            + DELIM + DEBIT + DELIM + FALSE + LINE_RET + RECEIVABLE_TRANS_ID
            + DELIM + REVENUE_ACCOUNT + DELIM + AMOUNT + DELIM + CREDIT + DELIM
            + FALSE;
    } else {
      input =
        REIMBURSEMENT_TRANS_ID + DELIM + CHECKING_ACCOUNT + DELIM + AMOUNT
            + DELIM + DEBIT + DELIM + FALSE + LINE_RET + REIMBURSEMENT_TRANS_ID
            + DELIM + RECEIVABLE_ACCOUNT + DELIM + AMOUNT + DELIM + CREDIT
            + DELIM + FALSE;
    }
    return new StringReader(input);
  }

  /**
   * Method for this implementation; sets the current year for use in
   * conditionals that determine actual data returned
   * 
   * @param year the current fiscal year being processed
   */
  public void setCurrentYear(int year) {
    currentYear = year;
  }

  @Override
  public void createWriters(Integer year) {
    try {
      if (balanceSheetWriter != null) {
        balanceSheetWriter.close();
      }
      balanceSheetWriter = new StringWriter();

      if (incomeStatementWriter != null) {
        incomeStatementWriter.close();
      }
      incomeStatementWriter = new StringWriter();
    } catch (IOException e) {
      throw new RuntimeException("Exception closing writer", e);
    }
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
    return new RjmMlsFiscalYearUpdater();
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
