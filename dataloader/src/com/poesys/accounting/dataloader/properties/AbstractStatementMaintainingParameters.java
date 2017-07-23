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

  /** map of balance sheet data sets indexed by year */
  protected Map<Integer, String> balanceSheets = new HashMap<Integer, String>();
  /** map of income statement data sets indexed by year */
  protected Map<Integer, String> incomeStatements =
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
  public Writer getIncomeStatementWriter() {
    return incomeStatementWriter;
  }

  @Override
  public String getBalanceSheetData(int year) {
    return balanceSheets.toString();
  }

  @Override
  public String getIncomeStatementData(int year) {
    return incomeStatements.toString();
  }
}