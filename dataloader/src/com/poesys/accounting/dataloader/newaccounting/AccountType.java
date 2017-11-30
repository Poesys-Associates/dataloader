/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;

/**
 * A set of enumerated values that represent classes of accounts in an
 * accounting system; note that the comparison ordering is specified by the
 * order of the enum values in the declaration.
 * 
 * @author Robert J. Muller
 */
/**
 * The kind of account
 */
public enum AccountType {
  /** a kind of property with a value owned by the accounting entity */
  ASSETS("Assets"),
  /** a kind of debt owned by the accounting entity to another entity */
  LIABILITIES("Liabilities"),
  /**
   * a kind of fund invested by the accounting entity in the business; the
   * difference between value of assets and value of liabilities
   */
  EQUITY("Equity"),
  /** revenues paid to the accounting entity */
  INCOME("Income"),
  /** money paid by the accounting entity to another entity */
  EXPENSES("Expenses");

  /** the string to store the in the database */
  private final String databaseRepresentation;

  /**
   * Create an AccountType object.
   * 
   * @param databaseRepresentation the string to store to the database
   */
  private AccountType(String databaseRepresentation) {
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
   * @return the enum value
   */
  public static AccountType databaseValueOf(String value) {
    switch (value) {
    case "Expenses":
      return EXPENSES;
    case "Income":
      return INCOME;
    case "Equity":
      return EQUITY;
    case "Liabilities":
      return LIABILITIES;
    case "Assets":
      return ASSETS;
    default:
      throw new RuntimeException("Unknown account type: " + value);
    }
  }
}