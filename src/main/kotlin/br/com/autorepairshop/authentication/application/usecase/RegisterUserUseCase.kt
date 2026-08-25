package br.com.autorepairshop.authentication.application.usecase

import br.com.autorepairshop.authentication.application.dto.RegisterUserCommand
import br.com.autorepairshop.authentication.application.dto.UserResponse
import br.com.autorepairshop.authentication.application.dto.toResponse
import br.com.autorepairshop.authentication.application.security.PasswordHasher
import br.com.autorepairshop.authentication.domain.aggregate.User
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.repository.UserRepository
import br.com.autorepairshop.authentication.domain.valueobject.LoginEmail
import br.com.autorepairshop.authentication.domain.valueobject.Role
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterUserUseCase(
    private val users: UserRepository,
    private val passwords: PasswordHasher,
) : UseCase<RegisterUserCommand, UserResponse> {

    @Transactional
    override fun execute(input: RegisterUserCommand): UserResponse {
        val email = LoginEmail.of(raw = input.email)
        if (users.existsByEmail(email = email)) {
            throw AuthenticationException.UserAlreadyExists(
                message = "User with this email already exists.",
            )
        }
        val role = runCatching { Role.valueOf(value = input.role) }.getOrElse {
            throw AuthenticationException.InvalidRole(message = "Unknown role: ${input.role}")
        }
        if (!users.existsAny() && role != Role.MANAGER) {
            throw AuthenticationException.InvalidRole(
                message = "The first user must be MANAGER.",
            )
        }
        val user = User.register(
            email = email,
            hashedPassword = passwords.hash(raw = input.password),
            role = role,
            customerId = input.customerId,
        )
        users.save(user = user)
        return user.toResponse()
    }
}
