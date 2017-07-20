/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.oldaccounting;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.poesys.accounting.dataloader.IBuilder;
import com.poesys.accounting.dataloader.newaccounting.FiscalYear;
import com.poesys.accounting.dataloader.properties.IParameters;
import com.poesys.db.InvalidParametersException;


/**
 * Production implementation of the IBuilder interface that loads data from a
 * designated path for a list of fiscal years.
 * 
 * @author Robert J. Muller
 */
public class OldDataBuilder implements IBuilder {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(OldDataBuilder.class);
  /** the program parameters object */
  private final IParameters parameters;
  /** the fiscal year being built */
  private FiscalYear fiscalYear;

  // operational constants
  /** limit on number of rows from file, prevents infinite loop */
  private static final int LIMIT = 5000;
  /**
   * integer id for the start of the integer id series for balance transactions,
   * because the old accounting system doesn't actually have transaction ids for
   * these transactions
   */
  private static final String FIRST_BALANCE_TRANSACTION_ID = "10000";

  // Data sets shared across process instances
  /** the shared set of account groups */
  private final Set<com.poesys.accounting.dataloader.newaccounting.AccountGroup> groups =
    new HashSet<com.poesys.accounting.dataloader.newaccounting.AccountGroup>();
  /** the shared set of accounts */
  private final Set<com.poesys.accounting.dataloader.newaccounting.Account> accounts =
    new HashSet<com.poesys.accounting.dataloader.newaccounting.Account>();

  // Messages
  private static final String FILE_READER_CLOSE_ERROR =
    "IO exception closing file reader";
  private static final String READER_CLOSE_ERROR =
    "IO exception closing buffered reader";
  private static final String GROUP_LOOKUP_ERROR =
    "cannot find group for account ";
  private static final String NULL_PARAMETER_ERROR =
    "parameters required but are null";
  private static final String NO_SUCH_TRANSACTION_ERROR =
    "no such transaction ";
  private static final String NO_SUCH_ACCOUNT_ERROR = "no such account ";
  private static final String FISCAL_YEAR_ERROR = " in fiscal year ";
  private static final String INVALID_TRANSACTION_ERROR =
    "invalid transaction: ";
  private static final String INVALID_TRANSACTIONS_ERROR =
    "invalid transactions";

  // lookup maps

  /**
   * lookup map that translates account numbers to new-accounting account group;
   * the account numbers are integers, e.g., 100.1 becomes 10010, 109.99 becomes
   * 10999; this avoids problems with floating point representation and rounding
   * errors for comparisons.
   */
  private final Map<Integer, com.poesys.accounting.dataloader.newaccounting.AccountGroup> groupMap =
    new HashMap<Integer, com.poesys.accounting.dataloader.newaccounting.AccountGroup>();

  /**
   * lookup map that translates account numbers to new-accounting account names;
   * permits mapping of incoming accounts for the current fiscal year to
   * existing accounts from prior fiscal years by name mapping
   */
  private final Map<Integer, String> accountMap =
    new HashMap<Integer, String>();

  /**
   * lookup map that translates account numbers to new accounts; this provides a
   * way to get the account name for balance transactions to use as a
   * transaction description; cleared by buildFiscalYear()
   */
  private final Map<Integer, com.poesys.accounting.dataloader.newaccounting.Account> accountNumberMap =
    new HashMap<Integer, com.poesys.accounting.dataloader.newaccounting.Account>();

  /**
   * lookup map that translates account names to new accounts; this provides a
   * way to get the account object based on the name; cleared by
   * buildFiscalYear()
   */
  private final Map<String, com.poesys.accounting.dataloader.newaccounting.Account> accountNameMap =
    new HashMap<String, com.poesys.accounting.dataloader.newaccounting.Account>();

  /**
   * lookup map that translates an old transaction id into a new-accounting
   * transaction object; this provides a way to map items to transactions;
   * cleared by buildFiscalYear()
   */
  private final Map<Integer, com.poesys.accounting.dataloader.newaccounting.Transaction> transactionMap =
    new HashMap<Integer, com.poesys.accounting.dataloader.newaccounting.Transaction>();

