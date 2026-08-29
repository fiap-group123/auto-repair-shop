package br.com.autorepairshop.authentication.infrastructure.persistence

import br.com.autorepairshop.authentication.domain.aggregate.User
import br.com.autorepairshop.authentication.domain.repository.UserRepository
import br.com.autorepairshop.authentication.domain.valueobject.HashedPassword
import br.com.autorepairshop.authentication.domain.valueobject.LoginEmail
import br.com.autorepairshop.authentication.domain.valueobject.Role
import br.com.autorepairshop.authentication.domain.valueobject.UserId
import org.springframework.stereotype.Repository
import java.util.UUID
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class UserRepositoryImpl(private val jpa: UserJpaRepository) : UserRepository {

    override fun save(user: User) {
        jpa.save(user.toEntity())
    }

    override fun findById(id: UserId): User? = jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findByEmail(email: LoginEmail): User? = jpa.findByEmail(email = email.value)?.toDomain()

    override fun existsByEmail(email: LoginEmail): Boolean = jpa.existsByEmail(email = email.value)

    override fun existsByCustomerId(customerId: UUID): Boolean = jpa.existsByCustomerId(customerId = customerId)

    override fun existsAny(): Boolean = jpa.count() > 0

    private fun User.toEntity() = UserEntity(
        id = id.value,
        email = email.value,
        passwordHash = hashedPassword.value,
        role = UserRoleColumn.valueOf(value = role.name),
        active = active,
        customerId = customerId,
        createdAt = createdAt.toJavaInstant(),
    )

    private fun UserEntity.toDomain() = User.rehydrate(
        id = UserId(value = id),
        email = LoginEmail.of(raw = email),
        hashedPassword = HashedPassword(value = passwordHash),
        role = Role.valueOf(value = role.name),
        active = active,
        customerId = customerId,
        createdAt = createdAt.toKotlinInstant(),
    )
}
