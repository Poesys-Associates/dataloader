/**
 * Copyright (c) 2017 Poesys Associates. All rights reserved.
 */
package com.poesys.accounting.dataloader.newaccounting;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.poesys.accounting.dataloader.IBuilder;
import com.poesys.accounting.dataloader.newaccounting.Statement.StatementType;
import com.poesys.db.InvalidParametersException;


/**
 * <p>
 * Represents a collection of capital entities in a specific order, permitting
 * various operations to production transactions for each entity in the
 * structure; each entity has a set of specific accounts and a specific
 * percentage ownership. Examples of standard capital structures:
 * </p>
 * <ul>
 * <li>individual (one entity corresponding to the individual person)</li>
 * <li>joint (one entity corresponding to a joint, multiple-person entity such
 * as a marriage)</li>
 * <li>sole (one entity corresponding to the sole proprietor of a company)</li>
 * <li>partnership (multiple entities corresponding to the partners)</li>
 * <li>corporation (one entity corresponding to the corporation)</li>
 * </ul>
 * <p>
 * The ownership percentage provides the multiplier used to allocate amounts to
 * the relevant accounts. Single-entity structures will always have this set to
 * 100%, but multiple-entity structures should validate that the entity
 * ownership percentages sum to exactly 100% using the isValid() function.
 * </p>
 * <p>
 * The Income Summary account is the income account that summarizes net income
 * for transfer to the capital accounts of the capital entities.
 * </p>
 * 
 * @author Robert J. Muller
 */
public class CapitalStructure {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(CapitalStructure.class);
  /** name for balance sheet statement */
  private static final String BALANCE_SHEET = "Balance Sheet";
  /** name for income statement */
  private static final String INCOME_STMT = "Income Statement";
  /** description for income summary transactions */
  private static final String INCOME_SUMMARY_DESCRIPTION =
    "Summarize income for ";
  /** description for distribution transactions */
  private static final String DISTRIBUTION_DESCRIPTION =
    "Transfer distribution to capital for ";
  /** description for capital account adjustment transaction */
  private static final String ADJUST_DESCRIPTION =
    "Adjust capital accounts to ownership";

  /** constant divisor to translate integer to monetary amount */
  private static final BigDecimal DIVISOR =
    new BigDecimal("100.00").setScale(CapitalEntity.SCALE);

  /** the ordered list of entities in the capital structure */
  private final List<CapitalEntity> entities = new ArrayList<CapitalEntity>(2);

  /** the name of the income summary account */
  private final String incomeSummaryAccountName;

  // messages

  private static final String OWNERSHIP_ERROR =
    "ownership across entities does not sum to 100%";
  private static final String INVALID_TRANSACTION_ERROR =
    "invalid income summary transaction ";
  private static final String NO_ENTITIES_ERROR =
    "no entities in capital structure";

  /**
   * Get the entities. This returns a synchronized list. If you iterate through
   * the list, you must do so in a synchronized (list) block.
   * 
   * @return a list of entities in order
   */
  public List<CapitalEntity> getEntities() {
    return Collections.synchronizedList(entities);
  }

  /**
   * Get the income summary account name.
   * 
   * @return a incomeSummaryAccount
   */
  public String getIncomeSummaryAccount() {
    return incomeSummaryAccountName;
  }

  /**
   * Create a CapitalStructure object with the income summary account name.
   * 
   * @param incomeSummaryAccountName the account name
   */
  public CapitalStructure(String incomeSummaryAccountName) {
    this.incomeSummaryAccountName = incomeSummaryAccountName;
  }

  /**
   * Add a list of entities to the current list. The list must have a valid
   * ownership structure after the additions.
   * 
   * @param entities a list of CapitalEntity objects
   */
  public void addEntities(List<CapitalEntity> entities) {
    this.entities.addAll(entities);
    if (!isValid()) {
      throw new InvalidParametersException(OWNERSHIP_ERROR);
    }
  }

  /**
   * Is the list of entities valid as a capital structure? There must be at
   * least one entity, and the ownership percentages must sum to 1.
   * 
   * @return true for valid structures, false for invalid ones
   */
  public boolean isValid() {
    boolean valid = false;
    synchronized (entities) {
      if (entities.size() > 0) {
        // At least one entity, check the sum of the ownership percentages.
        BigDecimal sum = BigDecimal.ZERO.setScale(CapitalEntity.SCALE);
        for (CapitalEntity entity : entities) {
          sum = sum.add(entity.getOwnership()).setScale(CapitalEntity.SCALE);
        }
        if (sum.compareTo(BigDecimal.ONE.setScale(CapitalEntity.SCALE)) == 0) {
          valid = true;
        }
      } else {
        throw new RuntimeException(NO_ENTITIES_ERROR);
      }
    }
    return valid;
  }

