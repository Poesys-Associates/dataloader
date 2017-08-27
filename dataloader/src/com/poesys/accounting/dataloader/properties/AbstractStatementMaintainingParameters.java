package com.poesys.accounting.dataloader.properties;


import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


/**
 * An abstract superclass implementation of IParameters that implements the
 * shared logic for the writing and maintenance of statements. Most
 * implementations of the interface use this logic.
 * 
 * @author Robert J. Muller
 */
public abstract class AbstractStatementMaintainingParameters implements
    IParameters {
  /** the Writer for balance sheets */
  protected Writer balanceSheetWriter = null;
  /** the writer for income statements */
  protected Writer incomeStatementWriter = null;
  /** the Writer for balance sheet details */
  protected Writer balanceSheetDetailsWriter = null;
  /** the writer for income statement details */
  protected Writer incomeStatementDetailsWriter = null;

  /** map of balance sheet data sets indexed by year */
  protected Map<Integer, String> balanceSheets = new HashMap<Integer, String>();
  /** map of income statement data sets indexed by year */
  protected Map<Integer, String> incomeStatements =
    new HashMap<Integer, String>();
  /** map of balance sheet detail data sets indexed by year */
  protected Map<Integer, String> balanceSheetDetails = new HashMap<Integer, String>();
  /** map of income statement detail data sets indexed by year */
  protected Map<Integer, String> incomeStatementDetails =
    new HashMap<Integer, String>();

  /** current year for writers */
  protected Integer year;

  /**
   * Create an AbstractStatementMaintainingParameters object.
   */
  public AbstractStatementMaintainingParameters() {
    super();
  }

  @Override
  public void closeWriters() {
    try {
      if (balanceSheetWriter != null) {
        balanceSheets.put(year, balanceSheetWriter.toString());
        balanceSheetWriter.close();
        balanceSheetWriter = null;
      }

      if (incomeStatementWriter != null) {
        incomeStatements.put(year, incomeStatementWriter.toString());
        incomeStatementWriter.close();
        incomeStatementWriter = null;
      }
      
      if (balanceSheetDetailsWriter != null) {
        balanceSheetDetails.put(year, balanceSheetDetailsWriter.toString());
        balanceSheetDetailsWriter.close();
        balanceSheetDetailsWriter = null;
      }

      if (incomeStatementDetailsWriter != null) {
        incomeStatementDetails.put(year, incomeStatementDetailsWriter.toString());
        incomeStatementDetailsWriter.close();
        incomeStatementDetailsWriter = null;
      }

      year = null;
    } catch (IOException e) {
      throw new RuntimeException("Exception closing writer", e);
    }
  }

  @Override
  public Writer getBalanceSheetWriter() {
    return balanceSheetWriter;
  }

  @Override
  public String getBalanceSheetData(int year) {
    return balanceSheets.toString();
  }

  @Override
  public Writer getBalanceSheetDetailsWriter() {
    return balanceSheetDetailsWriter;
  }

  @Override
  public String getBalanceSheetDetailsData(int year) {
    return balanceSheetDetails.toString();
  }

  @Override
  public Writer getIncomeStatementWriter() {
    return incomeStatementWriter;
  }

  @Override
  public String getIncomeStatementData(int year) {
    return incomeStatements.toString();
  }

  @Override
  public Writer getIncomeStatementDetailsWriter() {
    return incomeStatementDetailsWriter;
  }

  @Override
  public String getIncomeStatementDetailsData(int year) {
    return incomeStatementDetails.toString();
  }
}