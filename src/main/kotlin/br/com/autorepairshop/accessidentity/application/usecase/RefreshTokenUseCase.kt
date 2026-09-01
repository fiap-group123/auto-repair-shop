package br.com.autorepairshop.accessidentity.application.usecase

import br.com.autorepairshop.accessidentity.application.dto.RefreshTokenCommand
import br.com.autorepairshop.accessidentity.application.dto.TokenResponse
import br.com.autorepairshop.accessidentity.application.security.TokenIssuer
import br.com.autorepairshop.accessidentity.domain.SecureToken
import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.accessidentity.domain.repository.RefreshSessionRepository
import br.com.autorepairshop.accessidentity.domain.repository.UserRepository
import br.com.autorepairshop.accessidentity.domain.valueobject.UserId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Duration.Companion.seconds

@Service
class RefreshTokenUseCase(
    private val sessions: RefreshSessionRepository,
    private val users: UserRepository,
    private val tokens: TokenIssuer,
    @Value("\${app.security.jwt.ttl-seconds}") private val accessTtlSeconds: Long,
    @Value("\${app.security.refresh.ttl-seconds}") private val refreshTtlSeconds: Long,
) : UseCase<RefreshTokenCommand, TokenResponse> {

    @Transactional
    override fun execute(input: RefreshTokenCommand): TokenResponse {
        val session = sessions.findByTokenHash(tokenHash = SecureToken.hash(raw = input.refreshToken))
            ?: throw AuthenticationException.InvalidRefresh(message = "Refresh token was not found.")
        if (session.revokedAt != null) {
            sessions.findAllByFamilyId(familyId = session.familyId).forEach { open ->
                open.revoke()
                sessions.save(session = open)
            }
            throw AuthenticationException.RefreshReuse(message = "Refresh token was reused.")
        }
        session.requireUsable()
        val user = users.findById(id = UserId(value = session.userId))
            ?: throw AuthenticationException.UserNotFound(message = "User ${session.userId} was not found.")
        if (!user.active) {
            throw AuthenticationException.UserInactive(message = "User is inactive.")
        }
        val rotated = session.rotate(ttl = refreshTtlSeconds.seconds)
        sessions.save(session = session)
        sessions.save(session = rotated.session)
        return TokenResponse(
            accessToken = tokens.issue(user = user),
            refreshToken = rotated.rawToken,
            expiresIn = accessTtlSeconds,
        )
    }
}