  /**
   * Get a transaction that transfers money between capital accounts to bring
   * all the accounts to their desired balances based on the capital ownership
   * structure and tolerance of the accounting system. If there is only one
   * capital entity, the method returns null as there is never an adjusting
   * transaction for a single-entity system. If there is no change required
   * based on the current balances and ownership percentages, the method returns
   * null.
   * 
   * @param builder the builder containing the accounts, capital structure, and
   *          transactions of the accounting system
   * @return an adjusting transaction or null if no change is required
   */
  public Transaction getCapitalAdjustmentTransaction(IBuilder builder) {
    FiscalYear year = builder.getFiscalYear();
    Transaction transaction = null;
    Statement balanceSheet =
      new Statement(year, "Balance Sheet", StatementType.BALANCE_SHEET);
    AccountCollectionDistributor distributor =
      new AccountCollectionDistributor(BigDecimal.ZERO.setScale(CapitalEntity.SCALE));

    // Add the capital account balances to the distributor.
    for (CapitalEntity entity : entities) {
      Account account = builder.getAccountByName(entity.getCapitalAccount());
      distributor.addBalance(account, balanceSheet.getAccountBalance(account));
    }

    // Equalize the capital accounts.
    boolean adjusted = distributor.equalize();

    if (adjusted) {
      BigInteger id = year.getMaxId().add(BigInteger.ONE);
      transaction =
        new Transaction(id, ADJUST_DESCRIPTION, year.getEnd(), false, false);
      for (CapitalEntity entity : entities) {
        Account account = builder.getAccountByName(entity.getCapitalAccount());
        BigDecimal item = distributor.getItemAmount(account);
        if (!item.equals(BigDecimal.ZERO.setScale(CapitalEntity.SCALE))) {
          boolean debit =
            item.compareTo(BigDecimal.ZERO.setScale(CapitalEntity.SCALE)) < 0;
          transaction.addItem(item.abs().doubleValue(), account, debit, false);
        }
      }
    }
    return transaction;
  }

  /**
   * Get a transaction that helps to close a fiscal year by transferring net
   * income to the appropriate capital accounts given the capital structure of
   * the accounting entity. The debit/credit structure depends on the nature of
   * net income, which can be either a credit (positive net income) or a debit
   * (negative net income). For a credit net income, the transaction debits
   * income summary and credits the capital accounts; for a debit net income,
   * the transaction does the reverse. The allocation to multiple accounts
   * should bring those accounts to equality or near equality (at most a one
   * cent difference between any two capital accounts).
   * 
   * @param year the fiscal year for which to create the transaction
   * @param builder the builder containing the accounts
   * 
   * @return the transaction
   */
  public Transaction getIncomeToCapitalTransaction(FiscalYear year,
                                                   IBuilder builder) {
    List<Account> capitalAccounts = getCapitalAccounts(builder);

    AccountCollectionDistributor distributor =
      getDistributor(year, capitalAccounts);

    return buildCapitalTransaction(year, builder, capitalAccounts, distributor);
  }

  /**
   * Get a list of the capital accounts from the entities using the builder.
   * 
   * @param builder the builder containing the set of accounts
   * @return a list of capital accounts for the entities
   */
  private List<Account> getCapitalAccounts(IBuilder builder) {
    List<Account> capitalAccounts = new ArrayList<Account>();
    for (CapitalEntity entity : entities) {
      String accountName = entity.getCapitalAccount();
      Account account = builder.getAccountByName(accountName);
      capitalAccounts.add(account);
    }
    return capitalAccounts;
  }

  /**
   * Get an account distributor builder object for the fiscal year and set of
   * entity capital accounts.
   * 
   * @param year the fiscal year
   * @param capitalAccounts the list of capital accounts for the entities
   * @return the distributor
   */
  private AccountCollectionDistributor getDistributor(FiscalYear year,
                                                      List<Account> capitalAccounts) {
    Statement balanceSheet =
      new Statement(year, BALANCE_SHEET, StatementType.BALANCE_SHEET);
    Statement incomeStatement =
      new Statement(year, INCOME_STMT, StatementType.INCOME_STATEMENT);

    // Set up the distributor with the income statement balance.
    AccountCollectionDistributor distributor =
      new AccountCollectionDistributor(incomeStatement.getBalance());

    // Add the capital account balances to the distributor.
    for (Account account : capitalAccounts) {
      // Get the balance for the account from the balance sheet.
      BigDecimal balance = balanceSheet.getAccountBalance(account);
      distributor.addBalance(account, balance);
    }

    return distributor;
  }

