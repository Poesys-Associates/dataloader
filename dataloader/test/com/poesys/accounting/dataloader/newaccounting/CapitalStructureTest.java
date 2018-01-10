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
package com.poesys.accounting.dataloader.newaccounting;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.poesys.accounting.dataloader.properties.*;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.poesys.accounting.dataloader.IBuilder;
import com.poesys.accounting.dataloader.newaccounting.Statement.StatementType;
import com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder;

/**
 * CUT: CapitalStructure
 *
 * @author Robert J. Muller
 */
public class CapitalStructureTest {
  private static final Logger logger = Logger.getLogger(CapitalStructureTest.class);
  private static final String DESCRIPTION = "Equity Account";
  private static final String CAP_ENTITY_1_NAME = "Partner 1";
  private static final String CAP_ENTITY_2_NAME = "Partner 2";
  private static final String CAPITAL_ACCOUNT_1_NAME = "Partner 1 Capital";
  private static final Account CAPITAL_ACCOUNT_PARTNER_1 =
    new Account(CAPITAL_ACCOUNT_1_NAME, DESCRIPTION, AccountType.EQUITY, false, false);
  private static final String DIST_ACCOUNT_1_NAME = "Partner 1 Distributions";
  private static final Account DIST_ACCOUNT_1 =
    new Account(DIST_ACCOUNT_1_NAME, DESCRIPTION, AccountType.EQUITY, false, false);
  private static final String DIST_ACCOUNT_NAME = "Distributions";
  private static final String CAPITAL_ACCOUNT_2_NAME = "Partner 2 Capital";
  private static final Account CAPITAL_ACCOUNT_PARTNER_2 =
    new Account(CAPITAL_ACCOUNT_2_NAME, DESCRIPTION, AccountType.EQUITY, false, false);
  private static final String DIST_ACCOUNT_2_NAME = "Partner 2 Distributions";
  private static final Account DIST_ACCOUNT_2 =
    new Account(DIST_ACCOUNT_2_NAME, DESCRIPTION, AccountType.EQUITY, false, false);
  private static final BigDecimal OWNERSHIP_2 =
    new BigDecimal("0.50").setScale(CapitalEntity.SCALE, RoundingMode.HALF_UP);
  private static final Integer YEAR1 = 2017;

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#CapitalStructure(java.lang.String)}
   * . Tests the constructor and getters.
   */
  @Test
  public void testCapitalStructure() {
    IParameters parameters = new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    CapitalStructure capStruct = new CapitalStructure(parameters.getIncomeSummaryAccountName());
    assertTrue("wrong income summary name " + capStruct.getIncomeSummaryAccount(),
               parameters.getIncomeSummaryAccountName().equals(
                 capStruct.getIncomeSummaryAccount()));
    assertTrue("no entities list for capital structure", capStruct.getEntities() != null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#addEntities(java.util.List)}
   * . Also tests isValid() for valid structure.
   */
  @Test
  public void testAddEntities() {
    IParameters parameters = new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    CapitalStructure capStruct = new CapitalStructure(parameters.getIncomeSummaryAccountName());
    assertTrue("no entities list for capital structure", capStruct.getEntities() != null);
    CapitalEntity entity1 =
      new CapitalEntity(CAP_ENTITY_1_NAME, CAPITAL_ACCOUNT_1_NAME, DIST_ACCOUNT_1_NAME,
                        OWNERSHIP_2);
    entity1.setCapitalAccount(CAPITAL_ACCOUNT_PARTNER_1);
    entity1.setDistributionAccount(DIST_ACCOUNT_1);
    CapitalEntity entity2 =
      new CapitalEntity(CAP_ENTITY_2_NAME, CAPITAL_ACCOUNT_2_NAME, DIST_ACCOUNT_2_NAME,
                        OWNERSHIP_2);
    entity2.setCapitalAccount(CAPITAL_ACCOUNT_PARTNER_2);
    entity2.setDistributionAccount(DIST_ACCOUNT_2);
    List<CapitalEntity> list = new ArrayList<>(2);
    list.add(entity1);
    list.add(entity2);
    capStruct.addEntities(list);
    assertTrue("wrong number of entities", capStruct.getEntities().size() == 2);
    assertTrue("capital structure invalid after adding entity", capStruct.isValid());
    assertTrue("wrong entity 1 added: " + capStruct.getEntities().get(0).getName(),
               capStruct.getEntities().get(0).getName().equals(CAP_ENTITY_1_NAME));
    assertTrue("wrong entity 2 added: " + capStruct.getEntities().get(1).getName(),
               capStruct.getEntities().get(1).getName().equals(CAP_ENTITY_2_NAME));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#isValid()}
   * .
   */
  @Test
  public void testIsValidNotValid() {
    IParameters parameters = new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    CapitalStructure capStruct = new CapitalStructure(parameters.getIncomeSummaryAccountName());
    assertTrue("no entities list for capital structure", capStruct.getEntities() != null);
    CapitalEntity entity1 =
      new CapitalEntity(CAP_ENTITY_1_NAME, CAPITAL_ACCOUNT_1_NAME, DIST_ACCOUNT_1_NAME,
                        OWNERSHIP_2);
    entity1.setCapitalAccount(CAPITAL_ACCOUNT_PARTNER_1);
    entity1.setDistributionAccount(DIST_ACCOUNT_1);
    CapitalEntity entity2 =
      new CapitalEntity(CAP_ENTITY_2_NAME, CAPITAL_ACCOUNT_2_NAME, DIST_ACCOUNT_2_NAME,
                        OWNERSHIP_2);
    entity2.setCapitalAccount(CAPITAL_ACCOUNT_PARTNER_2);
    entity2.setDistributionAccount(DIST_ACCOUNT_2);
    List<CapitalEntity> list = new ArrayList<>(2);
    list.add(entity1);
    list.add(entity2);
    capStruct.addEntities(list);
    entity1.setOwnership(new BigDecimal("0.25"));
    assertTrue("capital structure valid after resetting entity", !capStruct.isValid());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalStructure#getIncomeToCapitalTransaction(com.poesys.accounting.dataloader
   * .newaccounting.FiscalYear, * com.poesys.accounting.dataloader.IBuilder)} . Tests getting
   * transaction for a single capital entity in a single fiscal year.
   */
  @Test
  public void testGetIncomeToCapitalTransactionSingleEntitySingleYear() {
    IParameters parameters = new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();
    Transaction transaction = capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    //year.addTransaction(transaction);
    Statement stmt = new Statement(year, "Balance Sheet", StatementType.BALANCE_SHEET);
    assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalStructure#getDistributionTransactions(com.poesys.accounting.dataloader.newaccounting
   * .FiscalYear, * com.poesys.accounting.dataloader.IBuilder)} . Tests getting transactions for a
   * single capital entity in a single fiscal year with no distribution account and no distribution
   * transactions.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsSingleEntitySingleYearNoDist() {
    IParameters parameters = new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();
    Transaction transaction = capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    List<Transaction> transactions = capStruct.getDistributionTransactions(year, builder);
    assertTrue("no distribution transaction list created", transactions != null);
    assertTrue(
      "distribution transaction list is not empty for structure with no distribution account",
      transactions.isEmpty());
    Statement stmt =
      new Statement(year, "1 Entity 1 Year Balance Sheet", StatementType.BALANCE_SHEET);
    assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalStructure#getDistributionTransactions(com.poesys.accounting.dataloader.newaccounting
   * .FiscalYear, * com.poesys.accounting.dataloader.IBuilder)} . Tests getting transactions for a
   * single capital entity in a single fiscal year.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsSingleEntitySingleYearWithDist() {
    IParameters parameters = new UnitTestParametersCapitalOneEntityOneYearWithDistribution();

    // Build the year.
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();

    // Transfer income summary to capital account
    Transaction transaction = capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);

