package br.com.autorepairshop.authentication.application.usecase

import br.com.autorepairshop.authentication.application.antilayer.CustomerAntiLayer
import br.com.autorepairshop.authentication.application.dto.CustomerInviteResponse
import br.com.autorepairshop.authentication.domain.InviteToken
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.repository.CustomerInviteRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.time.toJavaInstant

@Service
class FindCustomerInviteUseCase(
    private val invites: CustomerInviteRepository,
    private val customers: CustomerAntiLayer,
) : UseCase<String, CustomerInviteResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: String): CustomerInviteResponse {
        val invite = invites.findByTokenHash(tokenHash = InviteToken.hash(raw = input))
            ?: throw AuthenticationException.InviteNotFound(message = "Invite was not found.")
        invite.requireUsable()
        val customer = customers.find(id = invite.customerId)
            ?: throw AuthenticationException.LinkedCustomerNotFound(
                message = "Customer ${invite.customerId} was not found.",
            )
        return CustomerInviteResponse(
            customerName = customer.name,
            expiresAt = invite.expiresAt.toJavaInstant(),
        )
    }
}
