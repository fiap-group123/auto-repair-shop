package br.com.autorepairshop.inputmanagment.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InventoryJpaRepository : JpaRepository<InventoryEntity, UUID> {
    fun existsByName(name: String): Boolean
}
