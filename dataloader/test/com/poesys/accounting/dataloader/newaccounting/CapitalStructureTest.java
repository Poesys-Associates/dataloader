/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.poesys.accounting.dataloader.IBuilder;
import com.poesys.accounting.dataloader.newaccounting.Statement.StatementType;
import com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder;
import com.poesys.accounting.dataloader.properties.IParameters;
import com.poesys.accounting.dataloader.properties.UnitTestParametersCapitalOneEntityOneYearNoDistribution;
import com.poesys.accounting.dataloader.properties.UnitTestParametersCapitalOneEntityOneYearWithDistribution;
import com.poesys.accounting.dataloader.properties.UnitTestParametersCapitalOneEntityTwoYearsNoDistribution;
import com.poesys.accounting.dataloader.properties.UnitTestParametersCapitalOneEntityTwoYearsWithDistribution;
import com.poesys.accounting.dataloader.properties.UnitTestParametersCapitalPoesys1997Bug;
import com.poesys.accounting.dataloader.properties.UnitTestParametersCapitalTwoEntitiesOneYear;
import com.poesys.accounting.dataloader.properties.UnitTestParametersCapitalTwoEntitiesTwoYears;


/**
 * CUT: CapitalStructure
 * 
 * @author Robert J. Muller
 */
public class CapitalStructureTest {
  private static final Logger logger =
    Logger.getLogger(CapitalStructureTest.class);
  private static final String CAP_ACCOUNT_1_NAME = "Personal Capital Partner 1";
  private static final String CAP_ACCOUNT_2_NAME = "Personal Capital Partner 2";
  private static final String DIST_ACCOUNT_NAME = "Distributions";
  private static final String DIST_ACCOUNT_1_NAME =
    "Distributions to Partner 1";
  private static final String DIST_ACCOUNT_2_NAME =
    "Distributions to Partner 2";
  private static final BigDecimal OWNERSHIP_2 =
    new BigDecimal("0.50").setScale(CapitalEntity.SCALE);
  private static final Integer YEAR1 = 2017;

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#CapitalStructure(java.lang.String)}
   * . Tests the constructor and getters.
   */
  @Test
  public void testCapitalStructure() {
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    CapitalStructure capStruct =
      new CapitalStructure(parameters.getIncomeSummaryAccountName());
    assertTrue("could not create capital structure", capStruct != null);
    assertTrue("wrong income summary name "
                   + capStruct.getIncomeSummaryAccount(),
               parameters.getIncomeSummaryAccountName().equals(capStruct.getIncomeSummaryAccount()));
    assertTrue("no entities list for capital structure",
               capStruct.getEntities() != null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#addEntities(java.util.List)}
   * . Also tests isValid() for valid structure.
   */
  @Test
  public void testAddEntities() {
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    CapitalStructure capStruct =
      new CapitalStructure(parameters.getIncomeSummaryAccountName());
    assertTrue("no entities list for capital structure",
               capStruct.getEntities() != null);
    CapitalEntity entity1 =
      new CapitalEntity(CAP_ACCOUNT_1_NAME, DIST_ACCOUNT_1_NAME, OWNERSHIP_2);
    CapitalEntity entity2 =
      new CapitalEntity(CAP_ACCOUNT_2_NAME, DIST_ACCOUNT_2_NAME, OWNERSHIP_2);
    List<CapitalEntity> list = new ArrayList<CapitalEntity>(2);
    list.add(entity1);
    list.add(entity2);
    capStruct.addEntities(list);
    assertTrue("wrong number of entities", capStruct.getEntities().size() == 2);
    assertTrue("capital structure invalid after adding entity",
               capStruct.isValid());
    assertTrue("wrong entity 1 added: "
                   + capStruct.getEntities().get(0).getCapitalAccount(),
               capStruct.getEntities().get(0).getCapitalAccount().equals(CAP_ACCOUNT_1_NAME));
    assertTrue("wrong entity 2 added: "
                   + capStruct.getEntities().get(1).getCapitalAccount(),
               capStruct.getEntities().get(1).getCapitalAccount().equals(CAP_ACCOUNT_2_NAME));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#isValid()}
   * .
   */
  @Test
  public void testIsValidNotValid() {
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    CapitalStructure capStruct =
      new CapitalStructure(parameters.getIncomeSummaryAccountName());
    assertTrue("no entities list for capital structure",
               capStruct.getEntities() != null);
    CapitalEntity entity1 =
      new CapitalEntity(CAP_ACCOUNT_1_NAME, DIST_ACCOUNT_1_NAME, OWNERSHIP_2);
    CapitalEntity entity2 =
      new CapitalEntity(CAP_ACCOUNT_2_NAME, DIST_ACCOUNT_2_NAME, OWNERSHIP_2);
    List<CapitalEntity> list = new ArrayList<CapitalEntity>(2);
    list.add(entity1);
    list.add(entity2);
    capStruct.addEntities(list);
    entity1.setOwnership(new BigDecimal("0.25"));
    assertTrue("capital structure valid after resetting entity",
               !capStruct.isValid());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getIncomeToCapitalTransaction(com.poesys.accounting.dataloader.newaccounting.FiscalYear, com.poesys.accounting.dataloader.IBuilder)}
   * . Tests getting transaction for a single capital entity in a single fiscal
   * year.
   */
  @Test
  public void testGetIncomeToCapitalTransactionSingleEntitySingleYear() {
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();
    Transaction transaction =
      capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    year.addTransaction(transaction);
    Statement stmt =
      new Statement(year, "Balance Sheet", StatementType.BALANCE_SHEET);
    assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getDistributionFromCapitalTransactions(com.poesys.accounting.dataloader.newaccounting.FiscalYear, com.poesys.accounting.dataloader.IBuilder)}
   * . Tests getting transactions for a single capital entity in a single fiscal
   * year with no distribution account and no distribution transactions.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsSingleEntitySingleYearNoDist() {
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();
    Transaction transaction =
      capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    year.addTransaction(transaction);
    List<Transaction> transactions =
      capStruct.getDistributionFromCapitalTransactions(year, builder);
    assertTrue("no distribution transaction list created", transactions != null);
    assertTrue("distribution transaction list is not empty for structure with no distribution account",
               transactions.isEmpty());
    Statement stmt =
      new Statement(year,
                    "1 Entity 1 Year Balance Sheet",
                    StatementType.BALANCE_SHEET);
    assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getDistributionFromCapitalTransactions(com.poesys.accounting.dataloader.newaccounting.FiscalYear, com.poesys.accounting.dataloader.IBuilder)}
   * . Tests getting transactions for a single capital entity in a single fiscal
   * year.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsSingleEntitySingleYearWithDist() {
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityOneYearWithDistribution();

    // Build the year.
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();

    // Transfer income summary to capital account
    Transaction transaction =
      capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    year.addTransaction(transaction);

    // Transfer distribution account to capital account
    List<Transaction> transactions =
      capStruct.getDistributionFromCapitalTransactions(year, builder);
    assertTrue("no distribution transaction list created", transactions != null);
    assertTrue("distribution transaction list is empty for structure with distribution account and transactions",
               !transactions.isEmpty());
    for (Transaction t : transactions) {
      year.addTransaction(t);
    }

    // Test results.
    logger.info("1 Entity 1 Year Balance Sheet test started");
    Statement stmt =
      new Statement(year,
                    "1 Entity 1 Year Balance Sheet",
                    StatementType.BALANCE_SHEET);
    BigDecimal balance = stmt.getBalance();
    assertTrue("balance sheet balance is not 0: " + balance,
               balance.compareTo(BigDecimal.ZERO) == 0);
    logger.info("1 Entity 1 Year Balance Sheet test started");
    Account distAccount = builder.getAccountByName(DIST_ACCOUNT_NAME);
    balance = stmt.getAccountBalance(distAccount);
    assertTrue("distribution account balance is not zero: " + balance,
               balance.compareTo(BigDecimal.ZERO.setScale(2)) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getIncomeToCapitalTransaction(com.poesys.accounting.dataloader.newaccounting.FiscalYear, com.poesys.accounting.dataloader.IBuilder)}
   * . Tests getting transaction for two capital entities in a single fiscal
   * year.
   */
  @Test
  public void testGetIncomeToCapitalTransactionTwoEntitiesSingleYear() {
    IParameters parameters = new UnitTestParametersCapitalTwoEntitiesOneYear();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();
    Transaction transaction =
      capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    year.addTransaction(transaction);
    Statement stmt =
      new Statement(year,
                    "2 Entities 1 Year Balance Sheet",
                    StatementType.BALANCE_SHEET);
    assertTrue("Balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getDistributionFromCapitalTransactions(com.poesys.accounting.dataloader.newaccounting.FiscalYear, com.poesys.accounting.dataloader.IBuilder)}
   * . Tests getting transactions for two capital entities in a single fiscal
   * year.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsTwoEntitiesSingleYear() {
    IParameters parameters = new UnitTestParametersCapitalTwoEntitiesOneYear();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();

    Transaction transaction =
      capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    year.addTransaction(transaction);
    List<Transaction> transactions =
      capStruct.getDistributionFromCapitalTransactions(year, builder);
    assertTrue("no distribution transaction list created", transactions != null);
    assertTrue("distribution transaction list is empty for structure with no distribution account",
               !transactions.isEmpty());
    for (Transaction distTransaction : transactions) {
      year.addTransaction(distTransaction);
    }
    Statement stmt =
      new Statement(year,
                    "2 Entities 1 Year Balance Sheet",
                    StatementType.BALANCE_SHEET);
    assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
    Account distAccount1 = builder.getAccountByName(DIST_ACCOUNT_1_NAME);
    assertTrue("distribution account 1 balance is not zero: "
                   + stmt.getAccountBalance(distAccount1),
               stmt.getAccountBalance(distAccount1).compareTo(BigDecimal.ZERO.setScale(2)) == 0);
    Account distAccount2 = builder.getAccountByName(DIST_ACCOUNT_2_NAME);
    assertTrue("distribution account 2 balance is not zero: "
                   + stmt.getAccountBalance(distAccount2),
               stmt.getAccountBalance(distAccount2).compareTo(BigDecimal.ZERO.setScale(2)) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getIncomeToCapitalTransaction(com.poesys.accounting.dataloader.newaccounting.FiscalYear, com.poesys.accounting.dataloader.IBuilder)}
   * . Tests getting transaction for a single capital entity across two fiscal
   * years.
   */
  @Test
  public void testGetIncomeToCapitalTransactionSingleEntityTwoYears() {
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityTwoYearsNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    for (int i = parameters.getStartYear(); i <= parameters.getEndYear(); i++) {
      builder.buildFiscalYear(i);
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();
      if (i == parameters.getStartYear()) {
        builder.buildBalances();
      }
      builder.buildTransactions();

      FiscalYear year = builder.getFiscalYear();
      CapitalStructure capStruct = builder.getCapitalStructure();
      Transaction transaction =
        capStruct.getIncomeToCapitalTransaction(year, builder);
      assertTrue("no capital accounts transaction created", transaction != null);
      year.addTransaction(transaction);
      Statement stmt =
        new Statement(year, "Balance Sheet", StatementType.BALANCE_SHEET);
      assertTrue("Balance sheet balance is not 0: " + stmt.getBalance(),
                 stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getDistributionFromCapitalTransactions(com.poesys.accounting.dataloader.newaccounting.FiscalYear, com.poesys.accounting.dataloader.IBuilder)}
   * . Tests getting transactions for a single capital entity across two fiscal
   * years with no distribution account or distribution transactions.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsSingleEntityTwoYearsNoDist() {
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityTwoYearsNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    for (Integer year = parameters.getStartYear(); year <= parameters.getEndYear(); year++) {
      builder.buildFiscalYear(year);
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();
      builder.buildBalances();
      builder.buildTransactions();

      FiscalYear fiscalYear = builder.getFiscalYear();
      CapitalStructure capStruct = builder.getCapitalStructure();
      Transaction transaction =
        capStruct.getIncomeToCapitalTransaction(fiscalYear, builder);
      assertTrue("no capital accounts transaction created", transaction != null);
      fiscalYear.addTransaction(transaction);
      List<Transaction> transactions =
        capStruct.getDistributionFromCapitalTransactions(fiscalYear, builder);
      assertTrue("no distribution transaction list created",
                 transactions != null);
      assertTrue("distribution transaction list is not empty for structure with distribution account for year "
                     + year,
                 transactions.isEmpty());
      Statement stmt =
        new Statement(fiscalYear, "Balance Sheet", StatementType.BALANCE_SHEET);
      assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
                 stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getDistributionFromCapitalTransactions(com.poesys.accounting.dataloader.newaccounting.FiscalYear, com.poesys.accounting.dataloader.IBuilder)}
   * . Tests getting transactions for a single capital entity across two fiscal
   * years with a distribution account and 2 distribution transactions.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsSingleEntityTwoYearsWithDist() {
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityTwoYearsWithDistribution();

    for (Integer year = parameters.getStartYear(); year <= parameters.getEndYear(); year++) {
      // Build the year.
      IBuilder builder = new OldDataBuilder(parameters);
      builder.buildFiscalYear(year);
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();
      builder.buildBalances();
      builder.buildTransactions();

      FiscalYear fiscalYear = builder.getFiscalYear();
      CapitalStructure capStruct = builder.getCapitalStructure();

      // Transfer income summary to capital account
      Transaction transaction =
        capStruct.getIncomeToCapitalTransaction(fiscalYear, builder);
      assertTrue("no capital accounts transaction created", transaction != null);
      fiscalYear.addTransaction(transaction);

      // Transfer distribution account to capital account
      List<Transaction> transactions =
        capStruct.getDistributionFromCapitalTransactions(fiscalYear, builder);
      assertTrue("no distribution transaction list created",
                 transactions != null);
      assertTrue("distribution transaction list is empty for structure with distribution account and transactions",
                 !transactions.isEmpty());
      for (Transaction t : transactions) {
        fiscalYear.addTransaction(t);
      }

      // Test results.
      logger.info("1 Entity 2 Years Balance Sheet test started for year "
                  + fiscalYear.getYear());
      Statement stmt =
        new Statement(fiscalYear,
                      "1 Entity 2 Years Balance Sheet",
                      StatementType.BALANCE_SHEET);
      BigDecimal balance = stmt.getBalance();
      assertTrue("balance sheet balance is not 0: " + balance,
                 balance.compareTo(BigDecimal.ZERO) == 0);
      logger.info("1 Entity 2 Years Balance Sheet test started");
      Account distAccount = builder.getAccountByName(DIST_ACCOUNT_NAME);
      balance = stmt.getAccountBalance(distAccount);
      assertTrue("distribution account balance is not zero: " + balance,
                 balance.compareTo(BigDecimal.ZERO.setScale(2)) == 0);
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getDistributionFromCapitalTransactions(com.poesys.accounting.dataloader.newaccounting.FiscalYear, com.poesys.accounting.dataloader.IBuilder)}
   * . Tests a bug with computing the two income summary transaction items being
   * added to the wrong accounts, resulting in more than a penny's difference
   * between the two capital accounts. This bug was a result of computing the
   * net income using the wrong sign, so the signs got reversed and the
   * remainder was added to the "larger" capital balance rather than the smaller
   * one.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsPoesys1997Bug() {
    IParameters parameters = new UnitTestParametersCapitalPoesys1997Bug();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();

    logger.info("Starting income summary transfer");
    Transaction transaction =
      capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    year.addTransaction(transaction);
    logger.info("Done with income summary transfer");

    // @formatter:off
    /*
    logger.info("Starting distribution transfer");
    List<Transaction> transactions =
      capStruct.getDistributionFromCapitalTransactions(year, builder);
    assertTrue("no distribution transaction list created", transactions != null);
    assertTrue("distribution transaction list is empty for structure with no distribution account",
               !transactions.isEmpty());
    for (Transaction distTransaction : transactions) {
      year.addTransaction(distTransaction);
    }
    logger.info("Done with distribution transfer");
    */
    // @formatter:on

    Statement stmt =
      new Statement(year,
                    "Poesys Associates 1997 Balance Sheet",
                    StatementType.BALANCE_SHEET);
    assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
    // @formatter:off
    /*
    assertTrue("distribution account 1 balance is not zero: "
                   + stmt.getAccountBalance(distAccount1),
               stmt.getAccountBalance(distAccount1).compareTo(BigDecimal.ZERO.setScale(2)) == 0);
    Account distAccount2 = builder.getAccountByName(DIST_ACCOUNT_2_NAME);
    assertTrue("distribution account 2 balance is not zero: "
                   + stmt.getAccountBalance(distAccount2),
               stmt.getAccountBalance(distAccount2).compareTo(BigDecimal.ZERO.setScale(2)) == 0);
    */
    // @formatter:on
    Account capAccount1 = builder.getAccountByName(CAP_ACCOUNT_1_NAME);
    BigDecimal cap1Balance = stmt.getAccountBalance(capAccount1);
    Account capAccount2 = builder.getAccountByName(CAP_ACCOUNT_2_NAME);
    BigDecimal cap2Balance = stmt.getAccountBalance(capAccount2);
    // Test the bug in the Poesys 1997 balance sheet, 2-cent difference
    logger.info("capital balances: " + cap1Balance + " : " + cap2Balance);
    logger.info("difference: " + cap1Balance.subtract(cap2Balance).abs());
    assertTrue("capital accounts differ by more than a penny: " + cap1Balance
                   + " : " + cap2Balance,
               cap1Balance.subtract(cap2Balance).abs().compareTo(new BigDecimal(".01").setScale(2)) <= 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getIncomeToCapitalTransaction(com.poesys.accounting.dataloader.newaccounting.FiscalYear, com.poesys.accounting.dataloader.IBuilder)}
   * . Tests getting transaction for two capital entities across two fiscal
   * years.
   */
  @Test
  public void testGetIncomeToCapitalTransactionTwoEntitiesTwoYears() {
    IParameters parameters = new UnitTestParametersCapitalTwoEntitiesTwoYears();
    IBuilder builder = new OldDataBuilder(parameters);
    for (int i = parameters.getStartYear(); i <= parameters.getEndYear(); i++) {
      builder.buildFiscalYear(i);
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();
      if (i == parameters.getStartYear()) {
        builder.buildBalances();
      }
      builder.buildTransactions();

      FiscalYear year = builder.getFiscalYear();
      CapitalStructure capStruct = builder.getCapitalStructure();
      Transaction transaction =
        capStruct.getIncomeToCapitalTransaction(year, builder);
      assertTrue("no capital accounts transaction created", transaction != null);
      year.addTransaction(transaction);
      Statement stmt =
        new Statement(year, "Balance Sheet", StatementType.BALANCE_SHEET);
      assertTrue("Balance sheet balance is not 0: " + stmt.getBalance(),
                 stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getDistributionFromCapitalTransactions(com.poesys.accounting.dataloader.newaccounting.FiscalYear, com.poesys.accounting.dataloader.IBuilder)}
   * . Tests getting transactions for two capital entities across two fiscal
   * years.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsTwoEntitiesTwoYears() {
    IParameters parameters = new UnitTestParametersCapitalTwoEntitiesTwoYears();

    for (Integer year = parameters.getStartYear(); year <= parameters.getEndYear(); year++) {
      // Build the year.
      IBuilder builder = new OldDataBuilder(parameters);
      builder.buildFiscalYear(year);
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();
      builder.buildBalances();
      builder.buildTransactions();

      FiscalYear fiscalYear = builder.getFiscalYear();
      CapitalStructure capStruct = builder.getCapitalStructure();

      // Transfer income summary to capital account
      Transaction transaction =
        capStruct.getIncomeToCapitalTransaction(fiscalYear, builder);
      assertTrue("no capital accounts transaction created", transaction != null);
      fiscalYear.addTransaction(transaction);

      // Transfer distribution account to capital account
      List<Transaction> transactions =
        capStruct.getDistributionFromCapitalTransactions(fiscalYear, builder);
      assertTrue("no distribution transaction list created",
                 transactions != null);
      assertTrue("distribution transaction list is empty for structure with distribution account and transactions",
                 !transactions.isEmpty());
      for (Transaction t : transactions) {
        fiscalYear.addTransaction(t);
      }

      // Test results.
      logger.info("2 Entities 2 Years Balance Sheet test started, year "
                  + fiscalYear.getYear());
      Statement stmt =
        new Statement(fiscalYear,
                      "2 Entities 2 Years Balance Sheet",
                      StatementType.BALANCE_SHEET);
      BigDecimal balance = stmt.getBalance();
      assertTrue("balance sheet balance is not 0: " + balance,
                 balance.compareTo(BigDecimal.ZERO) == 0);
      logger.info("2 Entities 2 Years Balance Sheet test done");
      Account distAccount1 = builder.getAccountByName(DIST_ACCOUNT_1_NAME);
      assertTrue("distribution account 1 balance is not zero: "
                     + stmt.getAccountBalance(distAccount1),
                 stmt.getAccountBalance(distAccount1).compareTo(BigDecimal.ZERO.setScale(2)) == 0);
      Account distAccount2 = builder.getAccountByName(DIST_ACCOUNT_2_NAME);
      assertTrue("distribution account 2 balance is not zero: "
                     + stmt.getAccountBalance(distAccount2),
                 stmt.getAccountBalance(distAccount2).compareTo(BigDecimal.ZERO.setScale(2)) == 0);
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#toString()}
   * .
   */
  @Test
  public void testToString() {
    IParameters parameters = new UnitTestParametersCapitalTwoEntitiesOneYear();
    // Build the year.
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(YEAR1);
    CapitalStructure capStruct = builder.getCapitalStructure();
    assertTrue("could not create capital structure", capStruct != null);
    assertTrue("string representation is wrong: " + capStruct,
               capStruct.toString().equals("CapitalStructure [incomeSummaryAccountName=Income Summary, entities=[CapitalEntity [capitalAccount=Personal Capital Partner 1, distributionAccount=Distributions to Partner 1, ownership=0.500], CapitalEntity [capitalAccount=Personal Capital Partner 2, distributionAccount=Distributions to Partner 2, ownership=0.500]]]"));
  }
}