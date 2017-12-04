/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader;


import java.io.Reader;
import java.util.Set;

import com.poesys.accounting.dataloader.newaccounting.Account;
import com.poesys.accounting.dataloader.newaccounting.AccountGroup;
import com.poesys.accounting.dataloader.newaccounting.CapitalStructure;
import com.poesys.accounting.dataloader.newaccounting.FiscalYear;
import com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder.IBuildStrategy;


/**
 * The Builder component of a Builder pattern system that builds a "new"
 * accounting system for a specific fiscal year with a series of process steps.
 * Implementations may load old accounting data from disk files or may provide
 * unit test data created in memory.
 * 
 * @author Robert J. Muller
 */
public interface IBuilder {
  /**
   * Build a fiscal year object based on a specified integer year. This is the
   * initial step in the build process, and it clears any existing year-based
   * data to provide a clean build for the specified year. It leaves the shared
   * data structures passed through the constructor arguments, such as account
   * groups, accounts, and receivable-account-item lookups.
   * 
   * @param year the year number
   */
  public void buildFiscalYear(Integer year);

  /**
   * Build a set of account groups. This builds a list of account groups in the
   * current fiscal year and orders them. Depends on buildFiscalYear().
   */
  public void buildAccountGroups();

  /**
   * Build a map of new-accounting account names indexed by current-fiscal-year
   * account number. This map provides a mapping for the current year's accounts
   * identified by account number to the new-accounting accounts identified by
   * name. It also permits mapping account-number changes from year to year for
   * balance sheet accounts. Depends on buildFiscalYear().
   */
  public void buildAccountMap();

  /**
   * Build the set of accounts from input data. Depends on buildAccountMap().
   */
  public void buildAccounts();

  /**
   * Build the set of balance transactions from input data. Call this method
   * only for the first fiscal year; the new accounting system maintains only
   * the initial balances for the entity, not an annual balance. Depends on
   * buildAccounts().
   */
  public void buildBalances();

  /**
   * Build the set of transactions and items from input data. The implementation
   * should throw a RuntimeException if there is any invalid transaction.
   * Depends on buildAccounts().
   */
  public void buildTransactions();

  /**
   * Build the reimbursement links between receivable items and reimbursing
   * items. Depends on buildTransactions(), which creates all the items needed
   * for linking (receivable and reimbursement items).
   */
  public void buildReimbursements();

  /**
   * Get the data file path.
   * 
   * @return a path string
   */
  public String getPath();

  /**
   * Get the fiscal year.
   * 
   * @return the fiscal year
   */
  public FiscalYear getFiscalYear();

  /**
   * Get the accounting system's capital structure. This is available
   * immediately after construction of the builder.
   * 
   * @return the capital structure
   */
  public CapitalStructure getCapitalStructure();

  /**
   * Get the set of account groups.
   * 
   * @param year the fiscal year to get
   * 
   * @return the set of account groups
   */
  Set<AccountGroup> getAccountGroups(FiscalYear year);

  /**
   * Get the set of accounts.
   * 
   * @return the set of accounts
   */
  public Set<Account> getAccounts();

  /**
   * Get an account by looking it up by its name.
   * 
   * @param name the name to look up
   * 
   * @return the account corresponding to the name, or null if there is none by
   *         that name
   */
  public Account getAccountByName(String name);

  /**
   * Internal method to read a file
   * 
   * @param reader the reader pointing to current input data
   * @param strategy the strategy with which to build the DTOs from input data
   */
  void readFile(Reader reader, IBuildStrategy strategy);

}
