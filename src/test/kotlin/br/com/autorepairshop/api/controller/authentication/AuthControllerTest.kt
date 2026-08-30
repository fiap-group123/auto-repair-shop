package br.com.autorepairshop.api.controller.authentication

import br.com.autorepairshop.api.dto.authentication.CompleteInviteRequest
import br.com.autorepairshop.api.dto.authentication.LoginRequest
import br.com.autorepairshop.api.dto.authentication.RegisterUserRequest
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.authentication.AuthFixtures
import br.com.autorepairshop.authentication.application.dto.CompleteInviteCommand
import br.com.autorepairshop.authentication.application.dto.CustomerInviteResponse
import br.com.autorepairshop.authentication.application.dto.LoginCommand
import br.com.autorepairshop.authentication.application.dto.RegisterUserCommand
import br.com.autorepairshop.authentication.application.dto.TokenResponse
import br.com.autorepairshop.authentication.application.dto.toResponse
import br.com.autorepairshop.authentication.application.usecase.CompleteInviteUseCase
import br.com.autorepairshop.authentication.application.usecase.FindCustomerInviteUseCase
import br.com.autorepairshop.api.dto.authentication.RefreshTokenRequest
import br.com.autorepairshop.authentication.application.usecase.IssueCustomerInviteUseCase
import br.com.autorepairshop.authentication.application.usecase.LoginUseCase
import br.com.autorepairshop.authentication.application.usecase.LogoutUseCase
import br.com.autorepairshop.authentication.application.usecase.RefreshTokenUseCase
import br.com.autorepairshop.authentication.application.usecase.RegisterUserUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

@Tag("unit")
class AuthControllerTest {
    private val login = mockk<LoginUseCase>()
    private val registerUser = mockk<RegisterUserUseCase>()
    private val findInvite = mockk<FindCustomerInviteUseCase>()
    private val completeInvite = mockk<CompleteInviteUseCase>()
    private val issueInviteUseCase = mockk<IssueCustomerInviteUseCase>(relaxUnitFun = true)
    private val refreshToken = mockk<RefreshTokenUseCase>()
    private val logoutUseCase = mockk<LogoutUseCase>(relaxUnitFun = true)
    private val controller = AuthController(
        login = login,
        registerUser = registerUser,
        findInvite = findInvite,
        completeInvite = completeInvite,
        issueInviteUseCase = issueInviteUseCase,
        refreshToken = refreshToken,
        logoutUseCase = logoutUseCase,
    )

    @Test
    fun `login returns the token from the use case`() {
        every { login.execute(input = any()) } returns TokenResponse(
            accessToken = "jwt",
            refreshToken = "refresh",
            expiresIn = 900,
        )

        val response = controller.login(
            request = LoginRequest(
                email = AuthFixtures.MANAGER_EMAIL,
                password = AuthFixtures.RAW_PASSWORD,
            ),
        )

        assertEquals(
            expected = HttpStatus.OK,
            actual = response.statusCode,
        )
        assertEquals(
            expected = "jwt",
            actual = response.body?.accessToken,
        )
        verify {
            login.execute(
                input = LoginCommand(
                    email = AuthFixtures.MANAGER_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                ),
            )
        }
    }

    @Test
    fun `register maps the request and returns 201`() {
        val created = AuthFixtures.manager().toResponse()
        every { registerUser.execute(input = any()) } returns created

        withHttpRequest(requestUri = "/auth/users") {
            val response = controller.register(
                request = RegisterUserRequest(
                    email = AuthFixtures.MANAGER_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "MANAGER",
                ),
            )

            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
            assertEquals(
                expected = created.id,
                actual = response.body?.id,
            )
        }
        verify {
            registerUser.execute(
                input = RegisterUserCommand(
                    email = AuthFixtures.MANAGER_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "MANAGER",
                    customerId = null,
                ),
            )
        }
    }

    @Test
    fun `find invite delegates to the use case`() {
        val preview = CustomerInviteResponse(
            customerName = "Ana Souza",
            expiresAt = Instant.parse("2026-09-01T00:00:00Z"),
        )
        every { findInvite.execute(input = "token") } returns preview

        assertEquals(
            expected = preview.customerName,
            actual = controller.findInvite(token = "token").body?.customerName,
        )
    }

    @Test
    fun `complete invite maps the request and returns 201`() {
        val created = AuthFixtures.client().toResponse()
        every { completeInvite.execute(input = any()) } returns created

        withHttpRequest(requestUri = "/auth/invites/token") {
            val response = controller.completeInvite(
                token = "token",
                request = CompleteInviteRequest(
                    email = AuthFixtures.CLIENT_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                ),
            )
            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
        }
        verify {
            completeInvite.execute(
                input = CompleteInviteCommand(
                    token = "token",
                    email = AuthFixtures.CLIENT_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                ),
            )
        }
    }

    @Test
    fun `issue invite delegates to the use case`() {
        val customerId = UUID.randomUUID()

        assertEquals(
            expected = HttpStatus.NO_CONTENT,
            actual = controller.issueInvite(customerId = customerId).statusCode,
        )
        verify { issueInviteUseCase.execute(input = customerId) }
    }

    @Test
    fun `refresh delegates to the use case`() {
        val tokens = TokenResponse(
            accessToken = "new-jwt",
            refreshToken = "new-refresh",
            expiresIn = 900,
        )
        every { refreshToken.execute(input = any()) } returns tokens

        assertEquals(
            expected = "new-jwt",
            actual = controller.refresh(request = RefreshTokenRequest(refreshToken = "old")).body?.accessToken,
        )
    }

    @Test
    fun `logout returns no content`() {
        assertEquals(
            expected = HttpStatus.NO_CONTENT,
            actual = controller.logout(request = RefreshTokenRequest(refreshToken = "refresh")).statusCode,
        )
        verify { logoutUseCase.execute(input = any()) }
    }
}
