package br.com.autorepairshop.api.controller.accessidentity

import br.com.autorepairshop.accessidentity.application.dto.LoginCommand
import br.com.autorepairshop.accessidentity.application.dto.RefreshTokenCommand
import br.com.autorepairshop.accessidentity.application.dto.RegisterUserCommand
import br.com.autorepairshop.accessidentity.application.dto.TokenResponse
import br.com.autorepairshop.accessidentity.application.dto.UserResponse
import br.com.autorepairshop.accessidentity.application.usecase.LoginUseCase
import br.com.autorepairshop.accessidentity.application.usecase.LogoutUseCase
import br.com.autorepairshop.accessidentity.application.usecase.RefreshTokenUseCase
import br.com.autorepairshop.accessidentity.application.usecase.RegisterUserUseCase
import br.com.autorepairshop.api.dto.accessidentity.LoginRequest
import br.com.autorepairshop.api.dto.accessidentity.RefreshTokenRequest
import br.com.autorepairshop.api.dto.accessidentity.RegisterUserRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Login and staff registration (bounded context Authentication)")
class AuthController(
    private val login: LoginUseCase,
    private val registerUser: RegisterUserUseCase,
    private val refreshToken: RefreshTokenUseCase,
    private val logout: LogoutUseCase,
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
        logout.execute(input = RefreshTokenCommand(refreshToken = request.refreshToken))
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/users")
    @Operation(
        summary = "Register staff (public only when the database is empty; afterwards MANAGER JWT required)",
    )
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
}
