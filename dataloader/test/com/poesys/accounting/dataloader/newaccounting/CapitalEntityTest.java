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

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.poesys.accounting.dataloader.properties.UnitTestParametersCapitalTwoEntitiesTwoYears;
import com.poesys.db.InvalidParametersException;
import org.junit.Test;

import com.poesys.accounting.dataloader.IBuilder;
import com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder;
import com.poesys.accounting.dataloader.properties.IParameters;
import com.poesys.accounting.dataloader.properties
  .UnitTestParametersCapitalOneEntityOneYearNoDistribution;
import com.poesys.accounting.dataloader.properties
  .UnitTestParametersCapitalOneEntityTwoYearsNoDistribution;

/**
 * CUT: CapitalEntity
 *
 * @author Robert J. Muller
 */
public class CapitalEntityTest {

  private static final String CAPITAL_ENTITY_NAME = " Accounting System";
  private static final String CAPITAL_ACCOUNT_NAME = "Personal Capital";
  private static final String DESCRIPTION = "Equity Account";
  private static final Account CAPITAL_ACCOUNT_SINGLE =
    new Account(CAPITAL_ACCOUNT_NAME, DESCRIPTION, AccountType.EQUITY, false, false);
  private static final String CAPITAL_ACCOUNT_1_NAME = "Partner 1 Capital";
  private static final Account CAPITAL_ACCOUNT_PARTNER_1 =
    new Account(CAPITAL_ACCOUNT_1_NAME, DESCRIPTION, AccountType.EQUITY, false, false);
  private static final String DIST_ACCOUNT_1_NAME = "Partner 1 Distributions";
  private static final Account DIST_ACCOUNT_PARTNER_1 =
    new Account(DIST_ACCOUNT_1_NAME, DESCRIPTION, AccountType.EQUITY, false, false);
  private static final BigDecimal OWNERSHIP_1 = BigDecimal.ONE;
  private static final BigDecimal OWNERSHIP_2 = new BigDecimal("0.50");
  private static final BigDecimal CAPITAL_BALANCE_AMOUNT =
    new BigDecimal("877.00").setScale(CapitalEntity.SCALE, RoundingMode.HALF_UP);
  private static final String TEST_NAME = "test name";

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalEntity#CapitalEntity(java.lang.String, java.math.BigDecimal)} . Tests the main
   * constructor with null entity name exception.
   */
  @Test
  public void testCapitalEntityNullName() {
    try {
      new CapitalEntity(null, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
      fail("no exception from null entity name");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("wrong exception from null entity name: " + e.getMessage());
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalEntity#CapitalEntity(java.lang.String, java.math.BigDecimal)} . Tests the main
   * constructor with null capital account name exception.
   */
  @Test
  public void testCapitalEntityNullCapitalAccountName() {
    try {
      new CapitalEntity(CAPITAL_ENTITY_NAME, null, null, OWNERSHIP_1);
      fail("no exception from null capital account name");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("wrong exception from null capital account name: " + e.getMessage());
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .CapitalEntity#CapitalEntity(java.lang.String, * java.math.BigDecimal)} . Tests the main
   * constructor and the getters for capital account and name.
   */
  @Test
  public void testCapitalEntityIntegerOwnership() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    assertTrue("wrong capital entity name " + entity.getName(),
               CAPITAL_ENTITY_NAME.equals(entity.getName()));
    assertTrue("wrong capital account name " + entity.getCapitalAccountName(),
               CAPITAL_ACCOUNT_NAME.equals(entity.getCapitalAccountName()));
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    // no distribution account
    assertTrue("wrong capital account " + entity.getCapitalAccount(),
               CAPITAL_ACCOUNT_SINGLE.equals(entity.getCapitalAccount()));
    assertTrue("wrong capital account name after account set " + entity.getCapitalAccountName(),
               CAPITAL_ACCOUNT_SINGLE.getName().equals(entity.getCapitalAccountName()));
    assertTrue("wrong ownership percentage " + entity.getOwnership(),
               OWNERSHIP_1.compareTo(entity.getOwnership()) == 0);
  }

  /**
   * Test method for {@link com.poesys .accounting.dataloader.newaccounting
   * .CapitalEntity#CapitalEntity(java.lang.String, * java .math.BigDecimal)} . Tests the main
   * constructor with a partial (less than one) initial ownership value. Tests setting the
   * distribution account. Tests getters for distribution account and name.
   */
  @Test
  public void testCapitalEntityPartialOwnership() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_1_NAME, DIST_ACCOUNT_1_NAME,
                        OWNERSHIP_2);
    assertTrue("wrong capital entity name " + entity.getName(),
               CAPITAL_ENTITY_NAME.equals(entity.getName()));
    assertTrue("wrong capital account name " + entity.getCapitalAccountName(),
               CAPITAL_ACCOUNT_1_NAME.equals(entity.getCapitalAccountName()));
    assertTrue("wrong distribution account name " + entity.getDistributionAccountName(),
               DIST_ACCOUNT_1_NAME.equals(entity.getDistributionAccountName()));
    entity.setCapitalAccount(CAPITAL_ACCOUNT_PARTNER_1);
    assertTrue("wrong capital account " + entity.getCapitalAccount(),
               CAPITAL_ACCOUNT_PARTNER_1.equals(entity.getCapitalAccount()));
    assertTrue("wrong capital account name after account set " + entity.getCapitalAccountName(),
               CAPITAL_ACCOUNT_PARTNER_1.getName().equals(entity.getCapitalAccountName()));
    entity.setDistributionAccount(DIST_ACCOUNT_PARTNER_1);
    assertTrue("wrong distribution account " + entity.getCapitalAccount(),
               DIST_ACCOUNT_PARTNER_1.equals(entity.getDistributionAccount()));
    assertTrue(
      "wrong distribution account name after account set " + entity.getDistributionAccountName(),
      DIST_ACCOUNT_PARTNER_1.getName().equals(entity.getDistributionAccountName()));
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
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    entity.setOwnership(OWNERSHIP_2);
    assertTrue("wrong ownership percentage " + entity.getOwnership(),
               OWNERSHIP_2.compareTo(entity.getOwnership()) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#setName(String)}
   * .
   */
  @Test
  public void testSetName() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    entity.setName(TEST_NAME);
    assertTrue("wrong name set: " + entity.getName(), TEST_NAME.equals(entity.getName()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#setName(String)}
   * . Tests null name input.
   */
  @Test
  public void testSetNameNull() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    try {
      entity.setName(null);
      fail("No exception from setName() with null input");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception from setName() with null input: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#setCapitalAccount(Account)}
   * . Tests exception when input is null.
   */
  @Test
  public void testSetCapAccountNull() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    try {
      entity.setCapitalAccount(null);
      fail("No exception from setCapitalAccount() with capital account null");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception from setCapitalAccount() with null input: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#setDistributionAccount(Account)}
   * . Tests exception when input is null.
   */
  @Test
  public void testSetDistAccountNull() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    try {
      entity.setDistributionAccount(null);
      fail("No exception from setDistributionAccount() with capital account null");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception from setDistributionAccount() with null input: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#setCapitalAccountName(String)}
   * .
   */
  @Test
  public void testSetCapName() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    entity.setCapitalAccountName(CAPITAL_ACCOUNT_1_NAME);
    assertTrue("wrong capital account name set: " + entity.getCapitalAccountName(),
               CAPITAL_ACCOUNT_1_NAME.equals(entity.getCapitalAccountName()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#setCapitalAccountName(String)}
   * . Tests exception when capital account set.
   */
  @Test
  public void testSetCapNameCapAccount() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    try {
      entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
      entity.setCapitalAccountName(CAPITAL_ACCOUNT_1_NAME);
      fail("No exception from setCapitalAccountName() with capital account set");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception from setCapitalAccountName() with null input: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#setDistributionAccountName(String)}
   * .
   */
  @Test
  public void testSetDistName() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    entity.setDistributionAccountName(DIST_ACCOUNT_1_NAME);
    assertTrue("wrong distribution account name set: " + entity.getDistributionAccountName(),
               DIST_ACCOUNT_1_NAME.equals(entity.getDistributionAccountName()));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#setDistributionAccountName(String)}
   * . Tests exception when distribution account set.
   */
  @Test
  public void testSetCapNameDistAccount() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    try {
      entity.setDistributionAccount(DIST_ACCOUNT_PARTNER_1);
      entity.setDistributionAccountName(DIST_ACCOUNT_1_NAME);
      fail("No exception from setDistributionAccountName() with distribution account set");
    } catch (InvalidParametersException e) {
      // success
    } catch (Exception e) {
      fail("Wrong exception from setDistributionAccountName() with null input: " + e.getMessage());
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#getCapitalBalance(com.poesys.accounting.dataloader.IBuilder)}
   * .
   */
  @Test
  public void testGetCapitalBalanceSingleYearOneEntity() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    // no distribution account
    IParameters parameters = new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();
    BigDecimal balance = entity.getCapitalBalance(builder);
    assertTrue("no balance for capital account " + CAPITAL_ACCOUNT_NAME, balance != null);
    assertTrue("wrong balance for capital account: " + balance,
               balance.compareTo(CAPITAL_BALANCE_AMOUNT) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#getDistributionBalance(com.poesys.accounting.dataloader.IBuilder)}
   * . Tests base case.
   */
  @Test
  public void testGetDistributionBalance() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_PARTNER_1);
    entity.setDistributionAccount(DIST_ACCOUNT_PARTNER_1);
    IParameters parameters = new UnitTestParametersCapitalTwoEntitiesTwoYears();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();
    BigDecimal balance = entity.getDistributionBalance(builder);
    BigDecimal testBalance = BigDecimal.ZERO;
    assertTrue("wrong balance for distribution account: " + balance, balance.equals(testBalance));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#getDistributionBalance(com.poesys.accounting.dataloader.IBuilder)}
   * . Tests null distribution account.
   */
  @Test
  public void testGetDistributionBalanceSingleYearNoAccount() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    // no distribution account
    IParameters parameters = new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();
    BigDecimal balance = entity.getDistributionBalance(builder);
    assertTrue("balance for distribution account but that account does not exist", balance == null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#getDistributionBalance(com.poesys.accounting.dataloader.IBuilder)}
   * . Tests for exception when capital account isn't set.
   */
  @Test
  public void testGetCapitalBalanceNoAccountSet() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    IParameters parameters = new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();
    try {
      BigDecimal balance = entity.getCapitalBalance(builder);
      fail("no exception for getCapitalBalance when capital account not set");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#getDistributionBalance(com.poesys.accounting.dataloader.IBuilder)}
   * . Tests for exception when builder input is null.
   */
  @Test
  public void testGetCapitalBalanceNoBuilder() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_PARTNER_1);
    IParameters parameters = new UnitTestParametersCapitalOneEntityOneYearNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();
    try {
      BigDecimal balance = entity.getCapitalBalance(null);
      fail("no exception for getCapitalBalance when builder is null");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#getCapitalBalance(com.poesys.accounting.dataloader.IBuilder)}
   * .
   */
  @Test
  public void testGetCapitalBalanceTwoYearsOneEntity() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    // no distribution account
    IParameters parameters = new UnitTestParametersCapitalOneEntityTwoYearsNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();
    BigDecimal balance = entity.getCapitalBalance(builder);
    assertTrue("no balance for capital account " + CAPITAL_ACCOUNT_NAME, balance != null);
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
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    // no distribution account
    IParameters parameters = new UnitTestParametersCapitalOneEntityTwoYearsNoDistribution();
    IBuilder builder = new OldDataBuilder(parameters);
    builder.buildFiscalYear(parameters.getStartYear());
    builder.buildAccountGroups();
    builder.buildAccountMap();
    builder.buildAccounts();
    builder.buildBalances();
    builder.buildTransactions();
    BigDecimal balance = entity.getDistributionBalance(builder);
    assertTrue("balance for distribution account but that account does not exist", balance == null);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.CapitalEntity#toString()}
   * .
   */
  @Test
  public void testToString() {
    CapitalEntity entity =
      new CapitalEntity(CAPITAL_ENTITY_NAME, CAPITAL_ACCOUNT_NAME, null, OWNERSHIP_1);
    entity.setCapitalAccount(CAPITAL_ACCOUNT_SINGLE);
    // no distribution account for individual
    assertTrue("string representation not correct: " + entity, entity.toString().equals(
      "CapitalEntity [name= Accounting System, ownership=1.000, capitalAccount=Account " +
      "[name=Personal Capital, description=Equity Account, debitDefault=false, " +
      "receivable=false, years=[]], distributionAccount=null]"));
  }
}
