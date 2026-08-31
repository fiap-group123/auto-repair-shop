package br.com.autorepairshop.api.controller.budget

import br.com.autorepairshop.budget.application.dto.BudgetResponse
import br.com.autorepairshop.budget.application.usecase.ApproveBudgetUseCase
import br.com.autorepairshop.budget.application.usecase.DeleteBudgetUseCase
import br.com.autorepairshop.budget.application.usecase.FindBudgetUseCase
import br.com.autorepairshop.budget.application.usecase.RejectBudgetUseCase
import br.com.autorepairshop.budget.application.usecase.TradeBudgetUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/budgets")
@Tag(name = "Budget", description = "Budget registration and control (bounded context Budget)")
class BudgetController(
    private val approveBudget: ApproveBudgetUseCase,
    private val rejectBudget: RejectBudgetUseCase,
    private val tradeBudget: TradeBudgetUseCase,
    private val deleteBudget: DeleteBudgetUseCase,
    private val findBudget: FindBudgetUseCase,
) {

    @GetMapping("/{id}")
    @Operation(summary = "Find a budget by service order id")
    fun findBudgetByServiceId(@PathVariable id: UUID): ResponseEntity<BudgetResponse> =
        ResponseEntity.ok(findBudget.execute(input = id))

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a budget by service order id")
    fun approveBudget(@PathVariable id: UUID): ResponseEntity<BudgetResponse> =
        ResponseEntity.ok(approveBudget.execute(input = id))

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a budget by service order id")
    fun rejectBudget(@PathVariable id: UUID): ResponseEntity<BudgetResponse> =
        ResponseEntity.ok(rejectBudget.execute(input = id))

    @PostMapping("/{id}/trade")
    @Operation(summary = "Trade a budget by service order id")
    fun tradeBudget(@PathVariable id: UUID): ResponseEntity<BudgetResponse> =
        ResponseEntity.ok(tradeBudget.execute(input = id))

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget by service order id")
    fun deleteBudgetByServiceId(@PathVariable id: UUID): ResponseEntity<BudgetResponse> {
        deleteBudget.execute(input = id)
        return ResponseEntity.noContent().build()
    }
}