  /**
   * the shared lookup map of fiscal year maps of receivable items indexed by
   * year, then transaction id (that is, Map(year, Map(tranId, item)); the
   * buildFiscalYear() method initializes the nested map for the year
   */
  private final Map<Integer, Map<Integer, com.poesys.accounting.dataloader.newaccounting.Item>> receivables =
    new HashMap<Integer, Map<Integer, com.poesys.accounting.dataloader.newaccounting.Item>>();
  /**
   * the shared lookup map of fiscal year maps of reimbursing items indexed by
   * year, then transaction id (that is, Map(year, Map(tranId, item)); the
   * buildFiscalYear() method initializes the nested map for the year
   */
  private final Map<Integer, Map<Integer, com.poesys.accounting.dataloader.newaccounting.Item>> reimbursements =
    new HashMap<Integer, Map<Integer, com.poesys.accounting.dataloader.newaccounting.Item>>();

  // sets of the data read directly; cleared by buildFiscalYear()

  private final Set<Receivable> receivableDataSet = new HashSet<Receivable>();
  private final Set<Reimbursement> reimbursementDataSet =
    new HashSet<Reimbursement>();
  private final Set<Balance> balanceDataSet = new HashSet<Balance>();
  private final Set<Transaction> transactionDataSet =
    new HashSet<Transaction>();
  private final Set<Item> itemDataSet = new HashSet<Item>();

  /**
   * Strategy pattern that allows the Builder to share the basic file-reading
   * code, putting the construction-specific work into each strategy class. Each
   * strategy builds all the data sets that can be built a line at a time. The
   * interface is publicly accessible for unit testing.
   */
  public interface IBuildStrategy {
    /**
     * Strategy execution method, builds the target DTOs using the input data
     * from the reader
     * 
     * @param r the reader pointing to input data
     */
    void build(BufferedReader r);
  }

