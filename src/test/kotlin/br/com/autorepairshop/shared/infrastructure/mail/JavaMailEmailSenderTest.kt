package br.com.autorepairshop.shared.infrastructure.mail

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import kotlin.test.assertEquals

@Tag("unit")
class JavaMailEmailSenderTest {
    private val mail = mockk<JavaMailSender>()
    private val sender = JavaMailEmailSender(
        mail = mail,
        settings = MailSettings(
            from = "oficina@localhost",
            inviteBaseUrl = "http://localhost:8080/invite",
        ),
    )

    @Test
    fun `sends a simple mail with the configured from address`() {
        val message = slot<SimpleMailMessage>()
        every { mail.send(capture(message)) } just Runs

        sender.send(
            to = "john.doe@email.com",
            subject = "Crie seu acesso a oficina",
            body = "token",
        )

        verify { mail.send(any<SimpleMailMessage>()) }
        assertEquals(expected = "oficina@localhost", actual = message.captured.from)
        assertEquals(expected = "john.doe@email.com", actual = message.captured.to?.single())
        assertEquals(expected = "Crie seu acesso a oficina", actual = message.captured.subject)
        assertEquals(expected = "token", actual = message.captured.text)
    }
}
