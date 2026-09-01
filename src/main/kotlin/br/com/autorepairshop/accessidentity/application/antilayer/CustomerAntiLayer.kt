package br.com.autorepairshop.accessidentity.application.antilayer

import java.util.UUID

interface CustomerAntiLayer {
    fun find(id: UUID): CustomerRecord?
}
