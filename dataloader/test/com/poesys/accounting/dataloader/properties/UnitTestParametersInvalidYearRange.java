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
 * An implementation of IParameters that returns the parameters for a unit test without looking at a
 * properties file or command line arguments; maintains counts of the calls to each parameter getter
 * for unit testing methods that call the parameters, to make sure the method does make the required
 * calls. This particular version returns an end year that is less than the start year.
 *
 * @author Robert J. Muller
 */
public class UnitTestParametersInvalidYearRange extends AbstractStatementMaintainingParameters {

  // test counters
  private int pathCalls = 0;
  private int entityCalls = 0;
  private int incomeSummaryCalls = 0;
  private int startCalls = 0;
  private int endCalls = 0;

  // data input
  private static final String DELIM = "\t";
  private static final String LINE_RET = "\n";

  private static final String SINGLE_ENTITY_NAME = "John Q. Doe";

  private static final Integer START_YEAR = 1990;
  /** typo in year, less than start */
  private static final Integer END_YEAR = 201;

  private static final String CASH_GROUP = "Cash";
  private static final String AR_GROUP = "Accounts Receivable";
  private static final String CREDIT_GROUP = "Credit Cards";
  private static final String CAPITAL_GROUP = "Capital";
  private static final String INCOME_GROUP = "Income";
  private static final String TAXES_GROUP = "Taxes";

  private static final Float CASH_START = 100.00F;
  private static final Float CASH_END = 109.99F;
  private static final Float AR_START = 110.00F;
  private static final Float AR_END = 119.99F;
  private static final Float CREDIT_START = 200.00F;
  private static final Float CREDIT_END = 209.99F;
  private static final Float CAPITAL_START = 300.00F;
  private static final Float CAPITAL_END = 399.99F;
  private static final Float INCOME_START = 400.00F;
  private static final Float INCOME_END = 409.99F;
  private static final Float TAXES_START = 500.00F;
  private static final Float TAXES_END = 509.99F;

  private static final Float CHECKING_ACCOUNT = 100.0F;
  private static final String CHECKING_ACCOUNT_NAME = "Citicorp Checking (111222333444)";
  private static final String NEW_CHECKING_ACCOUNT_NAME = "Citicorp Checking";

  private static final Float CASH_ACCOUNT = 109.0F;
  private static final String CASH_ACCOUNT_NAME = "Other Cash";

  private static final Float RECEIVABLE_ACCOUNT = 110.0F;
  private static final String RECEIVABLE_ACCOUNT_NAME = "Accounts Receivable";

  private static final Float CREDIT_ACCOUNT = 200.0F;
  private static final String CREDIT_ACCOUNT_NAME = "Credit Card";

  private static final Float CAP_ACCOUNT = 300.0F;
  private static final String CAP_ACCOUNT_NAME = "Personal Capital";
  private static final Double OWNERSHIP = 1.0D;

  private static final Float REVENUE_ACCOUNT = 400.0F;
  private static final String REVENUE_ACCOUNT_NAME = "Revenue";

  private static final Float INCOME_SUMMARY_ACCOUNT = 409.0F;
  private static final String INCOME_SUMMARY_ACCOUNT_NAME = "Income Summary";

  private static final Float TAX_ACCOUNT = 500.0F;
  private static final String TAX_ACCOUNT_NAME = "Federal Tax";

  private static final double CHECKING_BALANCE = 1000.00D;
  private static final double CASH_BALANCE = 20.00D;
  private static final double CREDIT_CARD_BALANCE = 143.00D;
  private static final double CAPITAL_BALANCE_AMOUNT =
    CHECKING_BALANCE + CASH_BALANCE - CREDIT_CARD_BALANCE;

  private static final Double REC_AMOUNT = 100.00D;
  private static final double INCOME_AMOUNT = 5000.00D;
  private static final double EXPENSE_AMOUNT = 237.45D;
  private static final Double ALLOCATED_AMOUNT = 0.00D;

  private static final Integer RECEIVABLE_TRANS_ID = 200;
  private static final Integer REIMBURSEMENT_TRANS_ID = 400;
  private static final Integer INCOME_TRANS_ID = 300;
  private static final Integer EXPENSE_TRANS_ID = 500;

  private static final String CREDIT = "CR";
  private static final String DEBIT = "DR";

