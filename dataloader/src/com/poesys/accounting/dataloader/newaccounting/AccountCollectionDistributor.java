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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.poesys.db.InvalidParametersException;

/**
 * <p> Distributes a monetary amount among a set of accounts with the constraint of making the
 * resulting amounts equal or differing only by a penny. This class is a type of Builder pattern:
 * first construct the object, supplying the monetary amount to distribute, then add the accounts
 * into which to distribute the amount, then distribute the amount, then get the current balances
 * for the accounts. </p> <p> The account balances should already be equal or near-equal (different
 * by at most one penny). The validate() method verifies that; the distribute method calls
 * validate() and throws an exception if the current balances are not near-equal. </p> <p> This
 * class abstracts the distribution process, providing a clear set of process steps that you can
 * unit-test individually. It also exposes most of the logic of remainder distribution to enable
 * unit testing of the logic component methods. These methods may be of interest at some point for
 * use in other classes. </p>
 *
 * @author Robert J. Muller
 */
public class AccountCollectionDistributor {
  /** logger for this class */
  private static final Logger logger = Logger.getLogger(AccountCollectionDistributor.class);
  /** the amount to distribute */
  protected Integer amount;
  /** map of integer balances indexed by Account */
  protected final Map<Account, Integer> balances = new HashMap<>();
  /** map of decimal item credits/debit amounts indexed by Account */
  private final Map<Account, Integer> itemAmounts = new HashMap<>();
  /** ordered list of balance accounts */
  private Account firstAccount = null;

  // constants
  /** the scale for BigDecimal values */
  protected static final int SCALE = 2;
  /** the multiplier that converts a BigDecimal monetary amount to integer */
  private static final BigDecimal MULTIPLIER =
    new BigDecimal("100.00").setScale(SCALE, RoundingMode.HALF_UP);
  /** the amount by which accounts may differ */
  private static final Integer PENNY = 1;

  // messages
  private static final String INVALID_COLLECTION_ERROR =
    "invalid balances, check whether balances were added to distributor and that balance amounts " +
    "are equal or at most one penny different";
  private static final String NULL_ACCOUNT_ERROR = "account is required but is null";
  private static final String NULL_BALANCE_ERROR = "balance is required but is null";
  private static final String ACCOUNT_NOT_ADDED_ERROR = "account not added: ";
  private static final String NO_BALANCE_ERROR = "no balance for account ";
  private static final String NO_AMOUNT_ERROR = "no item amount for account ";
  private static final String NO_BALANCES_ERROR = "no balances";

  /**
   * Create a AccountCollectionDistributor object.
   *
   * @param amount the amount to distribute
   */
  public AccountCollectionDistributor(BigDecimal amount) {
    this.amount = amount.multiply(MULTIPLIER).intValue();
  }

  /**
   * Get the amount.
   *
   * @return a monetary amount
   */
  public Integer getAmount() {
    return amount;
  }

  /**
   * Add an account and its current balance to the balance map, converting the BigDecimal monetary
   * amount (scale SCALE) into an integer amount for computation with integer arithmetic. Add the
   * account to the ordered list of accounts, which imposes an order on the set of accounts.
   *
   * @param account the account that has the balance
   * @param balance the decimal monetary amount (scale SCALE)
   */
  public void addBalance(Account account, BigDecimal balance) {
    if (account == null) {
      throw new InvalidParametersException(NULL_ACCOUNT_ERROR);
    }
    if (balance == null) {
      throw new InvalidParametersException(NULL_BALANCE_ERROR);
    }
    Integer intValue = balance.multiply(MULTIPLIER).intValue();
    balances.put(account, intValue);
    itemAmounts.put(account, 0);
    if (firstAccount == null) {
      firstAccount = account;
    }
  }

  /**
   * Get the current balance for an account as a BigDecimal monetary amount (scale SCALE).
   *
   * @param account the account for which to get the balance
   * @return the balance as a monetary amount (scale SCALE)
   */
  public BigDecimal getBalance(Account account) {
    if (account == null) {
      throw new InvalidParametersException(NULL_ACCOUNT_ERROR);
    }
    Integer balance = balances.get(account);
    if (balance == null) {
      throw new InvalidParametersException(ACCOUNT_NOT_ADDED_ERROR + account);
    }
    BigDecimal decimalBalance = new BigDecimal(balance).setScale(SCALE, RoundingMode.HALF_UP);
    return decimalBalance.divide(MULTIPLIER, RoundingMode.HALF_UP).setScale(SCALE,
                                                                            RoundingMode.HALF_UP);
  }

