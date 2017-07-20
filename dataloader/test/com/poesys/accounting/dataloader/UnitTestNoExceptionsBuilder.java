/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader;


import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import com.poesys.accounting.dataloader.newaccounting.Account;
import com.poesys.accounting.dataloader.newaccounting.AccountGroup;
import com.poesys.accounting.dataloader.newaccounting.FiscalYear;
import com.poesys.accounting.dataloader.oldaccounting.OldDataBuilder.IBuildStrategy;


/**
 * An implementation of the IBuilder interface suitable for unit testing an
 * IDirector implementation; the process step methods do nothing and throw no
 * exceptions; the object maintains a count of the calls to each build method
 * for validation in the unit test
 * 
 * @author Robert J. Muller
 */
public class UnitTestNoExceptionsBuilder implements IBuilder {
  private Integer year;
  private int yearCalls = 0;
  private int groupCalls = 0;
  private int accountMapCalls = 0;
  private int accountCalls = 0;
  private int reimbursementCalls = 0;
  private int balanceCalls = 0;
  private int transactionCalls = 0;

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
  public FiscalYear getFiscalYear() {
    // Check that the buildYear() method has been called to set the year.
    if (year == null) {
      throw new RuntimeException("buildYear() not called before getting fiscal year");
    }
    return new FiscalYear(year);
  }

  @Override
  public Set<AccountGroup> getAccountGroups() {
    // Just return an empty set.
    return new HashSet<AccountGroup>();
  }

  @Override
  public Set<Account> getAccounts() {
    return new HashSet<Account>();
  }

  /**
   * Get the count of calls to buildYear().
   * 
   * @return a count
   */
  public int getYearCalls() {
    return yearCalls;
  }

  /**
   * Get the count of calls to buildAccountGroups().
   * 
   * @return a count
   */
  public int getGroupCalls() {
    return groupCalls;
  }

  /**
   * Get the count of calls to buildAccountMap().
   * 
   * @return a count
   */
  public int getAccountMapCalls() {
    return accountMapCalls;
  }

  /**
   * Get the count of calls to buildAccounts().
   * 
   * @return a count
   */
  public int getAccountCalls() {
    return accountCalls;
  }

  /**
   * Get the count of calls to buildBalances().
   * 
   * @return a count
   */
  public int getBalanceCalls() {
    return balanceCalls;
  }

  /**
   * Get the count of calls to buildTransactions().
   * 
   * @return a count
   */
  public int getTransactionCalls() {
    return transactionCalls;
  }

  /**
   * Get the count of calls to buildReimbursements().
   * 
   * @return a count
   */
  public int getReimbursementCalls() {
    return reimbursementCalls;
  }

  @Override
  public void readFile(Reader reader, IBuildStrategy strategy) {
    // Do nothing, not used    
  }
}
