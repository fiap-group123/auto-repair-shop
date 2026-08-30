package br.com.autorepairshop.authentication.application.directory

import java.util.UUID

interface CustomerDirectory {
    fun find(id: UUID): CustomerRecord?
}
