package br.com.autorepairshop.shared.application.mail

interface EmailSender {
    fun send(
        to: String,
        subject: String,
        body: String,
    )
}