    // Transfer distribution account to capital account
    List<Transaction> transactions = capStruct.getDistributionTransactions(year, builder);
    assertTrue("no distribution transaction list created", transactions != null);
    assertTrue(
      "distribution transaction list is empty for structure with distribution account and " +
      "transactions", !transactions.isEmpty());

    // Test results.
    logger.info("1 Entity 1 Year Balance Sheet test started");
    Statement stmt =
      new Statement(year, "1 Entity 1 Year Balance Sheet", StatementType.BALANCE_SHEET);
    BigDecimal balance = stmt.getBalance();
    assertTrue("balance sheet balance is not 0: " + balance,
               balance.compareTo(BigDecimal.ZERO) == 0);
    logger.info("1 Entity 1 Year Balance Sheet test started");
    Account distAccount = builder.getAccountByName(DIST_ACCOUNT_NAME);
    balance = stmt.getAccountBalance(distAccount);
    assertTrue("distribution account balance is not zero: " + balance,
               balance.compareTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalStructure#getIncomeToCapitalTransaction(com.poesys.accounting.dataloader
   * .newaccounting.FiscalYear, * com.poesys.accounting.dataloader.IBuilder)} . Tests getting
   * transaction for two capital entities in a single fiscal year.
   */
  @Test
  public void testGetIncomeToCapitalTransactionTwoEntitiesSingleYear() {
    IParameters parameters = new UnitTestParametersCapitalTwoEntitiesOneYear();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();
    Transaction transaction = capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    Statement stmt =
      new Statement(year, "2 Entities 1 Year Balance Sheet", StatementType.BALANCE_SHEET);
    assertTrue("Balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalStructure#getDistributionTransactions(com.poesys.accounting.dataloader.newaccounting
   * .FiscalYear, * com.poesys.accounting.dataloader.IBuilder)} . Tests getting transactions for two
   * capital entities in a single fiscal year.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsTwoEntitiesSingleYear() {
    IParameters parameters = new UnitTestParametersCapitalTwoEntitiesOneYear();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();

    Transaction transaction = capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    List<Transaction> transactions = capStruct.getDistributionTransactions(year, builder);
    assertTrue("no distribution transaction list created", transactions != null);
    assertTrue("distribution transaction list is empty for structure with no distribution account",
               !transactions.isEmpty());
    Statement stmt =
      new Statement(year, "2 Entities 1 Year Balance Sheet", StatementType.BALANCE_SHEET);
    assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
    Account distAccount1 = builder.getAccountByName(DIST_ACCOUNT_1_NAME);
    assertTrue(
      "distribution account 1 balance is not zero: " + stmt.getAccountBalance(distAccount1),
      stmt.getAccountBalance(distAccount1).compareTo(
        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) == 0);
    Account distAccount2 = builder.getAccountByName(DIST_ACCOUNT_2_NAME);
    assertTrue(
      "distribution account 2 balance is not zero: " + stmt.getAccountBalance(distAccount2),
      stmt.getAccountBalance(distAccount2).compareTo(
        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalStructure#getIncomeToCapitalTransaction(com.poesys.accounting.dataloader
   * .newaccounting.FiscalYear, * com.poesys.accounting.dataloader.IBuilder)} . Tests getting
   * transaction for a single capital entity across two fiscal years.
   */
  @Test
  public void testGetIncomeToCapitalTransactionSingleEntityTwoYears() {
    IParameters parameters = new UnitTestParametersCapitalOneEntityTwoYearsNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
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
      Transaction transaction = capStruct.getIncomeToCapitalTransaction(year, builder);
      assertTrue("no capital accounts transaction created", transaction != null);
      Statement stmt = new Statement(year, "Balance Sheet", StatementType.BALANCE_SHEET);
      assertTrue("Balance sheet balance is not 0: " + stmt.getBalance(),
                 stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalStructure#getDistributionTransactions(com.poesys.accounting.dataloader.newaccounting
   * .FiscalYear, * com.poesys.accounting.dataloader.IBuilder)} . Tests getting transactions for a
   * single capital entity across two fiscal years with no distribution account or distribution
   * transactions.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsSingleEntityTwoYearsNoDist() {
    IParameters parameters = new UnitTestParametersCapitalOneEntityTwoYearsNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
    for (Integer year = parameters.getStartYear(); year <= parameters.getEndYear(); year++) {
      builder.buildFiscalYear(year);
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();
      builder.buildBalances();
      builder.buildTransactions();

      FiscalYear fiscalYear = builder.getFiscalYear();
      CapitalStructure capStruct = builder.getCapitalStructure();
      Transaction transaction = capStruct.getIncomeToCapitalTransaction(fiscalYear, builder);
      assertTrue("no capital accounts transaction created", transaction != null);
      List<Transaction> transactions = capStruct.getDistributionTransactions(fiscalYear, builder);
      assertTrue("no distribution transaction list created", transactions != null);
      assertTrue(
        "distribution transaction list is not empty for structure with distribution account for " +
        "year " + year, transactions.isEmpty());
      Statement stmt = new Statement(fiscalYear, "Balance Sheet", StatementType.BALANCE_SHEET);
      assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
                 stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalStructure#getDistributionTransactions(com.poesys.accounting.dataloader.newaccounting
   * .FiscalYear, * com.poesys.accounting.dataloader.IBuilder)} . Tests getting transactions for a
   * single capital entity across two fiscal years with a distribution account and 2 distribution
   * transactions.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsSingleEntityTwoYearsWithDist() {
    IParameters parameters = new UnitTestParametersCapitalOneEntityTwoYearsWithDistribution();

    for (Integer year = parameters.getStartYear(); year <= parameters.getEndYear(); year++) {
      // Build the year.
      IBuilder builder = new OldDataBuilder(parameters);
      builder.buildCapitalStructure();
      builder.buildFiscalYear(year);
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();
      builder.buildBalances();
      builder.buildTransactions();

      FiscalYear fiscalYear = builder.getFiscalYear();
      CapitalStructure capStruct = builder.getCapitalStructure();

      // Transfer income summary to capital account
      Transaction transaction = capStruct.getIncomeToCapitalTransaction(fiscalYear, builder);
      assertTrue("no capital accounts transaction created", transaction != null);

      // Transfer distribution account to capital account
      List<Transaction> transactions = capStruct.getDistributionTransactions(fiscalYear, builder);
      assertTrue("no distribution transaction list created", transactions != null);
      assertTrue(
        "distribution transaction list is empty for structure with distribution account and " +
        "transactions", !transactions.isEmpty());

      // Test results.
      logger.info("1 Entity 2 Years Balance Sheet test started for year " + fiscalYear.getYear());
      Statement stmt =
        new Statement(fiscalYear, "1 Entity 2 Years Balance Sheet", StatementType.BALANCE_SHEET);
      BigDecimal balance = stmt.getBalance();
      assertTrue("balance sheet balance is not 0: " + balance,
                 balance.compareTo(BigDecimal.ZERO) == 0);
      logger.info("1 Entity 2 Years Balance Sheet test started");
      Account distAccount = builder.getAccountByName(DIST_ACCOUNT_NAME);
      balance = stmt.getAccountBalance(distAccount);
      assertTrue("distribution account balance is not zero: " + balance,
                 balance.compareTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) == 0);
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalStructure#getDistributionTransactions(com.poesys.accounting.dataloader.newaccounting
   * .FiscalYear, * com.poesys.accounting.dataloader.IBuilder)} . Tests a bug with computing the two
   * income summary transaction items being added to the wrong accounts, resulting in more than a
   * penny's difference between the two capital accounts. This bug was a result of computing the net
   * income using the wrong sign, so the signs got reversed and the remainder was added to the
   * "larger" capital balance rather than the smaller one.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsPoesys1997Bug() {
    IParameters parameters = new UnitTestParametersCapitalPoesys1997Bug();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();

    logger.info("Starting income summary transfer");
    Transaction transaction = capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    logger.info("Done with income summary transfer");

    Statement stmt =
      new Statement(year, "Poesys Associates 1997 Balance Sheet", StatementType.BALANCE_SHEET);
    assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
    Account capAccount1 = builder.getAccountByName(CAPITAL_ACCOUNT_1_NAME);
    BigDecimal cap1Balance = stmt.getAccountBalance(capAccount1);
    Account capAccount2 = builder.getAccountByName(CAPITAL_ACCOUNT_2_NAME);
    BigDecimal cap2Balance = stmt.getAccountBalance(capAccount2);
    // Test the bug in the Poesys 1997 balance sheet, 2-cent difference
    logger.info("capital balances: " + cap1Balance + " : " + cap2Balance);
    logger.info("difference: " + cap1Balance.subtract(cap2Balance).abs());
    assertTrue("capital accounts differ by more than a penny: " + cap1Balance + " : " + cap2Balance,
               cap1Balance.subtract(cap2Balance).abs().compareTo(
                 new BigDecimal(".01").setScale(2, RoundingMode.HALF_UP)) <= 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalStructure#getDistributionTransactions(com.poesys.accounting.dataloader.newaccounting
   * .FiscalYear, * com.poesys.accounting.dataloader.IBuilder)} . Tests a bug with computing the two
   * income summary transaction items being added to the wrong accounts, resulting in more than a
   * penny's difference between the two capital accounts. This bug was a result of computing the net
   * income using the wrong sign, so the signs got reversed and the remainder was added to the
   * "larger" capital balance rather than the smaller one.
   */
  @Test
  public void testGetCapitalAdjustTwoEntitiesUnequalAmounts() {
    IParameters parameters = new UnitTestParametersCapitalPoesys1998Bug();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();

    logger.info("Starting income summary transfer");
    Transaction transaction = capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    logger.info("Done with income summary transfer");

    logger.info("Starting distribution transfer");
    List<Transaction> transactions = capStruct.getDistributionTransactions(year, builder);
    assertTrue("no distribution transaction list created", transactions != null);
    assertTrue("distribution transaction list is empty for structure with no distribution account",
               !transactions.isEmpty());
    logger.info("Done with distribution transfer: " + transactions);
    Transaction adjustingTransaction = capStruct.getCapitalAdjustmentTransaction(builder);
    assertTrue("did not get adjusting transaction", adjustingTransaction != null);
    logger.info("Done with adjusting transaction: " + adjustingTransaction);

    Statement stmt =
      new Statement(year, "Poesys Associates 1997 Balance Sheet", StatementType.BALANCE_SHEET);
    logger.info(
      "\nBalance Sheet Poesys 1998 Bug\n" + stmt.toData() + "\n--------------------------------\n");
    logger.info("\nBalance Sheet Detail Poesys 1998 Bug\n" + stmt.toDetailData() +
                "\n--------------------------------\n");
    assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);

    assertTrue("no capital entity", capStruct.getEntities() != null);
    assertTrue("wrong number of capital entities", capStruct.getEntities().size() == 2);
    CapitalEntity entity1 = capStruct.getEntities().get(0);
    CapitalEntity entity2 = capStruct.getEntities().get(1);
    Account distAccount1 = entity1.getDistributionAccount();
    assertTrue(
      "distribution account 1 balance is not zero: " + stmt.getAccountBalance(distAccount1),
      stmt.getAccountBalance(distAccount1).compareTo(
        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) == 0);
    Account distAccount2 = entity2.getDistributionAccount();
    assertTrue(
      "distribution account 2 balance is not zero: " + stmt.getAccountBalance(distAccount2),
      stmt.getAccountBalance(distAccount2).compareTo(
        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) == 0);

    Account capAccount1 = entity1.getCapitalAccount();
    BigDecimal cap1Balance = stmt.getAccountBalance(capAccount1);
    Account capAccount2 = entity2.getCapitalAccount();
    BigDecimal cap2Balance = stmt.getAccountBalance(capAccount2);
    // Test the bug in the Poesys 1998 balance sheet, 2-cent difference
    logger.info("capital balances: " + cap1Balance + " : " + cap2Balance);
    logger.info("difference: " + cap1Balance.subtract(cap2Balance).abs());
    assertTrue("capital accounts differ by more than a penny: " + cap1Balance + " : " + cap2Balance,
               cap1Balance.subtract(cap2Balance).abs().compareTo(
                 new BigDecimal(".01").setScale(2, RoundingMode.HALF_UP)) <= 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalStructure#getIncomeToCapitalTransaction(com.poesys.accounting.dataloader
   * .newaccounting.FiscalYear, * com.poesys.accounting.dataloader.IBuilder)} . Tests getting
   * transaction for two capital entities across two fiscal years.
   */
  @Test
  public void testGetIncomeToCapitalTransactionTwoEntitiesTwoYears() {
    IParameters parameters = new UnitTestParametersCapitalTwoEntitiesTwoYears();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
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
      Transaction transaction = capStruct.getIncomeToCapitalTransaction(year, builder);
      assertTrue("no capital accounts transaction created", transaction != null);
      Statement stmt = new Statement(year, "Balance Sheet", StatementType.BALANCE_SHEET);
      assertTrue("Balance sheet balance is not 0: " + stmt.getBalance(),
                 stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalStructure#getDistributionTransactions(com.poesys.accounting.dataloader.newaccounting
   * .FiscalYear, * com.poesys.accounting.dataloader.IBuilder)} . Tests getting transactions for two
   * capital entities across two fiscal years.
   */
  @Test
  public void testGetDistributionFromCapitalTransactionsTwoEntitiesTwoYears() {
    IParameters parameters = new UnitTestParametersCapitalTwoEntitiesTwoYears();

    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
    for (Integer year = parameters.getStartYear(); year <= parameters.getEndYear(); year++) {
      // Build the year.
      builder.buildFiscalYear(year);
      builder.buildAccountGroups();
      builder.buildAccountMap();
      builder.buildAccounts();
      builder.buildBalances();
      builder.buildTransactions();

      FiscalYear fiscalYear = builder.getFiscalYear();
      CapitalStructure capStruct = builder.getCapitalStructure();

      // Transfer income summary to capital account
      Transaction transaction = capStruct.getIncomeToCapitalTransaction(fiscalYear, builder);
      assertTrue("no capital accounts transaction created", transaction != null);

      // Transfer distribution account to capital account
      List<Transaction> transactions = capStruct.getDistributionTransactions(fiscalYear, builder);
      assertTrue("no distribution transaction list created", transactions != null);
      assertTrue(
        "distribution transaction list is empty for structure with distribution account and " +
        "transactions", !transactions.isEmpty());

      // Test results.
      logger.info("2 Entities 2 Years Balance Sheet test started, year " + fiscalYear.getYear());
      Statement stmt =
        new Statement(fiscalYear, "2 Entities 2 Years Balance Sheet", StatementType.BALANCE_SHEET);
      BigDecimal balance = stmt.getBalance();
      assertTrue("balance sheet balance is not 0: " + balance,
                 balance.compareTo(BigDecimal.ZERO) == 0);
      logger.info("2 Entities 2 Years Balance Sheet test done");
      assertTrue("no capital entity", capStruct.getEntities() != null);
      assertTrue("wrong number of capital entities", capStruct.getEntities().size() == 2);
      CapitalEntity entity1 = capStruct.getEntities().get(0);
      CapitalEntity entity2 = capStruct.getEntities().get(1);
      Account distAccount1 = entity1.getDistributionAccount();
      assertTrue(
        "distribution account 1 balance is not zero: " + stmt.getAccountBalance(distAccount1),
        stmt.getAccountBalance(distAccount1).compareTo(
          BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) == 0);
      Account distAccount2 = entity2.getDistributionAccount();
      assertTrue(
        "distribution account 2 balance is not zero: " + stmt.getAccountBalance(distAccount2),
        stmt.getAccountBalance(distAccount2).compareTo(
          BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) == 0);
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
    builder.buildCapitalStructure();
    builder.buildFiscalYear(YEAR1);
    CapitalStructure capStruct = builder.getCapitalStructure();
    assertTrue("could not create capital structure", capStruct != null);
    assertTrue("string representation is wrong: " + capStruct, capStruct.toString().equals(
      "CapitalStructure [incomeSummaryAccountName=Income Summary, entities=[CapitalEntity " +
      "[name=Partner 1, ownership=0.500, capitalAccount=null, distributionAccount=null], " +
      "CapitalEntity [name=Partner 2, ownership=0.500, capitalAccount=null, " +
      "distributionAccount=null]]]"));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getCapitalAdjustmentTransaction(com.poesys.accounting.dataloader.IBuilder)}
   * . Tests capital adjustment for single-entity, single-year accounts
   */
  @Test
  public void testGetCapitalAdjustSingleEntitySingleYear() {
    IParameters parameters = new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();
    Transaction transaction = capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    List<Transaction> transactions = capStruct.getDistributionTransactions(year, builder);
    assertTrue("no distribution transaction list created", transactions != null);
    assertTrue(
      "distribution transaction list is not empty for structure with no distribution account",
      transactions.isEmpty());
    Transaction adjust = capStruct.getCapitalAdjustmentTransaction(builder);
    assertTrue("adjusting transaction created but should not have been", adjust == null);
    Statement stmt =
      new Statement(year, "1 Entity 1 Year Balance Sheet", StatementType.BALANCE_SHEET);
    assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getCapitalAdjustmentTransaction(com.poesys.accounting.dataloader.IBuilder)}
   * . Tests getting transactions for two capital entities in a single fiscal year with a distribution.
   */
  @Test
  public void testGetCapitalAdjustTwoEntitiesSingleYear() {
    IParameters parameters = new UnitTestParametersCapitalTwoEntitiesOneYear();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();

    Transaction transaction = capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    List<Transaction> transactions = capStruct.getDistributionTransactions(year, builder);
    assertTrue("no distribution transaction list created", transactions != null);
    assertTrue("distribution transaction list is empty for structure with distribution account",
               !transactions.isEmpty());
    Transaction adjust = capStruct.getCapitalAdjustmentTransaction(builder);
    // should have adjustment because distribution moved into capital account
    assertTrue("no adjusting transaction created", adjust != null);
    Statement stmt =
      new Statement(year, "2 Entities 1 Year Balance Sheet", StatementType.BALANCE_SHEET);
    assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
    Account distAccount1 = builder.getAccountByName(DIST_ACCOUNT_1_NAME);
    assertTrue(
      "distribution account 1 balance is not zero: " + stmt.getAccountBalance(distAccount1),
      stmt.getAccountBalance(distAccount1).compareTo(
        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) == 0);
    Account distAccount2 = builder.getAccountByName(DIST_ACCOUNT_2_NAME);
    assertTrue(
      "distribution account 2 balance is not zero: " + stmt.getAccountBalance(distAccount2),
      stmt.getAccountBalance(distAccount2).compareTo(
        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalStructure#getCapitalAdjustmentTransaction(com.poesys.accounting.dataloader.IBuilder)}
   * . Tests getting transactions for two capital entities in a single fiscal year but with no
   * distributions to process (zero amount distribution transfer).
   */
  @Test
  public void testGetCapitalAdjustTwoEntitiesSingleYearNoDistributions() {
    IParameters parameters = new UnitTestParametersCapitalTwoEntitiesOneYearNoDistributionAmount();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildCapitalStructure();
    builder.buildFiscalYear(YEAR1);
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();

    FiscalYear year = builder.getFiscalYear();
    CapitalStructure capStruct = builder.getCapitalStructure();

    Transaction transaction = capStruct.getIncomeToCapitalTransaction(year, builder);
    assertTrue("no capital accounts transaction created", transaction != null);
    List<Transaction> transactions = capStruct.getDistributionTransactions(year, builder);
    assertTrue("no distribution transaction list created", transactions != null);
    assertTrue("distribution transaction list has transaction, but no distributions made",
               transactions.isEmpty());
    Transaction adjust = capStruct.getCapitalAdjustmentTransaction(builder);
    assertTrue("adjusting transaction created but should not have been", adjust == null);
    Statement stmt =
      new Statement(year, "2 Entities 1 Year Balance Sheet", StatementType.BALANCE_SHEET);
    assertTrue("balance sheet balance is not 0: " + stmt.getBalance(),
               stmt.getBalance().compareTo(BigDecimal.ZERO) == 0);
    Account distAccount1 = builder.getAccountByName(DIST_ACCOUNT_1_NAME);
    assertTrue(
      "distribution account 1 balance is not zero: " + stmt.getAccountBalance(distAccount1),
      stmt.getAccountBalance(distAccount1).compareTo(
        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) == 0);
    Account distAccount2 = builder.getAccountByName(DIST_ACCOUNT_2_NAME);
    assertTrue(
      "distribution account 2 balance is not zero: " + stmt.getAccountBalance(distAccount2),
      stmt.getAccountBalance(distAccount2).compareTo(
        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) == 0);
  }
}
