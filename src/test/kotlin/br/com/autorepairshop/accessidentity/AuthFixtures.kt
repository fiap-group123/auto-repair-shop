package br.com.autorepairshop.accessidentity

import br.com.autorepairshop.accessidentity.domain.aggregate.User
import br.com.autorepairshop.accessidentity.domain.valueobject.HashedPassword
import br.com.autorepairshop.accessidentity.domain.valueobject.LoginEmail
import br.com.autorepairshop.accessidentity.domain.valueobject.Role
import br.com.autorepairshop.accessidentity.domain.valueobject.UserId
import java.util.UUID
import kotlin.time.Clock

object AuthFixtures {
    const val MANAGER_EMAIL = "gerente@oficina.com"
    const val CLIENT_EMAIL = "john.doe@email.com"
    const val RAW_PASSWORD = "senha123"

    fun hashedPassword(): HashedPassword = HashedPassword(value = "hashed")

    fun manager(): User = User.register(
        email = LoginEmail.of(raw = MANAGER_EMAIL),
        hashedPassword = hashedPassword(),
        role = Role.MANAGER,
    )

    fun client(customerId: UUID = UUID.randomUUID()): User = User.register(
        email = LoginEmail.of(raw = CLIENT_EMAIL),
        hashedPassword = hashedPassword(),
        role = Role.CLIENT,
        customerId = customerId,
    )

    fun inactiveManager(): User = User.rehydrate(
        id = UserId.generate(),
        email = LoginEmail.of(raw = MANAGER_EMAIL),
        hashedPassword = hashedPassword(),
        role = Role.MANAGER,
        active = false,
        customerId = null,
        createdAt = Clock.System.now(),
    )
}
