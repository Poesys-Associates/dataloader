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

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.poesys.accounting.dataloader.IBuilder;
import com.poesys.accounting.dataloader.newaccounting.Statement.StatementType;
import com.poesys.db.InvalidParametersException;

/**
 * Represents a capital entity in a capital structure
 *
 * @author Robert J. Muller
 */
public class CapitalEntity {
  /** the name of the capital entity */
  private String name;
  /** the required capital account for the entity */
  private Account capitalAccount = null;
  /** the required capital account name, used to find the account */
  private String capitalAccountName = null;
  /** the optional distribution account for the entity */
  private Account distributionAccount = null;
  /** the optional distribution account name, used to find the account */
  private String distributionAccountName = null;

  // messages
  private static final String NO_CAPITAL_ACCOUNT_ERROR =
    "no capital account supplied but one is required";
  private static final String NO_DISTRIBUTION_ACCOUNT_ERROR =
    "no distribution account supplied but one is required";
  private static final String CANNOT_RENAME_CAP_ACCOUNT_ERROR =
    "cannot set capital account name when capital account associated";
  private static final String CANNOT_RENAME_DIST_ACCOUNT_ERROR =
    "cannot set distribution account name when distribution account associated";

  /**
   * decimal percentage ownership/allocation (0.5, for example); ownership should always be scaled
   * at 3 decimal digits, permitting one digit of percentage scale (45.8, for example, but not
   * 36.75); the number should follow the HALF_UP rounding convention
   */
  private BigDecimal ownership;
  /** the decimal scale of the ownership percentage */
  public static final int SCALE = 3;
  /** the rounding mode for the ownership percentage */
  private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

  // messages

  private static final String NO_BUILDER_ERROR = "no builder supplied for updating";
  private static final String NO_NAME_ERROR = "no capital entity name but one is required";
  private static final String NO_ACCOUNT_ERROR = "no account supplied but one is required";
  private static final String NO_CAPITAL_ACCOUNT_NAME_ERROR =
    "no capital account name but one is" + " required";

  /**
   * Create a Partner object.
   *
   * @param name                    the name of the capital entity (required)
   * @param capitalAccountName      the name of the capital account (required)
   * @param distributionAccountName the name of the distribution account
   * @param ownership               the decimal percentage ownership of the company for the partner
   *                                (should sum to 1 across all partners); the number will be scaled
   *                                and rounded according to the SCALE and ROUNDING_MODE constants;
   *                                optional and defaults to 1 (100%)
   */
  public CapitalEntity(String name, String capitalAccountName, String distributionAccountName,
                       BigDecimal ownership) {
    if (name == null) {
      throw new InvalidParametersException(CapitalEntity.NO_NAME_ERROR);
    }
    this.name = name;

    if (capitalAccountName == null) {
      throw new InvalidParametersException(CapitalEntity.NO_CAPITAL_ACCOUNT_NAME_ERROR);
    }
    this.capitalAccountName = capitalAccountName;
    this.distributionAccountName = distributionAccountName;
    this.ownership =
      ownership != null ? ownership.setScale(SCALE, ROUNDING_MODE) : BigDecimal.ONE.setScale(2,
                                                                                             ROUNDING_MODE);
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
    if (name == null) {
      throw new InvalidParametersException(NO_NAME_ERROR);
    }
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
   * Set the required capital account. Also sets the account name.
   *
   * @param capitalAccount the capital account
   */
  public void setCapitalAccount(Account capitalAccount) {
    if (capitalAccount == null) {
      throw new InvalidParametersException(NO_CAPITAL_ACCOUNT_ERROR);
    }
    this.capitalAccount = capitalAccount;
    this.capitalAccountName = capitalAccount.getName();
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
   * Set the optional distribution account. Also sets the distribution account name.
   *
   * @param distributionAccount the distribution account
   */
  public void setDistributionAccount(Account distributionAccount) {
    if (distributionAccount == null) {
      throw new InvalidParametersException(NO_DISTRIBUTION_ACCOUNT_ERROR);
    }
    this.distributionAccount = distributionAccount;
    this.distributionAccountName = distributionAccount.getName();
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
   * Set the ownership percentage of the entity as a decimal value (0.5, not 50 for 50%, for
   * example). The method ensures the number is scaled and rounded using the SCALE and ROUNDING_MODE
   * constants.
   *
   * @param ownership the ownership percentage as a decimal percentage
   */
  public void setOwnership(BigDecimal ownership) {
    this.ownership = ownership.setScale(SCALE, ROUNDING_MODE);
  }

  /**
   * Get the balance for a specific fiscal year in the entity's capital account from a builder
   * object, which should have built the accounts and the fiscal year before the client calls this
   * method.
   *
   * @param builder the builder containing the set of accounts and the fiscal year; required
   * @return the balance of the capital account
   */
  BigDecimal getCapitalBalance(IBuilder builder) {
    return getAccountBalance(capitalAccount, builder);
  }

  /**
   * Get the balance for a specific fiscal year in the entity's distribution account from a builder
   * object, which should have built the accounts and the fiscal year before the client calls this
   * method. If there is no distribution account, the method returns null.
   *
   * @param builder the builder containing the set of accounts and the fiscal year; required
   * @return the balance of the distribution account or null if there is no distribution account
   */
  BigDecimal getDistributionBalance(IBuilder builder) {
    BigDecimal balance = null;
    if (distributionAccount != null) {
      balance = getAccountBalance(distributionAccount, builder);
    }
    return balance;
  }

  /**
   * Get the current balance of an account in a specific fiscal year using an IBuilder object that
   * contains the current set of accounts being built. This method depends on the builder having
   * built the complete transaction set for the fiscal year.
   *
   * @param account the name of the account; required
   * @param builder the builder, which should have built the fiscal year, the accounts, and the
   *                transactions before this call; required
   * @return the balance in the named account
   */
  private BigDecimal getAccountBalance(Account account, IBuilder builder) {
    if (account == null) {
      throw new InvalidParametersException(NO_ACCOUNT_ERROR);
    }
    if (builder == null) {
      throw new InvalidParametersException(NO_BUILDER_ERROR);
    }

    Statement stmt =
      new Statement(builder.getFiscalYear(), "Balance Sheet", StatementType.BALANCE_SHEET);
    return stmt.getAccountBalance(account);
  }

  @Override
  public String toString() {
    return "CapitalEntity [name=" + name + ", ownership=" + ownership + ", capitalAccount=" +
           capitalAccount + ", distributionAccount=" + distributionAccount + "]";
  }

  /**
   * Get the name of the capital account.
   *
   * @return an account name
   */
  String getCapitalAccountName() {
    return capitalAccountName;
  }

  /**
   * Set the capital account name. Only valid if there is no capital account; set the account to set
   * the name in that case.
   *
   * @param capitalAccountName a name
   */
  public void setCapitalAccountName(String capitalAccountName) {
    if (capitalAccount != null) {
      throw new InvalidParametersException(CANNOT_RENAME_CAP_ACCOUNT_ERROR);
    }
    this.capitalAccountName = capitalAccountName;
  }

  /**
   * Get the distribution account name.
   *
   * @return an account name
   */
  String getDistributionAccountName() {
    return distributionAccountName;
  }

  /**
   * Set the distribution account name. Only valid if there is no capital account; set the account
   * to set the name in that case.
   *
   * @param distributionAccountName a name
   */
  public void setDistributionAccountName(String distributionAccountName) {
    if (distributionAccount != null) {
      throw new InvalidParametersException(CANNOT_RENAME_DIST_ACCOUNT_ERROR);
    }
    this.distributionAccountName = distributionAccountName;
  }
}
