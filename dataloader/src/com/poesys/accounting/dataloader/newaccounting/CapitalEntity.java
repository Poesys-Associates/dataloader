/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.math.BigDecimal;
import java.math.RoundingMode;

import com.poesys.accounting.dataloader.IBuilder;
import com.poesys.accounting.dataloader.newaccounting.Statement.StatementType;


/**
 * Represents a capital entity in a capita structure
 * 
 * @author Robert J. Muller
 */
public class CapitalEntity {

  /** name of the entity's capital account */
  private final String capitalAccount;
  /** name of the entity's distribution/drawing account */
  private final String distributionAccount;
  /**
   * decimal percentage ownership/allocation (0.5, for example); ownership
   * should always be scaled at 3 decimal digits, permitting one digit of
   * percentage scale (45.8, for example, but not 36.75); the number should
   * follow the HALF_UP rounding convention
   */
  private BigDecimal ownership;
  /** the decimal scale of the ownership percentage */
  public static final int SCALE = 3;
  /** the rounding mode for the ownership percentage */
  public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

  // messages

  private static final String NULL_PARAMETER_ERROR =
    "Statement parameters are required but one is null";
  private static final String NO_BUILDER_ERROR =
    "no builder supplied for updating";
  private static final String NO_NAME_ERROR = null;

  /**
   * Create a Partner object.
   * 
   * @param capitalAccount the name of the capital account for the partner;
   *          required
   * @param distributionAccount the name of the distributions account for the
   *          partner; optional because individuals and other non-commercial
   *          entities do not usually have distribution accounts
   * @param ownership the decimal percentage ownership of the company for the
   *          partner (should sum to 1 across all partners); the number will be
   *          scaled and rounded according to the SCALE and ROUNDING_MODE
   *          constants; optional and defaults to 1 (100%)
   */
  public CapitalEntity(String capitalAccount,
                       String distributionAccount,
                       BigDecimal ownership) {
    // validate the inputs
    if (capitalAccount == null || capitalAccount.isEmpty()) {
      throw new RuntimeException(NULL_PARAMETER_ERROR);
    }
    this.capitalAccount = capitalAccount;
    this.distributionAccount = distributionAccount;
    this.ownership =
      ownership != null ? ownership.setScale(SCALE, ROUNDING_MODE)
          : BigDecimal.ONE.setScale(2);
  }

  /**
   * Get the capital account name.
   * 
   * @return an account name
   */
  public String getCapitalAccount() {
    return capitalAccount;
  }

  /**
   * Get the distribution account name.
   * 
   * @return an account name, or null if there is no distribution account
   */
  public String getDistributionAccount() {
    return distributionAccount;
  }

  /**
   * Get the ownership.
   * 
   * @return a ownership
   */
  public BigDecimal getOwnership() {
    return ownership;
  }

  /**
   * Set the ownership percentage of the entity as a decimal value (0.5, not 50
   * for 50%, for example). The method ensures the number is scaled and rounded
   * using the SCALE and ROUDING_MODE constants.
   * 
   * @param ownership the ownership percentage as a decimal percentage
   */
  public void setOwnership(BigDecimal ownership) {
    this.ownership = ownership.setScale(SCALE, ROUNDING_MODE);
  }

  /**
   * Get the balance for a specific fiscal year in the entity's capital account
   * from a builder object, which should have built the accounts and the fiscal
   * year before the client calls this method.
   * 
   * @param builder the builder containing the set of accounts and the fiscal
   *          year; required
   * @return the balance of the capital account
   */
  public BigDecimal getCapitalBalance(IBuilder builder) {
    return getAccountBalance(capitalAccount, builder);
  }

  /**
   * Get the balance for a specific fiscal year in the entity's distribution
   * account from a builder object, which should have built the accounts and the
   * fiscal year before the client calls this method. If there is no
   * distribution account, the method returns null.
   * 
   * @param builder the builder containing the set of accounts and the fiscal
   *          year; required
   * @return the balance of the distribution account or null if there is no
   *         distribution account
   */
  public BigDecimal getDistributionBalance(IBuilder builder) {
    BigDecimal balance = null;
    if (distributionAccount != null) {
      balance = getAccountBalance(distributionAccount, builder);
    }
    return balance;
  }

  /**
   * Get the current balance of an account in a specific fiscal year using an
   * IBuilder object that contains the current set of accounts being built. This
   * method depends on the builder having built the complete transaction set for
   * the fiscal year.
   * 
   * @param accountName the name of the account; required
   * @param builder the builder, which should have built the fiscal year, the
   *          accounts, and the transactions before this call; required
   * @return the balance in the named account
   */
  private BigDecimal getAccountBalance(String accountName, IBuilder builder) {
    if (accountName == null) {
      throw new RuntimeException(NO_NAME_ERROR);
    }
    if (builder == null) {
      throw new RuntimeException(NO_BUILDER_ERROR);
    }

    Statement stmt =
      new Statement(builder.getFiscalYear(),
                    "Balance Sheet",
                    StatementType.BALANCE_SHEET);
    Account account = builder.getAccountByName(accountName);
    return stmt.getAccountBalance(account);
  }

  @Override
  public String toString() {
    return "CapitalEntity [capitalAccount=" + capitalAccount
           + ", distributionAccount=" + distributionAccount + ", ownership="
           + ownership + "]";
  }
}
