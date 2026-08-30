package br.com.autorepairshop.shared.infrastructure.mail

import br.com.autorepairshop.shared.application.mail.EmailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class JavaMailEmailSender(
    private val mail: JavaMailSender,
    private val settings: MailSettings,
) : EmailSender {

    override fun send(
        to: String,
        subject: String,
        body: String,
    ) {
        val message = SimpleMailMessage()
        message.setFrom(settings.from)
        message.setTo(to)
        message.subject = subject
        message.text = body
        mail.send(message)
    }
}
