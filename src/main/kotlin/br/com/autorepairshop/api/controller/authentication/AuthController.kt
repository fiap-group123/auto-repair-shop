package br.com.autorepairshop.api.controller.authentication

import br.com.autorepairshop.api.dto.authentication.CompleteInviteRequest
import br.com.autorepairshop.api.dto.authentication.LoginRequest
import br.com.autorepairshop.api.dto.authentication.RefreshTokenRequest
import br.com.autorepairshop.api.dto.authentication.RegisterUserRequest
import br.com.autorepairshop.authentication.application.dto.CompleteInviteCommand
import br.com.autorepairshop.authentication.application.dto.CustomerInviteResponse
import br.com.autorepairshop.authentication.application.dto.LoginCommand
import br.com.autorepairshop.authentication.application.dto.RefreshTokenCommand
import br.com.autorepairshop.authentication.application.dto.RegisterUserCommand
import br.com.autorepairshop.authentication.application.dto.TokenResponse
import br.com.autorepairshop.authentication.application.dto.UserResponse
import br.com.autorepairshop.authentication.application.usecase.CompleteInviteUseCase
import br.com.autorepairshop.authentication.application.usecase.FindCustomerInviteUseCase
import br.com.autorepairshop.authentication.application.usecase.IssueCustomerInviteUseCase
import br.com.autorepairshop.authentication.application.usecase.LoginUseCase
import br.com.autorepairshop.authentication.application.usecase.LogoutUseCase
import br.com.autorepairshop.authentication.application.usecase.RefreshTokenUseCase
import br.com.autorepairshop.authentication.application.usecase.RegisterUserUseCase
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
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Login and user registration (bounded context Authentication)")
class AuthController(
    private val login: LoginUseCase,
    private val registerUser: RegisterUserUseCase,
    private val findInvite: FindCustomerInviteUseCase,
    private val completeInvite: CompleteInviteUseCase,
    private val issueInviteUseCase: IssueCustomerInviteUseCase,
    private val refreshToken: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
) {

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Issue JWT")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> = ResponseEntity.ok(
        login.execute(
            input = LoginCommand(
                email = request.email,
                password = request.password,
            ),
        ),
    )

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Rotate refresh token and issue a new access JWT")
    fun refresh(@RequestBody request: RefreshTokenRequest): ResponseEntity<TokenResponse> = ResponseEntity.ok(
        refreshToken.execute(input = RefreshTokenCommand(refreshToken = request.refreshToken)),
    )

    @PostMapping("/logout")
    @SecurityRequirements
    @Operation(summary = "Revoke a refresh session")
    fun logout(@RequestBody request: RefreshTokenRequest): ResponseEntity<Void> {
        logoutUseCase.execute(input = RefreshTokenCommand(refreshToken = request.refreshToken))
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/users")
    @SecurityRequirements
    @Operation(summary = "Register user (first user must be MANAGER)")
    fun register(@RequestBody request: RegisterUserRequest): ResponseEntity<UserResponse> {
        val created = registerUser.execute(
            input = RegisterUserCommand(
                email = request.email,
                password = request.password,
                role = request.role,
                customerId = request.customerId,
            ),
        )
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id)
            .toUri()
        return ResponseEntity.created(location).body(created)
    }

    @PostMapping("/invites/customer/{customerId}")
    @Operation(summary = "Resend the customer login invite")
    fun issueInvite(@PathVariable customerId: UUID): ResponseEntity<Void> {
        issueInviteUseCase.execute(input = customerId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/invites/{token}")
    @SecurityRequirements
    @Operation(summary = "Preview a customer invite")
    fun findInvite(@PathVariable token: String): ResponseEntity<CustomerInviteResponse> =
        ResponseEntity.ok(findInvite.execute(input = token))

    @PostMapping("/invites/{token}")
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
