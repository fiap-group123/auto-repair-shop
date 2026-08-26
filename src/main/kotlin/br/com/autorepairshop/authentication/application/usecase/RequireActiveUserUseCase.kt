package br.com.autorepairshop.authentication.application.usecase

import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.repository.UserRepository
import br.com.autorepairshop.authentication.domain.valueobject.UserId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RequireActiveUserUseCase(private val users: UserRepository) : UseCase<UUID, Unit> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID) {
        val user = users.findById(id = UserId(value = input))
            ?: throw AuthenticationException.UserNotFound(message = "User $input was not found.")
        if (!user.active) {
            throw AuthenticationException.UserInactive(message = "User is inactive.")
        }
    }
}
