/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.properties;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;


/**
 * An implementation of IParameters that returns the parameters for a unit test
 * without looking at a properties file or command line arguments; maintains
 * counts of the calls to each parameter getter for unit testing methods that
 * call the parameters, to make sure the method does make the required calls.
 * This particular version creates a complete set of transactions that set up a
 * test of year closing through the capital accounts for a single capital entity
 * over a single fiscal year.
 * 
 * @author Robert J. Muller
 */
public class UnitTestParametersCapitalPoesys1997Bug extends
    AbstractStatementMaintainingParameters {

  // test counters
  private int pathCalls = 0;
  private int entityCalls = 0;
  private int incomeSummaryCalls = 0;
  private int startCalls = 0;
  private int endCalls = 0;

  // data input
  private static final String DELIM = "\t";
  private static final String LINE_RET = "\n";

  private static final String DOUBLE_ENTITY_NAME = "Poesys Associates";

  private static final Integer YEAR = 2017;

  private static final Double OWNERSHIP = 0.5D;

  private static final String CASH_GROUP = "Cash";
  private static final String AR_GROUP = "Accounts Receivable";
  private static final String CREDIT_GROUP = "Credit Cards";
  private static final String CAPITAL_GROUP = "Capital";
  private static final String CAPITAL_CONTRA_GROUP = "Capital Contra";
  private static final String INCOME_GROUP = "Income";
  private static final String TAXES_GROUP = "Taxes";

  private static final Float CASH_START = 100.00F;
  private static final Float CASH_END = 109.99F;
  private static final Float AR_START = 110.00F;
  private static final Float AR_END = 119.99F;
  private static final Float CREDIT_START = 200.00F;
  private static final Float CREDIT_END = 209.99F;
  private static final Float CAPITAL_START = 300.00F;
  private static final Float CAPITAL_END = 309.99F;
  private static final Float CAPITAL_CONTRA_START = 310.00F;
  private static final Float CAPITAL_CONTRA_END = 319.99F;
  private static final Float INCOME_START = 400.00F;
  private static final Float INCOME_END = 409.99F;
  private static final Float TAXES_START = 500.00F;
  private static final Float TAXES_END = 509.99F;

  private static final Float CHECKING_ACCOUNT = 100.0F;
  private static final String CHECKING_ACCOUNT_NAME =
    "Citicorp Checking (111222333444)";
  private static final String NEW_CHECKING_ACCOUNT_NAME = "Citicorp Checking";

  private static final Float CASH_ACCOUNT = 109.0F;
  private static final String CASH_ACCOUNT_NAME = "Other Cash";

  private static final Float RECEIVABLE_ACCOUNT = 110.0F;
  private static final String RECEIVABLE_ACCOUNT_NAME = "Accounts Receivable";

  private static final Float CREDIT_ACCOUNT = 200.0F;
  private static final String CREDIT_ACCOUNT_NAME = "Credit Card";

  private static final String CAP_ACCOUNT_1_NAME = "Personal Capital Partner 1";
  private static final Float CAP_ACCOUNT_1 = 300.0F;
  private static final String CAP_ACCOUNT_2_NAME = "Personal Capital Partner 2";
  private static final Float CAP_ACCOUNT_2 = 301.0F;
  private static final String DIST_ACCOUNT_1_NAME =
    "Distributions to Partner 1";
  private static final Float DIST_ACCOUNT_1 = 310.0F;
  private static final String DIST_ACCOUNT_2_NAME =
    "Distributions to Partner 2";
  private static final Float DIST_ACCOUNT_2 = 311.0F;

  private static final Float REVENUE_ACCOUNT = 400.0F;
  private static final String REVENUE_ACCOUNT_NAME = "Revenue";

  private static final Float INCOME_SUMMARY_ACCOUNT = 409.0F;
  private static final String INCOME_SUMMARY_ACCOUNT_NAME = "Income Summary";

  private static final Float TAX_ACCOUNT = 500.0F;
  private static final String TAX_ACCOUNT_NAME = "Federal Tax";

  private static final double FULL_DIST_AMOUNT = 69638.48D; // full amount
  private static final double DIST_AMOUNT = 34819.24D; // 1/2 of full amount
  private static final double INCOME_AMOUNT = 43964.37D;
  private static final double CHECKING_BALANCE_AMOUNT = 2458.47D;
  private static final double CREDIT_BALANCE_AMOUNT = 935.50D;
  private static final double CAP_1_BALANCE_AMOUNT = 761.48D;
  private static final double CAP_2_BALANCE_AMOUNT = 761.49D;
  private static final double DIST_1_BALANCE_AMOUNT = 0.00D;
  private static final double DIST_2_BALANCE_AMOUNT = 0.00D;

  private static final Integer INCOME_TRANS_ID = 300;
  private static final Integer DIST_TRANS_ID = 600;

  private static final String CREDIT = "CR";
  private static final String DEBIT = "DR";

  private static final String FALSE = "N";

  /** description for income transaction enclosed in quotes with trailing blanks */
  private static final String INCOME_DESC = "\"cash income              \"";

  /** description for dist transaction enclosed in quotes with trailing blanks */
  private static final String DIST_DESC =
    "\"cash distribution to partners by check        \"";

  /** balance date as Oracle-formatted string representation */
  private static final String BALANCE_DATE = "01-JAN-17";
  /** transaction date as Oracle-formatted string representation */
  private static final String TRANS_DATE = "01-APR-17";

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
    return DOUBLE_ENTITY_NAME;
  }

  @Override
  public String getIncomeSummaryAccountName() {
    return INCOME_SUMMARY_ACCOUNT_NAME;
  }

  @Override
  public Integer getStartYear() {
    startCalls++;
    return YEAR;
  }

  @Override
  public Integer getEndYear() {
    endCalls++;
    return YEAR;
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

  // two entities here, partnership
  @Override
  public Reader getCapitalEntityReader() {
    String input =
      CAP_ACCOUNT_1_NAME + DELIM + DIST_ACCOUNT_1_NAME + DELIM
          + OWNERSHIP.toString() + LINE_RET + CAP_ACCOUNT_2_NAME + DELIM
          + DIST_ACCOUNT_2_NAME + DELIM + OWNERSHIP.toString();
    return new StringReader(input);
  }

  @Override
  public Reader getAccountGroupReader(Integer year) {
    // @formatter:off
    String input =
      CASH_START.toString() + DELIM + CASH_END.toString() + DELIM + CASH_GROUP + LINE_RET 
          + AR_START.toString() + DELIM + AR_END.toString() + DELIM + AR_GROUP + LINE_RET 
          + INCOME_START.toString() + DELIM + INCOME_END.toString() + DELIM + INCOME_GROUP + LINE_RET
          + CREDIT_START.toString() + DELIM + CREDIT_END.toString() + DELIM + CREDIT_GROUP + LINE_RET 
          + CAPITAL_START.toString() + DELIM + CAPITAL_END.toString() + DELIM + CAPITAL_GROUP + LINE_RET
          + CAPITAL_CONTRA_START.toString() + DELIM + CAPITAL_CONTRA_END.toString() + DELIM + CAPITAL_CONTRA_GROUP + LINE_RET
          + TAXES_START.toString() + DELIM + TAXES_END.toString() + DELIM + TAXES_GROUP;
    // @formatter:on
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
          + DELIM + DEBIT + LINE_RET + CASH_ACCOUNT + DELIM + CASH_ACCOUNT_NAME
          + DELIM + CREDIT + LINE_RET + CREDIT_ACCOUNT + DELIM
          + CREDIT_ACCOUNT_NAME + DELIM + CREDIT + LINE_RET + CAP_ACCOUNT_1
          + DELIM + CAP_ACCOUNT_1_NAME + DELIM + CREDIT + LINE_RET
          + CAP_ACCOUNT_2 + DELIM + CAP_ACCOUNT_2_NAME + DELIM + CREDIT
          + LINE_RET + DIST_ACCOUNT_1 + DELIM + DIST_ACCOUNT_1_NAME + DELIM
          + DEBIT + LINE_RET + DIST_ACCOUNT_2 + DELIM + DIST_ACCOUNT_2_NAME
          + DELIM + DEBIT + LINE_RET + REVENUE_ACCOUNT + DELIM
          + REVENUE_ACCOUNT_NAME + DELIM + CREDIT + LINE_RET
          + INCOME_SUMMARY_ACCOUNT + DELIM + INCOME_SUMMARY_ACCOUNT_NAME
          + DELIM + DEBIT + LINE_RET + TAX_ACCOUNT + DELIM + TAX_ACCOUNT_NAME
          + DELIM + DEBIT;
    return new StringReader(input);
  }

  @Override
  public Reader getReimbursementReader(Integer year) {
    // no reimbursements
    String input = "";
    return new StringReader(input);
  }

  @Override
  public Reader getBalanceReader(Integer year) {
    // @formatter:off
    String input =
            CHECKING_ACCOUNT + DELIM + BALANCE_DATE + DELIM + DEBIT + DELIM + CHECKING_BALANCE_AMOUNT + LINE_RET 
          + CASH_ACCOUNT + DELIM + BALANCE_DATE + DELIM + DEBIT + DELIM + 00.00D + LINE_RET 
          + CREDIT_ACCOUNT + DELIM + BALANCE_DATE + DELIM + CREDIT + DELIM + CREDIT_BALANCE_AMOUNT + LINE_RET
          + CAP_ACCOUNT_1 + DELIM + BALANCE_DATE + DELIM + CREDIT + DELIM + CAP_1_BALANCE_AMOUNT + LINE_RET 
          + CAP_ACCOUNT_2 + DELIM + BALANCE_DATE + DELIM + CREDIT + DELIM + CAP_2_BALANCE_AMOUNT + LINE_RET 
          + DIST_ACCOUNT_1 + DELIM + BALANCE_DATE + DELIM + CREDIT + DELIM + DIST_1_BALANCE_AMOUNT + LINE_RET 
          + DIST_ACCOUNT_2 + DELIM + BALANCE_DATE + DELIM + CREDIT + DELIM + DIST_2_BALANCE_AMOUNT;
    // @formatter:on
    return new StringReader(input);
  }

  @Override
  public Reader getTransactionReader(Integer year) {
    // Transactions include income, cash distribution
    // @formatter:off
    String input =
            INCOME_TRANS_ID + DELIM + INCOME_DESC + DELIM + TRANS_DATE + DELIM + FALSE + LINE_RET 
          + DIST_TRANS_ID + DELIM + DIST_DESC + DELIM + TRANS_DATE + DELIM + FALSE;
    // @formatter:on
    return new StringReader(input);
  }

  @Override
  public Reader getItemReader(Integer year) {
    // Income: debit checking, credit revenue--net income plus distribution
    // Distribution: credit checking account, debit distribution accounts (2)
    // @formatter:off
    String input =
            INCOME_TRANS_ID + DELIM + CHECKING_ACCOUNT + DELIM + INCOME_AMOUNT + DELIM + DEBIT + DELIM + FALSE + LINE_RET
          + INCOME_TRANS_ID + DELIM + REVENUE_ACCOUNT + DELIM + INCOME_AMOUNT + DELIM + CREDIT + DELIM + FALSE + LINE_RET 
          + DIST_TRANS_ID + DELIM + CHECKING_ACCOUNT + DELIM + FULL_DIST_AMOUNT + DELIM + CREDIT + DELIM + FALSE + LINE_RET 
          + DIST_TRANS_ID + DELIM + DIST_ACCOUNT_1 + DELIM + DIST_AMOUNT + DELIM + DEBIT + DELIM + FALSE+ LINE_RET 
          + DIST_TRANS_ID + DELIM + DIST_ACCOUNT_2 + DELIM + DIST_AMOUNT + DELIM + DEBIT + DELIM + FALSE;
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
}
