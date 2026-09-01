package br.com.autorepairshop.accessidentity.application.usecase

import br.com.autorepairshop.accessidentity.application.dto.LoginCommand
import br.com.autorepairshop.accessidentity.application.dto.TokenResponse
import br.com.autorepairshop.accessidentity.application.security.PasswordHasher
import br.com.autorepairshop.accessidentity.application.security.TokenIssuer
import br.com.autorepairshop.accessidentity.domain.aggregate.RefreshSession
import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.accessidentity.domain.repository.RefreshSessionRepository
import br.com.autorepairshop.accessidentity.domain.repository.UserRepository
import br.com.autorepairshop.accessidentity.domain.valueobject.LoginEmail
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Duration.Companion.seconds

@Service
class LoginUseCase(
    private val users: UserRepository,
    private val passwords: PasswordHasher,
    private val tokens: TokenIssuer,
    private val sessions: RefreshSessionRepository,
    @Value("\${app.security.jwt.ttl-seconds}") private val accessTtlSeconds: Long,
    @Value("\${app.security.refresh.ttl-seconds}") private val refreshTtlSeconds: Long,
) : UseCase<LoginCommand, TokenResponse> {

    @Transactional
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
        val issued = RefreshSession.issue(
            userId = user.id.value,
            ttl = refreshTtlSeconds.seconds,
        )
        sessions.save(session = issued.session)
        return TokenResponse(
            accessToken = tokens.issue(user = user),
            refreshToken = issued.rawToken,
            expiresIn = accessTtlSeconds,
        )
    }
}
