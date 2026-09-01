package br.com.autorepairshop.api.controller.authentication

import br.com.autorepairshop.api.dto.authentication.CompleteInviteRequest
import br.com.autorepairshop.accessindentity.application.dto.CompleteInviteCommand
import br.com.autorepairshop.accessindentity.application.dto.CustomerInviteResponse
import br.com.autorepairshop.accessindentity.application.dto.UserResponse
import br.com.autorepairshop.accessindentity.application.usecase.CompleteInviteUseCase
import br.com.autorepairshop.accessindentity.application.usecase.FindCustomerInviteUseCase
import br.com.autorepairshop.accessindentity.application.usecase.IssueCustomerInviteUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/invite")
@Tag(name = "Invite", description = "Customer login invites (bounded context Authentication)")
class InviteController(
    private val findInvite: FindCustomerInviteUseCase,
    private val completeInvite: CompleteInviteUseCase,
    private val issueInvite: IssueCustomerInviteUseCase,
) {

    @PostMapping("/customer/{customerId}")
    @Operation(summary = "Resend the customer login invite")
    fun issueInvite(@PathVariable customerId: UUID): ResponseEntity<Void> {
        issueInvite.execute(input = customerId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{token}")
    @SecurityRequirements
    @Operation(summary = "Preview a customer invite")
    fun findInvite(@PathVariable token: String): ResponseEntity<CustomerInviteResponse> =
        ResponseEntity.ok(findInvite.execute(input = token))

    @PostMapping("/{token}")
    @SecurityRequirements
    @Operation(summary = "Create a CLIENT login from an invite")
    fun completeInvite(
        @PathVariable token: String,
        @RequestBody request: CompleteInviteRequest,
    ): ResponseEntity<UserResponse> {
        val created = completeInvite.execute(
            input = CompleteInviteCommand(
                token = token,
                email = request.email,
                password = request.password,
            ),
        )
        val location = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/auth/users/{id}")
            .buildAndExpand(created.id)
            .toUri()
        return ResponseEntity.created(location).body(created)
    }
}