  /**
   * Get the current item amount for an account as a BigDecimal monetary amount (scale SCALE).
   *
   * @param account the account for which to get the amount
   * @return the balance as a monetary amount (scale SCALE)
   */
  public BigDecimal getItemAmount(Account account) {
    if (account == null) {
      throw new InvalidParametersException(NULL_ACCOUNT_ERROR);
    }
    Integer amount = itemAmounts.get(account);
    if (amount == null) {
      throw new InvalidParametersException(ACCOUNT_NOT_ADDED_ERROR + account);
    }
    BigDecimal decimalAmount = new BigDecimal(amount).setScale(SCALE, RoundingMode.HALF_UP);
    return decimalAmount.divide(MULTIPLIER, RoundingMode.HALF_UP).setScale(SCALE,
                                                                           RoundingMode.HALF_UP);
  }

  /**
   * There must be at least one balance. Compare the balances; all should be equal or differ by at
   * most one penny.
   *
   * @return true if balances are equal or near-equal, false if not
   */
  public boolean isValid() {
    boolean valid = true;
    if (balances.size() > 0) {
      outer:
      for (Account account : balances.keySet()) {
        for (Account otherAccount : balances.keySet()) {
          // Skip same-account comparisons
          if (!account.equals(otherAccount)) {
            int difference = balances.get(account) - balances.get(otherAccount);
            if (difference < 0) {
              difference = -difference;
            }
            if (difference != 0 && difference != PENNY) {
              logger.warn("Imbalance between capital accounts " + account.getName() + " and " +
                          otherAccount.getName() + ": " + balances.get(account) + " vs. " +
                          balances.get(otherAccount));
              valid = false;
              break outer;
            }
          }
        }
      }
    } else {
      // no balances, mark invalid
      logger.warn("No balances added to distributor");
      valid = false;
    }
    return valid;
  }

  /**
   * Equalize the balances by shifting pennies between the accounts until the account balances are
   * equal or differ by at most a penny. The direction of penny-shifting depends on the sign of the
   * balances, negative or positive.
   *
   * @return true if balances were equalized, false if they were unchanged
   */
  public boolean equalize() {
    boolean equalized = false;
    if (!equal()) {
      // At least a penny difference somewhere, possibly more
      // Get the min and max accounts and shift a penny, then recurse.
      Account min = getMinimumBalanceAccount();
      Account max = getMaximumBalanceAccount();
      Integer minBalance = balances.get(min);
      Integer maxBalance = balances.get(max);
      Integer minItemAmount = itemAmounts.get(min);
      Integer maxItemAmount = itemAmounts.get(max);

      if (Math.abs(maxBalance - minBalance) > 1) {
        equalized = true;
        // more than a penny different
        minBalance++;
        balances.put(min, minBalance);
        minItemAmount++;
        itemAmounts.put(min, minItemAmount);
        maxBalance--;
        balances.put(max, maxBalance);
        maxItemAmount--;
        itemAmounts.put(max, maxItemAmount);
      }
    }
    return equalized;
  }

  /**
   * Distribute the amount across the current set of account balances, changing the balances to the
   * distributed amounts plus the original amounts. This distributes an equal amount, leaving a
   * remainder of zero or more pennies. This method does not distribute the remainder, to simplify
   * unit testing. The method updates both the balances and the item amounts for the distributor.
   */
  public void distributeAmount() {
    // Check validity of initial balances.
    if (!isValid()) {
      throw new RuntimeException(INVALID_COLLECTION_ERROR);
    }
    // Get the initial distribution amount by equally dividing by the count of
    // the balances.
    Integer distAmount = amount / balances.size();

    for (Account account : balances.keySet()) {
      int currentBalance = balances.get(account);
      int newBalance = currentBalance + distAmount;

      // Put the new balance into the balances map.
      balances.put(account, newBalance);

      // Put the distribution amount into the itemAmount map.
      itemAmounts.put(account, distAmount);
    }
  }

  /**
   * Distribute the number of pennies remaining after the main distribution across the current set
   * of account balances, keeping the balances within one penny of one another. The remainder can be
   * positive or negative, and the balances can be positive, negative, or zero; the logic depends on
   * the various combinations of these states. Also note that the remainder logic dictates that
   * there must be at least two balances for there to be a remainder, as x%1 never produces a
   * remainder.
   */
  public void distributeRemainder() {
    if (balances.size() == 0) {
      throw new RuntimeException(INVALID_COLLECTION_ERROR);
    }

    Integer remainder = amount % balances.size();

    // Check validity to make sure everything is there that needs to be. Don't
    // do anything if there is no remainder to distribute.
    if (remainder != 0 && isValid()) {
      distributePennyFromRemainder(remainder);
    } else if (remainder != 0) {
      // isValid() failed, throw exception
      throw new RuntimeException(INVALID_COLLECTION_ERROR);
    }
  }

