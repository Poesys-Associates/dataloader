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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * CUT: AccountType
 *
 * @author muller
 */
public class AccountTypeTest {
  /**
   * Test toString() method for all valid types
   *
   * @throws Exception
   */
  @Test
  public void toStringTest() throws Exception {
    AccountType.ASSETS.toString().equals(AccountType.Constants.ASSETS_VALUE);
    AccountType.LIABILITIES.toString().equals(AccountType.Constants.LIAB_VALUE);
    AccountType.EQUITY.toString().equals(AccountType.Constants.EQUITY_VALUE);
    AccountType.INCOME.toString().equals(AccountType.Constants.INCOME_VALUE);
    AccountType.EXPENSES.toString().equals(AccountType.Constants.EXPENSES_VALUE);
  }

  /**
   * Test databaseValueOf() method for all valid types
   *
   * @throws Exception
   */
  @Test
  public void databaseValueOfTest() throws Exception {
    AccountType.databaseValueOf(AccountType.Constants.ASSETS_VALUE).equals(AccountType.ASSETS);
    AccountType.databaseValueOf(AccountType.Constants.LIAB_VALUE).equals(AccountType.LIABILITIES);
    AccountType.databaseValueOf(AccountType.Constants.EQUITY_VALUE).equals(AccountType.EQUITY);
    AccountType.databaseValueOf(AccountType.Constants.INCOME_VALUE).equals(AccountType.INCOME);
    AccountType.databaseValueOf(AccountType.Constants.EXPENSES_VALUE).equals(AccountType.EXPENSES);
  }

  /**
   * Test databaseValueOf() method for all valid types
   *
   * @throws Exception
   */
  @Test
  public void databaseValueOfUnknownTypeTest() throws Exception {
    AccountType type = AccountType.databaseValueOf("UNKNOWN");
    assertTrue("Account type from uknown type string is not null", type == null);
  }
}