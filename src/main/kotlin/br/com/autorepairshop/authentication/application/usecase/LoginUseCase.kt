package br.com.autorepairshop.authentication.application.usecase

import br.com.autorepairshop.authentication.application.dto.LoginCommand
import br.com.autorepairshop.authentication.application.dto.TokenResponse
import br.com.autorepairshop.authentication.application.security.PasswordHasher
import br.com.autorepairshop.authentication.application.security.TokenIssuer
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.repository.UserRepository
import br.com.autorepairshop.authentication.domain.valueobject.LoginEmail
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginUseCase(
    private val users: UserRepository,
    private val passwords: PasswordHasher,
    private val tokens: TokenIssuer,
) : UseCase<LoginCommand, TokenResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: LoginCommand): TokenResponse {
        val email = LoginEmail.of(raw = input.email)
        val user = users.findByEmail(email = email)
            ?: throw AuthenticationException.InvalidCredentials(message = "Invalid credentials.")
        if (!user.active) {
            throw AuthenticationException.UserInactive(message = "User is inactive.")
        }
        if (!passwords.matches(raw = input.password, hashed = user.hashedPassword)) {
            throw AuthenticationException.InvalidCredentials(message = "Invalid credentials.")
        }
        return TokenResponse(accessToken = tokens.issue(user = user))
    }
}
