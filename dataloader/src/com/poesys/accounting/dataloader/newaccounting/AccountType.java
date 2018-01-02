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

import org.apache.log4j.Logger;

/**
 * A set of enumerated values that represent classes of accounts in an accounting system; note that
 * the comparison ordering is specified by the order of the enum values in the declaration.
 *
 * @author Robert J. Muller
 */
public enum AccountType {
  // @formatter:off
  /** a kind of property with a value owned by the accounting entity */
  ASSETS(Constants.ASSETS_VALUE),
  /** a kind of debt owned by the accounting entity to another entity */
  LIABILITIES(Constants.LIAB_VALUE),
  /** a kind of fund invested by the accounting entity in the business; the difference between value of assets and value of liabilities */
  EQUITY(Constants.EQUITY_VALUE),
  /** revenues paid to the accounting entity */
  INCOME(Constants.INCOME_VALUE),
  /** money paid by the accounting entity to another entity */
  EXPENSES(Constants.EXPENSES_VALUE);
  // @formatter:on

  // messages
  private static final String UNKNOWN_TYPE_WARNING = "Unknown account type from database value: ";

  /** logger for the enum type */
  private static final Logger logger = Logger.getLogger(AccountType.class);

  /** the string to store the in the database */
  private final String databaseRepresentation;

  /**
   * Create an AccountType object.
   *
   * @param databaseRepresentation the string to store to the database
   */
  AccountType(String databaseRepresentation) {
    this.databaseRepresentation = databaseRepresentation;
  }

  @Override
  public String toString() {
    return databaseRepresentation;
  }

  /**
   * Get the enum value corresponding to a database string value.
   *
   * @param value the database string
   * @return the enum value or null if there is no such value
   */
  public static AccountType databaseValueOf(String value) {
    switch (value) {
      case Constants.EXPENSES_VALUE:
        return EXPENSES;
      case Constants.INCOME_VALUE:
        return INCOME;
      case Constants.EQUITY_VALUE:
        return EQUITY;
      case Constants.LIAB_VALUE:
        return LIABILITIES;
      case Constants.ASSETS_VALUE:
        return ASSETS;
      default:
        logger.warn(UNKNOWN_TYPE_WARNING + value);
        return null;
    }
  }

  // String constants for database representation
  public static class Constants {
    static final String ASSETS_VALUE = "Assets";
    static final String LIAB_VALUE = "Liabilities";
    static final String EQUITY_VALUE = "Equity";
    static final String INCOME_VALUE = "Income";
    static final String EXPENSES_VALUE = "Expenses";
  }
}