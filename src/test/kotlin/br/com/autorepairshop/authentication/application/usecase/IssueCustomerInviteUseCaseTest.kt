package br.com.autorepairshop.authentication.application.usecase

import br.com.autorepairshop.authentication.application.directory.CustomerDirectory
import br.com.autorepairshop.authentication.application.directory.CustomerRecord
import br.com.autorepairshop.authentication.domain.aggregate.CustomerInvite
import br.com.autorepairshop.authentication.domain.event.CustomerInviteIssued
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.repository.CustomerInviteRepository
import br.com.autorepairshop.authentication.domain.repository.UserRepository
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.aggregate.Customer
import br.com.autorepairshop.shared.application.event.EventPublisher
import br.com.autorepairshop.shared.domain.DomainEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class IssueCustomerInviteUseCaseTest {
    private val customers = mockk<CustomerDirectory>()
    private val users = mockk<UserRepository>()
    private val invites = mockk<CustomerInviteRepository>(relaxUnitFun = true)
    private val events = mockk<EventPublisher>(relaxUnitFun = true)
    private val useCase = IssueCustomerInviteUseCase(
        customers = customers,
        users = users,
        invites = invites,
        events = events,
    )

    @Test
    fun `issues an invite and publishes the raw token`() {
        val customer = CustomerFixtures.activeCustomer().toRecord()
        every { customers.find(id = customer.id) } returns customer
        every { users.existsByCustomerId(customerId = customer.id) } returns false
        every { invites.findOpenByCustomerId(customerId = customer.id) } returns emptyList()
        val event = slot<DomainEvent>()
        every { events.publish(event = capture(event)) } returns Unit

        useCase.execute(input = customer.id)

        val issued = event.captured as CustomerInviteIssued
        assertEquals(
            expected = customer.email,
            actual = issued.contactEmail,
        )
        assertTrue(issued.rawToken.isNotBlank())
        verify { invites.save(invite = any()) }
    }

    @Test
    fun `revokes previous open invites`() {
        val customer = CustomerFixtures.activeCustomer().toRecord()
        val open = CustomerInvite.issue(customerId = customer.id).invite
        every { customers.find(id = customer.id) } returns customer
        every { users.existsByCustomerId(customerId = customer.id) } returns false
        every { invites.findOpenByCustomerId(customerId = customer.id) } returns listOf(element = open)

        useCase.execute(input = customer.id)

        assertTrue(open.consumedAt != null)
        verify(atLeast = 2) { invites.save(invite = any()) }
    }

    @Test
    fun `missing customer fails`() {
        val customerId = UUID.randomUUID()
        every { customers.find(id = any()) } returns null

        assertFailsWith<AuthenticationException.LinkedCustomerNotFound> {
            useCase.execute(input = customerId)
        }
    }

    @Test
    fun `inactive customer fails`() {
        val customer = CustomerFixtures.inactiveCustomer().toRecord()
        every { customers.find(id = customer.id) } returns customer

        assertFailsWith<AuthenticationException.LinkedCustomerInactive> {
            useCase.execute(input = customer.id)
        }
    }

    @Test
    fun `existing login fails`() {
        val customer = CustomerFixtures.activeCustomer().toRecord()
        every { customers.find(id = customer.id) } returns customer
        every { users.existsByCustomerId(customerId = customer.id) } returns true

        assertFailsWith<AuthenticationException.CustomerAlreadyHasUser> {
            useCase.execute(input = customer.id)
        }
        verify(exactly = 0) { invites.save(invite = any()) }
    }

    private fun Customer.toRecord() = CustomerRecord(
        id = id.value,
        name = name.value,
        email = contact.email.value,
        active = active,
    )
}
