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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;

/**
 * Implementation of the IStorageManager interface that validates the data using statements
 *
 * @author Robert J. Muller
 */
public abstract class AbstractValidatingStorageManager implements IStorageManager {

  /** logger for this class */
  static final Logger logger = Logger.getLogger(AbstractValidatingStorageManager.class);

  /** title for income statement */
  private static final String INCOME_STATEMENT = "Income Statement";

  /** title for balance sheet */
  private static final String BALANCE_SHEET = "Balance Sheet";

  // messages

  private static final String BALANCE_ERROR = "Statements don't balance for year ";
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
      Statement balanceSheet = new Statement(year, year.getYear() + " " + BALANCE_SHEET,
                                             Statement.StatementType.BALANCE_SHEET);
      String formattedEnd = format.format(year.getEnd());
      Statement incomeStatement = new Statement(year, INCOME_STATEMENT + " " + formattedEnd,
                                                Statement.StatementType.INCOME_STATEMENT);
      BigDecimal balanceSheetBalance = balanceSheet.getBalance();
      BigDecimal incomeStatementBalance = incomeStatement.getBalance();
      BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
      logger.debug(
        "Validating " + year.getYear() + " balance sheet balance: " + balanceSheetBalance);
      logger.debug(
        "Validating " + year.getYear() + " income statement balance: " + incomeStatementBalance);
      BigDecimal systemBalance = balanceSheetBalance.add(incomeStatementBalance);
      if (systemBalance.compareTo(zero) != 0) {
        valid = Boolean.FALSE;
        logger.error(BALANCE_ERROR + year);
        break;
      }
    }
    return valid;
  }
}