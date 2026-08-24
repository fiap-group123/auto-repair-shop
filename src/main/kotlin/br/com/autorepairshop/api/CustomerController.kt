package br.com.autorepairshop.api

import br.com.autorepairshop.customer.application.dto.customer.CustomerResponse
import br.com.autorepairshop.customer.application.dto.customer.RegisterCustomerCommand
import br.com.autorepairshop.customer.application.dto.customer.UpdateCustomerCommand
import br.com.autorepairshop.customer.application.dto.vehicle.RegisterVehicleCommand
import br.com.autorepairshop.customer.application.dto.vehicle.VehicleResponse
import br.com.autorepairshop.customer.application.usecase.customer.DeactivateCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.customer.FindCustomerByDocumentUseCase
import br.com.autorepairshop.customer.application.usecase.customer.FindCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.customer.ListCustomersUseCase
import br.com.autorepairshop.customer.application.usecase.customer.RegisterCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.customer.UpdateCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.ListVehiclesByOwnerUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.RegisterVehicleUseCase
import br.com.autorepairshop.api.dto.RegisterVehicleRequest
import br.com.autorepairshop.api.dto.UpdateCustomerRequest
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/customers")
@Tag(name = "Customer", description = "Cadastro de clientes e veículos (bounded context Customer)")
class CustomerController(
    private val registerCustomer: RegisterCustomerUseCase,
    private val updateCustomer: UpdateCustomerUseCase,
    private val deactivateCustomer: DeactivateCustomerUseCase,
    private val findCustomer: FindCustomerUseCase,
    private val findCustomerByDocument: FindCustomerByDocumentUseCase,
    private val listCustomers: ListCustomersUseCase,
    private val registerVehicle: RegisterVehicleUseCase,
    private val listVehiclesByOwner: ListVehiclesByOwnerUseCase,
) {

    @PostMapping
    @Operation(summary = "Cadastrar cliente")
    fun register(@RequestBody command: RegisterCustomerCommand): ResponseEntity<CustomerResponse> {
        val customer = registerCustomer.execute(command)
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(customer.id)
            .toUri()
        return ResponseEntity.created(location).body(customer)
    }

    @GetMapping
    @Operation(summary = "Listar clientes ou buscar por CPF/CNPJ")
    fun list(@RequestParam(required = false) document: String?): ResponseEntity<Any> {
        if (document != null) {
            return ResponseEntity.ok(findCustomerByDocument.execute(document))
        }
        return ResponseEntity.ok(listCustomers.execute(Unit))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por id")
    fun findById(@PathVariable id: UUID): ResponseEntity<CustomerResponse> =
        ResponseEntity.ok(findCustomer.execute(id))

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar nome e contato")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: UpdateCustomerRequest,
    ): ResponseEntity<CustomerResponse> =
        ResponseEntity.ok(
            updateCustomer.execute(
                UpdateCustomerCommand(
                    customerId = id,
                    name = request.name,
                    email = request.email,
                    phone = request.phone,
                )
            )
        )

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar cliente (não apaga o histórico)")
    fun deactivate(@PathVariable id: UUID): ResponseEntity<Void> {
        deactivateCustomer.execute(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/vehicles")
    @Operation(summary = "Cadastrar veículo do cliente")
    fun registerVehicle(
        @PathVariable id: UUID,
        @RequestBody request: RegisterVehicleRequest,
    ): ResponseEntity<VehicleResponse> {
        val vehicle = registerVehicle.execute(
            RegisterVehicleCommand(
                ownerId = id,
                plate = request.plate,
                brand = request.brand,
                model = request.model,
                year = request.year,
            )
        )
        val location = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/vehicles/{id}")
            .buildAndExpand(vehicle.id)
            .toUri()
        return ResponseEntity.created(location).body(vehicle)
    }

    @GetMapping("/{id}/vehicles")
    @Operation(summary = "Listar veículos do cliente")
    fun listVehicles(@PathVariable id: UUID): ResponseEntity<List<VehicleResponse>> =
        ResponseEntity.ok(listVehiclesByOwner.execute(id))
}
