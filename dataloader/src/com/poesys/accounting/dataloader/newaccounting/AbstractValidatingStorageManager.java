package com.poesys.accounting.dataloader.newaccounting;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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

  /** title for income statement */
  private static final String INCOME_STATEMENT = "Income Statement";

  /** title for balance sheet */
  private static final String BALANCE_SHEET = "Balance Sheet";

  // messages

  private static final String BALANCE_ERROR =
    "Statements don't balance for year ";
  private static final String NO_YEARS_TO_VALIDATE = "no years to validate";

  /**
   * Create a AbstractValidatingStorageManager object.
   */
  public AbstractValidatingStorageManager() {
    super();
  }

  @Override
  public Boolean validate(List<FiscalYear> years) {
    if (years == null || years.size() == 0) {
      throw new InvalidParametersException(NO_YEARS_TO_VALIDATE);
    }
    Boolean valid = Boolean.TRUE;
    
    SimpleDateFormat format = new SimpleDateFormat("MM dd, yyyy");

    // Create the balance sheet and income statement for each year, then
    // compare the balances by addition; if the sum is not zero, there's
    // a validation problem somewhere. This method does not need any exception
    // handling, as there are no possible explicit exceptions, just runtime
    // exceptions like Java heap exhaustion.
    for (FiscalYear year : years) {
      Statement balanceSheet =
        new Statement(year,
                      year.getYear() + " " +BALANCE_SHEET,
                      Statement.StatementType.BALANCE_SHEET);
      String formattedEnd = format.format(year.getEnd());
      Statement incomeStatement =
        new Statement(year,
                      INCOME_STATEMENT + " " + formattedEnd,
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