  private class AccountGroupStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      AccountGroup group = new AccountGroup(fiscalYear.getYear(), r);
      // Create new group, add to set
      com.poesys.accounting.dataloader.newaccounting.AccountGroup newGroup =
        new com.poesys.accounting.dataloader.newaccounting.AccountGroup(group.getName());
      if (groups.add(newGroup)) {
        // Added group, so map new group range of accounts to group; use integer
        // values to avoid numerical representation problems
        Integer start = new Float(group.getStart() * 100F).intValue();
        Integer end = new Float(group.getEnd() * 100F).intValue();
        for (Integer accountNumber = start; accountNumber <= end; accountNumber++) {
          // logger.debug("Putting account " + accountNumber + " in group "
          // + newGroup.getName());
          groupMap.put(accountNumber, newGroup);
        }
      }
    }
  }

  private class AccountMapStrategy implements IBuildStrategy {

    @Override
    public void build(BufferedReader r) {
      // Read the map data and store in the map.
      AccountMap map = new AccountMap(r);
      // Convert account number from float to int, e.g., 100.21 -> 10021.
      Integer accountNumber =
        new Float(map.getAccountNumber() * 100F).intValue();
      accountMap.put(accountNumber, map.getName());
    }
  }

  /**
   * Accounts in the old system had unique account numbers with two parts
   * separated by a decimal point, which represented the main account and
   * sub-accounts. These accounts were distinct for each fiscal year, and this
   * introduced the need to map from account number to account number across
   * fiscal years for balance sheet accounts. It also made comparing accounts
   * across years impossible. The new accounting system dispenses with the
   * account numbers and the concept of sub-accounts and just identifies an
   * account by its name, and the account may be active or inactive in a given
   * fiscal year but applies to all fiscal years. This requires using a mapping
   * file to determine the account mapping. At the end of the account build()
   * process step, the accounts set reflects the unified set of accounts across
   * all fiscal years read so far. The account map, which maps account numbers
   * to new accounts, represents the accounts in the current fiscal year. Any
   * account in the accounts set that is not mapped in the account map is not
   * active in the current fiscal year, meaning there is no FiscalYearAccount
   * link between the account and the FiscalYear in the database.
   */
  private class AccountStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      Account account = new Account(fiscalYear.getYear(), r);
      // Convert account number to 5-digit integer for comparisons
      Integer accountNumber =
        new Float(account.getAccountNumber() * 100F).intValue();
      // Determine whether the account is a receivable account.
      Boolean receivable = accountNumber >= 11000 && accountNumber < 12000;
      // Get the account group for the account.
      com.poesys.accounting.dataloader.newaccounting.AccountGroup group =
        groupMap.get(accountNumber);
      if (group == null) {
        throw new RuntimeException(GROUP_LOOKUP_ERROR + account);
      }
      // Get the account name using the account map. This mapping links the
      // account to an account created in an earlier fiscal year or sets the
      // name to a "standardized" name rather than to the old name.
      String name = accountMap.get(accountNumber);
      // If not mapped, use the input name.
      name = (name == null ? account.getName() : name);

      // Create new account, add to set
      com.poesys.accounting.dataloader.newaccounting.Account newAccount =
        new com.poesys.accounting.dataloader.newaccounting.Account(name,
                                                                   name,
                                                                   account.getAccountType(),
                                                                   account.getDefaultDebit(),
                                                                   receivable,
                                                                   group);
      if (!accounts.add(newAccount)) {
        // The account is already in the accounts set, so get that account.
        newAccount = accountNameMap.get(name);
      } else {
        // The account is new, so add it to the name map. The OldDataBuilder
        // preserves the accounts set and the account name map over multiple
        // fiscal years.
        accountNameMap.put(name, newAccount);
      }

      // Index the account in the fiscal year account-number lookup map. The
      // getFiscalYear() method clears this map for each fiscal year, so this
      // method needs to add the account with the account number for this year
      // regardless of whether a previous year created the shared new-accounting
      // account.
      accountNumberMap.put(accountNumber, newAccount);

      // Add the account to the fiscal year. This links the account to the
      // fiscal year by creating a linking object in the database on store().
      fiscalYear.addAccount(newAccount);
    }
  }

  private class BalanceStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      Balance balance = new Balance(fiscalYear.getYear(), r);
      balanceDataSet.add(balance);
    }
  }

  private class ReimbursementStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      Reimbursement reimbursement = new Reimbursement(fiscalYear.getYear(), r);
      reimbursementDataSet.add(reimbursement);
    }
  }

  private class TransactionStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      Transaction transaction = new Transaction(fiscalYear.getYear(), r);
      transactionDataSet.add(transaction);
    }
  }

  private class ItemStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      Item item = new Item(fiscalYear.getYear(), r);
      itemDataSet.add(item);
    }
  }

  /**
   * Create a OldDataBuilder object. The parameters object must contain a path
   * to a directory containing one sub-directory for each fiscal year, no gaps
   * allowed, and must provide a series of readers with the correct data
   * characteristics. The fiscal year contains all the relevant data that the
   * client can use to create the accounting system. Building a fiscal year
   * clears all the data sets not shared between years, so the client must call
   * all the process steps for a given fiscal year, then get the fiscal year
   * object. Use the appropriate getters to get the shared data sets.
   * 
   * @param parameters the program parameters object (path and file names)
   */
  public OldDataBuilder(IParameters parameters) {
    this.parameters = parameters;
    if (parameters.getPath() == null) {
      throw new InvalidParametersException("Null year or path for old data builder");
    }
  }

  @Override
  public void buildFiscalYear(Integer year) {
    this.fiscalYear = new FiscalYear(year);

    logger.debug("Building fiscal year " + year);

    // Add the sub-map for the receivables.
    receivables.put(year,
                    new HashMap<Integer, com.poesys.accounting.dataloader.newaccounting.Item>());

    // Add the sub-map for the reimbursements.
    reimbursements.put(year,
                       new HashMap<Integer, com.poesys.accounting.dataloader.newaccounting.Item>());

    // Clear the temporary data sets for the fiscal year.
    receivableDataSet.clear();
    reimbursementDataSet.clear();
    balanceDataSet.clear();
    transactionDataSet.clear();
    itemDataSet.clear();

    // Clear the lookup maps for accounts by account number and transactions by
    // transaction id, as those values vary by fiscal year. Note that this
    // method does not clear the accounts by account name lookup map, which uses
    // the new-accounting name that does not vary by fiscal year.
    accountNumberMap.clear();
    transactionMap.clear();
  }

  /**
   * Read a specified file into the corresponding data structures using the
   * appropriate IBuildStrategy object. The file must have fewer than 5,000 rows
   * of data, which prevents infinite loops. This method has public access to
   * allow direct unit testing, as it is the critical section of code in the
   * class.
   * 
   * @param reader the input data reader
   * @param strategy the build strategy object
   */
  public void readFile(Reader reader, IBuildStrategy strategy) {
    BufferedReader r = null;

    if (reader == null || strategy == null) {
      throw new InvalidParametersException(NULL_PARAMETER_ERROR);
    }

    try {
      r = new BufferedReader(reader);
      // iterate building objects until end-of-stream exception or hard-coded
      // LIMIT is reached; the latter approach prevents an infinite loop from
      // buggy readers
      for (int i = 0; i < LIMIT; i++) {
        strategy.build(r);
      }
    } catch (EndOfStream e) {
      // reached end of file, ready to close
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          // log and ignore
          logger.error(FILE_READER_CLOSE_ERROR, e);
        }
      }
      if (r != null) {
        try {
          r.close();
        } catch (IOException e) {
          // log and ignore
          logger.error(READER_CLOSE_ERROR, e);
        }
      }
    }
  }

  @Override
  public void buildAccountGroups() {
    IBuildStrategy strategy = new AccountGroupStrategy();
    readFile(parameters.getAccountGroupReader(fiscalYear.getYear()), strategy);
  }

  @Override
  public void buildAccountMap() {
    IBuildStrategy strategy = new AccountMapStrategy();
    readFile(parameters.getAccountMapReader(fiscalYear.getYear()), strategy);
  }

  @Override
  public void buildAccounts() {
    IBuildStrategy strategy = new AccountStrategy();
    readFile(parameters.getAccountReader(fiscalYear.getYear()), strategy);
  }

  @Override
  public void buildBalances() {
    IBuildStrategy strategy = new BalanceStrategy();
    readFile(parameters.getBalanceReader(fiscalYear.getYear()), strategy);
    if (!balanceDataSet.isEmpty()) {
      buildBalanceTransactions();
    }
  }

  /**
   * Build the balance transactions and put them into the transaction set in the
   * fiscal year. The method creates one balance transaction per account
   * balance, with one item.
   */
  private void buildBalanceTransactions() {
    // Use a special, large transaction id for the balance transactions. This id
    // must be larger than any fiscal year transaction id in the old accounting
    // system.
    BigInteger id = new BigInteger(FIRST_BALANCE_TRANSACTION_ID);

    for (Balance balance : balanceDataSet) {
      com.poesys.accounting.dataloader.newaccounting.Account account =
        getAccountFromBalance(balance);
      // Create a new-accounting balance transaction (balance flag is true).
      com.poesys.accounting.dataloader.newaccounting.Transaction transaction =
        new com.poesys.accounting.dataloader.newaccounting.Transaction(id,
                                                                       account.getName(),
                                                                       fiscalYear.getStart(),
                                                                       false,
                                                                       true);
      // add the item to the transaction
      transaction.addItem(balance.getAmount(),
                          account,
                          balance.isDebit(),
                          false);
      // validate the transaction
      if (!transaction.isValid()) {
        throw new RuntimeException(INVALID_TRANSACTION_ERROR + transaction);
      }
      // add the transaction to the fiscal year
      fiscalYear.addTransaction(transaction);
    }
  }

  /**
   * Given a specified balance, get the new accounting system account for the
   * balance by looking up the balance account number in the account map created
   * earlier in the build process.
   * 
   * @param balance the balance for which to get the account
   * @return the new accounting system account
   */
  private com.poesys.accounting.dataloader.newaccounting.Account getAccountFromBalance(Balance balance) {
    Integer accountNumber =
      new Float(balance.getAccountNumber() * 100F).intValue();
    return accountNumberMap.get(accountNumber);
  }

  @Override
  public void buildTransactions() {
    readTransactionsAndItems();
    createTransactions();
    createItems();
    if (!validateTransactions()) {
      throw new RuntimeException(INVALID_TRANSACTIONS_ERROR);
    }
  }

  /**
   * Read the transactions and items from the old-accounting data with the
   * appropriate strategies. Items are children of transactions, so the loader
   * needs to load both together.
   */
  private void readTransactionsAndItems() {
    // Read the transaction, item, and reimbursement files.
    IBuildStrategy strategy = new TransactionStrategy();
    readFile(parameters.getTransactionReader(fiscalYear.getYear()), strategy);
    strategy = new ItemStrategy();
    readFile(parameters.getItemReader(fiscalYear.getYear()), strategy);
  }

  /**
   * Iterate through the transaction data set and create all the new-accounting
   * transactions, adding them to the transaction lookup map indexed by
   * transaction id.
   */
  private void createTransactions() {
    // Iterate through transactions and create new-accounting transactions
    for (Transaction transaction : transactionDataSet) {
      BigInteger id = new BigInteger(transaction.getTransactionId().toString());
      com.poesys.accounting.dataloader.newaccounting.Transaction newTransaction =
        new com.poesys.accounting.dataloader.newaccounting.Transaction(id,
                                                                       transaction.getDescription(),
                                                                       transaction.getTransactionDate(),
                                                                       false,
                                                                       false);
      transactionMap.put(transaction.getTransactionId(), newTransaction);
      // Add the transaction to the fiscal year.
      fiscalYear.addTransaction(newTransaction);
    }
  }

  /**
   * Iterate through the old-accounting items. Look up the transaction in the
   * transaction map. Look up the account in the account map. Add the item to
   * the transaction, which creates the item and associates it to the
   * transaction and account. Throw a runtime exception if the transaction id or
   * the account number is not in the relevant lookup map. Index each
   * receivable-account item either as a receivable (debit) or reimbursement
   * (credit). The buildReimbursements process step that follows will use these
   * lookups along with the reimbursements data set to create reimbursement
   * links.
   */
  private void createItems() {
    for (Item item : itemDataSet) {
      // Get the transaction based on transaction id.
      com.poesys.accounting.dataloader.newaccounting.Transaction transaction =
        transactionMap.get(item.getTransactionId());
      if (transaction == null) {
        throw new RuntimeException(NO_SUCH_TRANSACTION_ERROR
                                   + item.getTransactionId()
                                   + FISCAL_YEAR_ERROR + fiscalYear.getYear());
      }
      // Convert account number to 5-digit integer for comparisons, then look up
      // the account with the integer.
      Integer accountNumber =
        new Float(item.getAccountNumber() * 100F).intValue();
      com.poesys.accounting.dataloader.newaccounting.Account account =
        accountNumberMap.get(accountNumber);
      if (account == null) {
        throw new RuntimeException(NO_SUCH_ACCOUNT_ERROR
                                   + item.getAccountNumber()
                                   + FISCAL_YEAR_ERROR + fiscalYear.getYear());
      }

      com.poesys.accounting.dataloader.newaccounting.Item newItem =
        transaction.addItem(item.getAmount(),
                            account,
                            item.isDebit(),
                            item.isChecked());

      // Index the item in receivables or reimbursements as required.
      if (account.isReceivable() && item.isDebit()) {
        // Receivable item, add to map indexed by year and transaction id
        logger.debug("Adding receivable item to receivables map: "
                     + fiscalYear.getYear() + " - " + item.getTransactionId());
        receivables.get(fiscalYear.getYear()).put(item.getTransactionId(),
                                                  newItem);
      } else if (account.isReceivable() && !item.isDebit()) {
        // Reimbursing item, add to map indexed by year and transaction id
        logger.debug("Adding reimbursing item to reimbursements map: "
                     + fiscalYear.getYear() + " - " + item.getTransactionId());
        reimbursements.get(fiscalYear.getYear()).put(item.getTransactionId(),
                                                     newItem);
      }
    }
  }

  /**
   * Validate all the transactions currently in the transaction lookup map. If
   * there are invalid transactions, log them as errors. Log all the invalid
   * transactions. If there is at least one invalid transaction, return false;
   * otherwise, return true.
   * 
   * @return false if there is at least one invalid transaction; otherwise, true
   */
  private boolean validateTransactions() {
    boolean valid = true;
    for (com.poesys.accounting.dataloader.newaccounting.Transaction transaction : transactionMap.values()) {
      logger.error(INVALID_TRANSACTION_ERROR + transaction);
      if (valid) {
        // set flag to invalid if valid only, thus never resets to valid state
        valid = false;
      }
    }
    return valid;
  }

  @Override
  public void buildReimbursements() {
    IBuildStrategy strategy = new ReimbursementStrategy();
    readFile(parameters.getReimbursementReader(fiscalYear.getYear()), strategy);

    // Iterate through reimbursements. Use the shared item lookup maps to get
    // the new-accounting items to link, then reimburse the receivable items.
    for (Reimbursement reimbursement : reimbursementDataSet) {
      Integer receivableYear = reimbursement.getReceivableYear();
      Integer receivableId = reimbursement.getReceivableTransactionId();
      com.poesys.accounting.dataloader.newaccounting.Item receivableItem =
        lookupReceivableItem(receivableYear, receivableId);
      Integer reimbursementYear = reimbursement.getReimbursementYear();
      Integer reimbursementId = reimbursement.getReimbursementTransactionId();
      com.poesys.accounting.dataloader.newaccounting.Item reimbursementItem =
        lookupReimbursingItem(reimbursementYear, reimbursementId);

      // if both there, link the items.
      if (receivableItem != null && reimbursementItem != null) {
        receivableItem.reimburse(reimbursementItem,
                                 reimbursement.getReimbursedAmount(),
                                 reimbursement.getAllocatedAmount());
      } else if (receivableItem == null) {
        // The receivable wasn't found, warn but ignore.
        logger.warn("Reimbursing transaction id " + reimbursementId
                    + " reimburses year " + reimbursementYear + " id "
                    + receivableId + " but " + receivableId
                    + " was not found in " + receivableYear
                    + " as a receivable.");
      } else {
        // The reimbursing item wasn't found, warn but ignore.
        logger.warn("Reimbursing transaction id " + reimbursementId
                    + " reimburses year " + receivableYear + " id "
                    + receivableId + " but " + reimbursementId
                    + " was not found in " + reimbursementYear
                    + " as a reimbursement.");
      }
    }
  }

  /**
   * Look up a receivable item in the receivables map.
   * 
   * @param year the fiscal year of the item
   * @param id the transaction id of the item
   * @return the receivable item
   */
  private com.poesys.accounting.dataloader.newaccounting.Item lookupReceivableItem(Integer year,
                                                                                   Integer id) {
    logger.debug("Looking up receivable item for reimbursement: " + year
                 + " - " + id);
    Map<Integer, com.poesys.accounting.dataloader.newaccounting.Item> items =
      receivables.get(year);
    if (items == null) {
      throw new RuntimeException("no receivables item map for fiscal year "
                                 + year);
    }
    com.poesys.accounting.dataloader.newaccounting.Item receivableItem =
      items.get(id);
    return receivableItem;
  }

  /**
   * Look up a reimbursing item in the reimbursements map.
   * 
   * @param year the fiscal year of the item
   * @param id the transaction id of the item
   * @return the reimbursing item
   */
  private com.poesys.accounting.dataloader.newaccounting.Item lookupReimbursingItem(Integer year,
                                                                                    Integer id) {
    logger.debug("Looking up reimbursing item for receivable: " + year + " - "
                 + id);
    Map<Integer, com.poesys.accounting.dataloader.newaccounting.Item> items =
      reimbursements.get(year);
    if (items == null) {
      throw new RuntimeException("no reimbursements item map for fiscal year "
                                 + year);
    }
    com.poesys.accounting.dataloader.newaccounting.Item receivableItem =
      items.get(id);
    return receivableItem;
  }

  @Override
  public String getPath() {
    return parameters.getPath();
  }

  @Override
  public com.poesys.accounting.dataloader.newaccounting.FiscalYear getFiscalYear() {
    return fiscalYear;
  }

  @Override
  public Set<com.poesys.accounting.dataloader.newaccounting.AccountGroup> getAccountGroups() {
    return groups;
  }

  @Override
  public Set<com.poesys.accounting.dataloader.newaccounting.Account> getAccounts() {
    return accounts;
  }

  /**
   * Get the balance data set. This package-access method permits unit testing
   * of the buildBalances() method, which fills in this balance data set.
   * 
   * @return returns the balance data set for unit test validation
   */
  Set<Balance> getBalanceDataSet() {
    return balanceDataSet;
  }
}
