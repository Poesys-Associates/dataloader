/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import com.poesys.accounting.dataloader.IBuilder;
import com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder;
import com.poesys.accounting.dataloader.properties.IParameters;
import com.poesys.accounting.dataloader.properties.UnitTestParametersCapitalOneEntityOneYearNoDistribution;
import com.poesys.accounting.dataloader.properties.UnitTestParametersCapitalOneEntityTwoYearsNoDistribution;


/**
 * CUT: CapitalEntity
 * 
 * @author Robert J. Muller
 */
public class CapitalEntityTest {

  private static final String CAPITAL_ENTITY_NAME = "Accounting System";
  private static final String CAPITAL_ACCOUNT_NAME = "Personal Capital";
  private static final String DESCRIPTION = "Equity Account";
  private static final Account CAPITAL_ACCOUNT_SINGLE =
    new Account(CAPITAL_ACCOUNT_NAME,
                DESCRIPTION,
                AccountType.EQUITY,
                false,
                false);
  private static final String CAPITAL_ACCOUNT_1_NAME = "Partner 1 Capital";
  private static final Account CAPITAL_ACCOUNT_PARTNER_1 =
    new Account(CAPITAL_ACCOUNT_1_NAME,
                DESCRIPTION,
                AccountType.EQUITY,
                false,
                false);
  private static final String DIST_ACCOUNT_1_NAME = "Partner 1 Distributions";
  private static final Account DIST_ACCOUNT_1 =
    new Account(DIST_ACCOUNT_1_NAME,
                DESCRIPTION,
                AccountType.EQUITY,
                false,
                false);
  private static final BigDecimal OWNERSHIP_1 = BigDecimal.ONE;
  private static final BigDecimal OWNERSHIP_2 = new BigDecimal("0.50");
  private static final BigDecimal CAPITAL_BALANCE_AMOUNT =
    new BigDecimal("877.00").setScale(CapitalEntity.SCALE);

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#CapitalEntity(java.lang.String, java.math.BigDecimal)}
   * . Tests the main constructor and the getters.
   */
  @Test
  public void testCapitalEntityIntegerOwnership() {
    CapitalEntity entity = new CapitalEntity(CAPITAL_ENTITY_NAME, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    // no distribution account
    assertTrue("could not create capital entity", entity != null);
    assertTrue("wrong capital account name " + entity.getCapitalAccount(),
               CAPITAL_ACCOUNT_SINGLE.equals(entity.getCapitalAccount()));
    assertTrue("wrong ownership percentage " + entity.getOwnership(),
               OWNERSHIP_1.compareTo(entity.getOwnership()) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#CapitalEntity(java.lang.String, java.math.BigDecimal)}
   * . Tests the main constructor with a partial (less than one) initial
   * ownership value.
   */
  @Test
  public void testCapitalEntityPartialOwnership() {
    CapitalEntity entity = new CapitalEntity(CAPITAL_ENTITY_NAME, OWNERSHIP_2);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_PARTNER_1);
    entity.setDistributionAccount(DIST_ACCOUNT_1);
    assertTrue("wrong ownership percentage " + entity.getOwnership(),
               OWNERSHIP_2.compareTo(entity.getOwnership()) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#setOwnership(java.math.BigDecimal)}
   * .
   */
  @Test
  public void testSetOwnership() {
    CapitalEntity entity = new CapitalEntity(CAPITAL_ENTITY_NAME, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    entity.setOwnership(OWNERSHIP_2);
    assertTrue("wrong ownership percentage " + entity.getOwnership(),
               OWNERSHIP_2.compareTo(entity.getOwnership()) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#getCapitalBalance(com.poesys.accounting.dataloader.IBuilder)}
   * .
   */
  @Test
  public void testGetCapitalBalanceSingleYearOneEntity() {
    CapitalEntity entity = new CapitalEntity(CAPITAL_ENTITY_NAME, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    // no distribution account
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();
    BigDecimal balance = entity.getCapitalBalance(builder);
    assertTrue("no balance for capital account " + CAPITAL_ACCOUNT_NAME,
               balance != null);
    assertTrue("wrong balance for capital account: " + balance,
               balance.compareTo(CAPITAL_BALANCE_AMOUNT) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#getDistributionBalance(com.poesys.accounting.dataloader.IBuilder)}
   * .
   */
  @Test
  public void testGetDistributionBalanceSingleYearNoAccount() {
    CapitalEntity entity = new CapitalEntity(CAPITAL_ENTITY_NAME, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    // no distribution account
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();
    BigDecimal balance = entity.getDistributionBalance(builder);
    assertTrue("balance for distribution account but that account does not exist",
               balance == null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#getCapitalBalance(com.poesys.accounting.dataloader.IBuilder)}
   * .
   */
  @Test
  public void testGetCapitalBalanceTwoYearsOneEntity() {
    CapitalEntity entity = new CapitalEntity(CAPITAL_ENTITY_NAME, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    // no distribution account
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityTwoYearsNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();
    BigDecimal balance = entity.getCapitalBalance(builder);
    assertTrue("no balance for capital account " + CAPITAL_ACCOUNT_NAME,
               balance != null);
    assertTrue("wrong balance for capital account: " + balance,
               balance.compareTo(CAPITAL_BALANCE_AMOUNT) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#getDistributionBalance(com.poesys.accounting.dataloader.IBuilder)}
   * .
   */
  @Test
  public void testGetDistributionBalanceTwoYearsNoAccount() {
    CapitalEntity entity = new CapitalEntity(CAPITAL_ENTITY_NAME, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    // no distribution account
    IParameters parameters =
      new UnitTestParametersCapitalOneEntityTwoYearsNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    assertTrue("no builder created", builder != null);
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();
    BigDecimal balance = entity.getDistributionBalance(builder);
    assertTrue("balance for distribution account but that account does not exist",
               balance == null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#toString()}
   * .
   */
  @Test
  public void testToString() {
    CapitalEntity entity = new CapitalEntity(CAPITAL_ENTITY_NAME, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    // no distribution account for individual
    assertTrue("string representation not correct: " + entity,
               entity.toString().equals("CapitalEntity [name=Accounting System, ownership=1.000, capitalAccount=Account [name=Personal Capital, description=Equity Account, debitDefault=false, receivable=false, years=[]], distributionAccount=null]"));
  }
}
