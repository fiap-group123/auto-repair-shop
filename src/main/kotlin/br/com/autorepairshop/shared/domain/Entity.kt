package br.com.autorepairshop.shared.domain

abstract class Entity<ID : Any>(val id: ID) {
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity<*> || this::class != other::class) return false
        return id == other.id
    }

    final override fun hashCode(): Int = id.hashCode()
}