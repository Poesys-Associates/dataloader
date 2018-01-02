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
import java.util.List;
import java.util.Set;

import com.poesys.accounting.dataloader.newaccounting.*;
import com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder.IBuildStrategy;

/**
 * The Builder component of a Builder pattern system that builds a "new" accounting system for a
 * specific fiscal year with a series of process steps. Implementations may load old accounting data
 * from disk files or may provide unit test data created in memory.
 *
 * @author Robert J. Muller
 */
public interface IBuilder {
  /**
   * Build the capital structure of the accounting system from input data. This is the initial step
   * in the build process. It creates the capital entities for the accounting system.
   */
  void buildCapitalStructure();

  /**
   * Build a fiscal year object based on a specified integer year. This step clears any existing
   * year-based data to provide a clean build for the specified year. It leaves the shared data
   * structures passed through the constructor arguments, such as account groups, accounts,
   * transactions, and receivable-account-item lookups.
   *
   * @param year the year number
   */
  void buildFiscalYear(Integer year);

  /**
   * Build a set of account groups. This builds a list of account groups in the current fiscal year
   * and orders them. Depends on buildFiscalYear().
   */
  void buildAccountGroups();

  /**
   * Build a map of new-accounting account names indexed by current-fiscal-year account number. This
   * map provides a mapping for the current year's accounts identified by account number to the
   * new-accounting accounts identified by name. It also permits mapping account-number changes from
   * year to year for balance sheet accounts. Depends on buildFiscalYear().
   */
  void buildAccountMap();

  /**
   * Build the set of accounts from input data. Depends on buildAccountMap().
   */
  void buildAccounts();

  /**
   * Build the set of balance transactions from input data. Call this method only for the first
   * fiscal year; the new accounting system maintains only the initial balances for the entity, not
   * an annual balance. Depends on buildAccounts().
   */
  void buildBalances();

  /**
   * Build the set of transactions and items from input data for the current fiscal year. The method
   * updates the set of transactions with generated transactions as required by the parameterized
   * updater. The method adds all the transactions to the accumulating set of transactions that
   * spans fiscal years. The implementation should throw a RuntimeException if there is any invalid
   * transaction. Depends on buildAccounts().
   */
  void buildTransactions();

  /**
   * Build the reimbursement links between receivable items and reimbursing items. Depends on
   * buildTransactions(), which creates all the items needed for linking (receivable and
   * reimbursement items).
   */
  void buildReimbursements();

  /**
   * Get the data file path.
   *
   * @return a path string
   */
  String getPath();

  /**
   * Get the list of fiscal years built so far.
   *
   * @return the list of all fiscal years
   */
  List<FiscalYear> getFiscalYears();

  /**
   * Get the current fiscal year being built.
   *
   * @return the fiscal year
   */
  FiscalYear getFiscalYear();

  /**
   * Get the accounting system's capital structure. This is available immediately after construction
   * of the builder.
   *
   * @return the capital structure
   */
  CapitalStructure getCapitalStructure();

  /**
   * Get the set of account groups.
   *
   * @param year the fiscal year to get
   * @return the set of account groups
   */
  Set<AccountGroup> getAccountGroups(FiscalYear year);

  /**
   * Get the set of accounts.
   *
   * @return the set of accounts
   */
  Set<Account> getAccounts();

  /**
   * Get an account by looking it up by its name.
   *
   * @param name the name to look up
   * @return the account corresponding to the name, or null if there is none by that name
   */
  Account getAccountByName(String name);

  /**
   * Get the set of transactions built by the builder.
   *
   * @return a set of transactions
   */
  Set<Transaction> getTransactions();

  /**
   * Internal method to read a file
   *
   * @param reader   the reader pointing to current input data
   * @param strategy the strategy with which to build the DTOs from input data
   */
  void readFile(Reader reader, IBuildStrategy strategy);
}
