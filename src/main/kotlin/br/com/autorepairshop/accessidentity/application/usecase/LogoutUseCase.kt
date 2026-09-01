package br.com.autorepairshop.accessidentity.application.usecase

import br.com.autorepairshop.accessidentity.application.dto.RefreshTokenCommand
import br.com.autorepairshop.accessidentity.domain.SecureToken
import br.com.autorepairshop.accessidentity.domain.repository.RefreshSessionRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LogoutUseCase(private val sessions: RefreshSessionRepository) : UseCase<RefreshTokenCommand, Unit> {

    @Transactional
    override fun execute(input: RefreshTokenCommand) {
        val session = sessions.findByTokenHash(tokenHash = SecureToken.hash(raw = input.refreshToken)) ?: return
        session.revoke()
        sessions.save(session = session)
    }
}
