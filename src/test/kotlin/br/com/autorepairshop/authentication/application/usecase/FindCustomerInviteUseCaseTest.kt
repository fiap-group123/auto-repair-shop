package br.com.autorepairshop.authentication.application.usecase

import br.com.autorepairshop.authentication.application.antilayer.CustomerAntiLayer
import br.com.autorepairshop.authentication.application.antilayer.CustomerRecord
import br.com.autorepairshop.authentication.domain.InviteToken
import br.com.autorepairshop.authentication.domain.aggregate.CustomerInvite
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.repository.CustomerInviteRepository
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.aggregate.Customer
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Tag("unit")
class FindCustomerInviteUseCaseTest {
    private val invites = mockk<CustomerInviteRepository>()
    private val customers = mockk<CustomerAntiLayer>()
    private val useCase = FindCustomerInviteUseCase(
        invites = invites,
        customers = customers,
    )

    @Test
    fun `returns the customer name when the invite is usable`() {
        val customer = CustomerFixtures.activeCustomer()
        val issued = CustomerInvite.issue(customerId = customer.id.value)
        every { invites.findByTokenHash(tokenHash = InviteToken.hash(raw = issued.rawToken)) } returns issued.invite
        every { customers.find(id = customer.id.value) } returns customer.toRecord()

        val response = useCase.execute(input = issued.rawToken)

        assertEquals(
            expected = customer.name.value,
            actual = response.customerName,
        )
    }

    @Test
    fun `missing invite fails`() {
        every { invites.findByTokenHash(tokenHash = any()) } returns null

        assertFailsWith<AuthenticationException.InviteNotFound> {
            useCase.execute(input = "missing")
        }
    }

    @Test
    fun `expired invite fails`() {
        val at = Instant.parse("2020-01-01T00:00:00Z")
        val issued = CustomerInvite.issue(
            customerId = CustomerFixtures.activeCustomer().id.value,
            at = at,
            ttl = 1.hours,
        )
        every { invites.findByTokenHash(tokenHash = any()) } returns issued.invite

        assertFailsWith<AuthenticationException.InviteExpired> {
            useCase.execute(input = issued.rawToken)
        }
    }

    @Test
    fun `consumed invite fails`() {
        val issued = CustomerInvite.issue(customerId = CustomerFixtures.activeCustomer().id.value)
        issued.invite.consume()
        every { invites.findByTokenHash(tokenHash = any()) } returns issued.invite

        assertFailsWith<AuthenticationException.InviteConsumed> {
            useCase.execute(input = issued.rawToken)
        }
    }

    @Test
    fun `orphan invite fails when the customer is gone`() {
        val issued = CustomerInvite.issue(customerId = CustomerFixtures.activeCustomer().id.value)
        every { invites.findByTokenHash(tokenHash = any()) } returns issued.invite
        every { customers.find(id = any()) } returns null

        assertFailsWith<AuthenticationException.LinkedCustomerNotFound> {
            useCase.execute(input = issued.rawToken)
        }
    }

    private fun Customer.toRecord() = CustomerRecord(
        id = id.value,
        name = name.value,
        email = contact.email.value,
        active = active,
    )
}
