package com.github.motyka.moneyapp.account

import com.github.motyka.moneyapp.exception.ConstrainViolationException
import com.github.motyka.moneyapp.exception.ResourceNotFoundException
import io.mockk.junit5.MockKExtension
import org.javalite.activejdbc.test.DBSpec
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class AccountServiceTest : DBSpec() {

    private var accountService = AccountService()

    @Test
    fun `add account`() {
        var account = Account()
        account.balance = BigDecimal(123)

        account = accountService.create(account)

        assertNotNull(account.id)
        assertEquals(BigDecimal(123), account.balance)
        assertTrue(account.isActive)
    }

    @Test
    fun `delete account`() {
        val id = saveAccount(BigDecimal(0))

        accountService.delete(id)

        val deletedAccount = findAccount(id)

        assertEquals(id, deletedAccount.id)
        assertFalse(deletedAccount.isActive)
    }

    @Test
    fun `delete account with funds`() {
        val id = saveAccount(BigDecimal(300))

        assertThrows<ConstrainViolationException> { accountService.delete(id) }
    }

    @Test
    fun `delete account with debit`() {
        val id = saveAccount(BigDecimal(-300))

        assertThrows<ConstrainViolationException> { accountService.delete(id) }
    }

    @Test
    fun `find account`() {
        val id = saveAccount(BigDecimal(300))

        val account = accountService.findById(id)

        assertEquals(id, account.id)
        assertEquals(BigDecimal(300), account.balance)
        assertTrue(account.isActive)
    }

    @Test
    fun `find all active accounts`() {
        saveAccount(BigDecimal(300))
        saveAccount(BigDecimal(500))
        val id3 = saveAccount(BigDecimal(200))
        saveAccount(BigDecimal(1000))

        val account = findAccount(id3)
        account.isActive = false
        account.save()

        val list = accountService.findAll()

        assertEquals(3, list.size)
    }

    @Test
    fun `transfer funds`() {
        val senderId = saveAccount(BigDecimal(3000))
        val recipientId = saveAccount(BigDecimal(1000))

        accountService.transferFunds(senderId, recipientId, BigDecimal(500))

        val sender = findAccount(senderId)
        val recipient = findAccount(recipientId)

        assertEquals(BigDecimal(2500), sender.balance)
        assertEquals(BigDecimal(1500), recipient.balance)
    }

    @Test
    fun `not enough funds to transfer`() {
        val senderId = saveAccount(BigDecimal(300))
        val recipientId = saveAccount(BigDecimal(1000))

        assertThrows<ConstrainViolationException> { accountService.transferFunds(senderId, recipientId, BigDecimal(500)) }
    }

    @Test
    fun `transfer funds, not existing sender`() {
        val recipientId = saveAccount(BigDecimal(1000))

        assertThrows<ResourceNotFoundException> { accountService.transferFunds(-1, recipientId, BigDecimal(500)) }
    }

    @Test
    fun `transfer funds, not existing recipient`() {
        val senderId = saveAccount(BigDecimal(300))

        assertThrows<ResourceNotFoundException> { accountService.transferFunds(senderId, -1, BigDecimal(500)) }
    }

    private fun findAccount(id: Long): Account {
        return Account.findById(id)
    }

    private fun saveAccount(balance: BigDecimal): Long {
        val account = Account()
        account.balance = balance
        account.isActive = true
        account.save()
        return account.id.toString().toLong()
    }
}