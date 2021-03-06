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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.poesys.db.InvalidParametersException;

/**
 * CUT: Transaction, Item, and Reimburse cluster
 *
 * @author Robert J. Muller
 */
public class TransactionTest {
  private static final Logger logger = Logger.getLogger(TransactionTest.class);

  private static final BigInteger TRANSACTION_ID = new BigInteger("1234");
  private static final String DESCRIPTION = "description";
  private static final Timestamp DATE = Timestamp.valueOf("2017-05-01 00:00:00");
  private static final Timestamp PRIOR_DATE = Timestamp.valueOf("2016-06-01 00:00:00");
  private static final Boolean NOT_CHECKED = Boolean.FALSE;
  private static final Boolean CHECKED = Boolean.TRUE;
  private static final Boolean NOT_BALANCE = Boolean.FALSE;
  private static final Double AMOUNT = 10.00D;
  private static final Double ZERO = 0.00D;
  private static final Double ALLOCATED_AMOUNT = ZERO;
  private static final Boolean DEBIT = Boolean.TRUE;
  private static final Boolean CREDIT = Boolean.FALSE;

  private static final AccountType INCOME_TYPE = AccountType.INCOME;
  private static final AccountType ASSET_TYPE = AccountType.ASSETS;
  private static final AccountType EQUITY_TYPE = AccountType.EQUITY;
  private static final AccountType EXPENSE_TYPE = AccountType.EXPENSES;

  private static final Boolean DEBIT_DEFAULT = Boolean.TRUE;
  private static final Boolean CREDIT_DEFAULT = Boolean.FALSE;
  private static final Boolean NOT_RECEIVABLE = Boolean.FALSE;
  private static final Boolean RECEIVABLE = Boolean.TRUE;

  // accounts for testing

