package br.com.autorepairshop.catalog.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OfferedServiceJpaRepository : JpaRepository<OfferedServiceEntity, UUID> {
    fun existsByName(name: String): Boolean
}