  /**
   * Build the capital account transaction with the income summary and capital
   * items. Validate the transaction. The income summary item will set the value
   * for the Income Summary account in the income statement to show the net
   * income for the year. The capital items distribute that net income to the
   * capital accounts for the accounting entities.
   * 
   * @param year the fiscal year
   * @param builder the builder containing the accounts for the fiscal year
   * @param capitalAccounts the list of capital accounts for the entities
   * @param distributor the distributor created with the net income amount
   * @return the validated transaction
   */
  private Transaction buildCapitalTransaction(FiscalYear year,
                                              IBuilder builder,
                                              List<Account> capitalAccounts,
                                              AccountCollectionDistributor distributor) {
    // Get the distributor amount, which will be positive for a net gain or
    // negative for a net loss in the income statement.
    BigDecimal netIncome =
      new BigDecimal(distributor.getAmount()).divide(DIVISOR).setScale(CapitalEntity.SCALE);

    // Translate the net income to unsigned amount and appropriate debit/credit
    // flag. The latter depends on the sign of the net income. For a net gain,
    // the net income will be a debit to subtract the income from the income
    // statement; for a net loss, the net income will be a credit to add the
    // loss to the income statement. The end result will be a 0 balance on the
    // income statement.
    Double amount = netIncome.abs().doubleValue();
    Boolean debit = netIncome.compareTo(BigDecimal.ZERO) >= 0 ? true : false;

    // Create the income summary account transaction to summarize income.
    BigInteger id =
      year.getMaxId() != null ? year.getMaxId().add(BigInteger.ONE)
          : BigInteger.ONE;
    Transaction transaction =
      new Transaction(id,
                      INCOME_SUMMARY_DESCRIPTION + year.getYear(),
                      year.getEnd(),
                      false,
                      false);

    // Create the income summary item.
    Account incomeSummaryAccount =
      builder.getAccountByName(incomeSummaryAccountName);
    transaction.addItem(amount, incomeSummaryAccount, debit, false);
    // Create the capital account items.
    distributor.distributeAmount();
    distributor.distributeRemainder();
    for (Account account : capitalAccounts) {
      // Get absolute value of amount.
      amount = distributor.getItemAmount(account).abs().doubleValue();
      // debit flag is reverse of income summary item flag
      transaction.addItem(amount, account, !debit, false);
    }
    if (!transaction.isValid()) {
      throw new RuntimeException(INVALID_TRANSACTION_ERROR + transaction);
    }
    for (Item item : transaction.getItems()) {
      logger.debug("added capital transfer item " + item);
    }
    return transaction;
  }

  /**
   * <p>
   * Get a set of transactions that help to close a fiscal year by transferring
   * distributions or draws from the distribution accounts to the corresponding
   * capital accounts. There is always a capital account, but for some
   * accounting entities, there is no distribution account. If there is such an
   * account, the method builds a transaction for each entity that transfers the
   * balance in the account to the corresponding capital account for the entity.
   * The debit/credit status depends on the sign of the balance. The
   * distribution account is a contra account (default debit), and positive
   * distributions debit that account; if the distribution account has a debit
   * amount, the transaction for the entity credits that account and debits the
   * capital account (removing money from capital). Distributions are not
   * usually credits, unless there is a reversal of a distribution. Also, the
   * usual case for no distribution account is a single-entity capital
   * structure, and hence the returned list will usually be empty.
   * </p>
   * <p>
   * Note that the method assumes a straight transfer of amounts from the
   * distribution accounts to the corresponding capital accounts for each
   * capital entity. If there is a requirement that the capital accounts divide
   * up by the ownership percentage, such a transfer may throw the capital
   * accounts out of whack. In this case, the caller is responsible for
   * re-balancing the capital accounts with an appropriate transaction between
   * those accounts. This method will log a warning message to highlight a
   * possible problem in the data.
   * </p>
   * 
   * 
   * @param year the fiscal year for which to create distribution transactions
   * @param builder the builder containing the set of accounts
   * @return a list of transactions, one per capital entity
   */
  public List<Transaction> getDistributionTransactions(FiscalYear year,
                                                       IBuilder builder) {
    List<Transaction> transactions =
      new ArrayList<Transaction>(entities.size());

    Statement stmt =
      new Statement(year, BALANCE_SHEET, StatementType.BALANCE_SHEET);

    for (CapitalEntity entity : entities) {
      // Get the accounts for this entity by name.
      Account capAccount = builder.getAccountByName(entity.getCapitalAccount());
      Account distAccount =
        builder.getAccountByName(entity.getDistributionAccount());

      // Proceed only if there is a distribution account.
      if (distAccount != null) {
        // Get the distribution balance for this entity.
        BigDecimal balance = stmt.getAccountBalance(distAccount);
        // Create the transaction to transfer distribution to capital.
        BigInteger id = year.getMaxId().add(BigInteger.ONE);
        Transaction transaction =
          new Transaction(id,
                          DISTRIBUTION_DESCRIPTION + distAccount.getName()
                              + " for " + year.getYear(),
                          year.getEnd(),
                          false,
                          false);
        // Convert amount to absolute value.
        Double amount = balance.abs().doubleValue();
        // Set debit flag by sign of balance. A debit balance is a positive
        // distribution (contra account).
        Boolean debit =
          balance.compareTo(BigDecimal.ZERO.setScale(CapitalEntity.SCALE)) < 0 ? true
              : false;
        // Zero out distribution account for this entity.
        transaction.addItem(amount, distAccount, !debit, false);
        // Transfer distribution to capital account for this entity.
        transaction.addItem(amount, capAccount, debit, false);
        // Add the transaction to the list to return.
        transactions.add(transaction);
        for (Item item : transaction.getItems()) {
          logger.debug("added distribution transfer item " + item);
        }
      }
    }
    return transactions;
  }

  @Override
  public String toString() {
    return "CapitalStructure [incomeSummaryAccountName="
           + incomeSummaryAccountName + ", entities=" + entities + "]";
  }
}
