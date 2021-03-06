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
 * calls. This particular version creates a receivable transaction and a later reimbursement for
 * that receivable (2 transactions and a reimbursement link).
 *
 * @author Robert J. Muller
 */
public class UnitTestParametersReimbursementSameYear extends
  AbstractStatementMaintainingParameters {
  private static final String LINE_RET = "\n";
  private int pathCalls = 0;
  private int entityCalls = 0;
  private int incomeSummaryCalls = 0;
  private int startCalls = 0;
  private int endCalls = 0;

  // Data input
  private static final String DELIM = "\t";

  private static final Integer YEAR = 2017;

  private static final String SINGLE_ENTITY_NAME = "John Q. Doe";

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
  private static final String CHECKING_ACCOUNT_NAME = "Citicorp Checking (111222333444)";
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
  private static final String RECEIVABLE_DESC = "\"receivable income              \"";
  /** description for reimbursement enclosed in quotes with trailing blanks */
  private static final String REIMBURSEMENT_DESC = "\"reimbursement              \"";

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
    return "Poesys Associates";
  }

  @Override
  public String getIncomeSummaryAccountName() {
    return "Income Summary";
  }

  @Override
  public Integer getStartYear() {
    startCalls++;
    return 2017;
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
    String input = SINGLE_ENTITY_NAME + DELIM + CAP_ACCOUNT + DELIM + DIST_ACCOUNT + DELIM +
                   OWNERSHIP.toString();
    return new StringReader(input);
  }

  @Override
  public Reader getAccountGroupReader(Integer year) {
    String input = START_1.toString() + DELIM + END_1.toString() + DELIM + GROUP_NAME_1 + LINE_RET +
                   START_2.toString() + DELIM + END_2.toString() + DELIM + GROUP_NAME_2 + LINE_RET +
                   START_3.toString() + DELIM + END_3.toString() + DELIM + GROUP_NAME_3;
    return new StringReader(input);
  }

  @Override
  public Reader getAccountMapReader(Integer year) {
    String input = CHECKING_ACCOUNT + DELIM + NEW_CHECKING_ACCOUNT_NAME;
    return new StringReader(input);
  }

  @Override
  public Reader getAccountReader(Integer year) {
    String input = CHECKING_ACCOUNT + DELIM + CHECKING_ACCOUNT_NAME + DELIM + CREDIT + LINE_RET +
                   RECEIVABLE_ACCOUNT + DELIM + RECEIVABLE_ACCOUNT_NAME + DELIM + CREDIT +
                   LINE_RET + CASH_ACCOUNT + DELIM + CASH_ACCOUNT_NAME + DELIM + CREDIT + LINE_RET +
                   REVENUE_ACCOUNT + DELIM + REVENUE_ACCOUNT_NAME + DELIM + CREDIT;
    return new StringReader(input);
  }

  @Override
  public Reader getReimbursementReader(Integer year) {
    String input = REIMBURSEMENT_TRANS_ID + DELIM + YEAR + DELIM + RECEIVABLE_TRANS_ID + DELIM +
                   RECEIVABLE_ACCOUNT + DELIM + AMOUNT + DELIM + ALLOCATED_AMOUNT;
    return new StringReader(input);
  }

  @Override
  public Reader getBalanceReader(Integer year) {
    String input = CASH_ACCOUNT + DELIM + RECEIVABLE_DATE + DELIM + DEBIT + DELIM + AMOUNT;
    return new StringReader(input);
  }

  @Override
  public Reader getTransactionReader(Integer year) {
    String input =
      RECEIVABLE_TRANS_ID + DELIM + RECEIVABLE_DESC + DELIM + RECEIVABLE_DATE + DELIM + FALSE +
      LINE_RET + REIMBURSEMENT_TRANS_ID + DELIM + REIMBURSEMENT_DESC + DELIM + REIMBURSEMENT_DATE +
      DELIM + FALSE;
    return new StringReader(input);
  }

  @Override
  public Reader getItemReader(Integer year) {
    String input =
      RECEIVABLE_TRANS_ID + DELIM + RECEIVABLE_ACCOUNT + DELIM + AMOUNT + DELIM + DEBIT + DELIM +
      FALSE + LINE_RET + RECEIVABLE_TRANS_ID + DELIM + REVENUE_ACCOUNT + DELIM + AMOUNT + DELIM +
      CREDIT + DELIM + FALSE + LINE_RET + REIMBURSEMENT_TRANS_ID + DELIM + CHECKING_ACCOUNT +
      DELIM + AMOUNT + DELIM + DEBIT + DELIM + FALSE + LINE_RET + REIMBURSEMENT_TRANS_ID + DELIM +
      RECEIVABLE_ACCOUNT + DELIM + AMOUNT + DELIM + CREDIT + DELIM + FALSE;
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