  /**
   * Recursive method that distributes one penny for each recursion until the remainder is gone. The
   * method ensures that the set of balances stays equal or nearly equal. Keep this private to hide
   * the recursion structure.
   *
   * @param remainder the current remainder
   */
  private void distributePennyFromRemainder(Integer remainder) {

    if (equal()) {
      // Distribute the penny to the first account.
      remainder = setBalanceAndDecrementRemainder(remainder, firstAccount);
    } else {
      remainder = distributeRemainderNearlyEqual(remainder);
    }

    if (remainder != 0) {
      // There is still a remainder to distribute, so recurse.
      distributePennyFromRemainder(remainder);
    }
  }

  /**
   * Distribute a penny from a remainder to nearly equal balances, then return the decremented
   * remainder. If the remainder is zero, the method just returns the current remainder.
   *
   * @param remainder the current remainder
   * @return the remainder after distribution
   */
  public Integer distributeRemainderNearlyEqual(Integer remainder) {
    Account account = getAccount(remainder);

    if (account != null) {
      remainder = setBalanceAndDecrementRemainder(remainder, account);
    }

    return remainder;
  }

  /**
   * Based on the current remainder, get the appropriate account to which to distribute part of the
   * remainder.
   *
   * @param remainder the current remainder
   * @return the account to which to distribute a penny or null if the remainder was zero
   */
  public Account getAccount(Integer remainder) {
    Account account = null;
    if (remainder > 0) {
      // positive remainder, add the penny to the minimum balance to bring
      // it up to the maximum balance
      account = getMinimumBalanceAccount();
    } else if (remainder < 0) {
      // negative remainder, add the penny to the maximum balance to bring
      // it down to the minimum balance.
      account = getMaximumBalanceAccount();
    }
    return account;
  }

  /**
   * Compute a revised balance for an account from a remainder and set that balance into the
   * balances map, then return the decremented remainder. If there is no balance for the specified
   * account, the method throws a RuntimeException. The method adds a penny to the balance and
   * decrements a penny from the remainder.
   *
   * @param remainder the current remainder
   * @param account   the account to set
   * @return the decremented remainder
   */
  public Integer setBalanceAndDecrementRemainder(Integer remainder, Account account) {
    // set penny as +1 or -1 depending on remainder sign
    Integer penny = remainder > 0 ? 1 : -1;
    Integer balance = balances.get(account);
    if (balance == null) {
      throw new RuntimeException(NO_BALANCE_ERROR + account);
    }

    Integer itemAmount = itemAmounts.get(account);
    if (itemAmount == null) {
      throw new RuntimeException(NO_AMOUNT_ERROR + account);
    }

    // Add the penny amount to the balance and amount.
    balance += penny;
    itemAmount += penny;
    // Update the balance for the account.
    balances.put(account, balance);
    // Update the item amount for the account.
    itemAmounts.put(account, itemAmount);
    remainder -= penny;

    return remainder;
  }

  /**
   * Get the account that contains the maximum balance. If several balances have this maximum,
   * return the first one found. If there are no balances at all, the method throws a
   * RuntimeException.
   *
   * @return the account containing the maximum balance
   */
  public Account getMaximumBalanceAccount() {
    if (balances.size() == 0) {
      throw new RuntimeException(NO_BALANCES_ERROR);
    }
    Account maxAccount = null;
    Integer maxBalance = null;
    for (Account account : balances.keySet()) {
      Integer balance = balances.get(account);
      // iterating existing accounts, so there is always a balance.
      if (maxAccount == null) {
        // first one, set max.
        maxAccount = account;
        maxBalance = balance;
      } else {
        // not first, compare and set max.
        if (balance > maxBalance) {
          maxAccount = account;
        }
      }
    }
    return maxAccount;
  }

  /**
   * Get the account that contains the minimum balance. If several balances have this minimum,
   * return the first one found.
   *
   * @return the account containing the minimum balance
   */
  public Account getMinimumBalanceAccount() {
    Account minAccount = null;
    Integer minBalance = null;
    for (Account account : balances.keySet()) {
      Integer balance = balances.get(account);
      if (minAccount == null) {
        // first one, set max.
        minAccount = account;
        minBalance = balance;
      } else {
        // not first, compare and set min.
        if (balance < minBalance) {
          minAccount = account;
        }
      }
    }
    return minAccount;
  }

  /**
   * Are the balance values all the same?
   *
   * @return true if all the integers are the same, false if not
   */
  public boolean equal() {
    boolean equal = true;
    Integer value = null;
    for (Integer integer : balances.values()) {
      if (value == null) {
        // first value, set comparison variable
        value = integer;
      } else if (value.compareTo(integer) != 0) {
        equal = false;
        break;
      }
    }
    return equal;
  }
}
