package br.com.autorepairshop.authentication.application.usecase

import br.com.autorepairshop.authentication.application.antilayer.CustomerAntiLayer
import br.com.autorepairshop.authentication.application.dto.CompleteInviteCommand
import br.com.autorepairshop.authentication.application.dto.UserResponse
import br.com.autorepairshop.authentication.application.dto.toResponse
import br.com.autorepairshop.authentication.application.security.PasswordHasher
import br.com.autorepairshop.authentication.domain.InviteToken
import br.com.autorepairshop.authentication.domain.aggregate.User
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.repository.CustomerInviteRepository
import br.com.autorepairshop.authentication.domain.repository.UserRepository
import br.com.autorepairshop.authentication.domain.valueobject.LoginEmail
import br.com.autorepairshop.authentication.domain.valueobject.RawPassword
import br.com.autorepairshop.authentication.domain.valueobject.Role
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CompleteInviteUseCase(
    private val invites: CustomerInviteRepository,
    private val users: UserRepository,
    private val customers: CustomerAntiLayer,
    private val passwords: PasswordHasher,
) : UseCase<CompleteInviteCommand, UserResponse> {

    @Transactional
    override fun execute(input: CompleteInviteCommand): UserResponse {
        val password = RawPassword.of(raw = input.password)
        val invite = invites.findByTokenHash(tokenHash = InviteToken.hash(raw = input.token))
            ?: throw AuthenticationException.InviteNotFound(message = "Invite was not found.")
        invite.consume()
        val customer = customers.find(id = invite.customerId)
            ?: throw AuthenticationException.LinkedCustomerNotFound(
                message = "Customer ${invite.customerId} was not found.",
            )
        if (!customer.active) {
            throw AuthenticationException.LinkedCustomerInactive(
                message = "Customer ${invite.customerId} is inactive.",
            )
        }
        if (users.existsByCustomerId(customerId = invite.customerId)) {
            throw AuthenticationException.CustomerAlreadyHasUser(
                message = "Customer already has a login.",
            )
        }
        val email = LoginEmail.of(raw = input.email)
        if (users.existsByEmail(email = email)) {
            throw AuthenticationException.UserAlreadyExists(
                message = "User with this email already exists.",
            )
        }
        val user = User.register(
            email = email,
            hashedPassword = passwords.hash(raw = password.value),
            role = Role.CLIENT,
            customerId = invite.customerId,
        )
        invites.save(invite = invite)
        users.save(user = user)
        return user.toResponse()
    }
}
