/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;


/**
 * Production implementation of the IStorageManager interface
 * 
 * @author Robert J. Muller
 */
public class StorageManager implements IStorageManager {
  private static final Logger logger = Logger.getLogger(StorageManager.class);

  @Override
  public Boolean validate(List<FiscalYear> years) {
    if (years == null || years.size() == 0) {
      throw new InvalidParametersException("no years to validate");
    }
    Boolean valid = Boolean.TRUE;

    // Create the balance sheet and income statement for each year, then
    // compare the balances by addition; if the sum is not zero, there's
    // a validation problem somewhere. This method does not need any exception
    // handling, as there are no possible explicit exceptions, just runtime
    // exceptions like Java heap exhaustion.
    for (FiscalYear year : years) {
      Statement balanceSheet =
        new Statement(year,
                      "Balance Sheet",
                      Statement.StatementType.BALANCE_SHEET);
      Statement incomeStatement =
        new Statement(year,
                      "Income Statement",
                      Statement.StatementType.INCOME_STATEMENT);
      Double balanceSheetBalance = balanceSheet.getBalance();
      Double incomeStatementBalance = incomeStatement.getBalance();
      logger.debug("Validating " + year.getYear() + " balance sheet balance: "
                   + balanceSheetBalance);
      logger.debug("Validating " + year.getYear()
                   + " income statement balance: " + incomeStatementBalance);
      if (balanceSheetBalance + incomeStatementBalance != 0.00D) {
        valid = Boolean.FALSE;
        break;
      }
    }
    return valid;
  }

  @Override
  public void store(String entityName, List<FiscalYear> years,
                    IDataAccessService storageService) {
    Set<Transaction> transactions = new HashSet<Transaction>();

    try {
      // Store the entity, fiscal years, account groups, and accounts.
      storageService.storeEntity(entityName, years);
      // Build a complete set of transactions for all the years.
      for (FiscalYear year : years) {
        transactions.addAll(year.getTransactions());
      }
      // Store all the transactions in one committed transaction.
      storageService.storeTransactions(transactions);
    } catch (Throwable e) {
      // Pass on exception with store failed message
      throw new RuntimeException("exception in fiscal year storage operation",
                                 e);
    }
  }
}
