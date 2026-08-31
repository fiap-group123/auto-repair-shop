package br.com.autorepairshop.api.controller.customer

import br.com.autorepairshop.api.dto.customer.RegisterCustomerRequest
import br.com.autorepairshop.api.dto.customer.UpdateCustomerRequest
import br.com.autorepairshop.customer.application.dto.customer.CustomerResponse
import br.com.autorepairshop.customer.application.dto.customer.RegisterCustomerCommand
import br.com.autorepairshop.customer.application.dto.customer.UpdateCustomerCommand
import br.com.autorepairshop.customer.application.usecase.customer.DeactivateCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.customer.FindCustomerByDocumentUseCase
import br.com.autorepairshop.customer.application.usecase.customer.FindCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.customer.ListCustomersUseCase
import br.com.autorepairshop.customer.application.usecase.customer.ReactivateCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.customer.RegisterCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.customer.UpdateCustomerUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/customers")
@Tag(name = "Customer", description = "Customer and vehicle registration (bounded context Customer)")
class CustomerController(
    private val registerCustomer: RegisterCustomerUseCase,
    private val updateCustomer: UpdateCustomerUseCase,
    private val deactivateCustomer: DeactivateCustomerUseCase,
    private val reactivateCustomer: ReactivateCustomerUseCase,
    private val findCustomer: FindCustomerUseCase,
    private val findCustomerByDocument: FindCustomerByDocumentUseCase,
    private val listCustomers: ListCustomersUseCase,
) {

    @PostMapping
    @Operation(summary = "Register customer")
    fun register(@RequestBody request: RegisterCustomerRequest): ResponseEntity<CustomerResponse> {
        val customer = registerCustomer.execute(
            input = RegisterCustomerCommand(
                documentId = request.documentId,
                name = request.name,
                email = request.email,
                phone = request.phone,
            ),
        )
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(customer.id)
            .toUri()
        return ResponseEntity.created(location).body(customer)
    }

    @GetMapping
    @Operation(summary = "List customers")
    fun list(): ResponseEntity<List<CustomerResponse>> = ResponseEntity.ok(listCustomers.execute(input = Unit))

    @GetMapping("/document/{document}")
    @Operation(summary = "Search by CPF/CNPJ")
    fun findByDocument(@PathVariable document: String): ResponseEntity<CustomerResponse> =
        ResponseEntity.ok(findCustomerByDocument.execute(input = document))

    @GetMapping("/{id}")
    @Operation(summary = "Search for a customer by id")
    fun findById(@PathVariable id: UUID): ResponseEntity<CustomerResponse> =
        ResponseEntity.ok(findCustomer.execute(input = id))

    @PutMapping("/{id}")
    @Operation(summary = "Update name and/or contact information")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: UpdateCustomerRequest,
    ): ResponseEntity<CustomerResponse> = ResponseEntity.ok(
        updateCustomer.execute(
            input = UpdateCustomerCommand(
                customerId = id,
                name = request.name,
                email = request.email,
                phone = request.phone,
            ),
        ),
    )

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate customer (does not delete history)\n")
    open fun deactivate(@PathVariable id: UUID): ResponseEntity<Void> {
        deactivateCustomer.execute(input = id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}")
    @Operation(summary = "Reactivate customer")
    fun reactivate(@PathVariable id: UUID): ResponseEntity<Void> {
        reactivateCustomer.execute(input = id)
        return ResponseEntity.noContent().build()
    }
}
