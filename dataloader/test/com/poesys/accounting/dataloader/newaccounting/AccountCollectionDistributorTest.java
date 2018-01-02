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
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

import com.poesys.db.InvalidParametersException;

/**
 * CUT: AccountCollectionDistributor
 *
 * @author Robert J. Muller
 */
public class AccountCollectionDistributorTest {
  /** one cent in decimal form */
  private static final BigDecimal PENNY = new BigDecimal(".01");
  /** negative one cent in decimal form */
  private static final BigDecimal NEG_PENNY = new BigDecimal("-.01");

  /** BigDecimal scale for arithmetic and comparisons */
  private static final int SCALE = 2;

  /** multiplier for converting money to integer value */
  private static final BigDecimal CONVERTER = new BigDecimal("100");

  // amounts divisible by 2 ("even", remainder 0)
  private static final BigDecimal AMOUNT = new BigDecimal("23455.20");
  private static final BigDecimal NEG_AMOUNT = new BigDecimal("-23456.20");
  // integer (x100) versions of the above decimals
  // private static final Integer INT_AMOUNT = 2345520;
  // private static final Integer INT_NEG_AMOUNT = -2345620;

  // nearly equal amount to use in combination with above amounts
  private static final BigDecimal NEAR_AMOUNT = new BigDecimal("23455.19");
  private static final BigDecimal NEAR_NEG_AMOUNT = new BigDecimal("-23456.19");
  // integer (x100) versions of the above decimals
  // private static final Integer INT_NEAR_AMOUNT = 2345519;
  // private static final Integer INT_NEAR_NEG_AMOUNT = -2345619;

  // unequal amount to use in combination with above amounts
  private static final BigDecimal UNEQUAL_AMOUNT = new BigDecimal("23455.18");
  private static final BigDecimal UNEQUAL_NEG_AMOUNT = new BigDecimal("-23456.18");

  // amounts not divisible by 2 ("odd", remainder 1)
  private static final BigDecimal ODD_AMOUNT = new BigDecimal("23456.25");
  private static final BigDecimal NEG_ODD_AMOUNT = new BigDecimal("-23456.25");
  // integer (x100) versions of the above decimals
  // private static final Integer INT_ODD_AMOUNT = 2345625;
  // private static final Integer INT_NEG_ODD_AMOUNT = -2345625;

  // amounts divisible by 3 (remainder 0)
  private static final BigDecimal DIV_AMOUNT = new BigDecimal("23455.98");
  private static final BigDecimal NEG_DIV_AMOUNT = new BigDecimal("-23455.98");
  // integer (x100) versions of the above decimals
  private static final Integer INT_DIV_AMOUNT = 2345598;
  private static final Integer INT_NEG_DIV_AMOUNT = -2345598;

  // amounts indivisible by 3 (remainder 1)
  private static final BigDecimal INDIV_AMOUNT_R1 = new BigDecimal("23455.99");
  // integer (x100) versions of the above decimals
  private static final Integer INT_INDIV_AMOUNT_R1 = 2345599;

  // amounts not-divisible by 3 (remainder 2)
  private static final BigDecimal INDIV_AMOUNT_R2 = new BigDecimal("23456.00");
  private static final BigDecimal NEG_INDIV_AMOUNT_R2 = new BigDecimal("-23456.00");
  // integer (x100) versions of the above decimals
  private static final Integer INT_INDIV_AMOUNT_R2 = 2345600;
  private static final Integer INT_NEG_INDIV_AMOUNT_R2 = -2345600;

  // account constants
  private static final String DESCRIPTION = "description";
  private static final String INCOME_ACCOUNT_NAME = "Salary";
  private static final String EQUITY_ACCOUNT_NAME_1 = "Shared Capital 1";
  private static final String EQUITY_ACCOUNT_NAME_2 = "Shared Capital 2";
  private static final String EQUITY_ACCOUNT_NAME_3 = "Shared Capital 3";
  private static final Boolean DEBIT_DEFAULT = Boolean.TRUE;
  private static final Boolean CREDIT_DEFAULT = Boolean.FALSE;
  private static final Boolean NOT_RECEIVABLE = Boolean.FALSE;

  // Setup accounts for each main account type.
  private final Account incomeAccount =
    new Account(INCOME_ACCOUNT_NAME, DESCRIPTION, AccountType.INCOME, CREDIT_DEFAULT,
                NOT_RECEIVABLE);

  private final Account equityAccount1 =
    new Account(EQUITY_ACCOUNT_NAME_1, DESCRIPTION, AccountType.EQUITY, !DEBIT_DEFAULT,
                NOT_RECEIVABLE);
  private final Account equityAccount2 =
    new Account(EQUITY_ACCOUNT_NAME_2, DESCRIPTION, AccountType.EQUITY, !DEBIT_DEFAULT,
                NOT_RECEIVABLE);
  private final Account equityAccount3 =
    new Account(EQUITY_ACCOUNT_NAME_3, DESCRIPTION, AccountType.EQUITY, !DEBIT_DEFAULT,
                NOT_RECEIVABLE);

  // messages for comparison
  private static final String INVALID_COLLECTION_ERROR =
    "invalid balances, check whether balances were added to distributor and that balance amounts " +
    "are equal or at most one penny different";

