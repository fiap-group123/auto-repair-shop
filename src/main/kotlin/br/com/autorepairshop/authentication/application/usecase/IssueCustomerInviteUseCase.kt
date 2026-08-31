package br.com.autorepairshop.authentication.application.usecase

import br.com.autorepairshop.authentication.application.antilayer.CustomerAntiLayer
import br.com.autorepairshop.authentication.application.antilayer.CustomerRecord
import br.com.autorepairshop.authentication.domain.aggregate.CustomerInvite
import br.com.autorepairshop.authentication.domain.event.CustomerInviteIssued
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.repository.CustomerInviteRepository
import br.com.autorepairshop.authentication.domain.repository.UserRepository
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.application.event.EventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.toJavaInstant

@Service
class IssueCustomerInviteUseCase(
    private val customers: CustomerAntiLayer,
    private val users: UserRepository,
    private val invites: CustomerInviteRepository,
    private val events: EventPublisher,
) : UseCase<UUID, Unit> {

    @Transactional
    override fun execute(input: UUID) {
        val customer = requireActiveCustomer(id = input)
        if (users.existsByCustomerId(customerId = input)) {
            throw AuthenticationException.CustomerAlreadyHasUser(
                message = "Customer already has a login.",
            )
        }
        val now = Clock.System.now()
        invites.findOpenByCustomerId(customerId = input).forEach { open ->
            open.revoke(at = now)
            invites.save(invite = open)
        }
        val issued = CustomerInvite.issue(customerId = input)
        invites.save(invite = issued.invite)
        events.publish(
            event = CustomerInviteIssued(
                customerId = input,
                customerName = customer.name,
                contactEmail = customer.email,
                rawToken = issued.rawToken,
                occurredOn = now.toJavaInstant(),
            ),
        )
    }

    private fun requireActiveCustomer(id: UUID): CustomerRecord {
        val customer = customers.find(id = id)
            ?: throw AuthenticationException.LinkedCustomerNotFound(
                message = "Customer $id was not found.",
            )
        if (!customer.active) {
            throw AuthenticationException.LinkedCustomerInactive(
                message = "Customer $id is inactive.",
            )
        }
        return customer
    }
}
