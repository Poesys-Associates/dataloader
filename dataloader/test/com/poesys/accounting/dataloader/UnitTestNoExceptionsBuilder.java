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
package com.poesys.accounting.dataloader;

import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.poesys.accounting.dataloader.newaccounting.*;
import com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder.IBuildStrategy;

/**
 * An implementation of the IBuilder interface suitable for unit testing an IDirector
 * implementation; the process step methods do nothing and throw no exceptions; the object maintains
 * a count of the calls to each build method for validation in the unit test
 *
 * @author Robert J. Muller
 */
public class UnitTestNoExceptionsBuilder implements IBuilder {
  private Integer year;
  private int structureCalls = 0;
  private int yearCalls = 0;
  private int groupCalls = 0;
  private int accountMapCalls = 0;
  private int accountCalls = 0;
  private int reimbursementCalls = 0;
  private int balanceCalls = 0;
  private int transactionCalls = 0;

  /** name of the capital entity */
  private static final String CAP_ENTITY_NAME = "Capital Entity";
  private static final String CAP_ACCOUNT_NAME = "Personal Capital";
  /** name of the income-summary account */
  private static final String INCOME_SUMMARY_ACCOUNT = "Income Summary";

  @Override
  public void buildCapitalStructure() {
    structureCalls++;
  }

  @Override
  public void buildFiscalYear(Integer year) {
    yearCalls++;
    // Set the year data member for later use in getYear().
    this.year = year;
  }

  @Override
  public void buildAccountGroups() {
    groupCalls++;
  }

  @Override
  public void buildAccountMap() {
    accountMapCalls++;
  }

  @Override
  public void buildAccounts() {
    accountCalls++;
  }

  @Override
  public void buildBalances() {
    balanceCalls++;
  }

  @Override
  public void buildTransactions() {
    transactionCalls++;
  }

  @Override
  public void buildReimbursements() {
    reimbursementCalls++;
  }

  @Override
  public String getPath() {
    return "path";
  }

  @Override
  public List<FiscalYear> getFiscalYears() {
    return new ArrayList<>();
  }

  @Override
  public FiscalYear getFiscalYear() {
    // Check that the buildYear() method has been called to set the year.
    if (year == null) {
      throw new RuntimeException("buildYear() not called before getting fiscal year");
    }
    return new FiscalYear(year);
  }

  @Override
  public CapitalStructure getCapitalStructure() {
    CapitalStructure structure = new CapitalStructure(INCOME_SUMMARY_ACCOUNT);
    CapitalEntity entity =
      new CapitalEntity(CAP_ENTITY_NAME, CAP_ACCOUNT_NAME, null, BigDecimal.ONE);
    List<CapitalEntity> entities = new ArrayList<>();
    entities.add(entity);
    structure.addEntities(entities);
    return structure;
  }

  @Override
  public Set<Account> getAccounts() {
    return new HashSet<>();
  }

  @Override
  public Set<AccountGroup> getAccountGroups(FiscalYear year) {
    // Just return an empty set for this test class.
    return new HashSet<>();
  }

  @Override
  public Set<Transaction> getTransactions() {
    return new HashSet<>();
  }

  /**
   * Get the count of calls to buildCapitalStructure().
   *
   * @return a count
   */
  int getStructureCalls() {
    return structureCalls;
  }

  /**
   * Get the count of calls to buildYear().
   *
   * @return a count
   */
  int getYearCalls() {
    return yearCalls;
  }

  /**
   * Get the count of calls to buildAccountGroups().
   *
   * @return a count
   */
  int getGroupCalls() {
    return groupCalls;
  }

  /**
   * Get the count of calls to buildAccountMap().
   *
   * @return a count
   */
  int getAccountMapCalls() {
    return accountMapCalls;
  }

  /**
   * Get the count of calls to buildAccounts().
   *
   * @return a count
   */
  int getAccountCalls() {
    return accountCalls;
  }

  /**
   * Get the count of calls to buildBalances().
   *
   * @return a count
   */
  int getBalanceCalls() {
    return balanceCalls;
  }

  /**
   * Get the count of calls to buildTransactions().
   *
   * @return a count
   */
  int getTransactionCalls() {
    return transactionCalls;
  }

  /**
   * Get the count of calls to buildReimbursements().
   *
   * @return a count
   */
  int getReimbursementCalls() {
    return reimbursementCalls;
  }

  @Override
  public void readFile(Reader reader, IBuildStrategy strategy) {
    // Do nothing, not used
  }

  @Override
  public Account getAccountByName(String name) {
    Account account = null;
    if (name != null) {
      // Return an arbitrary, valid account with the specified name.
      AccountType accountType = AccountType.INCOME;
      account = new Account(name, "The " + name + " account", accountType, false, false);
    }
    return account;
  }
}