  private static final String NULL_ACCOUNT_ERROR = "account is required but is null";
  private static final String NULL_BALANCE_ERROR = "balance is required but is null";
  private static final String NO_BALANCE_ERROR = "no balance for account ";
  private static final String ACCOUNT_NOT_ADDED_ERROR = "account not added: ";

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#AccountCollectionDistributor(java.math.BigDecimal)}
   * . Tests constructor and getAmount() getter.
   */
  @Test
  public void testAccountCollectionDistributor() {
    Integer intAmount = AMOUNT.multiply(CONVERTER).intValue();
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    assertTrue("wrong amount set: " + distributor.getAmount(),
               distributor.getAmount().compareTo(intAmount) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .AccountCollectionDistributor#addBalance(com.poesys.accounting.dataloader.newaccounting
   * .Account, * java.math.BigDecimal)} . Tests addBalance(), getBalance(), and getItemAmount().
   */
  @Test
  public void testAddBalance() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    BigDecimal amount = distributor.getBalance(equityAccount1);
    assertTrue("wrong balance: " + amount, AMOUNT.compareTo(amount) == 0);
    BigDecimal itemAmount = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount: " + itemAmount, BigDecimal.ZERO.compareTo(itemAmount) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .AccountCollectionDistributor#addBalance(com.poesys.accounting.dataloader.newaccounting
   * .Account, * java.math.BigDecimal)} . Tests addBalance() with negative amount.
   */
  @Test
  public void testAddBalanceNegativeAmount() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(NEG_AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    BigDecimal amount = distributor.getBalance(equityAccount1);
    assertTrue("wrong balance: " + amount, NEG_AMOUNT.compareTo(amount) == 0);
    BigDecimal itemAmount = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount: " + itemAmount, BigDecimal.ZERO.compareTo(itemAmount) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .AccountCollectionDistributor#addBalance(com.poesys.accounting.dataloader.newaccounting
   * .Account, * java.math.BigDecimal)} . Tests addBalance() with zero amount.
   */
  @Test
  public void testAddBalanceZeroAmount() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(BigDecimal.ZERO);
    distributor.addBalance(equityAccount1, BigDecimal.ZERO);
    BigDecimal amount = distributor.getBalance(equityAccount1);
    assertTrue("wrong amount: " + amount, BigDecimal.ZERO.compareTo(amount) == 0);
    BigDecimal itemAmount = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount: " + itemAmount, BigDecimal.ZERO.compareTo(itemAmount) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#getBalance(com.poesys.accounting.dataloader.newaccounting.Account)}
   * . Tests getBalance() with wrong account supplied.
   */
  @Test
  public void testGetBalanceWrongAccount() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    try {
      distributor.getBalance(incomeAccount);
    } catch (InvalidParametersException e) {
      assertTrue("wrong error message: " + e.getMessage(),
                 e.getMessage().contains(ACCOUNT_NOT_ADDED_ERROR));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#getItemAmount(com.poesys.accounting.dataloader.newaccounting.Account)}
   * . Tests getItemAmount() with wrong account supplied.
   */
  @Test
  public void testGetItemAmountWrongAccount() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    try {
      distributor.getItemAmount(incomeAccount);
    } catch (InvalidParametersException e) {
      assertTrue("wrong error message: " + e.getMessage(),
                 e.getMessage().contains(ACCOUNT_NOT_ADDED_ERROR));
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .AccountCollectionDistributor#addBalance(com.poesys.accounting.dataloader.newaccounting
   * .Account, * java.math.BigDecimal)} . Tests for addBalance given a null account throwing an
   * exception.
   */
  @Test
  public void testAddBalanceNullAccount() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    try {
      distributor.addBalance(null, AMOUNT);
      fail("no exception for null account");
    } catch (InvalidParametersException e) {
      assertTrue("wrong error message: " + e.getMessage(),
                 e.getMessage().contains(NULL_ACCOUNT_ERROR));
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .AccountCollectionDistributor#addBalance(com.poesys.accounting.dataloader.newaccounting
   * .Account, * java.math.BigDecimal)} . Tests addBalance() given a null balance throwing an
   * exception.
   */
  @Test
  public void testAddBalanceNullBalance() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    try {
      distributor.addBalance(equityAccount1, null);
      fail("no exception for null balance");
    } catch (InvalidParametersException e) {
      assertTrue("wrong error message: " + e.getMessage(),
                 e.getMessage().contains(NULL_BALANCE_ERROR));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#isValid()}
   * . Tests a single balance, the existence test.
   */
  @Test
  public void testIsValidBalanceExists() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    assertTrue("valid distributor tests as invalid", distributor.isValid());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#isValid()}
   * . Tests whether two balances that are equal is valid.
   */
  @Test
  public void testIsValidBalancesEqual() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    assertTrue("valid distributor tests as invalid (equal balances)", distributor.isValid());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#isValid()}
   * . Tests whether two balances that are nearly equal (differ by at most a penny) is valid.
   */
  @Test
  public void testIsValidBalancesNearlyEqual() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT.add(
      new BigDecimal("0.01").setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("valid distributor tests as invalid (nearly equal balances)", distributor.isValid());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#isValid()}
   * . Tests whether three balances that are nearly equal (differ by at most a penny) is valid. Uses
   * -1, 0, +1 differences in the test.
   */
  @Test
  public void testIsValid3BalancesNearlyEqual() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT.add(
      new BigDecimal("0.01").setScale(SCALE, RoundingMode.HALF_UP)));
    distributor.addBalance(equityAccount2, AMOUNT.add(
      new BigDecimal("-0.01").setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("valid distributor tests as invalid (nearly equal balances)", distributor.isValid());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#isValid()}
   * . Tests whether two balances that are nearly equal (differ by at most a penny) is valid. Uses
   * -5, 0, +5 differences in the test.
   */
  @Test
  public void testIsValid2BalancesUnequal() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT.add(
      new BigDecimal("0.05").setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("invalid distributor tests as valid (unequal balances)", !distributor.isValid());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#isValid()}
   * . Tests whether three balances that are unequal (differ by more than a penny) is invalid.
   */
  @Test
  public void testIsValid3BalancesUnequal() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT.add(
      new BigDecimal("0.05").setScale(SCALE, RoundingMode.HALF_UP)));
    distributor.addBalance(equityAccount3, AMOUNT.add(
      new BigDecimal("-0.05").setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("valid distributor tests as invalid (nearly equal balances)",
               !distributor.isValid());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#isValid()}
   * . Tests whether no-balance status is invalid.
   */
  @Test
  public void testIsValidNoBalances() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    assertTrue("invalid distributor tests as valid (no balances)", !distributor.isValid());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution validity checking.
   */
  @Test
  public void testDistributeAmountNoAccount() {
    try {
      AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
      distributor.distributeAmount();
      fail("no exception thrown for invalid collection on distribute()");
    } catch (RuntimeException e) {
      assertTrue("wrong message: " + e.getMessage(),
                 e.getMessage().contains(INVALID_COLLECTION_ERROR));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution across one account for a positive amount and balance.
   */
  @Test
  public void testDistributeAmount1AccountPosPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance = distributor.getBalance(equityAccount1);
    assertTrue("wrong balance " + newBalance, newBalance.compareTo(AMOUNT.add(AMOUNT)) == 0);
    BigDecimal newAmount = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount " + newAmount, newAmount.compareTo(AMOUNT) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution across one account for a positive amount and negative balance.
   */
  @Test
  public void testDistributeAmount1AccountPosNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance = distributor.getBalance(equityAccount1);
    assertTrue("wrong balance " + newBalance, newBalance.compareTo(AMOUNT.add(NEG_AMOUNT)) == 0);
    BigDecimal newAmount = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount " + newAmount, newAmount.compareTo(AMOUNT) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution across one account for a positive amount and balance.
   */
  @Test
  public void testDistributeAmount1AccountNegPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(NEG_AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance = distributor.getBalance(equityAccount1);
    assertTrue("wrong balance " + newBalance, newBalance.compareTo(NEG_AMOUNT.add(AMOUNT)) == 0);
    BigDecimal newAmount = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount " + newAmount, newAmount.compareTo(NEG_AMOUNT) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution across one account for a negative amount and negative balance.
   */
  @Test
  public void testDistributeAmount1AccountNegNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(NEG_AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance = distributor.getBalance(equityAccount1);
    assertTrue("wrong balance " + newBalance,
               newBalance.compareTo(NEG_AMOUNT.add(NEG_AMOUNT)) == 0);
    BigDecimal newAmount = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount " + newAmount, newAmount.compareTo(NEG_AMOUNT) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of positive even amount across two positive-balance accounts.
   */
  @Test
  public void testDistributeAmount2EqualAccountsEvenAmountPosPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    BigDecimal amountToAdd = AMOUNT.divide(new BigDecimal("2"), SCALE, RoundingMode.HALF_UP);

    BigDecimal testBalance = AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of positive even amount across two negative-balance accounts.
   */
  @Test
  public void testDistributeAmount2EqualAccountsEvenAmountPosNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    BigDecimal amountToAdd = AMOUNT.divide(new BigDecimal("2"), SCALE, RoundingMode.HALF_UP);
    BigDecimal testBalance = NEG_AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of negative even amount across two positive-balance accounts.
   */
  @Test
  public void testDistributeAmount2EqualAccountsEvenAmountNegPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(NEG_AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    BigDecimal amountToAdd = NEG_AMOUNT.divide(new BigDecimal("2"), SCALE, RoundingMode.HALF_UP);
    BigDecimal testBalance = AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of negative even amount across two negative-balance accounts.
   */
  @Test
  public void testDistributeAmount2EqualAccountsEvenAmountNegNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(NEG_AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    BigDecimal amountToAdd = NEG_AMOUNT.divide(new BigDecimal("2"), SCALE, RoundingMode.HALF_UP);
    BigDecimal testBalance = NEG_AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of positive odd amount across two positive-balance accounts.
   */
  @Test
  public void testDistributeAmount2EqualAccountsOddAmountPosPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(ODD_AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    Integer intAmount = ODD_AMOUNT.multiply(CONVERTER).intValue();
    intAmount = intAmount / 2;
    BigDecimal amountToAdd =
      new BigDecimal(intAmount).divide(CONVERTER, SCALE, RoundingMode.HALF_UP);
    BigDecimal testBalance = AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of positive odd amount across two negative-balance accounts.
   */
  @Test
  public void testDistributeAmount2EqualAccountsOddAmountPosNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(ODD_AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    Integer intAmount = ODD_AMOUNT.multiply(CONVERTER).intValue();
    intAmount = intAmount / 2;
    BigDecimal amountToAdd =
      new BigDecimal(intAmount).divide(CONVERTER, SCALE, RoundingMode.HALF_UP);
    BigDecimal testBalance = NEG_AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of negative odd amount across two positive-balance accounts.
   */
  @Test
  public void testDistributeAmount2EqualAccountsOddAmountNegPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(NEG_ODD_AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    Integer intAmount = NEG_ODD_AMOUNT.multiply(CONVERTER).intValue();
    intAmount = intAmount / 2;
    BigDecimal amountToAdd =
      new BigDecimal(intAmount).divide(CONVERTER, SCALE, RoundingMode.HALF_UP);
    BigDecimal testBalance = AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of positive odd amount across two positive-balance accounts.
   */
  @Test
  public void testDistributeAmount2EqualAccountsOddAmountNegNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(NEG_ODD_AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    Integer intAmount = NEG_ODD_AMOUNT.multiply(CONVERTER).intValue();
    intAmount = intAmount / 2;
    BigDecimal amountToAdd =
      new BigDecimal(intAmount).divide(CONVERTER, SCALE, RoundingMode.HALF_UP);
    BigDecimal testBalance = NEG_AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of positive even amount across three positive-balance accounts.
   */
  @Test
  public void testDistributeAmount3EqualAccountsDivisibleAmountPosPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(DIV_AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    // Use integer arithmetic to avoid non-terminating decimal expansion
    Integer intAmountToAdd = INT_DIV_AMOUNT / 3;
    BigDecimal amountToAdd =
      new BigDecimal(intAmountToAdd).divide(CONVERTER, SCALE, RoundingMode.HALF_UP).setScale(SCALE,
                                                                                             RoundingMode.HALF_UP);
    BigDecimal testBalance = AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);
    BigDecimal newBalance3 = distributor.getBalance(equityAccount3);
    assertTrue("wrong balance 3 " + newBalance3 + ", expecting " + testBalance,
               newBalance3.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
    BigDecimal newAmount3 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount3, newAmount3.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of positive even divisible across three negative-balance accounts.
   */
  @Test
  public void testDistributeAmount3EqualAccountsDivisibleAmountPosNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(DIV_AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.addBalance(equityAccount3, NEG_AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    // Use integer arithmetic to avoid non-terminating decimal expansion
    Integer intAmountToAdd = INT_DIV_AMOUNT / 3;
    BigDecimal amountToAdd =
      new BigDecimal(intAmountToAdd).divide(CONVERTER, SCALE, RoundingMode.HALF_UP).setScale(SCALE,
                                                                                             RoundingMode.HALF_UP);
    BigDecimal testBalance = NEG_AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);
    BigDecimal newBalance3 = distributor.getBalance(equityAccount3);
    assertTrue("wrong balance 3 " + newBalance3 + ", expecting " + testBalance,
               newBalance3.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
    BigDecimal newAmount3 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount3, newAmount3.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of negative divisible amount across three positive-balance accounts.
   */
  @Test
  public void testDistributeAmount3EqualAccountsDivisibleAmountNegPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(NEG_DIV_AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    // Use integer arithmetic to avoid non-terminating decimal expansion
    Integer intAmountToAdd = INT_NEG_DIV_AMOUNT / 3;
    BigDecimal amountToAdd =
      new BigDecimal(intAmountToAdd).divide(CONVERTER, SCALE, RoundingMode.HALF_UP).setScale(SCALE,
                                                                                             RoundingMode.HALF_UP);
    BigDecimal testBalance = AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);
    BigDecimal newBalance3 = distributor.getBalance(equityAccount3);
    assertTrue("wrong balance 3 " + newBalance3 + ", expecting " + testBalance,
               newBalance3.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
    BigDecimal newAmount3 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount3, newAmount3.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of negative divisible amount across three negative-balance accounts.
   */
  @Test
  public void testDistributeAmount3EqualAccountsDivisibleAmountNegNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(NEG_DIV_AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.addBalance(equityAccount3, NEG_AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    // Use integer arithmetic to avoid non-terminating decimal expansion
    Integer intAmountToAdd = INT_NEG_DIV_AMOUNT / 3;
    BigDecimal amountToAdd =
      new BigDecimal(intAmountToAdd).divide(CONVERTER, SCALE, RoundingMode.HALF_UP).setScale(SCALE,
                                                                                             RoundingMode.HALF_UP);
    BigDecimal testBalance = NEG_AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);
    BigDecimal newBalance3 = distributor.getBalance(equityAccount3);
    assertTrue("wrong balance 3 " + newBalance3 + ", expecting " + testBalance,
               newBalance3.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
    BigDecimal newAmount3 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount3, newAmount3.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of positive indivisible amount across three positive-balance accounts.
   */
  @Test
  public void testDistributeAmount3EqualAccountsRem2PosPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    // Use integer arithmetic to avoid non-terminating decimal expansion
    Integer intAmountToAdd = INT_INDIV_AMOUNT_R2 / 3;
    BigDecimal amountToAdd =
      new BigDecimal(intAmountToAdd).divide(CONVERTER, SCALE, RoundingMode.HALF_UP).setScale(SCALE,
                                                                                             RoundingMode.HALF_UP);
    BigDecimal testBalance = AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);
    BigDecimal newBalance3 = distributor.getBalance(equityAccount3);
    assertTrue("wrong balance 3 " + newBalance3 + ", expecting " + testBalance,
               newBalance3.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
    BigDecimal newAmount3 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount3, newAmount3.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of positive indivisible amount across three positive-balance accounts.
   */
  @Test
  public void testDistributeAmount3EqualAccountsRem2PosNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.addBalance(equityAccount3, NEG_AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    // Use integer arithmetic to avoid non-terminating decimal expansion
    Integer intAmountToAdd = INT_INDIV_AMOUNT_R2 / 3;
    BigDecimal amountToAdd =
      new BigDecimal(intAmountToAdd).divide(CONVERTER, SCALE, RoundingMode.HALF_UP).setScale(SCALE,
                                                                                             RoundingMode.HALF_UP);
    BigDecimal testBalance = NEG_AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);
    BigDecimal newBalance3 = distributor.getBalance(equityAccount3);
    assertTrue("wrong balance 3 " + newBalance3 + ", expecting " + testBalance,
               newBalance3.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
    BigDecimal newAmount3 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount3, newAmount3.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of negative indivisible amount across three positive-balance accounts.
   */
  @Test
  public void testDistributeAmount3EqualAccountsRem2NegPos() {
    AccountCollectionDistributor distributor =
      new AccountCollectionDistributor(NEG_INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    // Use integer arithmetic to avoid non-terminating decimal expansion
    Integer intAmountToAdd = INT_NEG_INDIV_AMOUNT_R2 / 3;
    BigDecimal amountToAdd =
      new BigDecimal(intAmountToAdd).divide(CONVERTER, SCALE, RoundingMode.HALF_UP).setScale(SCALE,
                                                                                             RoundingMode.HALF_UP);
    BigDecimal testBalance = AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);
    BigDecimal newBalance3 = distributor.getBalance(equityAccount3);
    assertTrue("wrong balance 3 " + newBalance3 + ", expecting " + testBalance,
               newBalance3.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
    BigDecimal newAmount3 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount3, newAmount3.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeAmount()}
   * . Tests distribution of negative indivisible amount across three negative-balance accounts.
   */
  @Test
  public void testDistributeAmount3EqualAccountsRem2NegNeg() {
    AccountCollectionDistributor distributor =
      new AccountCollectionDistributor(NEG_INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.addBalance(equityAccount3, NEG_AMOUNT);
    distributor.distributeAmount();
    BigDecimal newBalance1 = distributor.getBalance(equityAccount1);
    // Use integer arithmetic to avoid non-terminating decimal expansion
    Integer intAmountToAdd = INT_NEG_INDIV_AMOUNT_R2 / 3;
    BigDecimal amountToAdd =
      new BigDecimal(intAmountToAdd).divide(CONVERTER, SCALE, RoundingMode.HALF_UP).setScale(SCALE,
                                                                                             RoundingMode.HALF_UP);
    BigDecimal testBalance = NEG_AMOUNT.add(amountToAdd);
    assertTrue("wrong balance 1 " + newBalance1 + ", expecting " + testBalance,
               newBalance1.compareTo(testBalance) == 0);
    BigDecimal newBalance2 = distributor.getBalance(equityAccount2);
    assertTrue("wrong balance 2 " + newBalance2 + ", expecting " + testBalance,
               newBalance2.compareTo(testBalance) == 0);
    BigDecimal newBalance3 = distributor.getBalance(equityAccount3);
    assertTrue("wrong balance 3 " + newBalance3 + ", expecting " + testBalance,
               newBalance3.compareTo(testBalance) == 0);

    BigDecimal newAmount1 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 1 " + newAmount1, newAmount1.compareTo(amountToAdd) == 0);
    BigDecimal newAmount2 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount2, newAmount2.compareTo(amountToAdd) == 0);
    BigDecimal newAmount3 = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount 2 " + newAmount3, newAmount3.compareTo(amountToAdd) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests that a single account makes no change to balances (no remainder). Also tests basic
   * balance validity. for a positive amount and a positive balance.
   */
  @Test
  public void testDistributeRemainder1AccountPosPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.distributeRemainder();
    BigDecimal newBalance = distributor.getBalance(equityAccount1);
    assertTrue("wrong balance after remainder " + newBalance, newBalance.compareTo(AMOUNT) == 0);
    BigDecimal newItem = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount after remainder " + newItem,
               newItem.compareTo(BigDecimal.ZERO) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests case with no balances, should throw exception.
   */
  @Test
  public void testDistributeRemainderNoBalance() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    try {
      distributor.distributeRemainder();
      fail("remainder distribution did not throw invalid-collection exception");
    } catch (RuntimeException e) {
      assertTrue("wrong error exception: " + e.getMessage(),
                 e.getMessage().contains(INVALID_COLLECTION_ERROR));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests case with unequal balances, isValid should cause exception.
   */
  @Test
  public void testDistributeRemainder2UnequalBalances() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT.add(
      new BigDecimal("0.05").setScale(SCALE, RoundingMode.HALF_UP)));
    distributor.addBalance(equityAccount2, AMOUNT.add(
      new BigDecimal("-0.05").setScale(SCALE, RoundingMode.HALF_UP)));
    try {
      distributor.distributeAmount();
      fail("remainder distribution did not throw invalid-collection exception");
    } catch (RuntimeException e) {
      assertTrue("wrong error exception: " + e.getMessage(),
                 e.getMessage().contains(INVALID_COLLECTION_ERROR));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests that a single account makes no change to balances (no remainder) for a positive amount
   * and a negative balance.
   */
  @Test
  public void testDistributeRemainder1AccountPosNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.distributeRemainder();
    BigDecimal newBalance = distributor.getBalance(equityAccount1);
    assertTrue("wrong balance after remainder " + newBalance,
               newBalance.compareTo(NEG_AMOUNT) == 0);
    BigDecimal newItem = distributor.getItemAmount(equityAccount1);
    assertTrue("wrong item amount after remainder " + newItem,
               newItem.compareTo(BigDecimal.ZERO) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests that a single account makes no change to balances (no remainder) for a negative amount
   * and a negative balance.
   */
  @Test
  public void testDistributeRemainder1AccountNegNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(NEG_AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.distributeRemainder();
    BigDecimal newBalance = distributor.getBalance(equityAccount1);
    assertTrue("wrong balance before remainder " + newBalance,
               newBalance.compareTo(NEG_AMOUNT) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests remainder distribution of positive, odd amount for two accounts with positive balances
   */
  @Test
  public void testDistributeRemainder2EqualAccountsOddPosPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(ODD_AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.distributeRemainder();
    BigDecimal expectedAmount = AMOUNT.add(PENNY);
    BigDecimal balance1 = distributor.getBalance(equityAccount1);
    BigDecimal balance2 = distributor.getBalance(equityAccount2);
    assertTrue("remainder not distributed properly",
               (balance1.compareTo(AMOUNT) == 0 && balance2.compareTo(expectedAmount) == 0) ||
               (balance2.compareTo(AMOUNT) == 0 && balance1.compareTo(expectedAmount) == 0));
    BigDecimal item1 = distributor.getItemAmount(equityAccount1);
    BigDecimal item2 = distributor.getItemAmount(equityAccount2);
    assertTrue("remainder not distributed properly",
               (item1.compareTo(BigDecimal.ZERO) == 0 && item2.compareTo(PENNY) == 0) ||
               (item2.compareTo(BigDecimal.ZERO) == 0 && item1.compareTo(PENNY) == 0));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests remainder distribution of negative, odd amount for two accounts with positive balances
   */
  @Test
  public void testDistributeRemainder2EqualAccountsOddNegPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(NEG_ODD_AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.distributeRemainder();
    BigDecimal expectedAmount = AMOUNT.add(NEG_PENNY);
    BigDecimal balance1 = distributor.getBalance(equityAccount1);
    BigDecimal balance2 = distributor.getBalance(equityAccount2);
    assertTrue("remainder not distributed properly",
               (balance1.compareTo(AMOUNT) == 0 && balance2.compareTo(expectedAmount) == 0) ||
               (balance2.compareTo(AMOUNT) == 0 && balance1.compareTo(expectedAmount) == 0));
    BigDecimal item1 = distributor.getItemAmount(equityAccount1);
    BigDecimal item2 = distributor.getItemAmount(equityAccount2);
    assertTrue("remainder not distributed properly",
               (item1.compareTo(BigDecimal.ZERO) == 0 && item2.compareTo(NEG_PENNY) == 0) ||
               (item2.compareTo(BigDecimal.ZERO) == 0 && item1.compareTo(NEG_PENNY) == 0));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests remainder distribution of positive, odd amount for two accounts with negative balances
   */
  @Test
  public void testDistributeRemainder2EqualAccountsOddPosNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(ODD_AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.distributeRemainder();
    BigDecimal expectedAmount = NEG_AMOUNT.add(PENNY);
    BigDecimal balance1 = distributor.getBalance(equityAccount1);
    BigDecimal balance2 = distributor.getBalance(equityAccount2);
    assertTrue("remainder not distributed properly",
               (balance1.compareTo(NEG_AMOUNT) == 0 && balance2.compareTo(expectedAmount) == 0) ||
               (balance2.compareTo(NEG_AMOUNT) == 0 && balance1.compareTo(expectedAmount) == 0));
    BigDecimal item1 = distributor.getItemAmount(equityAccount1);
    BigDecimal item2 = distributor.getItemAmount(equityAccount2);
    assertTrue("remainder not distributed properly",
               (item1.compareTo(BigDecimal.ZERO) == 0 && item2.compareTo(PENNY) == 0) ||
               (item2.compareTo(BigDecimal.ZERO) == 0 && item1.compareTo(PENNY) == 0));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests remainder distribution of negative, odd amount for two accounts with negative balances
   */
  @Test
  public void testDistributeRemainder2EqualAccountsOddNegNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(NEG_ODD_AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.distributeRemainder();
    BigDecimal expectedAmount = NEG_AMOUNT.add(NEG_PENNY);
    BigDecimal balance1 = distributor.getBalance(equityAccount1);
    BigDecimal balance2 = distributor.getBalance(equityAccount2);
    assertTrue("remainder not distributed properly",
               (balance1.compareTo(NEG_AMOUNT) == 0 && balance2.compareTo(expectedAmount) == 0) ||
               (balance2.compareTo(NEG_AMOUNT) == 0 && balance1.compareTo(expectedAmount) == 0));
    BigDecimal item1 = distributor.getItemAmount(equityAccount1);
    BigDecimal item2 = distributor.getItemAmount(equityAccount2);
    assertTrue("remainder not distributed properly",
               (item1.compareTo(BigDecimal.ZERO) == 0 && item2.compareTo(NEG_PENNY) == 0) ||
               (item2.compareTo(BigDecimal.ZERO) == 0 && item1.compareTo(NEG_PENNY) == 0));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests remainder distribution of positive, indivisible amount remainder 2 for three accounts
   * with positive balances
   */
  @Test
  public void testDistributeRemainder3EqualAccountsRem2PosPos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, AMOUNT);
    distributor.distributeRemainder();
    BigDecimal expectedAmount = AMOUNT.add(PENNY);
    BigDecimal balance1 = distributor.getBalance(equityAccount1);
    BigDecimal balance2 = distributor.getBalance(equityAccount2);
    BigDecimal balance3 = distributor.getBalance(equityAccount3);
    assertTrue("remainder not distributed properly to balances",
               (balance1.compareTo(AMOUNT) == 0 && balance2.compareTo(expectedAmount) == 0 &&
                balance3.compareTo(expectedAmount) == 0) ||
               (balance2.compareTo(AMOUNT) == 0 && balance1.compareTo(expectedAmount) == 0 &&
                balance3.compareTo(expectedAmount) == 0) ||
               (balance3.compareTo(AMOUNT) == 0 && balance1.compareTo(expectedAmount) == 0 &&
                balance2.compareTo(expectedAmount) == 0));
    BigDecimal amount1 = distributor.getItemAmount(equityAccount1);
    BigDecimal amount2 = distributor.getItemAmount(equityAccount2);
    BigDecimal amount3 = distributor.getItemAmount(equityAccount3);
    assertTrue("remainder not distributed properly to items",
               (amount1.compareTo(BigDecimal.ZERO) == 0 && amount2.compareTo(PENNY) == 0 &&
                amount3.compareTo(PENNY) == 0) ||
               (amount2.compareTo(BigDecimal.ZERO) == 0 && amount1.compareTo(PENNY) == 0 &&
                amount3.compareTo(PENNY) == 0) ||
               (amount3.compareTo(BigDecimal.ZERO) == 0 && amount1.compareTo(PENNY) == 0 &&
                amount2.compareTo(PENNY) == 0));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests remainder distribution of positive, indivisible amount remainder 2 for three accounts
   * with negative balances
   */
  @Test
  public void testDistributeRemainder3EqualAccountsRem2PosNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.addBalance(equityAccount3, NEG_AMOUNT);
    distributor.distributeRemainder();
    BigDecimal expectedAmount = NEG_AMOUNT.add(PENNY);
    BigDecimal balance1 = distributor.getBalance(equityAccount1);
    BigDecimal balance2 = distributor.getBalance(equityAccount2);
    BigDecimal balance3 = distributor.getBalance(equityAccount3);
    // logger.info("3 accounts rem 2 pos neg balances: " + balance1 + ", "
    // + balance2 + ", " + balance3);
    assertTrue("remainder not distributed properly to balances",
               (balance1.compareTo(NEG_AMOUNT) == 0 && balance2.compareTo(expectedAmount) == 0 &&
                balance3.compareTo(expectedAmount) == 0) ||
               (balance2.compareTo(NEG_AMOUNT) == 0 && balance1.compareTo(expectedAmount) == 0 &&
                balance3.compareTo(expectedAmount) == 0) ||
               (balance3.compareTo(NEG_AMOUNT) == 0 && balance1.compareTo(expectedAmount) == 0 &&
                balance2.compareTo(expectedAmount) == 0));
    BigDecimal amount1 = distributor.getItemAmount(equityAccount1);
    BigDecimal amount2 = distributor.getItemAmount(equityAccount2);
    BigDecimal amount3 = distributor.getItemAmount(equityAccount3);
    assertTrue("remainder not distributed properly to items",
               (amount1.compareTo(BigDecimal.ZERO) == 0 && amount2.compareTo(PENNY) == 0 &&
                amount3.compareTo(PENNY) == 0) ||
               (amount2.compareTo(BigDecimal.ZERO) == 0 && amount1.compareTo(PENNY) == 0 &&
                amount3.compareTo(PENNY) == 0) ||
               (amount3.compareTo(BigDecimal.ZERO) == 0 && amount1.compareTo(PENNY) == 0 &&
                amount2.compareTo(PENNY) == 0));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests remainder distribution of negative, indivisible amount remainder -2 for three accounts
   * with positive balances
   */
  @Test
  public void testDistributeRemainder3EqualAccountsRem2NegPos() {
    AccountCollectionDistributor distributor =
      new AccountCollectionDistributor(NEG_INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, AMOUNT);
    distributor.distributeRemainder();
    BigDecimal expectedAmount = AMOUNT.add(NEG_PENNY);
    BigDecimal balance1 = distributor.getBalance(equityAccount1);
    BigDecimal balance2 = distributor.getBalance(equityAccount2);
    BigDecimal balance3 = distributor.getBalance(equityAccount3);
    // logger.info("3 accounts rem 2 neg pos balances: " + balance1 + ", "
    // + balance2 + ", " + balance3);
    assertTrue("remainder not distributed properly to balances",
               (balance1.compareTo(AMOUNT) == 0 && balance2.compareTo(expectedAmount) == 0 &&
                balance3.compareTo(expectedAmount) == 0) ||
               (balance2.compareTo(AMOUNT) == 0 && balance1.compareTo(expectedAmount) == 0 &&
                balance3.compareTo(expectedAmount) == 0) ||
               (balance3.compareTo(AMOUNT) == 0 && balance1.compareTo(expectedAmount) == 0 &&
                balance2.compareTo(expectedAmount) == 0));
    BigDecimal amount1 = distributor.getItemAmount(equityAccount1);
    BigDecimal amount2 = distributor.getItemAmount(equityAccount2);
    BigDecimal amount3 = distributor.getItemAmount(equityAccount3);
    assertTrue("remainder not distributed properly to items",
               (amount1.compareTo(BigDecimal.ZERO) == 0 && amount2.compareTo(NEG_PENNY) == 0 &&
                amount3.compareTo(NEG_PENNY) == 0) ||
               (amount2.compareTo(BigDecimal.ZERO) == 0 && amount1.compareTo(NEG_PENNY) == 0 &&
                amount3.compareTo(NEG_PENNY) == 0) ||
               (amount3.compareTo(BigDecimal.ZERO) == 0 && amount1.compareTo(NEG_PENNY) == 0 &&
                amount2.compareTo(NEG_PENNY) == 0));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainder()}
   * . Tests remainder distribution of positive, indivisible amount remainder 2 for three accounts
   * with negative balances
   */
  @Test
  public void testDistributeRemainder3EqualAccountsRem2NegNeg() {
    AccountCollectionDistributor distributor =
      new AccountCollectionDistributor(NEG_INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.addBalance(equityAccount3, NEG_AMOUNT);
    distributor.distributeRemainder();
    BigDecimal expectedAmount = NEG_AMOUNT.add(NEG_PENNY);
    BigDecimal balance1 = distributor.getBalance(equityAccount1);
    BigDecimal balance2 = distributor.getBalance(equityAccount2);
    BigDecimal balance3 = distributor.getBalance(equityAccount3);
    // logger.info("3 accounts rem 2 neg pos balances: " + balance1 + ", "
    // + balance2 + ", " + balance3);
    assertTrue("remainder not distributed properly",
               (balance1.compareTo(NEG_AMOUNT) == 0 && balance2.compareTo(expectedAmount) == 0 &&
                balance3.compareTo(expectedAmount) == 0) ||
               (balance2.compareTo(NEG_AMOUNT) == 0 && balance1.compareTo(expectedAmount) == 0 &&
                balance3.compareTo(expectedAmount) == 0) ||
               (balance3.compareTo(NEG_AMOUNT) == 0 && balance1.compareTo(expectedAmount) == 0 &&
                balance2.compareTo(expectedAmount) == 0));
    BigDecimal amount1 = distributor.getItemAmount(equityAccount1);
    BigDecimal amount2 = distributor.getItemAmount(equityAccount2);
    BigDecimal amount3 = distributor.getItemAmount(equityAccount3);
    assertTrue("remainder not distributed properly to items",
               (amount1.compareTo(BigDecimal.ZERO) == 0 && amount2.compareTo(NEG_PENNY) == 0 &&
                amount3.compareTo(NEG_PENNY) == 0) ||
               (amount2.compareTo(BigDecimal.ZERO) == 0 && amount1.compareTo(NEG_PENNY) == 0 &&
                amount3.compareTo(NEG_PENNY) == 0) ||
               (amount3.compareTo(BigDecimal.ZERO) == 0 && amount1.compareTo(NEG_PENNY) == 0 &&
                amount2.compareTo(NEG_PENNY) == 0));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainderNearlyEqual(java.lang.Integer)}
   * . Test balance and remainder for indivisible amount with remainder 1 distributed to three
   * nearly-equal positive accounts
   */
  @Test
  public void testDistributeRemainderNearlyEqual3PosAccountsRem1() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R1);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, NEAR_AMOUNT);

    Integer remainder = INT_INDIV_AMOUNT_R1 % 3;
    Integer newRemainder = distributor.distributeRemainderNearlyEqual(remainder);
    assertTrue("remainder is not 0 after distribution", newRemainder == 0);

    BigDecimal balance1 = distributor.getBalance(equityAccount1);
    BigDecimal balance2 = distributor.getBalance(equityAccount2);
    BigDecimal balance3 = distributor.getBalance(equityAccount3);

    assertTrue("balance 1 not as expected: " + balance1 + " != " + AMOUNT,
               balance1.compareTo(AMOUNT) == 0);
    assertTrue("balance 2 not as expected: " + balance2 + " != " + AMOUNT,
               balance2.compareTo(AMOUNT) == 0);
    assertTrue("balance 3 not as expected: " + balance3 + " != " + AMOUNT,
               balance3.compareTo(AMOUNT) == 0);

    BigDecimal amount1 = distributor.getItemAmount(equityAccount1);
    BigDecimal amount2 = distributor.getItemAmount(equityAccount2);
    BigDecimal amount3 = distributor.getItemAmount(equityAccount3);

    assertTrue("item amount 1 not as expected: " + amount1 + " != 0",
               amount1.compareTo(BigDecimal.ZERO) == 0);
    assertTrue("item amount 2 not as expected: " + amount2 + " != 0",
               amount2.compareTo(BigDecimal.ZERO) == 0);
    assertTrue("item amount 3 not as expected: " + amount3 + " != " + PENNY,
               amount3.compareTo(PENNY) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainderNearlyEqual(java.lang.Integer)}
   * . Test balances and remainder for indivisible amount with remainder 2 distributed to three
   * nearly-equal positive accounts
   */
  @Test
  public void testDistributeRemainderNearlyEqual3PosAccountsRem2() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, NEAR_AMOUNT);

    Integer remainder = INT_INDIV_AMOUNT_R2 % 3;
    Integer newRemainder = distributor.distributeRemainderNearlyEqual(remainder);
    assertTrue("remainder is not 1 after distribution", newRemainder == 1);

    BigDecimal balance1 = distributor.getBalance(equityAccount1);
    BigDecimal balance2 = distributor.getBalance(equityAccount2);
    BigDecimal balance3 = distributor.getBalance(equityAccount3);

    assertTrue("balance 1 not as expected: " + balance1 + " != " + AMOUNT,
               balance1.compareTo(AMOUNT) == 0);
    assertTrue("balance 2 not as expected: " + balance2 + " != " + AMOUNT,
               balance2.compareTo(AMOUNT) == 0);
    assertTrue("balance 3 not as expected: " + balance3 + " != " + AMOUNT,
               balance3.compareTo(AMOUNT) == 0);

    BigDecimal amount1 = distributor.getItemAmount(equityAccount1);
    BigDecimal amount2 = distributor.getItemAmount(equityAccount2);
    BigDecimal amount3 = distributor.getItemAmount(equityAccount3);

    assertTrue("item amount 1 not as expected: " + amount1 + " != 0",
               amount1.compareTo(BigDecimal.ZERO) == 0);
    assertTrue("item amount 2 not as expected: " + amount2 + " != 0",
               amount2.compareTo(BigDecimal.ZERO) == 0);
    assertTrue("item amount 3 not as expected: " + amount3 + " != " + PENNY,
               amount3.compareTo(PENNY) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainderNearlyEqual(java.lang.Integer)}
   * . Test balance and remainder for indivisible amount with remainder 1 distributed to three
   * nearly-equal negative accounts
   */
  @Test
  public void testDistributeRemainderNearlyEqual3NegAccountsRem1() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R1);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.addBalance(equityAccount3, NEAR_NEG_AMOUNT);

    Integer remainder = INT_INDIV_AMOUNT_R1 % 3;
    Integer newRemainder = distributor.distributeRemainderNearlyEqual(remainder);
    assertTrue("remainder is not 0 after distribution", newRemainder == 0);

    BigDecimal balance1 = distributor.getBalance(equityAccount1);
    BigDecimal balance2 = distributor.getBalance(equityAccount2);
    BigDecimal balance3 = distributor.getBalance(equityAccount3);

    assertTrue("balance 1 not as expected: " + balance1 + " != " + NEAR_NEG_AMOUNT,
               balance1.compareTo(NEAR_NEG_AMOUNT) == 0);
    assertTrue("balance 2 not as expected: " + balance2 + " != " + NEG_AMOUNT,
               balance2.compareTo(NEG_AMOUNT) == 0);
    assertTrue("balance 3 not as expected: " + balance3 + " != " + NEAR_NEG_AMOUNT,
               balance3.compareTo(NEAR_NEG_AMOUNT) == 0);

    BigDecimal amount1 = distributor.getItemAmount(equityAccount1);
    BigDecimal amount2 = distributor.getItemAmount(equityAccount2);
    BigDecimal amount3 = distributor.getItemAmount(equityAccount3);

    assertTrue("item amount 1 not as expected: " + amount1 + " != " + PENNY,
               amount1.compareTo(PENNY) == 0);
    assertTrue("item amount 2 not as expected: " + amount2 + " != 0",
               amount2.compareTo(BigDecimal.ZERO) == 0);
    assertTrue("item amount 3 not as expected: " + amount3 + " != 0",
               amount3.compareTo(BigDecimal.ZERO) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#distributeRemainderNearlyEqual(java.lang.Integer)}
   * . Test balance and remainder for indivisible amount with remainder 2 distributed to three
   * nearly-equal negative accounts
   */
  @Test
  public void testDistributeRemainderNearlyEqual3NegAccounts2() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.addBalance(equityAccount3, NEAR_NEG_AMOUNT);

    Integer remainder = INT_INDIV_AMOUNT_R2 % 3;
    Integer newRemainder = distributor.distributeRemainderNearlyEqual(remainder);
    assertTrue("remainder is not 1 after distribution", newRemainder == 1);

    BigDecimal balance1 = distributor.getBalance(equityAccount1);
    BigDecimal balance2 = distributor.getBalance(equityAccount2);
    BigDecimal balance3 = distributor.getBalance(equityAccount3);

    assertTrue("balance 1 not as expected: " + balance1 + " != " + NEAR_NEG_AMOUNT,
               balance1.compareTo(NEAR_NEG_AMOUNT) == 0);
    assertTrue("balance 2 not as expected: " + balance2 + " != " + NEG_AMOUNT,
               balance2.compareTo(NEG_AMOUNT) == 0);
    assertTrue("balance 3 not as expected: " + balance3 + " != " + NEAR_NEG_AMOUNT,
               balance3.compareTo(NEAR_NEG_AMOUNT) == 0);

    BigDecimal amount1 = distributor.getItemAmount(equityAccount1);
    BigDecimal amount2 = distributor.getItemAmount(equityAccount2);
    BigDecimal amount3 = distributor.getItemAmount(equityAccount3);

    assertTrue("item amount 1 not as expected: " + amount1 + " != " + PENNY,
               amount1.compareTo(PENNY) == 0);
    assertTrue("item amount 2 not as expected: " + amount2 + " != 0",
               amount2.compareTo(BigDecimal.ZERO) == 0);
    assertTrue("item amount 3 not as expected: " + amount3 + " != 0",
               amount3.compareTo(BigDecimal.ZERO) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .AccountCollectionDistributor#setBalanceAndDecrementRemainder(java.lang.Integer, * com
   * .poesys.accounting.dataloader.newaccounting.Account)} . Tests remainder and balance after
   * call for positive remainder and positive balances.
   */
  @Test
  public void testSetBalanceAndDecrementRemainderPosRemainderPosBalances() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R1);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);

    Integer remainder = distributor.setBalanceAndDecrementRemainder(1, equityAccount1);
    assertTrue("remainder is not zero", remainder.compareTo(0) == 0);
    BigDecimal balance = distributor.getBalance(equityAccount1);
    assertTrue("balance not set: " + balance + " != " + AMOUNT.add(PENNY),
               balance.compareTo(AMOUNT.add(PENNY)) == 0);
    BigDecimal amount = distributor.getItemAmount(equityAccount1);
    assertTrue("item amount not set: " + amount + " != " + PENNY, amount.compareTo(PENNY) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .AccountCollectionDistributor#setBalanceAndDecrementRemainder(java.lang.Integer, * com
   * .poesys.accounting.dataloader.newaccounting.Account)} . Tests remainder and balance after
   * call for negative remainder and positive balances.
   */
  @Test
  public void testSetBalanceAndDecrementRemainderNegRemainderPosBalances() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R1);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);

    Integer remainder = distributor.setBalanceAndDecrementRemainder(-1, equityAccount1);
    assertTrue("remainder is not zero", remainder.compareTo(0) == 0);
    BigDecimal balance = distributor.getBalance(equityAccount1);
    assertTrue("balance not set: " + balance + " != " + AMOUNT.add(NEG_PENNY),
               balance.compareTo(AMOUNT.add(NEG_PENNY)) == 0);
    BigDecimal amount = distributor.getItemAmount(equityAccount1);
    assertTrue("item amount not set: " + amount + " != " + NEG_PENNY,
               amount.compareTo(NEG_PENNY) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .AccountCollectionDistributor#setBalanceAndDecrementRemainder(java.lang.Integer, * com
   * .poesys.accounting.dataloader.newaccounting.Account)} . Tests remainder and balance after
   * call for positive remainder and negative balances.
   */
  @Test
  public void testSetBalanceAndDecrementRemainderPosRemainderNegBalances() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R1);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);

    Integer remainder = distributor.setBalanceAndDecrementRemainder(1, equityAccount1);
    assertTrue("remainder is not zero", remainder.compareTo(0) == 0);
    BigDecimal balance = distributor.getBalance(equityAccount1);
    assertTrue("balance not set: " + balance + " != " + NEG_AMOUNT.add(PENNY),
               balance.compareTo(NEG_AMOUNT.add(PENNY)) == 0);
    BigDecimal amount = distributor.getItemAmount(equityAccount1);
    assertTrue("item amount not set: " + amount + " != " + PENNY, amount.compareTo(PENNY) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .AccountCollectionDistributor#setBalanceAndDecrementRemainder(java.lang.Integer, * com
   * .poesys.accounting.dataloader.newaccounting.Account)} . Tests remainder and balance after
   * call for negative remainder and negative balances.
   */
  @Test
  public void testSetBalanceAndDecrementRemainderNegRemainderNegBalances() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R1);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);

    Integer remainder = distributor.setBalanceAndDecrementRemainder(-1, equityAccount1);
    assertTrue("remainder is not zero", remainder.compareTo(0) == 0);
    BigDecimal balance = distributor.getBalance(equityAccount1);
    assertTrue("balance not set: " + balance + " != " + NEG_AMOUNT.add(NEG_PENNY),
               balance.compareTo(NEG_AMOUNT.add(NEG_PENNY)) == 0);
    BigDecimal amount = distributor.getItemAmount(equityAccount1);
    assertTrue("item amount not set: " + amount + " != " + NEG_PENNY,
               amount.compareTo(NEG_PENNY) == 0);
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .AccountCollectionDistributor#setBalanceAndDecrementRemainder(java.lang.Integer, * com
   * .poesys.accounting.dataloader.newaccounting.Account)} . Tests whether setting an account not
   * in the balances throws exception
   */
  @Test
  public void testSetBalanceAndDecrementRemainderInvalidAccount() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R1);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    try {
      distributor.setBalanceAndDecrementRemainder(-1, equityAccount3);
    } catch (RuntimeException e) {
      assertTrue("wrong exception: " + e.getMessage(), e.getMessage().contains(NO_BALANCE_ERROR));
    }
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#getMaximumBalanceAccount()}
   * .
   */
  @Test
  public void testGetMaximumBalanceAccount2Pos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, NEAR_AMOUNT);

    Account max = distributor.getMaximumBalanceAccount();
    assertTrue("did not get max account: " + max, max.equals(equityAccount1));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#getMinimumBalanceAccount()}
   * .
   */
  @Test
  public void testGetMinimumBalanceAccount2Pos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, NEAR_AMOUNT);

    Account min = distributor.getMinimumBalanceAccount();
    assertTrue("did not get min account: " + min, min.equals(equityAccount2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#getMaximumBalanceAccount()}
   * .
   */
  @Test
  public void testGetMaximumBalanceAccount3Pos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, NEAR_AMOUNT);

    Account max = distributor.getMaximumBalanceAccount();
    assertTrue("did not get max account: " + max,
               max.equals(equityAccount1) || max.equals(equityAccount2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#getMinimumBalanceAccount()}
   * .
   */
  @Test
  public void testGetMinimumBalanceAccount3Pos() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, NEAR_AMOUNT);

    Account min = distributor.getMinimumBalanceAccount();
    assertTrue("did not get min account: " + min, min.equals(equityAccount3));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#getMaximumBalanceAccount()}
   * .
   */
  @Test
  public void testGetMaximumBalanceAccount2Neg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEAR_NEG_AMOUNT);

    Account max = distributor.getMaximumBalanceAccount();
    assertTrue("did not get max account: " + max, max.equals(equityAccount2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#getMinimumBalanceAccount()}
   * .
   */
  @Test
  public void testGetMinimumBalanceAccount2Neg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEAR_NEG_AMOUNT);

    Account min = distributor.getMinimumBalanceAccount();
    assertTrue("did not get min account: " + min, min.equals(equityAccount1));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#getMaximumBalanceAccount()}
   * .
   */
  @Test
  public void testGetMaximumBalanceAccount3Neg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.addBalance(equityAccount3, NEAR_NEG_AMOUNT);

    Account max = distributor.getMaximumBalanceAccount();
    assertTrue("did not get max account: " + max, max.equals(equityAccount3));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#getMinimumBalanceAccount()}
   * .
   */
  @Test
  public void testGetMinimumBalanceAccount3Neg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEG_AMOUNT);
    distributor.addBalance(equityAccount3, NEAR_NEG_AMOUNT);

    Account min = distributor.getMinimumBalanceAccount();
    assertTrue("did not get min account: " + min,
               min.equals(equityAccount1) || min.equals(equityAccount2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 2 equal accounts compare as equal.
   */
  @Test
  public void testEqualEqual2() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);

    assertTrue("balances not equal", distributor.equal());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 2 nearly-equal accounts compare as equal.
   */
  @Test
  public void testEqualNotEqual2() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(AMOUNT);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, NEAR_AMOUNT);

    assertTrue("balances equal", !distributor.equal());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 3 equal accounts compare as equal.
   */
  @Test
  public void testEqualEqual3() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, AMOUNT);

    assertTrue("balances not equal", distributor.equal());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 3 nearly-equal accounts compare as equal.
   */
  @Test
  public void testEqualNotEqual3() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(INDIV_AMOUNT_R2);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, NEAR_AMOUNT);

    assertTrue("balances equal", !distributor.equal());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether one balance is left alone.
   */
  @Test
  public void testEqualize1Account() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(BigDecimal.ZERO);
    distributor.addBalance(equityAccount1, AMOUNT);
    boolean equalized = distributor.equalize();
    assertTrue("balances not equal", distributor.equal());
    assertTrue("Item 1 amount not zero: " + distributor.getItemAmount(equityAccount1),
               distributor.getItemAmount(equityAccount1).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("wrongly equalized", !equalized);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 2 equal balances are left alone.
   */
  @Test
  public void testEqualize2Equal() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(BigDecimal.ZERO);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    boolean equalized = distributor.equalize();
    assertTrue("balances equalized", !equalized);
    assertTrue("balances not equal", distributor.equal());
    assertTrue("Item 1 amount not zero: " + distributor.getItemAmount(equityAccount1),
               distributor.getItemAmount(equityAccount1).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 2 amount not zero: " + distributor.getItemAmount(equityAccount2),
               distributor.getItemAmount(equityAccount2).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 3 equal balances are left alone.
   */
  @Test
  public void testEqualize3Equal() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(BigDecimal.ZERO);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, AMOUNT);
    distributor.addBalance(equityAccount3, AMOUNT);
    boolean equalized = distributor.equalize();
    assertTrue("balances equalized", !equalized);
    assertTrue("balances not equal", distributor.equal());
    assertTrue("Item 1 amount not zero: " + distributor.getItemAmount(equityAccount1),
               distributor.getItemAmount(equityAccount1).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 2 amount not zero: " + distributor.getItemAmount(equityAccount2),
               distributor.getItemAmount(equityAccount2).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 3 amount not zero: " + distributor.getItemAmount(equityAccount3),
               distributor.getItemAmount(equityAccount3).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 2 nearly equal balances are left alone.
   */
  @Test
  public void testEqualize2NearlyEqual() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(BigDecimal.ZERO);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, NEAR_AMOUNT);
    boolean equalized = distributor.equalize();
    assertTrue("balances equalized", !equalized);
    assertTrue("balances not nearly equal", distributor.isValid());
    assertTrue("Item 1 amount not zero: " + distributor.getItemAmount(equityAccount1),
               distributor.getItemAmount(equityAccount1).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 2 amount not zero: " + distributor.getItemAmount(equityAccount2),
               distributor.getItemAmount(equityAccount2).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 3 nearly equal balances are left alone.
   */
  @Test
  public void testEqualize3NearlyEqual() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(BigDecimal.ZERO);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, NEAR_AMOUNT);
    distributor.addBalance(equityAccount3, NEAR_AMOUNT);
    boolean equalized = distributor.equalize();
    assertTrue("balances equalized", !equalized);
    assertTrue("balances not nearly equal", distributor.isValid());
    assertTrue("Item 1 amount not zero: " + distributor.getItemAmount(equityAccount1),
               distributor.getItemAmount(equityAccount1).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 2 amount not zero: " + distributor.getItemAmount(equityAccount2),
               distributor.getItemAmount(equityAccount2).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 3 amount not zero: " + distributor.getItemAmount(equityAccount3),
               distributor.getItemAmount(equityAccount3).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 2 unequal balances are equalized.
   */
  @Test
  public void testEqualize2Unequal() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(BigDecimal.ZERO);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, UNEQUAL_AMOUNT);
    boolean equalized = distributor.equalize();
    assertTrue("balances not equalized", equalized);
    assertTrue("balances not nearly equal", distributor.isValid());
    assertTrue("Item 1 amount not correct: " + distributor.getItemAmount(equityAccount1),
               distributor.getItemAmount(equityAccount1).equals(
                 NEG_PENNY.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 2 amount not correct: " + distributor.getItemAmount(equityAccount2),
               distributor.getItemAmount(equityAccount2).equals(
                 PENNY.setScale(SCALE, RoundingMode.HALF_UP)));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 3 unequal balances are equalized.
   */
  @Test
  public void testEqualize3Unequal() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(BigDecimal.ZERO);
    distributor.addBalance(equityAccount1, AMOUNT);
    distributor.addBalance(equityAccount2, NEAR_AMOUNT);
    distributor.addBalance(equityAccount3, UNEQUAL_AMOUNT);
    boolean equalized = distributor.equalize();
    assertTrue("balances not equalized", equalized);
    assertTrue("balances not nearly equal", distributor.isValid());
    assertTrue("Item 1 amount not correct: " + distributor.getItemAmount(equityAccount1),
               distributor.getItemAmount(equityAccount1).equals(
                 NEG_PENNY.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 2 amount not correct: " + distributor.getItemAmount(equityAccount2),
               distributor.getItemAmount(equityAccount2).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 3 amount not correct: " + distributor.getItemAmount(equityAccount3),
               distributor.getItemAmount(equityAccount3).equals(
                 PENNY.setScale(SCALE, RoundingMode.HALF_UP)));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 2 nearly equal negative-value balances are left alone.
   */
  @Test
  public void testEqualize2NearlyEqualNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(BigDecimal.ZERO);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEAR_NEG_AMOUNT);
    boolean equalized = distributor.equalize();
    assertTrue("balances equalized", !equalized);
    assertTrue("balances not nearly equal", distributor.isValid());
    assertTrue("Item 1 amount not zero: " + distributor.getItemAmount(equityAccount1),
               distributor.getItemAmount(equityAccount1).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 2 amount not zero: " + distributor.getItemAmount(equityAccount2),
               distributor.getItemAmount(equityAccount2).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 3 nearly equal negative-value balances are left alone.
   */
  @Test
  public void testEqualize3NearlyEqualNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(BigDecimal.ZERO);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEAR_NEG_AMOUNT);
    distributor.addBalance(equityAccount3, NEAR_NEG_AMOUNT);
    boolean equalized = distributor.equalize();
    assertTrue("balances equalized", !equalized);
    assertTrue("balances not nearly equal", distributor.isValid());
    assertTrue("Item 1 amount not zero: " + distributor.getItemAmount(equityAccount1),
               distributor.getItemAmount(equityAccount1).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 2 amount not zero: " + distributor.getItemAmount(equityAccount2),
               distributor.getItemAmount(equityAccount2).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 3 amount not zero: " + distributor.getItemAmount(equityAccount3),
               distributor.getItemAmount(equityAccount3).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 2 nearly equal negative-valued balances are equalized.
   */
  @Test
  public void testEqualize2UnequalNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(BigDecimal.ZERO);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, UNEQUAL_NEG_AMOUNT);
    boolean equalized = distributor.equalize();
    assertTrue("balances not equalized", equalized);
    assertTrue("balances not nearly equal", distributor.isValid());
    assertTrue("Item 1 amount not correct: " + distributor.getItemAmount(equityAccount1),
               distributor.getItemAmount(equityAccount1).equals(
                 PENNY.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 2 amount not correct: " + distributor.getItemAmount(equityAccount2),
               distributor.getItemAmount(equityAccount2).equals(
                 NEG_PENNY.setScale(SCALE, RoundingMode.HALF_UP)));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.AccountCollectionDistributor#equal()}
   * . Tests whether 3 nearly equal negative-valued balances are equalized.
   */
  @Test
  public void testEqualize3UnequalNeg() {
    AccountCollectionDistributor distributor = new AccountCollectionDistributor(BigDecimal.ZERO);
    distributor.addBalance(equityAccount1, NEG_AMOUNT);
    distributor.addBalance(equityAccount2, NEAR_NEG_AMOUNT);
    distributor.addBalance(equityAccount3, UNEQUAL_NEG_AMOUNT);
    boolean equalized = distributor.equalize();
    assertTrue("balances not equalized", equalized);
    assertTrue("balances not nearly equal", distributor.isValid());
    assertTrue("Item 1 amount not correct: " + distributor.getItemAmount(equityAccount1),
               distributor.getItemAmount(equityAccount1).equals(
                 PENNY.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 2 amount not correct: " + distributor.getItemAmount(equityAccount2),
               distributor.getItemAmount(equityAccount2).equals(
                 BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP)));
    assertTrue("Item 3 amount not correct: " + distributor.getItemAmount(equityAccount3),
               distributor.getItemAmount(equityAccount3).equals(
                 NEG_PENNY.setScale(SCALE, RoundingMode.HALF_UP)));
  }
}
