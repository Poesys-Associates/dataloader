package com.poesys.accounting.dataloader.newaccounting;


import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;


/**
 * Implementation of the IStorageManager interface that validates the data using
 * statements
 * 
 * @author Robert J. Muller
 */
public abstract class AbstractValidatingStorageManager implements
    IStorageManager {
  /** logger for this class */
  static final Logger logger =
    Logger.getLogger(AbstractValidatingStorageManager.class);

  // messages

  /** statements don't balance */
  static final String BALANCE_ERROR = "Statements don't balance for year ";

  /**
   * Create a AbstractValidatingStorageManager object.
   */
  public AbstractValidatingStorageManager() {
    super();
  }

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
      BigDecimal balanceSheetBalance = balanceSheet.getBalance();
      BigDecimal incomeStatementBalance = incomeStatement.getBalance();
      BigDecimal zero = BigDecimal.ZERO.setScale(2);
      logger.debug("Validating " + year.getYear() + " balance sheet balance: "
                   + balanceSheetBalance);
      logger.debug("Validating " + year.getYear()
                   + " income statement balance: " + incomeStatementBalance);
      BigDecimal systemBalance =
        balanceSheetBalance.add(incomeStatementBalance);
      if (systemBalance.compareTo(zero) != 0) {
        valid = Boolean.FALSE;
        logger.error(BALANCE_ERROR + year);
        break;
      }
    }
    return valid;
  }

}