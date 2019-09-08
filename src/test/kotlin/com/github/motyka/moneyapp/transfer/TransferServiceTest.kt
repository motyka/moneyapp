package com.github.motyka.moneyapp.transfer;

import com.github.motyka.moneyapp.account.Account
import com.github.motyka.moneyapp.account.AccountService
import com.github.motyka.moneyapp.exception.ConstrainViolationException
import io.mockk.junit5.MockKExtension
import org.javalite.activejdbc.test.DBSpec
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class TransferServiceTest : DBSpec() {

    private var transferService = TransferService(AccountService())

    @Test
    fun `add transfer`() {
        val senderId = saveAccount(BigDecimal(3000))
        val recipientId = saveAccount(BigDecimal(1000))
        val transfer = createTransfer(senderId, recipientId, BigDecimal(500))

        val transferResult = transferService.create(transfer)

        val sender = findAccount(senderId)
        val recipient = findAccount(recipientId)

        assertEquals(senderId, transferResult.senderId)
        assertEquals(recipientId, transferResult.recipientId)
        assertTrue(transferResult.succeed)
        assertNull(transfer.errorMessage)
        assertEquals(BigDecimal(500), transferResult.amount)
        assertEquals(BigDecimal(2500), sender.balance)
        assertEquals(BigDecimal(1500), recipient.balance)
    }

    @Test
    fun `add transfer with insufficient funds`() {
        val senderId = saveAccount(BigDecimal(300))
        val recipientId = saveAccount(BigDecimal(1000))
        val transfer = createTransfer(senderId, recipientId, BigDecimal(500))

        val transferResult = transferService.create(transfer)

        val sender = findAccount(senderId)
        val recipient = findAccount(recipientId)

        assertEquals(senderId, transferResult.senderId)
        assertEquals(recipientId, transferResult.recipientId)
        assertFalse(transferResult.succeed)
        assertEquals("Not enough funds", transfer.errorMessage)
        assertEquals(BigDecimal(500), transferResult.amount)
        assertEquals(BigDecimal(300), sender.balance)
        assertEquals(BigDecimal(1000), recipient.balance)
    }

    @Test
    fun `add transfer with not existing sender`() {
        val recipientId = saveAccount(BigDecimal(1000))
        val transfer = createTransfer(-1, recipientId, BigDecimal(500))

        assertThrows<ConstrainViolationException> { transferService.create(transfer) }
    }

    @Test
    fun `add transfer with not existing recipient`() {
        val senderId = saveAccount(BigDecimal(300))
        val transfer = createTransfer(senderId, -1, BigDecimal(500))

        assertThrows<ConstrainViolationException> { transferService.create(transfer) }
    }

    @Test
    fun `find transfer`() {
        val senderId = saveAccount(BigDecimal(3000))
        val recipientId = saveAccount(BigDecimal(1000))
        val transferId = saveTransfer(senderId, recipientId, BigDecimal(500))

        val transferResult = transferService.findById(transferId)

        assertEquals(senderId, transferResult.senderId)
        assertEquals(recipientId, transferResult.recipientId)
        assertTrue(transferResult.succeed)
        assertEquals(BigDecimal(500), transferResult.amount)
    }

    @Test
    fun `find all transfers`() {
        val senderId = saveAccount(BigDecimal(3000))
        val recipientId = saveAccount(BigDecimal(1000))
        saveTransfer(senderId, recipientId, BigDecimal(500))
        saveTransfer(senderId, recipientId, BigDecimal(600))
        saveTransfer(senderId, recipientId, BigDecimal(700))

        val list = transferService.findAll()

        assertEquals(3, list.size)
    }

    private fun findAccount(id: Long): Account {
        return Account.findById(id)
    }

    private fun createTransfer(senderId: Long, recipientId: Long, amount: BigDecimal): Transfer {
        val transfer = Transfer()
        transfer.senderId = senderId
        transfer.recipientId = recipientId
        transfer.amount = amount
        return transfer
    }

    private fun saveTransfer(senderId: Long, recipientId: Long, amount: BigDecimal): Long {
        val transfer = Transfer()
        transfer.senderId = senderId
        transfer.recipientId = recipientId
        transfer.amount = amount
        transfer.succeed = true
        transfer.save()
        return transfer.id.toString().toLong()
    }

    private fun saveAccount(balance: BigDecimal): Long {
        val account = Account()
        account.setBigDecimal<Account>("balance", balance)
        account.setBoolean<Account>("active", true)
        account.save()
        return account.id.toString().toLong()
    }
}
