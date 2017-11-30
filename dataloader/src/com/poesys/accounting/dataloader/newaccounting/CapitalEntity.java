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
  /** the name of the capital entity */
  private String name;
  /** the required capital account for the entity */
  private Account capitalAccount = null;
  /** the optional distribution account for the entity */
  private Account distributionAccount = null;

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

  private static final String NO_BUILDER_ERROR =
    "no builder supplied for updating";
  private static final String NO_NAME_ERROR = null;

  /**
   * Create a Partner object.
   * 
   * @param name the name of the capital entity
   * @param ownership the decimal percentage ownership of the company for the
   *          partner (should sum to 1 across all partners); the number will be
   *          scaled and rounded according to the SCALE and ROUNDING_MODE
   *          constants; optional and defaults to 1 (100%)
   */
  public CapitalEntity(String name, BigDecimal ownership) {
    this.name = name;
    this.ownership =
      ownership != null ? ownership.setScale(SCALE, ROUNDING_MODE)
          : BigDecimal.ONE.setScale(2);
  }

  /**
   * Get the name.
   * 
   * @return a name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name.
   * 
   * @param name a name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the required capital account.
   * 
   * @return an account; null means the capital account has not yet been set
   */
  public Account getCapitalAccount() {
    return capitalAccount;
  }

  /**
   * Set the required capital account.
   * 
   * @param capitalAccount the capital account
   */
  public void setCapitalAccount(Account capitalAccount) {
    this.capitalAccount = capitalAccount;
  }

  /**
   * Get the optional distribution account.
   * 
   * @return an account, or null if there is no distribution account
   */
  public Account getDistributionAccount() {
    return distributionAccount;
  }

  /**
   * Set the optional distribution account.
   * 
   * @param distributionAccount the distribution account
   */
  public void setDistributionAccount(Account distributionAccount) {
    this.distributionAccount = distributionAccount;
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
   * @param account the name of the account; required
   * @param builder the builder, which should have built the fiscal year, the
   *          accounts, and the transactions before this call; required
   * @return the balance in the named account
   */
  private BigDecimal getAccountBalance(Account account, IBuilder builder) {
    if (account == null) {
      throw new RuntimeException(NO_NAME_ERROR);
    }
    if (builder == null) {
      throw new RuntimeException(NO_BUILDER_ERROR);
    }

    Statement stmt =
      new Statement(builder.getFiscalYear(),
                    "Balance Sheet",
                    StatementType.BALANCE_SHEET);
    return stmt.getAccountBalance(account);
  }

  @Override
  public String toString() {
    return "CapitalEntity [name=" + name + ", ownership=" + ownership
           + ", capitalAccount=" + capitalAccount + ", distributionAccount="
           + distributionAccount + "]";
  }
}
