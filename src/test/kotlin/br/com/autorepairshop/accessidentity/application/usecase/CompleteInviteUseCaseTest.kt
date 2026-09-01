package br.com.autorepairshop.accessidentity.application.usecase

import br.com.autorepairshop.accessidentity.AuthFixtures
import br.com.autorepairshop.accessidentity.application.antilayer.CustomerAntiLayer
import br.com.autorepairshop.accessidentity.application.antilayer.CustomerRecord
import br.com.autorepairshop.accessidentity.application.dto.CompleteInviteCommand
import br.com.autorepairshop.accessidentity.application.security.PasswordHasher
import br.com.autorepairshop.accessidentity.domain.InviteToken
import br.com.autorepairshop.accessidentity.domain.aggregate.CustomerInvite
import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.accessidentity.domain.repository.CustomerInviteRepository
import br.com.autorepairshop.accessidentity.domain.repository.UserRepository
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.aggregate.Customer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@Tag("unit")
class CompleteInviteUseCaseTest {
    private val invites = mockk<CustomerInviteRepository>(relaxUnitFun = true)
    private val users = mockk<UserRepository>(relaxUnitFun = true)
    private val customers = mockk<CustomerAntiLayer>()
    private val passwords = mockk<PasswordHasher>()
    private val useCase = CompleteInviteUseCase(
        invites = invites,
        users = users,
        customers = customers,
        passwords = passwords,
    )

    @Test
    fun `creates a client login and consumes the invite`() {
        val customer = CustomerFixtures.activeCustomer()
        val issued = CustomerInvite.issue(customerId = customer.id.value)
        stubUsableInvite(
            issued = issued,
            customer = customer,
        )
        every { users.existsByCustomerId(customerId = customer.id.value) } returns false
        every { users.existsByEmail(email = any()) } returns false
        every { passwords.hash(raw = AuthFixtures.RAW_PASSWORD) } returns AuthFixtures.hashedPassword()

        val response = useCase.execute(
            input = CompleteInviteCommand(
                token = issued.rawToken,
                email = AuthFixtures.CLIENT_EMAIL,
                password = AuthFixtures.RAW_PASSWORD,
            ),
        )

        assertEquals(
            expected = customer.id.value,
            actual = response.customerId,
        )
        assertEquals(
            expected = "CLIENT",
            actual = response.role,
        )
        assertNotNull(issued.invite.consumedAt)
        verify { users.save(user = any()) }
        verify { invites.save(invite = issued.invite) }
    }

    @Test
    fun `missing invite fails`() {
        every { invites.findByTokenHash(tokenHash = any()) } returns null

        assertFailsWith<AuthenticationException.InviteNotFound> {
            useCase.execute(input = command(token = "missing"))
        }
    }

    @Test
    fun `missing customer fails`() {
        val issued = CustomerInvite.issue(customerId = CustomerFixtures.activeCustomer().id.value)
        every { invites.findByTokenHash(tokenHash = InviteToken.hash(raw = issued.rawToken)) } returns issued.invite
        every { customers.find(id = any()) } returns null

        assertFailsWith<AuthenticationException.LinkedCustomerNotFound> {
            useCase.execute(input = command(token = issued.rawToken))
        }
    }

    @Test
    fun `inactive customer fails`() {
        val customer = CustomerFixtures.inactiveCustomer()
        val issued = CustomerInvite.issue(customerId = customer.id.value)
        stubUsableInvite(
            issued = issued,
            customer = customer,
        )

        assertFailsWith<AuthenticationException.LinkedCustomerInactive> {
            useCase.execute(input = command(token = issued.rawToken))
        }
    }

    @Test
    fun `existing login fails`() {
        val customer = CustomerFixtures.activeCustomer()
        val issued = CustomerInvite.issue(customerId = customer.id.value)
        stubUsableInvite(
            issued = issued,
            customer = customer,
        )
        every { users.existsByCustomerId(customerId = customer.id.value) } returns true

        assertFailsWith<AuthenticationException.CustomerAlreadyHasUser> {
            useCase.execute(input = command(token = issued.rawToken))
        }
        verify(exactly = 0) { users.save(user = any()) }
    }

    @Test
    fun `duplicate email fails`() {
        val customer = CustomerFixtures.activeCustomer()
        val issued = CustomerInvite.issue(customerId = customer.id.value)
        stubUsableInvite(
            issued = issued,
            customer = customer,
        )
        every { users.existsByCustomerId(customerId = customer.id.value) } returns false
        every { users.existsByEmail(email = any()) } returns true

        assertFailsWith<AuthenticationException.UserAlreadyExists> {
            useCase.execute(input = command(token = issued.rawToken))
        }
    }

    private fun stubUsableInvite(
        issued: CustomerInvite.Companion.Issued,
        customer: Customer,
    ) {
        every { invites.findByTokenHash(tokenHash = InviteToken.hash(raw = issued.rawToken)) } returns issued.invite
        every { customers.find(id = customer.id.value) } returns customer.toRecord()
    }

    private fun Customer.toRecord() = CustomerRecord(
        id = id.value,
        name = name.value,
        email = contact.email.value,
        active = active,
    )

    private fun command(token: String) = CompleteInviteCommand(
        token = token,
        email = AuthFixtures.CLIENT_EMAIL,
        password = AuthFixtures.RAW_PASSWORD,
    )
}
