package br.com.autorepairshop.api.controller.budget

import br.com.autorepairshop.api.dto.budget.BudgetNotificationRequest
import br.com.autorepairshop.api.security.NotificationTokenGuard
import br.com.autorepairshop.budget.application.dto.BudgetResponse
import br.com.autorepairshop.budget.application.dto.ReceiveBudgetDecisionCommand
import br.com.autorepairshop.budget.application.usecase.ReceiveBudgetDecisionUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/integrations/budget-decisions")
@Tag(name = "Budget notification", description = "External budget approval or rejection")
class BudgetNotificationController(
    private val tokens: NotificationTokenGuard,
    private val receive: ReceiveBudgetDecisionUseCase,
) {

    @PostMapping
    @Operation(
        summary = "Receive an external budget decision",
        description = "Authenticated by the X-Notification-Token header. decision is APPROVED or REJECTED.",
    )
    fun receive(
        @RequestHeader(name = "X-Notification-Token", required = false) token: String?,
        @RequestBody request: BudgetNotificationRequest,
    ): ResponseEntity<BudgetResponse> {
        tokens.require(presented = token)
        return ResponseEntity.ok(
            receive.execute(
                input = ReceiveBudgetDecisionCommand(
                    serviceOrderId = request.serviceOrderId,
                    decision = request.decision,
                ),
            ),
        )
    }
}