  private static final String FALSE = "N";

  /** description for receivable enclosed in quotes with trailing blanks */
  private static final String RECEIVABLE_DESC = "\"receivable income              \"";
  /** description for reimbursement enclosed in quotes with trailing blanks */
  private static final String REIMBURSEMENT_DESC = "\"reimbursement              \"";
  /** description for income transaction enclosed in quotes with trailing blanks */
  private static final String INCOME_DESC = "\"cash income              \"";
  /**
   * description for expense transaction enclosed in quotes with trailing blanks
   */
  private static final String EXPENSE_DESC = "\"credit card payment of taxes              \"";

  /** balance date as Oracle-formatted string representation */
  private static final String BALANCE_DATE = "01-JAN-17";
  /** transaction date as Oracle-formatted string representation */
  private static final String TRANS_DATE = "01-APR-17";
  /** receivable date as Oracle-formatted string representation */
  private static final String RECEIVABLE_DATE = "26-JUN-17";
  /** reimbursement date as Oracle-formatted string representation */
  private static final String REIMBURSEMENT_DATE = "20-JUL-17";

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
    return SINGLE_ENTITY_NAME;
  }

  @Override
  public String getIncomeSummaryAccountName() {
    return INCOME_SUMMARY_ACCOUNT_NAME;
  }

  @Override
  public Integer getStartYear() {
    startCalls++;
    return START_YEAR;
  }

  @Override
  public Integer getEndYear() {
    endCalls++;
    return END_YEAR;
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
      SINGLE_ENTITY_NAME + DELIM + CAP_ACCOUNT_NAME + DELIM + DELIM + OWNERSHIP.toString();
    return new StringReader(input);
  }

  @Override
  public Reader getAccountGroupReader(Integer year) {
    String input =
      CASH_START.toString() + DELIM + CASH_END.toString() + DELIM + CASH_GROUP + LINE_RET +
      AR_START.toString() + DELIM + AR_END.toString() + DELIM + AR_GROUP + LINE_RET +
      INCOME_START.toString() + DELIM + INCOME_END.toString() + DELIM + INCOME_GROUP + LINE_RET +
      CREDIT_START.toString() + DELIM + CREDIT_END.toString() + DELIM + CREDIT_GROUP + LINE_RET +
      CAPITAL_START.toString() + DELIM + CAPITAL_END.toString() + DELIM + CAPITAL_GROUP + LINE_RET +
      TAXES_START.toString() + DELIM + TAXES_END.toString() + DELIM + TAXES_GROUP;
    return new StringReader(input);
  }

  @Override
  public Reader getAccountMapReader(Integer year) {
    String input = CHECKING_ACCOUNT + DELIM + NEW_CHECKING_ACCOUNT_NAME;
    return new StringReader(input);
  }

  @Override
  public Reader getAccountReader(Integer year) {
    // @formatter:off
    String input =
            CHECKING_ACCOUNT + DELIM + CHECKING_ACCOUNT_NAME + DELIM + CREDIT + LINE_RET 
          + RECEIVABLE_ACCOUNT + DELIM + RECEIVABLE_ACCOUNT_NAME + DELIM + DEBIT + LINE_RET 
          + CASH_ACCOUNT + DELIM + CASH_ACCOUNT_NAME + DELIM + CREDIT + LINE_RET 
          + CREDIT_ACCOUNT + DELIM + CREDIT_ACCOUNT_NAME + DELIM + CREDIT + LINE_RET 
          + CAP_ACCOUNT + DELIM + CAP_ACCOUNT_NAME + DELIM + CREDIT + LINE_RET
          + REVENUE_ACCOUNT + DELIM + REVENUE_ACCOUNT_NAME + DELIM + CREDIT + LINE_RET 
          + INCOME_SUMMARY_ACCOUNT + DELIM + INCOME_SUMMARY_ACCOUNT_NAME + DELIM + DEBIT + LINE_RET
          + TAX_ACCOUNT + DELIM + TAX_ACCOUNT_NAME + DELIM + DEBIT;
    // @formatter:off
    return new StringReader(input);
  }

  @Override
  public Reader getReimbursementReader(Integer year) {
    String input =
      REIMBURSEMENT_TRANS_ID + DELIM + END_YEAR + DELIM + RECEIVABLE_TRANS_ID
          + DELIM + RECEIVABLE_ACCOUNT + DELIM + REC_AMOUNT + DELIM
          + ALLOCATED_AMOUNT;
    return new StringReader(input);
  }

  @Override
  public Reader getBalanceReader(Integer year) {
    // @formatter:off
    String input =
            CHECKING_ACCOUNT + DELIM + BALANCE_DATE + DELIM + DEBIT + DELIM + CHECKING_BALANCE + LINE_RET 
          + CASH_ACCOUNT + DELIM + BALANCE_DATE + DELIM + DEBIT + DELIM + CASH_BALANCE + LINE_RET 
          + CREDIT_ACCOUNT + DELIM + BALANCE_DATE + DELIM + CREDIT + DELIM + CREDIT_CARD_BALANCE + LINE_RET
          + CAP_ACCOUNT + DELIM + BALANCE_DATE + DELIM + CREDIT + DELIM + CAPITAL_BALANCE_AMOUNT;
    // @formatter:on
    return new StringReader(input);
  }

  @Override
  public Reader getTransactionReader(Integer year) {
    // Transactions include receivable, reimbursement, income, expense,
    // @formatter:off
    String input =
            RECEIVABLE_TRANS_ID + DELIM + RECEIVABLE_DESC + DELIM + RECEIVABLE_DATE + DELIM + FALSE + LINE_RET 
          + REIMBURSEMENT_TRANS_ID + DELIM + REIMBURSEMENT_DESC + DELIM + REIMBURSEMENT_DATE + DELIM + FALSE + LINE_RET 
          + INCOME_TRANS_ID + DELIM + INCOME_DESC + DELIM + TRANS_DATE + DELIM + FALSE + LINE_RET 
          + EXPENSE_TRANS_ID + DELIM + EXPENSE_DESC + DELIM + TRANS_DATE + DELIM + FALSE;
    // @formatter:on
    return new StringReader(input);
  }

  @Override
  public Reader getItemReader(Integer year) {
    // Receivable: debit receivable, credit revenue
    // Reimbursement: debit checking, credit receivable
    // Income: debit checking, credit revenue
    // Expense: debit tax expense, credit credit card
    // @formatter:off
    String input =
            RECEIVABLE_TRANS_ID + DELIM + RECEIVABLE_ACCOUNT + DELIM + REC_AMOUNT + DELIM + DEBIT + DELIM + FALSE + LINE_RET 
          + RECEIVABLE_TRANS_ID + DELIM + REVENUE_ACCOUNT + DELIM + REC_AMOUNT + DELIM + CREDIT + DELIM + FALSE + LINE_RET 
          + REIMBURSEMENT_TRANS_ID + DELIM + CHECKING_ACCOUNT + DELIM + REC_AMOUNT + DELIM + DEBIT + DELIM + FALSE + LINE_RET 
          + REIMBURSEMENT_TRANS_ID + DELIM + RECEIVABLE_ACCOUNT + DELIM + REC_AMOUNT + DELIM + CREDIT + DELIM + FALSE + LINE_RET 
          + INCOME_TRANS_ID + DELIM + CHECKING_ACCOUNT + DELIM + INCOME_AMOUNT + DELIM + DEBIT + DELIM + FALSE + LINE_RET
          + INCOME_TRANS_ID + DELIM + REVENUE_ACCOUNT + DELIM + INCOME_AMOUNT + DELIM + CREDIT + DELIM + FALSE + LINE_RET 
          + EXPENSE_TRANS_ID + DELIM + TAX_ACCOUNT + DELIM + EXPENSE_AMOUNT + DELIM + DEBIT + DELIM + FALSE + LINE_RET 
          + EXPENSE_TRANS_ID + DELIM + CREDIT_ACCOUNT + DELIM + EXPENSE_AMOUNT + DELIM + CREDIT + DELIM + FALSE;
    // @formatter:on
    return new StringReader(input);
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

      if (this.balanceSheetDetailsWriter != null) {
        balanceSheetDetailsWriter.close();
      }
      balanceSheetDetailsWriter = new StringWriter();

      if (this.incomeStatementDetailsWriter != null) {
        incomeStatementDetailsWriter.close();
      }
      incomeStatementDetailsWriter = new StringWriter();
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