  private static final String INCOME_ACCOUNT_NAME = "Revenue";
  private static final Account INCOME_ACCOUNT =
    new Account(INCOME_ACCOUNT_NAME, DESCRIPTION, INCOME_TYPE, CREDIT_DEFAULT, NOT_RECEIVABLE);
  private static final String CHECKING_ACCOUNT_NAME = "Citibank Checking";
  private static final Account CHECKING_ACCOUNT =
    new Account(CHECKING_ACCOUNT_NAME, DESCRIPTION, ASSET_TYPE, DEBIT_DEFAULT, NOT_RECEIVABLE);
  private static final String AR_ACCOUNT_NAME = "Accounts Receivable";
  private static final Account AR_ACCOUNT =
    new Account(AR_ACCOUNT_NAME, DESCRIPTION, ASSET_TYPE, DEBIT_DEFAULT, RECEIVABLE);
  private static final String CAPITAL_ACCOUNT_NAME_1 = "Partner 1 Basis";
  private static final Account CAPITAL_ACCOUNT_1 =
    new Account(CAPITAL_ACCOUNT_NAME_1, DESCRIPTION, EQUITY_TYPE, CREDIT_DEFAULT, NOT_RECEIVABLE);
  private static final String CAPITAL_ACCOUNT_NAME_2 = "Partner 2 Basis";
  private static final Account CAPITAL_ACCOUNT_2 =
    new Account(CAPITAL_ACCOUNT_NAME_2, DESCRIPTION, EQUITY_TYPE, CREDIT_DEFAULT, NOT_RECEIVABLE);
  private static final String INTERNET_ACCOUNT_NAME = "Internet Service";
  private static final Account INTERNET_ACCOUNT =
    new Account(INTERNET_ACCOUNT_NAME, DESCRIPTION, EXPENSE_TYPE, DEBIT_DEFAULT, NOT_RECEIVABLE);

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .Transaction#Transaction(BigInteger, * java.lang.String, java.sql.Timestamp, java.lang
   * .Boolean, java.lang.Boolean)} . Tests basic constructor and getters
   */
  @Test
  public void testTransaction() {
    Transaction transaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    assertTrue("description getter failed", DESCRIPTION.equals(transaction.getDescription()));
    assertTrue("date getter failed", DATE.equals(transaction.getDate()));
    assertTrue("checked getter failed", NOT_CHECKED.equals(transaction.isChecked()));
    assertTrue("balance getter failed", NOT_BALANCE.equals(transaction.isBalance()));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .Transaction#Transaction(BigInteger, * java.lang.String, java.sql.Timestamp, java.lang
   * .Boolean, java.lang.Boolean)} . Tests basic constructor with null id input.
   */
  @Test
  public void testTransactionNullId() {
    try {
      new Transaction(null, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
      fail("no exception from null input id");
    } catch (InvalidParametersException e) {
      assertTrue("wrong exception for null input id: " + e.getMessage(),
                 e.getMessage().equals("Transaction parameters are required but one is null"));
    } catch (Exception e) {
      fail("wrong exception from null input id: " + e.getMessage());
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting
   * .Transaction#Transaction(BigInteger, * java.lang.String, java.sql.Timestamp, java.lang
   * .Boolean, java.lang.Boolean)} . Tests basic constructor with null date input
   */
  @Test
  public void testTransactionNullDate() {
    try {
      new Transaction(TRANSACTION_ID, DESCRIPTION, null, NOT_CHECKED, NOT_BALANCE);
      fail("no exception from null input date");
    } catch (InvalidParametersException e) {
      assertTrue("wrong exception for null input date: " + e.getMessage(),
                 e.getMessage().equals("Transaction parameters are required but one is null"));
    } catch (Exception e) {
      fail("wrong exception from null input date: " + e.getMessage());
    }
  }

  /**
   * Create a valid transaction with two regular items (checking and income).
   *
   * @param id the unique identifier for the transaction
   * @return the new Transaction object
   */
  private Transaction createTransactionWithItems(BigInteger id) {
    Transaction transaction = new Transaction(id, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    Item checkingItem = transaction.addItem(AMOUNT, CHECKING_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("checking item constructor failed", checkingItem != null);
    Item incomeItem = transaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", incomeItem != null);
    return transaction;
  }

  /**
   * Create a valid transaction with two zero-valued items).
   *
   * @param id the unique identifier for the transaction
   * @return the new Transaction object with zero-valued items
   */
  private Transaction createTransactionWithZeroItems(BigInteger id) {
    Transaction transaction = new Transaction(id, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    Item checkingItem = transaction.addItem(ZERO, CHECKING_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("zero checking item constructor failed", checkingItem != null);
    Item incomeItem = transaction.addItem(ZERO, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("zero income item constructor failed", incomeItem != null);
    return transaction;
  }

  /**
   * Test method for Transaction.addItem() and getters.
   */
  @Test
  public void testItem() {
    Transaction transaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    Item item = transaction.addItem(AMOUNT, CHECKING_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("item constructor failed", item != null);
    assertTrue("transaction getter failed", transaction.equals(item.getTransaction()));
    assertTrue("item not in transaction", transaction.getItems().size() > 0);
    assertTrue("amount getter failed", AMOUNT.equals(item.getAmount()));
    assertTrue("account getter failed", CHECKING_ACCOUNT.equals(item.getAccount()));
    assertTrue("debit flag getter failed", DEBIT.equals(item.isDebit()));
    assertTrue("checked flag getter failed", NOT_CHECKED.equals(item.isChecked()));
    int found = 0;
    Collection<Item> items = CHECKING_ACCOUNT.getItems();
    for (Item accountItem : items) {
      if (accountItem.equals(item)) {
        found++;
      }
    }
    assertTrue("Item not in account item list", found > 0);
    assertTrue("More than one instance of item in account item list", !(found > 1));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Transaction#getItem(com.poesys.accounting.dataloader.newaccounting.Account)}
   * . Tests case where item is there.
   */
  @Test
  public void testGetItem() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item
    Item receivable = receivableTransaction.addItem(AMOUNT, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue("invalid receivable transaction", receivableTransaction.isValid());

    // Get the receivable item from the transaction.
    assertTrue("no receivable item found but one is expected",
               receivableTransaction.getItem(AR_ACCOUNT) != null);
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Transaction#getItem(com.poesys.accounting.dataloader.newaccounting.Account)}
   * . Tests case where item is not there.
   */
  @Test
  public void testGetItemNotFound() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item
    Item receivable = receivableTransaction.addItem(AMOUNT, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue("invalid receivable transaction", receivableTransaction.isValid());

    // Get the receivable item from the transaction.
    assertTrue("checking item found but was not added",
               receivableTransaction.getItem(CHECKING_ACCOUNT) == null);
  }

  /**
   * Test system around valid receivable and reimbursement items and the
   * com.poesys.accounting.dataloader.newaccounting.Item.reimburse() method, along with the getters
   * for reimbursements and receivable. Also tests Transaction.addItem(), Transaction.isValid(),
   * Item.getReimbursements(), Reimbursement.getReceivable(), Reimbursement.getReimbursingItem(),
   * Reimbursement.getReimbursedAmount, and Reimbursement.getAllocatedAmount().
   */
  @Test
  public void testValidReimbursement() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item
    Item receivable = receivableTransaction.addItem(AMOUNT, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue("invalid receivable transaction", receivableTransaction.isValid());

    // Create the reimbursing transaction with items.
    Transaction reimbursingTransaction =
      new Transaction(TRANSACTION_ID.add(BigInteger.ONE), DESCRIPTION, DATE, NOT_CHECKED,
                      NOT_BALANCE);
    // cash payment item
    Item payment = reimbursingTransaction.addItem(AMOUNT, CHECKING_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("payment item constructor failed", payment != null);
    // receivable reimbursement item
    Item reimbursingItem = reimbursingTransaction.addItem(AMOUNT, AR_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("reverse receivable item constructor failed", reimbursingItem != null);
    assertTrue("invalid reimbursing transaction", receivableTransaction.isValid());

    // Reimburse the receivable.
    receivable.reimburse(reimbursingItem, AMOUNT, ALLOCATED_AMOUNT);

    // Test the getReimbursements getter.
    assertTrue("no reimbursements in receivable", receivable.getReimbursements().size() > 0);

    for (Item.Reimbursement reimbursement : receivable.getReimbursements()) {
      // Test the reimbursement item getter and the item itself.
      assertTrue("wrong reimbursement in receivable",
                 reimbursement.getReimbursingItem().equals(reimbursingItem));
      // Test the receivable getter for the reimbursement.
      assertTrue("could not get receivable for reimbursement",
                 reimbursement.getReceivable().equals(receivable));
      // Test the allocatedAmount getter for the reimbursement.
      assertTrue("could not get reimbursement amount for reimbursement",
                 reimbursement.getReimbursedAmount().equals(AMOUNT));
      // Test the allocatedAmount getter for the reimbursement.
      assertTrue("could not get allocated amount for reimbursement",
                 reimbursement.getAllocatedAmount().equals(ALLOCATED_AMOUNT));
    }
    for (Item.Reimbursement reimbursement : reimbursingItem.getReimbursements()) {
      // Test the reimbursement item getter and the item itself.
      assertTrue("wrong reimbursement in receivable",
                 reimbursement.getReimbursingItem().equals(reimbursingItem));
      // Test the receivable getter for the reimbursement.
      assertTrue("could not get receivable for reimbursement",
                 reimbursement.getReceivable().equals(receivable));
      // Test the allocatedAmount getter for the reimbursement.
      assertTrue("could not get reimbursement amount for reimbursement",
                 reimbursement.getReimbursedAmount().equals(AMOUNT));
      // Test the allocatedAmount getter for the reimbursement.
      assertTrue("could not get allocated amount for reimbursement",
                 reimbursement.getAllocatedAmount().equals(ALLOCATED_AMOUNT));
    }
  }

  /**
   * Test system around valid receivable and reimbursement items and the
   * com.poesys.accounting.dataloader.newaccounting.Item.reimburse() method, along with the getters
   * for reimbursements and receivable. Also tests Transaction.addItem(), Transaction.isValid(),
   * Item.getReimbursements(), Reimbursement.getReceivable(), Reimbursement.getReimbursingItem(),
   * Reimbursement.getReimbursedAmount, and Reimbursement.getAllocatedAmount(). This version tests a
   * reimbursement that allocates the complete amount with no reimbursement amount.
   */
  @Test
  public void testValidAllocationReimbursement() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item
    Item receivable = receivableTransaction.addItem(AMOUNT, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue("invalid receivable transaction", receivableTransaction.isValid());

    // Create the reimbursing transaction with items.
    Transaction reimbursingTransaction =
      new Transaction(TRANSACTION_ID.add(BigInteger.ONE), DESCRIPTION, DATE, NOT_CHECKED,
                      NOT_BALANCE);
    Double reimbursementAmount = 1.17D;
    Double reimbursedAmount = 0.00D;
    // cash payment item
    Item payment =
      reimbursingTransaction.addItem(reimbursementAmount, CHECKING_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("payment item constructor failed", payment != null);
    // receivable reimbursement item
    Item reimbursingItem =
      reimbursingTransaction.addItem(reimbursementAmount, AR_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("reverse receivable item constructor failed", reimbursingItem != null);
    assertTrue("invalid reimbursing transaction", receivableTransaction.isValid());

    // Reimburse the receivable.
    receivable.reimburse(reimbursingItem, reimbursedAmount, reimbursementAmount);

    // Test the getReimbursements getter.
    assertTrue("no reimbursements in receivable", receivable.getReimbursements().size() > 0);

    for (Item.Reimbursement reimbursement : receivable.getReimbursements()) {
      // Test the reimbursement item getter and the item itself.
      assertTrue("wrong reimbursement in receivable",
                 reimbursement.getReimbursingItem().equals(reimbursingItem));
      // Test the receivable getter for the reimbursement.
      assertTrue("could not get receivable for reimbursement",
                 reimbursement.getReceivable().equals(receivable));
      // Test the allocatedAmount getter for the reimbursement.
      assertTrue("could not get reimbursement amount for reimbursement",
                 reimbursement.getReimbursedAmount().equals(reimbursedAmount));
      // Test the allocatedAmount getter for the reimbursement.
      assertTrue("could not get allocated amount for reimbursement",
                 reimbursement.getAllocatedAmount().equals(reimbursementAmount));
    }
    for (Item.Reimbursement reimbursement : reimbursingItem.getReimbursements()) {
      // Test the reimbursement item getter and the item itself.
      assertTrue("wrong reimbursement in receivable",
                 reimbursement.getReimbursingItem().equals(reimbursingItem));
      // Test the receivable getter for the reimbursement.
      assertTrue("could not get receivable for reimbursement",
                 reimbursement.getReceivable().equals(receivable));
      // Test the allocatedAmount getter for the reimbursement.
      assertTrue("could not get reimbursement amount for reimbursement",
                 reimbursement.getReimbursedAmount().equals(reimbursedAmount));
      // Test the allocatedAmount getter for the reimbursement.
      assertTrue("could not get allocated amount for reimbursement",
                 reimbursement.getAllocatedAmount().equals(reimbursementAmount));
    }
  }

  /**
   * Test error condition when the reimbursement has already occurred in a previous year.
   */
  @Test
  public void testPriorYearReimbursement() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, PRIOR_DATE, NOT_CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item
    Item receivable = receivableTransaction.addItem(AMOUNT, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue("invalid receivable transaction", receivableTransaction.isValid());

    // Create the prior-year reimbursing transaction with items.
    BigInteger transactionId = TRANSACTION_ID;

    Transaction reimbursingTransaction1 =
      new Transaction(transactionId.add(BigInteger.ONE), DESCRIPTION, PRIOR_DATE, NOT_CHECKED,
                      NOT_BALANCE);
    // cash payment item
    Item payment1 = reimbursingTransaction1.addItem(AMOUNT, CHECKING_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("payment item constructor failed", payment1 != null);
    // receivable reimbursement item
    Item reimbursingItem1 =
      reimbursingTransaction1.addItem(AMOUNT, AR_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("reverse receivable item constructor failed", reimbursingItem1 != null);
    assertTrue("invalid reimbursing transaction", receivableTransaction.isValid());

    // Reimburse the receivable.
    receivable.reimburse(reimbursingItem1, AMOUNT, ALLOCATED_AMOUNT);

    // Create the reimbursing transaction with items.
    Transaction reimbursingTransaction2 =
      new Transaction(transactionId.add(BigInteger.ONE), DESCRIPTION, DATE, NOT_CHECKED,
                      NOT_BALANCE);
    // cash payment item
    Item payment2 = reimbursingTransaction2.addItem(AMOUNT, CHECKING_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("payment item constructor failed", payment2 != null);
    // receivable reimbursement item
    Item reimbursingItem2 =
      reimbursingTransaction2.addItem(AMOUNT, AR_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("reverse receivable item constructor failed", reimbursingItem2 != null);
    assertTrue("invalid reimbursing transaction", receivableTransaction.isValid());

    try {
      // Reimburse the receivable.
      receivable.reimburse(reimbursingItem2, AMOUNT, ALLOCATED_AMOUNT);
      fail("Prior year reimbursement test did not generate exception");
    } catch (Exception e) {
      assertTrue("wrong exception for prior-year reimbursement error",
                 e.getMessage().contains("check for prior-year reimbursement"));
      logger.error(e.getMessage());
    }
  }

  /**
   * Test the case when the reimbursement amount exceeds the original amount.
   */
  @Test
  public void testInvalidReimbursement() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item
    Item receivable = receivableTransaction.addItem(AMOUNT, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue("invalid receivable transaction", receivableTransaction.isValid());

    // Create the reimbursing transaction with items.
    Transaction reimbursingTransaction =
      new Transaction(TRANSACTION_ID.add(BigInteger.ONE), DESCRIPTION, DATE, NOT_CHECKED,
                      NOT_BALANCE);
    // cash payment item--add $100 to take it past the original amount.
    Item payment =
      reimbursingTransaction.addItem(AMOUNT + 100.00D, CHECKING_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("payment item constructor failed", payment != null);
    // receivable reimbursement item
    Item reimbursingItem = reimbursingTransaction.addItem(AMOUNT, AR_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("reverse receivable item constructor failed", reimbursingItem != null);
    assertTrue("invalid reimbursing transaction", receivableTransaction.isValid());
    try {
      receivable.reimburse(reimbursingItem, AMOUNT + 100.00D, 0.00D);
      fail("Failed to throw exception on excessive reimbursement amount");
    } catch (InvalidParametersException e) {
      // success
    }
  }

  /**
   * Test the case when the allocated amount exceeds the original amount.
   */
  @Test
  public void testInvalidAllocationReimbursement() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item
    Item receivable = receivableTransaction.addItem(AMOUNT, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue("invalid receivable transaction", receivableTransaction.isValid());

    // Create the reimbursing transaction with items.
    Transaction reimbursingTransaction =
      new Transaction(TRANSACTION_ID.add(BigInteger.ONE), DESCRIPTION, DATE, NOT_CHECKED,
                      NOT_BALANCE);
    // cash payment item--add $100 to take it past the original amount.
    Item payment = reimbursingTransaction.addItem(AMOUNT, CHECKING_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("payment item constructor failed", payment != null);
    // receivable reimbursement item
    Item reimbursingItem = reimbursingTransaction.addItem(AMOUNT, AR_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("reverse receivable item constructor failed", reimbursingItem != null);
    assertTrue("invalid reimbursing transaction", receivableTransaction.isValid());
    try {
      // Set allocated amount higher than receivable amount.
      receivable.reimburse(reimbursingItem, AMOUNT, 200.00D);
      fail("Failed to throw exception on excessive allocated amount");
    } catch (RuntimeException e) {
      // success
    }
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#hashCode()}
   * .
   */
  @Test
  public void testTransactionHashCodeEquality() {
    Transaction transaction1 =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    Transaction transaction2 =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    assertTrue("hash codes don't match for equal transactions",
               transaction1.hashCode() == transaction2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Transaction#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testTransactionEqualsObjectEquality() {
    Transaction transaction1 =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    Transaction transaction2 =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    assertTrue("equal transactions don't compare with equals()", transaction1.equals(transaction2));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#hashCode()} .
   * Tests where transactions are different objects with different identity
   */
  @Test
  public void testTransactionHashCodeInequality() {
    Transaction transaction1 =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    Transaction transaction2 =
      new Transaction(TRANSACTION_ID.add(BigInteger.ONE), DESCRIPTION + " different", DATE,
                      NOT_CHECKED, NOT_BALANCE);
    assertTrue("hash codes match for unequal transactions",
               transaction1.hashCode() != transaction2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Transaction#equals(java.lang.Object)}
   * . Tests where transactions are different objects with different identity.
   */
  @Test
  public void testTransactionEqualsObjectInequality() {
    Transaction transaction1 =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    Transaction transaction2 =
      new Transaction(TRANSACTION_ID.add(BigInteger.ONE), DESCRIPTION, DATE, NOT_CHECKED,
                      NOT_BALANCE);
    assertTrue("unequal transactions compare as equals", !transaction1.equals(transaction2));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Transaction#equals(java.lang.Object)}
   * . Tests where comparison input is null.
   */
  @Test
  public void testTransactionEqualsNullObject() {
    Transaction transaction1 =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    assertTrue("equals with null input compares as equal", !transaction1.equals(null));
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Transaction#equals(java.lang.Object)}
   * . Tests where comparison input is different class.
   */
  @Test
  public void testTransactionEqualsDifferentClass() {
    Transaction transaction1 =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    assertTrue("equals with different class compares as equal",
               !transaction1.equals(new Integer(1234)));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#isValid()} .
   * Tests case where isValid must return false.
   */
  @Test
  public void testIsValidFalse() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item with different amount
    Item receivable =
      receivableTransaction.addItem(AMOUNT + 10.00D, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue("valid receivable transaction but differing amounts: " + receivableTransaction,
               !receivableTransaction.isValid());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#isValid()} .
   * Tests case where isValid must return true for balance transaction with single item.
   */
  @Test
  public void testIsValidBalance() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, !NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    assertTrue("invalid receivable balance transaction but correct item: " + receivableTransaction,
               receivableTransaction.isValid());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#isValid()} .
   * Tests case where isValid must return true based on 2 items balancing.
   */
  @Test
  public void testIsValidTrue2Items() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item with same amount
    Item receivable = receivableTransaction.addItem(AMOUNT, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue("invalid receivable transaction but same amounts: " + receivableTransaction,
               receivableTransaction.isValid());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#isValid()} .
   * Tests case where isValid finds no items in a balance transaction.
   */
  @Test
  public void testIsValidBalanceNoItems() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, !NOT_BALANCE);
    assertTrue("valid balance transaction but no items: " + receivableTransaction,
               !receivableTransaction.isValid());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#isValid()} .
   * Tests case where isValid finds two items in a balance transaction.
   */
  @Test
  public void testIsValidBalance2Items() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, !NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item with same amount
    Item receivable = receivableTransaction.addItem(AMOUNT, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue("valid balance transaction but two items: " + receivableTransaction,
               !receivableTransaction.isValid());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#isValid()} .
   * Tests case where isValid finds one items in a non-balance transaction.
   */
  @Test
  public void testIsValidSingleItem() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    assertTrue("valid non-balance transaction but one item: " + receivableTransaction,
               !receivableTransaction.isValid());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#isValid()} .
   * Tests case where isValid must return true based on 3 items balancing.
   */
  @Test
  public void testIsValidTrue3Items() {
    // Create the receivable transaction with items.
    Transaction transaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    // personal capital 1 item
    Item cap1 = transaction.addItem(7.48D, CAPITAL_ACCOUNT_1, CREDIT, NOT_CHECKED);
    assertTrue("capital 1 item constructor failed", cap1 != null);
    // personal capital 2 item
    Item cap2 = transaction.addItem(7.47D, CAPITAL_ACCOUNT_2, CREDIT, NOT_CHECKED);
    assertTrue("capital 2 item constructor failed", cap2 != null);
    // internet service item with total amount
    Item internet = transaction.addItem(14.95D, INTERNET_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("internet item constructor failed", internet != null);
    assertTrue("invalid transaction but same amounts: " + transaction, transaction.isValid());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#isChecked()}
   * . Tests case where an item is not checked while the transaction is checked, which should return
   * false for transaction.isChecked().
   */
  @Test
  public void testIsCheckedWithErrors() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item with checked flag set off
    Item receivable = receivableTransaction.addItem(AMOUNT, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue(
      "transaction checked, item not checked, but transaction not checked" + receivableTransaction,
      !receivableTransaction.isChecked());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#isChecked()}
   * . Tests case where an item is not checked while the transaction is checked, which should return
   * false for transaction.isChecked().
   */
  @Test
  public void testIsCheckedWithInvalidTransactionCheck() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, !CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item with checked flag set off
    Item receivable = receivableTransaction.addItem(AMOUNT, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue("transaction checked but item checked" + receivableTransaction,
               !receivableTransaction.isChecked());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#toString()}
   * .
   */
  @Test
  public void testTransactionToString() {
    // Create the receivable transaction with items.
    Transaction receivableTransaction =
      new Transaction(TRANSACTION_ID, DESCRIPTION, DATE, NOT_CHECKED, NOT_BALANCE);
    // income item
    Item income = receivableTransaction.addItem(AMOUNT, INCOME_ACCOUNT, CREDIT, NOT_CHECKED);
    assertTrue("income item constructor failed", income != null);
    // receivable item
    Item receivable = receivableTransaction.addItem(AMOUNT, AR_ACCOUNT, DEBIT, NOT_CHECKED);
    assertTrue("receivable item constructor failed", receivable != null);
    assertTrue("toString failed: " + receivableTransaction, receivableTransaction.toString().equals(
      "Transaction [id=1234, description=description, date=2017-05-01 00:00:00.0, checked=false, " +
      "" + "" + "balance=false, items=[Item [year=2017, transaction=1234, " +
      "description=description, " + "amount=10.0, account=Account [name=Accounts Receivable, " +
      "description=description, " + "debitDefault=true, receivable=true, years=[]], debit=true," +
      " checked=false], Item " + "[year=2017, transaction=1234, description=description, " +
      "amount=10.0, account=Account " + "[name=Revenue, description=description, " +
      "debitDefault=false, receivable=false, years=[]]," + "" + " debit=false, " +
      "checked=false]]]"));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Item#hashCode()}.
   */
  @Test
  public void testItemHashCodeEquality() {
    Transaction transaction1 = createTransactionWithItems(TRANSACTION_ID);
    Transaction transaction2 = createTransactionWithItems(TRANSACTION_ID);

    // Extract the "same" item, items with same id and account.
    Item item1 = null;
    Item item2 = null;
    for (Item item : transaction1.getItems()) {
      item1 = item;
      for (Item otherItem : transaction2.getItems()) {
        if (otherItem.getAccount().equals(item.getAccount()) &&
            otherItem.getTransaction().equals(item.getTransaction())) {
          item2 = otherItem;
          break;
        }
      }
      break; // one item only
    }
    assertTrue("Items identical but hashcodes aren't the same",
               item1 != null && item2 != null && item1.hashCode() == item2.hashCode());
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Item#hashCode()}.
   */
  @Test
  public void testItemHashCodeInequality() {
    Transaction transaction1 = createTransactionWithItems(TRANSACTION_ID);
    Transaction transaction2 = createTransactionWithItems(TRANSACTION_ID.add(BigInteger.ONE));

    // Extract the "same" item, items with same account but not the same id.
    Item item1 = null;
    Item item2 = null;
    for (Item item : transaction1.getItems()) {
      item1 = item;
      for (Item otherItem : transaction2.getItems()) {
        if (otherItem.getAccount().equals(item.getAccount())) {
          item2 = otherItem;
          break;
        }
      }
      break; // one item only
    }
    assertTrue("Items different but have same hash codes",
               item1 != null && item2 != null && item1.hashCode() != item2.hashCode());
  }

  /**
   * Test method for
   * {@link com.poesys.accounting.dataloader.newaccounting.Item#equals(java.lang.Object)}
   * .
   */
  @Test
  public void testItemEqualsObject() {
    Transaction transaction1 = createTransactionWithItems(TRANSACTION_ID);
    Transaction transaction2 = createTransactionWithItems(TRANSACTION_ID);

    // Extract the "same" item, items with same account.
    Item item1 = null;
    Item item2 = null;
    for (Item item : transaction1.getItems()) {
      item1 = item;
      for (Item otherItem : transaction2.getItems()) {
        if (otherItem.getAccount().equals(item.getAccount())) {
          item2 = otherItem;
          break;
        }
      }
      break; // one item only
    }
    assertTrue("Identical items but equals not true",
               item1 != null && item2 != null && item1.equals(item2));
  }

  /**
   * Test method for {@link com.poesys.accounting.dataloader.newaccounting.Item#toString()}.
   */
  @Test
  public void testItemToString() {
    Transaction transaction = createTransactionWithItems(TRANSACTION_ID);
    Item incomeItem = null;
    // Get the Income account item.
    for (Item item : transaction.getItems()) {
      if (item.getAccount().getName().equals(INCOME_ACCOUNT_NAME)) {
        incomeItem = item;
        break;
      }
    }
    assertTrue("Income item not found", incomeItem != null);
    assertTrue("item string incorrect: " + incomeItem, incomeItem.toString().equals(
      "Item [year=2017, transaction=1234, description=description, amount=10.0, account=Account " +
      "[name=Revenue, description=description, debitDefault=false, receivable=false, " +
      "years=[]]," + " debit=false, checked=false]"));
  }

  /**
   *   Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#isZero()}.
   *   Tests case when the items are all zero items.
   */
  @Test
  public void testIsZeroTrue() {
    Transaction transaction = createTransactionWithZeroItems(TRANSACTION_ID);
    assertTrue("transaction with 2 zero items has isZero false", transaction.isZero());
  }

  /**
   *   Test method for {@link com.poesys.accounting.dataloader.newaccounting.Transaction#isZero()}.
   *   Tests case when the items are all non-zero items.
   */
  @Test
  public void testIsZeroFalse() {
    Transaction transaction = createTransactionWithItems(TRANSACTION_ID);
    assertTrue("transaction with 2 non-zero items has isZero true", !transaction.isZero());
  }
}
