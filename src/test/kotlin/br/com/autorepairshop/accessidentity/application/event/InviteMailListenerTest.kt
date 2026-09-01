package br.com.autorepairshop.accessidentity.application.event

import br.com.autorepairshop.accessidentity.domain.event.CustomerInviteIssued
import br.com.autorepairshop.shared.application.mail.EmailSender
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@Tag("unit")
class InviteMailListenerTest {
    private val emails = mockk<EmailSender>(relaxUnitFun = true)
    private val listener = InviteMailListener(
        emails = emails,
        inviteBaseUrl = "http://localhost:8080/invite",
    )

    @Test
    fun `sends the invite link to the contact email`() {
        val event = issued()

        listener.on(event = event)

        verify {
            emails.send(
                to = event.contactEmail,
                subject = any(),
                body = match { it.contains(other = "/${event.rawToken}") },
            )
        }
    }

    @Test
    fun `appends token with ampersand when the base url already has a query`() {
        val withQuery = InviteMailListener(
            emails = emails,
            inviteBaseUrl = "app://invite?source=mail",
        )
        val event = issued()

        withQuery.on(event = event)

        verify {
            emails.send(
                to = event.contactEmail,
                subject = any(),
                body = match { it.contains(other = "source=mail&token=${event.rawToken}") },
            )
        }
    }

    @Test
    fun `swallows send failures`() {
        val event = issued()
        every { emails.send(to = any(), subject = any(), body = any()) } throws IllegalStateException("smtp down")

        listener.on(event = event)
    }

    private fun issued() = CustomerInviteIssued(
        customerId = UUID.randomUUID(),
        customerName = "Ana Souza",
        contactEmail = "ana.souza@email.com",
        rawToken = "raw-token",
        occurredOn = Instant.now(),
    )
}
