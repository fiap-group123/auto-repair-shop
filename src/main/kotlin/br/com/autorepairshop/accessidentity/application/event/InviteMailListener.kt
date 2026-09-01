package br.com.autorepairshop.accessidentity.application.event

import br.com.autorepairshop.accessidentity.domain.event.CustomerInviteIssued
import br.com.autorepairshop.shared.application.mail.EmailSender
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class InviteMailListener(
    private val emails: EmailSender,
    @Value("\${app.mail.invite-base-url}") private val inviteBaseUrl: String,
) {
    private val log = LoggerFactory.getLogger(InviteMailListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: CustomerInviteIssued) {
        val link = inviteLink(
            base = inviteBaseUrl,
            token = event.rawToken,
        )
        runCatching {
            emails.send(
                to = event.contactEmail,
                subject = "Crie seu acesso a oficina",
                body = """
                    Ola, ${event.customerName}.

                    Sua conta na oficina esta pronta para ser criada. Abra o link e escolha o e-mail de login e a senha:

                    $link

                    O link expira em 72 horas.
                """.trimIndent(),
            )
        }.onFailure { error ->
            log.warn("Failed to send invite email to customer {}", event.customerId, error)
        }
    }

    private fun inviteLink(
        base: String,
        token: String,
    ): String = when {
        base.contains(other = "?") -> {
            val separator = if (base.endsWith(suffix = "?") || base.endsWith(suffix = "&")) "" else "&"
            "${base}${separator}token=$token"
        }

        base.contains(other = "#") -> {
            val separator = if (base.endsWith(suffix = "#") || base.endsWith(suffix = "&")) "" else "&"
            "${base}${separator}token=$token"
        }

        else -> "${base.trimEnd('/')}/$token"
    }
}
