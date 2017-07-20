/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.properties;


import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;


/**
 * An implementation of IParameters that returns the parameters for a unit test
 * without looking at a properties file or command line arguments; maintains
 * counts of the calls to each parameter getter for unit testing methods that
 * call the parameters, to make sure the method does make the required calls.
 * This particular version creates a transaction with items that don't balance,
 * thus setting up a test of invalid transaction processing.
 * 
 * @author Robert J. Muller
 */
public class UnitTestParametersInvalidTransaction implements IParameters {
  private static final String LINE_RET = "\n";
  private int pathCalls = 0;
  private int entityCalls = 0;
  private int startCalls = 0;
  private int endCalls = 0;

  // Data input
  private static final String DELIM = "\t";

  private static final Integer YEAR = 2017;

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
  private static final Double ALT_AMOUNT = 80.00D;

  private static final Integer TRANS_ID = 200;

  private static final String CREDIT = "CR";
  private static final String DEBIT = "DR";

  private static final String FALSE = "N";

  /** description for receivable enclosed in quotes with trailing blanks */
  private static final String FORMATTED_DESC =
    "\"revenue to checking              \"";

  /** receivable date as Oracle-formatted string representation */
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
  public Integer getStartYear() {
    startCalls++;
    // single year
    return YEAR;
  }

  @Override
  public Integer getEndYear() {
    endCalls++;
    // single year
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
    String input = "";
    return new StringReader(input);
  }

  @Override
  public Reader getBalanceReader(Integer year) {
    String input = CASH_ACCOUNT + DELIM + DEBIT + DELIM + AMOUNT;
    return new StringReader(input);
  }

  @Override
  public Reader getTransactionReader(Integer year) {
    String input =
      TRANS_ID + DELIM + FORMATTED_DESC + DELIM + FORMATTED_TRANS_DATE + DELIM
          + FALSE;
    return new StringReader(input);
  }

  @Override
  public Reader getItemReader(Integer year) {
    // transaction with debit to checking and credit to revenue; the debit
    // amount is different from the credit amount, so the transaction doesn't
    // balance and is invalid.
    String input =
      TRANS_ID + DELIM + CHECKING_ACCOUNT + DELIM + ALT_AMOUNT + DELIM + DEBIT
          + DELIM + FALSE + LINE_RET + TRANS_ID + DELIM + REVENUE_ACCOUNT
          + DELIM + AMOUNT + DELIM + CREDIT + DELIM + FALSE;
    return new StringReader(input);
  }

  @Override
  public Writer getBalanceSheetWriter(Integer year) {
    return new StringWriter();
  }

  @Override
  public Writer getIncomeStatementWriter(Integer year) {
    return new StringWriter();
  }
}
