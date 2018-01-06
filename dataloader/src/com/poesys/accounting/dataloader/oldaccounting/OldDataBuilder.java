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
package com.poesys.accounting.dataloader.oldaccounting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.poesys.accounting.dataloader.newaccounting.*;
import org.apache.log4j.Logger;

import com.poesys.accounting.dataloader.IBuilder;
import com.poesys.accounting.dataloader.properties.IParameters;
import com.poesys.db.InvalidParametersException;

/**
 * Production implementation of the IBuilder interface that loads data from a designated path for a
 * list of fiscal years.
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
  /** the list of all fiscal years built so far */
  private List<FiscalYear> fiscalYears = new ArrayList<>();

  /** the set of transactions built from the data */
  private final Set<com.poesys.accounting.dataloader.newaccounting.Transaction> transactions =
    new HashSet<>();

  // operational constants
  /** limit on number of rows from file, prevents infinite loop */
  private static final int LIMIT = 10000;

  /**
   * integer id for the start of the integer id series for balance transactions, because the old
   * accounting system doesn't actually have transaction ids for these transactions
   */
  private static final String FIRST_BALANCE_TRANSACTION_ID = "100000";

  // Data sets shared across process iterations
  /** the capital entities for the accounting entity accounts */
  private CapitalStructure capitalStructure;
  /** map of capital entities indexed by capital/distribution account names */
  private Map<String, CapitalEntity> capitalEntityMap = new HashMap<>();
  /** the map of shared sets of account groups indexed by fiscal year */
  private final Map<FiscalYear, Set<com.poesys.accounting.dataloader.newaccounting.AccountGroup>>
    groupSetsMap = new HashMap<>();
  /** the shared set of accounts */
  private final Set<com.poesys.accounting.dataloader.newaccounting.Account> accounts =
    new HashSet<>();

  // Messages
  private static final String ZERO_RECEIVABLE_ID_ERROR =
    "zero receivable id for transaction not in first year";
  private static final String NO_BALANCE_ERROR = "no balance for receivable account ";
  private static final String ZERO_BALANCE_ERROR = "zero balance for account ";
  private static final String ACCOUNT_ERROR = "cannot find account ";
  private static final String NULL_FILE_PATH_ERROR = "Null file path in parameter file";
  private static final String FILE_READER_CLOSE_ERROR = "IO exception closing file reader";
  private static final String READER_CLOSE_ERROR = "IO exception closing buffered reader";
  private static final String GROUP_LOOKUP_ERROR = "cannot find group for account ";
  private static final String NULL_PARAMETER_ERROR = "parameters required but are null";
  private static final String NULL_INCOME_SUMMARY_ERROR =
    "Null income summary account name in parameter file";
  private static final String NO_SUCH_TRANSACTION_ERROR = "no such transaction ";
  private static final String NO_SUCH_ACCOUNT_ERROR = "no such account ";
  private static final String FISCAL_YEAR_ERROR = " in fiscal year ";
  private static final String INVALID_TRANSACTION_ERROR = "invalid transaction: ";
  private static final String INVALID_TRANSACTIONS_ERROR = "invalid transactions";
  private static final String NO_REIMBURSEMENTS_ITEM_MAP_ERROR =
    "no reimbursements item map for fiscal year ";
  private static final String NO_RECEIVABLES_ITEM_MAP_ERROR =
    "no receivables item map for fiscal year ";
  private static final String NOT_RECEIVABLE_ACCOUNT_ERROR =
    "indexing receivable item but account is not a receivable account: ";
  private static final String INVALID_CAPITAL_ACCOUNT =
    "found account name in map but no such account name in capital structure: ";
  private static final String NO_NEW_CAPITAL_ENTITY_NAME_ERROR = "no new capital entity name";
  private static final String NO_CAPITAL_ENTITY_NAME_ERROR = "no capital entity name";
  private static final String NO_ACCOUNT_NAME_ERROR = "no name for lookup of account";

  // lookup maps and index classes

  /**
   * lookup map that contains new-accounting groups indexed by old-accounting year and account
   * intervals
   */
  private final Map<AccountGroup, com.poesys.accounting.dataloader.newaccounting.AccountGroup>
    groupMap = new HashMap<>();

  /** map that organizes groups by account type for numbering */
  private final Map<AccountType, List<AccountGroup>> typeMap = new HashMap<>();

  /**
   * lookup map that translates account numbers to new-accounting account names; permits mapping of
   * incoming accounts for the current fiscal year to existing accounts from prior fiscal years by
   * name mapping
   */
  private final Map<Integer, String> accountMap = new HashMap<>();

  /**
   * lookup map that translates account numbers to new accounts; this provides a way to get the
   * account name for balance transactions to use as a transaction description; cleared by
   * buildFiscalYear()
   */
  private final Map<Integer, com.poesys.accounting.dataloader.newaccounting.Account>
    accountNumberMap = new HashMap<>();

  /**
   * lookup map that translates account names to new accounts; this provides a way to get the
   * account object based on the name; cleared by buildFiscalYear()
   */
  private final Map<String, com.poesys.accounting.dataloader.newaccounting.Account> accountNameMap =
    new HashMap<>();

  /**
   * lookup map that translates an old transaction id into a new-accounting transaction object; this
   * provides a way to map items to transactions; cleared by buildFiscalYear()
   */
  private final Map<Integer, com.poesys.accounting.dataloader.newaccounting.Transaction>
    transactionMap = new HashMap<>();

  /**
   * Map index object that indexes items by a combination of transaction id and account; used in
   * item lookup maps for receivables
   */
  private class ItemIndex {
    private final Integer id;
    private final Float accountNumber;

    /**
     * Create a ItemIndex object.
     *
     * @param id            the transaction id
     * @param accountNumber the account number (float, for example 111.1)
     */
    public ItemIndex(Integer id, Float accountNumber) {
      this.id = id;
      this.accountNumber = accountNumber;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      ItemIndex other = (ItemIndex)obj;
      if (!getOuterType().equals(other.getOuterType())) {
        return false;
      }
      if (accountNumber == null) {
        if (other.accountNumber != null) {
          return false;
        }
      } else if (!accountNumber.equals(other.accountNumber)) {
        return false;
      }
      if (id == null) {
        if (other.id != null) {
          return false;
        }
      } else if (!id.equals(other.id)) {
        return false;
      }
      return true;
    }

    private OldDataBuilder getOuterType() {
      return OldDataBuilder.this;
    }
  }

  /**
   * the shared lookup map of fiscal year maps of receivable items indexed by year, then by the
   * ItemIndex combination of transaction id and account (that is, Map(year, Map(itemIndex, item));
   * the buildFiscalYear() method initializes the nested map for the year
   */
  private final Map<Integer, Map<ItemIndex, com.poesys.accounting.dataloader.newaccounting.Item>>
    receivablesMap = new HashMap<>();

  /**
   * the shared lookup map of fiscal year maps of reimbursing items indexed by year, then by the
   * ItemIndex combination of transaction id and account (that is, Map(year, Map(itemIndex, item));
   * the buildFiscalYear() method initializes the nested map for the year
   */
  private final Map<Integer, Map<ItemIndex, com.poesys.accounting.dataloader.newaccounting.Item>>
    reimbursementsMap = new HashMap<>();

  // list of capital entities read directly
  private final List<CapitalEntity> entities = new ArrayList<>();

  // sets of the data read directly; cleared by buildFiscalYear()

  private final Set<Reimbursement> reimbursementDataSet = new HashSet<>();
  private final Set<Balance> balanceDataSet = new HashSet<>();
  private final Set<Transaction> transactionDataSet = new HashSet<>();
  private final Set<Item> itemDataSet = new HashSet<>();

  /**
   * Strategy pattern that allows the Builder to share the basic file-reading code, putting the
   * construction-specific work into each strategy class. Each strategy builds all the data sets
   * that can be built a line at a time. The interface is publicly accessible for unit testing.
   */
  public interface IBuildStrategy {
    /**
     * Strategy execution method, builds the target DTOs using the input data from the reader
     *
     * @param r the reader pointing to input data
     */
    void build(BufferedReader r);
  }

  /**
   * A capital entity is a named entity that owns some part of the capital of the accounting system.
   * Each system has one or more entities with accompanying capital and distribution accounts.
   */
  private class CapitalEntityStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      CapitalEntity entity = new CapitalEntity(r);
      entities.add(entity);
    }
  }

  /**
   * The system groups accounts into categories (Cash, Credit Cards, and so on) that grouped
   * accounts by number; each group had a range of accounts, always from nn0.00 to nn9.99, so groups
   * had "ten" accounts (plus some number of sub-accounts). The old accounting system had a set of
   * account groups for each year that included all the accounts in the year. The groups could be
   * rearranged from year to year (a group added, a group removed, or a group changed (that is, the
   * account number range would be associated with a different group name. The new system identifies
   * groups within account types ("Assets", "Liabilities" and so on) by name, so names are unique
   * within account type across all years. The Old Data Builder must therefore map groups into
   * new-accounting groups when creating fiscal-year-account links between accounts and fiscal
   * years, which is where groups associate with accounts and years. Also, to get the order number
   * of the group within the account type, the builder builds a data structure containing ordered
   * lists of groups indexed by account type. This operation builds the lists but leaves the
   * ordering and numbering to the client.
   */
  private class AccountGroupStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      AccountGroup group = new AccountGroup(fiscalYear.getYear(), r);
      // Create new group, add to set for fiscal year
      com.poesys.accounting.dataloader.newaccounting.AccountGroup newGroup =
        new com.poesys.accounting.dataloader.newaccounting.AccountGroup(group.getName());
      Set<com.poesys.accounting.dataloader.newaccounting.AccountGroup> groupSet =
        groupSetsMap.computeIfAbsent(fiscalYear, k -> new HashSet<>());
      // No set yet, create it.
      // Add the group to the set.
      groupSet.add(newGroup);
      // Add new group to map indexed by old group
      groupMap.put(group, newGroup);
      // Add the group to the account type.
      AccountType type = group.getAccountType();
      List<AccountGroup> groupList = typeMap.computeIfAbsent(type, k -> new ArrayList<>());
      // no list yet for this type, create one
      // Add the group to the list. Note the list is not sorted at this point.
      groupList.add(group);
    }
  }

  /**
   * The old accounting system was implemented separately for each fiscal year, allowing for changes
   * to the account name and the changing of account numbers from year to year. The account map
   * associates a particular new-accounting name with an account number in a given year and later
   * years. The buildFiscalYear() method does not clear the account map. The data file for a given
   * fiscal year thus contains one row for each account number that represents a specific account
   * shifted to that number from the prior year. Later years will associate that number to that name
   * unless the account map in the subsequent year replaces the name with another name. You only
   * need to set the account number once for all subsequent years.
   */
  private class AccountMapStrategy implements IBuildStrategy {

    @Override
    public void build(BufferedReader r) {
      // Read the map data and store in the map.
      AccountMap map = new AccountMap(r);
      // Convert account number from float to int, e.g., 100.21 -> 10021.
      Integer accountNumber = new Float(map.getAccountNumber() * 100F).intValue();
      accountMap.put(accountNumber, map.getName());
    }
  }

  /**
   * Accounts in the old system had unique account numbers with two parts separated by a decimal
   * point, which represented the main account and sub-accounts. These accounts were distinct for
   * each fiscal year, and this introduced the need to map from account number to account number
   * across fiscal years for balance sheet accounts. It also made comparing accounts across years
   * impossible. The new accounting system dispenses with the account numbers and the concept of
   * sub-accounts and just identifies an account by its name, and the account may be active or
   * inactive in a given fiscal year but applies to all fiscal years. This requires using a mapping
   * file to determine the account mapping. At the end of the account build() process step, the
   * accounts set reflects the unified set of accounts across all fiscal years read so far. The
   * account map, which maps account numbers to new accounts, represents the accounts in the current
   * fiscal year. Any account in the accounts set that is not mapped in the account map is not
   * active in the current fiscal year, meaning there is no FiscalYearAccount link between the
   * account and the FiscalYear in the database.
   */
  private class AccountStrategy implements IBuildStrategy {
    /** Tracks account order number within groups */
    Map<com.poesys.accounting.dataloader.newaccounting.AccountGroup, Integer>
      groupAccountOrderNumbers = new HashMap<>();

    @Override
    public void build(BufferedReader r) {
      Integer year = fiscalYear.getYear();
      Account oldAccount = new Account(fiscalYear.getYear(), r);
      // Convert account number to 5-digit integer for comparisons
      Float accountNumber = oldAccount.getAccountNumber();
      Integer intAccountNumber = new Float(accountNumber * 100F).intValue();
      // Determine whether the account is a receivable account.
      Boolean receivable = intAccountNumber >= 11000 && intAccountNumber < 12000;
      // Get the account group for the account.
      com.poesys.accounting.dataloader.newaccounting.AccountGroup group = null;
      Integer groupOrderNumber = null;
      for (AccountGroup key : groupMap.keySet()) {
        if (key.contains(year, accountNumber)) {
          group = groupMap.get(key);
          groupOrderNumber = key.getOrderNumber();
          break;
        }
      }
      if (group == null || groupOrderNumber == null) {
        throw new RuntimeException(GROUP_LOOKUP_ERROR + oldAccount);
      }
      // Get the account name using the account map. This mapping links the
      // account to an account created in an earlier fiscal year or sets the
      // name to a "standardized" name rather than to the old name.
      String name = accountMap.get(intAccountNumber);
      // If not mapped, use the input name.
      name = (name == null ? oldAccount.getName() : name);

      // Create new account, add to set
      com.poesys.accounting.dataloader.newaccounting.Account newAccount =
        new com.poesys.accounting.dataloader.newaccounting.Account(name, name,
                                                                   oldAccount.getAccountType(),
                                                                   oldAccount.getDefaultDebit(),
                                                                   receivable);
      if (!accounts.add(newAccount)) {
        // The account is already in the accounts set, so get that account.
        newAccount = accountNameMap.get(name);
      } else {
        // The account is new, so add it to the name map. The OldDataBuilder
        // preserves the accounts set and the account name map over multiple
        // fiscal years.
        accountNameMap.put(name, newAccount);
      }

      // Set the capital account; this conditionally sets the capital account if
      // this account is the capital account.
      setCapitalAccount(newAccount);

      Integer accountOrderNumber = incrementAccountOrderNumber(group);

      // Index the account in the fiscal year account-number lookup map. The
      // getFiscalYear() method clears this map for each fiscal year, so this
      // method needs to add the account with the account number for this year
      // regardless of whether a previous year created the shared new-accounting
      // account.
      accountNumberMap.put(intAccountNumber, newAccount);
      logger.debug("Indexed account " + oldAccount.getAccountNumber());

      // Add the account to the fiscal year by creating a FiscalYearAccount link
      // with the associated group. This operation links the account to the
      // fiscal year by creating a linking object in the database on store().
      // The link is put into the three linked objects as well.
      FiscalYearAccount fsYAccount =
        new FiscalYearAccount(fiscalYear, oldAccount.getAccountType(), group, groupOrderNumber,
                              newAccount, accountOrderNumber);
      fiscalYear.addAccount(fsYAccount);
      newAccount.addYear(fsYAccount);
      group.addLink(fsYAccount);
    }

    /**
     * If this account is one of the capital structure accounts, associate the generated capital
     * entity with the account. This links the account to the capital entity and vice versa.
     *
     * @param newAccount the account object
     */
    private void setCapitalAccount(com.poesys.accounting.dataloader.newaccounting.Account
                                     newAccount) {
      String name = newAccount.getName();
      CapitalEntity capEntity = capitalEntityMap.get(name);

      if (capEntity != null) {
        // The account name is in the capital entity map, so this is a capital
        // or distribution account; set the account in the entity in the proper
        // place.

        // Check for the capital entity name, required to get new entity object.
        if (capEntity.getName() == null) {
          throw new RuntimeException(NO_CAPITAL_ENTITY_NAME_ERROR);
        }

        for (com.poesys.accounting.dataloader.newaccounting.CapitalEntity newCapEntity :
          capitalStructure.getEntities()) {
          if (newCapEntity.getName() == null) {
            throw new RuntimeException(NO_NEW_CAPITAL_ENTITY_NAME_ERROR);
          }
          if (newCapEntity.getName().equals(capEntity.getName())) {
            // Found the entity to set with the account.
            if (capEntity.getCapitalAccountName().equals(name)) {
              // This is the capital account for the entity.
              newCapEntity.setCapitalAccount(newAccount);
              newAccount.setCapitalEntity(newCapEntity);
            } else if (capEntity.getDistributionAccountName() != null &&
                       capEntity.getDistributionAccountName().equals(name)) {
              newCapEntity.setDistributionAccount(newAccount);
              newAccount.setCapitalEntity(newCapEntity);
            } else if (capEntity.getDistributionAccountName() != null) {
              // error, neither capital nor distribution account matches
              throw new RuntimeException(INVALID_CAPITAL_ACCOUNT + name);
            } // else do nothing
          }
        }
      }
    }

    /**
     * Increment the account order number within the group.
     *
     * @param group the group containing the account
     * @return the next order number in the group
     */
    private Integer incrementAccountOrderNumber(com.poesys.accounting.dataloader.newaccounting
                                                  .AccountGroup group) {
      Integer accountOrderNumber;
      // Increment the counter for ordering the accounts within the group.
      accountOrderNumber = groupAccountOrderNumbers.get(group);
      if (accountOrderNumber == null) {
        // No count for this group yet, create it as 1.
        accountOrderNumber = 1;
      } else {
        // Order number is there, so increment it.
        accountOrderNumber++;
      }
      // Save the current order number.
      groupAccountOrderNumbers.put(group, accountOrderNumber);
      return accountOrderNumber;
    }
  }

  /**
   * Balances get set in the first fiscal year processed, associating an initial balance with an
   * account. The buildFiscalYear() method does not clear the balances.
   */
  private class BalanceStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      Balance balance = new Balance(fiscalYear.getYear(), r);
      balanceDataSet.add(balance);
    }
  }

  /**
   * Reimbursements link reimbursing transactions to receivable transactions in the current or prior
   * fiscal years. These get cleared for each fiscal year.
   */
  private class ReimbursementStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      Reimbursement reimbursement = new Reimbursement(fiscalYear.getYear(), r);
      reimbursementDataSet.add(reimbursement);
    }
  }

  /**
   * Transactions contain the set of transactions for each fiscal year. These get cleared for each
   * fiscal year.
   */
  private class TransactionStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      Transaction transaction = new Transaction(fiscalYear.getYear(), r);
      transactionDataSet.add(transaction);
    }
  }

  /**
   * Transaction items associate with a transaction by id and an account by number, specifying the
   * debit or credit amount by which the transaction changes the value of the account.These get
   * cleared for each fiscal year.
   *
   * @author Robert J. Muller
   */
  private class ItemStrategy implements IBuildStrategy {
    @Override
    public void build(BufferedReader r) {
      Item item = new Item(fiscalYear.getYear(), r);
      itemDataSet.add(item);
    }
  }

  /**
   * Create a OldDataBuilder object. The parameters object must contain a path to a directory
   * containing one sub-directory for each fiscal year, no gaps allowed, and must provide a series
   * of readers with the correct data characteristics. The fiscal year contains all the relevant
   * data that the client can use to create the accounting system. Building a fiscal year clears all
   * the data sets not shared between years, so the client must call all the process steps for a
   * given fiscal year, then get the fiscal year object. Use the appropriate getters to get the
   * shared data sets.
   *
   * @param parameters the program parameters object (path and file names)
   */
  public OldDataBuilder(IParameters parameters) {
    this.parameters = parameters;
    if (parameters.getPath() == null) {
      throw new InvalidParametersException(NULL_FILE_PATH_ERROR);
    }
  }

  @Override
  public void buildCapitalStructure() {
    // Build the capital entities list for the accounting system.
    String incomeSummary = parameters.getIncomeSummaryAccountName();
    if (incomeSummary == null || incomeSummary.isEmpty()) {
      throw new InvalidParametersException(NULL_INCOME_SUMMARY_ERROR);
    }
    capitalStructure = new CapitalStructure(incomeSummary);
    IBuildStrategy strategy = new CapitalEntityStrategy();
    readFile(parameters.getCapitalEntityReader(), strategy);
    // Build a list of new-accounting capital entities from the old ones and add
    // the account names to the capital entity map.
    List<com.poesys.accounting.dataloader.newaccounting.CapitalEntity> newEntities =
      new ArrayList<>();
    for (CapitalEntity entity : entities) {
      newEntities.add(
        new com.poesys.accounting.dataloader.newaccounting.CapitalEntity(entity.getName(),
                                                                         entity
                                                                           .getCapitalAccountName(),
                                                                         entity
                                                                           .getDistributionAccountName(),
                                                                         new BigDecimal(
                                                                           entity.getOwnership())));
      capitalEntityMap.put(entity.getCapitalAccountName(), entity);
      if (entity.getDistributionAccountName() != null) {
        capitalEntityMap.put(entity.getDistributionAccountName(), entity);
      }
    }
    capitalStructure.addEntities(newEntities);
  }

  @Override
  public void buildFiscalYear(Integer year) {
    this.fiscalYear = new FiscalYear(year);
    fiscalYears.add(this.fiscalYear);

    logger.debug("Building fiscal year " + year);

    // Add the sub-map for the receivables.
    receivablesMap.put(year, new HashMap<>());

    // Add the sub-map for the reimbursements.
    reimbursementsMap.put(year, new HashMap<>());

    // Clear the temporary data sets for the fiscal year.
    reimbursementDataSet.clear();
    balanceDataSet.clear();
    transactionDataSet.clear();
    itemDataSet.clear();

    // Clear the lookup maps for accounts by account number and transactions by transaction id,
    // as those values vary by fiscal year. Note that this method does not clear the accounts by
    // account name lookup map, which uses the new-accounting name that does not vary by fiscal
    // year. Also note that the transaction set itself is not cleared, as it spans the fiscal years.
    accountNumberMap.clear();
    transactionMap.clear();
  }

  /**
   * Read a specified file into the corresponding data structures using the appropriate
   * IBuildStrategy object. The file must have fewer than 5,000 rows of data, which prevents
   * infinite loops. This method has public access to allow direct unit testing, as it is the
   * critical section of code in the class.
   *
   * @param reader   the input data reader
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
    }
    finally {
      try {
        reader.close();
      } catch (IOException e) {
        // log and ignore
        logger.error(FILE_READER_CLOSE_ERROR, e);
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
    // type map is complete, sort the lists and generate order numbers; the
    // AccountGroup objects are also in the groupMap, so the order numbers
    // will be set for the objects in that map as well as the type map.
    for (List<AccountGroup> list : typeMap.values()) {
      // sort the list using compareTo
      Collections.sort(list);
      // generate order numbers starting at 1
      int orderNumber = 1;
      Integer previousYear = null;
      for (AccountGroup group : list) {
        if (previousYear != null && !group.getYear().equals(previousYear)) {
          // year changed, reset counter to 1
          orderNumber = 1;
        }
        group.setOrderNumber(orderNumber);
        previousYear = group.getYear();
        orderNumber++;
      }
    }
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
   * Build the balance transactions and put them into the transaction set in the fiscal year. The
   * method creates one balance transaction per account balance, with one item.
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
        new com.poesys.accounting.dataloader.newaccounting.Transaction(id, account.getName(),
                                                                       fiscalYear.getStart(), false,
                                                                       true);
      // add the item to the transaction
      transaction.addItem(balance.getAmount(), account, balance.isDebit(), false);
      // validate the transaction
      if (!transaction.isValid()) {
        throw new RuntimeException(INVALID_TRANSACTION_ERROR + transaction);
      }
      // add the transaction to set of transactions
      transactions.add(transaction);
      // for receivable accounts, index the balance item
      if (account.isReceivable()) {
        indexReceivableItem(id.intValue(), balance.getAccountNumber(), balance.isDebit(),
                            transaction.getItem(account));
      }
      // increment the transaction id; assign because BigInteger is immutable
      id = id.add(BigInteger.ONE);
    }
  }

  /**
   * Given a specified balance, get the new accounting system account for the balance by looking up
   * the balance account number in the account map created earlier in the build process.
   *
   * @param balance the balance for which to get the account
   * @return the new accounting system account
   */
  private com.poesys.accounting.dataloader.newaccounting.Account getAccountFromBalance(Balance
                                                                                         balance) {
    Integer accountNumber = new Float(balance.getAccountNumber() * 100F).intValue();
    return accountNumberMap.get(accountNumber);
  }

  @Override
  public void buildTransactions() {
    readTransactionsAndItems();
    createTransactions();
    createItems();
    if (!validateTransactions()) {
      throw new RuntimeException(INVALID_TRANSACTIONS_ERROR + " for year " + fiscalYear.getYear());
    }
    // Get the updater if any and update with generated transactions.
    IFiscalYearUpdater updater = parameters.getUpdater();
    if (updater != null) {
      updater.update(fiscalYear, transactions, this);
    }
  }

  /**
   * Read the transactions and items from the old-accounting data with the appropriate strategies.
   * Items are children of transactions, so the loader needs to load both together.
   */
  private void readTransactionsAndItems() {
    // Read the transaction, item, and reimbursement files.
    IBuildStrategy strategy = new TransactionStrategy();
    readFile(parameters.getTransactionReader(fiscalYear.getYear()), strategy);
    strategy = new ItemStrategy();
    readFile(parameters.getItemReader(fiscalYear.getYear()), strategy);
  }

  /**
   * Iterate through the transaction data set and create all the new-accounting transactions, adding
   * them to the transaction lookup map indexed by transaction id.
   */
  private void createTransactions() {
    // Iterate through transactions and create new-accounting transactions
    for (Transaction transaction : transactionDataSet) {
      BigInteger id = new BigInteger(transaction.getTransactionId().toString());
      com.poesys.accounting.dataloader.newaccounting.Transaction newTransaction =
        new com.poesys.accounting.dataloader.newaccounting.Transaction(id,
                                                                       transaction.getDescription(),
                                                                       transaction
                                                                         .getTransactionDate(),
                                                                       false, false);
      // Add the transaction to the transaction map keyed on id.
      transactionMap.put(transaction.getTransactionId(), newTransaction);
      // Add the transaction to the set of transactions.
      transactions.add(newTransaction);
      // Set the fiscal year's "last" id to the current id if it is greater than the current one.
      // At the end of the loop, the fiscal year's id will be the greatest id read from the file.
      fiscalYear.setLastId(id);
    }
  }

  /**
   * Iterate through the old-accounting items. Look up the transaction in the transaction map. Look
   * up the account in the account map. Add the item to the transaction, which creates the item and
   * associates it to the transaction and account. Throw a runtime exception if the transaction id
   * or the account number is not in the relevant lookup map. Index each receivable-account item
   * either as a receivable (debit) or reimbursement (credit). The buildReimbursements process step
   * that follows will use these lookups along with the reimbursements data set to create
   * reimbursement links.
   */
  private void createItems() {
    for (Item item : itemDataSet) {
      // Get the transaction based on transaction id.
      com.poesys.accounting.dataloader.newaccounting.Transaction transaction =
        transactionMap.get(item.getTransactionId());
      if (transaction == null) {
        throw new RuntimeException(
          NO_SUCH_TRANSACTION_ERROR + item.getTransactionId() + FISCAL_YEAR_ERROR +
          fiscalYear.getYear());
      }
      // Convert account number to 5-digit integer for comparisons, then look up
      // the account with the integer.
      Integer accountNumber = new Float(item.getAccountNumber() * 100F).intValue();
      com.poesys.accounting.dataloader.newaccounting.Account account =
        accountNumberMap.get(accountNumber);
      if (account == null) {
        throw new RuntimeException(
          NO_SUCH_ACCOUNT_ERROR + item.getAccountNumber() + FISCAL_YEAR_ERROR +
          fiscalYear.getYear());
      }

      com.poesys.accounting.dataloader.newaccounting.Item newItem =
        transaction.addItem(item.getAmount(), account, item.isDebit(), item.isChecked());

      // Index the item in receivables or reimbursements as required.
      if (account.isReceivable()) {
        indexReceivableItem(item.getTransactionId(), item.getAccountNumber(), item.isDebit(),
                            newItem);
      }
    }
  }

  /**
   * Put a receivable account item into the receivables (debit) or reimbursements (credit) map.
   *
   * @param id            the transaction id for the item
   * @param accountNumber the account number for the item; must be a receivable account number
   * @param debit         whether the item is debit (true) or credit (false)
   * @param newItem       the new-accounting version of the item
   */
  private void indexReceivableItem(Integer id, Float accountNumber, boolean debit, com.poesys
    .accounting.dataloader.newaccounting.Item newItem) {

    // Check for receivable account
    Integer key = new Float(accountNumber * 100F).intValue();
    com.poesys.accounting.dataloader.newaccounting.Account account = accountNumberMap.get(key);
    if (!account.isReceivable()) {
      throw new RuntimeException(NOT_RECEIVABLE_ACCOUNT_ERROR + accountNumber);
    }
    ItemIndex index = new ItemIndex(id, accountNumber);
    if (debit) {
      // Receivable item, add to map indexed by year and transaction id
      logger.debug(
        "Adding receivable item to receivables map: " + fiscalYear.getYear() + " - " + id + " " +
        "for" + " account " + accountNumber);
      receivablesMap.get(fiscalYear.getYear()).put(index, newItem);
    } else {
      // Reimbursing item, add to map indexed by year and transaction id
      logger.debug(
        "Adding reimbursing item to reimbursements map: " + fiscalYear.getYear() + " - " + id + "" +
        " for account " + accountNumber);
      reimbursementsMap.get(fiscalYear.getYear()).put(index, newItem);
    }
  }

  /**
   * Validate all the transactions currently in the transaction lookup map. If there are invalid
   * transactions, log them as errors. Log all the invalid transactions. If there is at least one
   * invalid transaction, return false; otherwise, return true.
   *
   * @return false if there is at least one invalid transaction; otherwise, true
   */
  private boolean validateTransactions() {
    boolean valid = true;
    for (com.poesys.accounting.dataloader.newaccounting.Transaction transaction : transactionMap
      .values()) {
      if (!transaction.isValid()) {
        logger.error(INVALID_TRANSACTION_ERROR + transaction);
        if (valid) {
          // set flag to invalid if valid only, thus never resets to valid state
          valid = false;
        }
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
      Float receivableAccountNumber = reimbursement.getAccountNumber();
      if (receivableId.equals(0)) {
        // unchanged prior year reimbursement, determine whether this is an
        // oversight or whether to link the reimbursement to the balance
        // transaction for the account.
        receivableId = getBalanceReceivableId(receivableYear, receivableAccountNumber);
        logger.debug("Reimbursement is for 0 id, balance id is " + receivableId);
      }
      com.poesys.accounting.dataloader.newaccounting.Item receivableItem =
        lookupReceivableItem(receivableYear, receivableId, receivableAccountNumber);
      Integer reimbursementYear = reimbursement.getReimbursementYear();
      Integer reimbursementId = reimbursement.getReimbursementTransactionId();
      com.poesys.accounting.dataloader.newaccounting.Item reimbursementItem =
        lookupReimbursingItem(reimbursementYear, reimbursementId, receivableAccountNumber);

      // if both there, link the items.
      if (receivableItem != null && reimbursementItem != null) {
        receivableItem.reimburse(reimbursementItem, reimbursement.getReimbursedAmount(),
                                 reimbursement.getAllocatedAmount());
      } else if (receivableItem == null) {
        // The receivable wasn't found, warn but ignore.
        logger.warn("Reimbursing transaction id " + reimbursementId + " reimburses year " +
                    reimbursementYear + " id " + receivableId + " but " + receivableId +
                    " was not found " + "" + "in " + receivableYear + " as a receivable.");
      } else {
        // The reimbursing item wasn't found, warn but ignore.
        logger.warn(
          "Reimbursing transaction id " + reimbursementId + " reimburses year " + receivableYear +
          " id " + receivableId + " but " + reimbursementId + " was not found in " +
          reimbursementYear + " as a reimbursement.");
      }
    }
  }

  /**
   * Get the balance id for a receivable account if the balance year is the same as the receivable
   * year; otherwise, throw a runtime exception. Make sure the balance for the account is non-zero,
   * and if it is zero, throw a runtime exception.
   *
   * @param receivableYear          the designated receivable year of a reimbursement
   * @param receivableAccountNumber the old-accounting account number for the receivable account
   * @return the transaction id for the balance transaction for the receivable account
   */
  private Integer getBalanceReceivableId(Integer receivableYear, Float receivableAccountNumber) {
    com.poesys.accounting.dataloader.newaccounting.Account account =
      getAccountFromNumber(receivableYear, receivableAccountNumber);
    // Find the balance transaction for the account.
    com.poesys.accounting.dataloader.newaccounting.Transaction balanceTransaction = null;
    for (com.poesys.accounting.dataloader.newaccounting.Item item : account.getItems()) {
      if (item.getTransaction().isBalance()) {
        balanceTransaction = item.getTransaction();
        validateBalanceReceivable(receivableYear, receivableAccountNumber, balanceTransaction,
                                  item);
      }
    }

    return balanceTransaction != null ? balanceTransaction.getId().intValue() : null;
  }

  /**
   * Get a new-accounting Account object from a fiscal year and account number. Note that the
   * old-accounting account numbers are unique only within a fiscal year, so you need to know the
   * fiscal year if there is an error.
   *
   * @param year          the fiscal year (used in error message)
   * @param accountNumber the account number in the fiscal year
   * @return the account
   */
  private com.poesys.accounting.dataloader.newaccounting.Account getAccountFromNumber(Integer
                                                                                        year,
                                                                                      Float
                                                                                        accountNumber) {
    // Get the account.
    Integer number = new Float(accountNumber * 100F).intValue();
    com.poesys.accounting.dataloader.newaccounting.Account account = accountNumberMap.get(number);
    if (account == null) {
      throw new RuntimeException(ACCOUNT_ERROR + accountNumber + " for year " + year);
    }
    return account;
  }

  /**
   * Validate a balance transaction: <ul> <li>Is the item amount non-zero? Balance must be
   * positive.</li> <li>Is the balance transaction non-null?</li> <li>Is the year of the transaction
   * the same as the receivable year?</li> </ul>
   *
   * @param receivableYear          the year of the receivable in the reimbursement
   * @param receivableAccountNumber the receivable account (for error report)
   * @param balanceTransaction      the balance transaction to check
   * @param item                    the balance item to check
   */
  private void validateBalanceReceivable(Integer receivableYear, Float receivableAccountNumber,
                                         com.poesys.accounting.dataloader.newaccounting
                                           .Transaction balanceTransaction, com.poesys.accounting
                                           .dataloader.newaccounting.Item item) {
    if (balanceTransaction == null) {
      throw new RuntimeException(NO_BALANCE_ERROR + receivableAccountNumber);
    }
    if (item.getAmount().equals(0.00D)) {
      throw new RuntimeException(ZERO_BALANCE_ERROR + receivableAccountNumber);
    }
    if (!balanceTransaction.getYear().equals(receivableYear)) {
      throw new RuntimeException(ZERO_RECEIVABLE_ID_ERROR);
    }
  }

  /**
   * Look up a receivable item in the receivables map.
   *
   * @param year          the fiscal year of the item
   * @param id            the transaction id of the item
   * @param accountNumber the account number of the item
   * @return the receivable item
   */
  private com.poesys.accounting.dataloader.newaccounting.Item lookupReceivableItem(Integer year,
                                                                                   Integer id,
                                                                                   Float
                                                                                     accountNumber) {
    logger.debug("Looking up receivable item for reimbursement: " + year + " - " + id);
    Map<ItemIndex, com.poesys.accounting.dataloader.newaccounting.Item> items =
      receivablesMap.get(year);
    if (items == null) {
      throw new RuntimeException(NO_RECEIVABLES_ITEM_MAP_ERROR + year);
    }
    ItemIndex index = new ItemIndex(id, accountNumber);
    return items.get(index);
  }

  /**
   * Look up a reimbursing item in the reimbursements map.
   *
   * @param year          the fiscal year of the item
   * @param id            the transaction id of the item
   * @param accountNumber the account number of the item
   * @return the reimbursing item
   */
  private com.poesys.accounting.dataloader.newaccounting.Item lookupReimbursingItem(Integer year,
                                                                                    Integer id,
                                                                                    Float
                                                                                      accountNumber) {
    logger.debug("Looking up reimbursing item for receivable: " + year + " - " + id);
    Map<ItemIndex, com.poesys.accounting.dataloader.newaccounting.Item> items =
      reimbursementsMap.get(year);
    if (items == null) {
      throw new RuntimeException(NO_REIMBURSEMENTS_ITEM_MAP_ERROR + year);
    }
    ItemIndex index = new ItemIndex(id, accountNumber);
    return items.get(index);
  }

  @Override
  public String getPath() {
    return parameters.getPath();
  }

  @Override
  public List<FiscalYear> getFiscalYears() {
    return fiscalYears;
  }

  @Override
  public com.poesys.accounting.dataloader.newaccounting.FiscalYear getFiscalYear() {
    return fiscalYear;
  }

  @Override
  public CapitalStructure getCapitalStructure() {
    return capitalStructure;
  }

  @Override
  public Set<com.poesys.accounting.dataloader.newaccounting.AccountGroup> getAccountGroups
    (FiscalYear year) {
    return groupSetsMap.get(year);
  }

  /**
   * Get the typeMap for unit testing.
   *
   * @return the typeMap
   */
  Map<AccountType, List<AccountGroup>> getTypeMap() {
    return typeMap;
  }

  @Override
  public Set<com.poesys.accounting.dataloader.newaccounting.Account> getAccounts() {
    return accounts;
  }

  /**
   * Get the balance data set. This package-access method permits unit testing of the
   * buildBalances() method, which fills in this balance data set.
   *
   * @return returns the balance data set for unit test validation
   */
  Set<Balance> getBalanceDataSet() {
    return balanceDataSet;
  }

  @Override
  public com.poesys.accounting.dataloader.newaccounting.Account getAccountByName(String name) {
    if (name == null) {
      throw new RuntimeException(NO_ACCOUNT_NAME_ERROR);
    }
    return accountNameMap.get(name);
  }

  public Set<com.poesys.accounting.dataloader.newaccounting.Transaction> getTransactions() {
    return transactions;
  }
}
