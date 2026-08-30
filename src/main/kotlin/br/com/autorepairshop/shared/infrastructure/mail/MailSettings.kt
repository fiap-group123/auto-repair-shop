package br.com.autorepairshop.shared.infrastructure.mail

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MailSettings(
    @Value("\${app.mail.from}") val from: String,
    @Value("\${app.mail.invite-base-url}") val inviteBaseUrl: String,
)
