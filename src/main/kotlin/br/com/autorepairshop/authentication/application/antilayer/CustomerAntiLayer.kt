package br.com.autorepairshop.authentication.application.antilayer

import java.util.UUID

interface CustomerAntiLayer {
    fun find(id: UUID): CustomerRecord?
}
