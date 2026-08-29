package br.com.autorepairshop.api.controller.customer

import br.com.autorepairshop.api.dto.customer.ChangeVehiclePlateRequest
import br.com.autorepairshop.api.dto.customer.RegisterVehicleRequest
import br.com.autorepairshop.api.dto.customer.TransferVehicleRequest
import br.com.autorepairshop.api.dto.customer.UpdateVehicleSpecRequest
import br.com.autorepairshop.api.security.AuthorizationSupport
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.application.dto.vehicle.ChangeVehiclePlateCommand
import br.com.autorepairshop.customer.application.dto.vehicle.RegisterVehicleCommand
import br.com.autorepairshop.customer.application.dto.vehicle.TransferVehicleCommand
import br.com.autorepairshop.customer.application.dto.vehicle.UpdateVehicleSpecCommand
import br.com.autorepairshop.customer.application.dto.vehicle.toResponse
import br.com.autorepairshop.customer.application.usecase.vehicle.ChangeVehiclePlateUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.DeactivateVehicleUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.FindVehicleByPlateUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.FindVehicleUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.ListVehiclesByOwnerUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.ReactivateVehicleUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.RegisterVehicleUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.TransferVehicleUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.UpdateVehicleSpecUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class VehicleControllerTest {
    private val registerVehicle = mockk<RegisterVehicleUseCase>()
    private val listVehiclesByOwner = mockk<ListVehiclesByOwnerUseCase>()
    private val findVehicle = mockk<FindVehicleUseCase>()
    private val findVehicleByPlate = mockk<FindVehicleByPlateUseCase>()
    private val updateVehicleSpec = mockk<UpdateVehicleSpecUseCase>()
    private val changeVehiclePlate = mockk<ChangeVehiclePlateUseCase>()
    private val transferVehicle = mockk<TransferVehicleUseCase>()
    private val deactivateVehicle = mockk<DeactivateVehicleUseCase>()
    private val reactivateVehicle = mockk<ReactivateVehicleUseCase>()
    private val authorization = mockk<AuthorizationSupport>(relaxUnitFun = true)
    private val controller = VehicleController(
        registerVehicle = registerVehicle,
        listVehiclesByOwner = listVehiclesByOwner,
        findVehicle = findVehicle,
        findVehicleByPlate = findVehicleByPlate,
        updateVehicleSpec = updateVehicleSpec,
        changeVehiclePlate = changeVehiclePlate,
        transferVehicle = transferVehicle,
        deactivateVehicle = deactivateVehicle,
        reactivateVehicle = reactivateVehicle,
        authorization = authorization,
    )

    @Test
    fun `find by id and plate delegate to use cases`() {
        val vehicle = CustomerFixtures.vehicle().toResponse()
        every { findVehicle.execute(input = vehicle.id) } returns vehicle
        every {
            findVehicleByPlate.execute(input = CustomerFixtures.MERCOSUL_PLATE)
        } returns vehicle

        assertEquals(
            expected = vehicle.id,
            actual = controller.findById(id = vehicle.id).body?.id,
        )
        assertEquals(
            expected = vehicle.id,
            actual = controller.findByPlate(plate = CustomerFixtures.MERCOSUL_PLATE).body?.id,
        )
        verify { authorization.requireCanAccessVehicleOwner(ownerId = vehicle.ownerId) }
    }

    @Test
    fun `update plate and transfer delegate to use cases`() {
        val vehicle = CustomerFixtures.vehicle().toResponse()
        val newOwnerId = UUID.randomUUID()
        every { updateVehicleSpec.execute(input = any()) } returns vehicle
        every { changeVehiclePlate.execute(input = any()) } returns vehicle
        every { transferVehicle.execute(input = any()) } returns vehicle

        assertEquals(
            expected = HttpStatus.OK,
            actual = controller.updateSpec(
                id = vehicle.id,
                request = UpdateVehicleSpecRequest(brand = "Toyota"),
            ).statusCode,
        )
        assertEquals(
            expected = HttpStatus.OK,
            actual = controller.changePlate(
                id = vehicle.id,
                request = ChangeVehiclePlateRequest(plate = CustomerFixtures.NATIONAL_PLATE),
            ).statusCode,
        )
        assertEquals(
            expected = HttpStatus.OK,
            actual = controller.transfer(
                id = vehicle.id,
                request = TransferVehicleRequest(newOwnerId = newOwnerId),
            ).statusCode,
        )
        verify {
            updateVehicleSpec.execute(
                input = UpdateVehicleSpecCommand(
                    vehicleId = vehicle.id,
                    brand = "Toyota",
                    model = null,
                    year = null,
                ),
            )
            changeVehiclePlate.execute(
                input = ChangeVehiclePlateCommand(
                    vehicleId = vehicle.id,
                    plate = CustomerFixtures.NATIONAL_PLATE,
                ),
            )
            transferVehicle.execute(
                input = TransferVehicleCommand(
                    vehicleId = vehicle.id,
                    newOwnerId = newOwnerId,
                ),
            )
        }
    }

    @Test
    fun `register and list vehicles for a customer`() {
        val ownerId = UUID.randomUUID()
        val vehicle = CustomerFixtures.vehicle().toResponse()
        every { registerVehicle.execute(input = any()) } returns vehicle
        every { listVehiclesByOwner.execute(input = ownerId) } returns listOf(element = vehicle)

        withHttpRequest(requestUri = "/vehicles") {
            val created = controller.register(
                request = RegisterVehicleRequest(
                    ownerId = ownerId,
                    plate = CustomerFixtures.MERCOSUL_PLATE,
                    brand = "Fiat",
                    model = "Argo",
                    color = "Black",
                    year = 2024,
                ),
            )
            assertEquals(
                expected = HttpStatus.CREATED,
                actual = created.statusCode,
            )
        }
        val listed = controller.listByOwner(ownerId = ownerId)
        assertTrue(listed.body!!.isNotEmpty())
        verify {
            registerVehicle.execute(
                input = RegisterVehicleCommand(
                    ownerId = ownerId,
                    plate = CustomerFixtures.MERCOSUL_PLATE,
                    brand = "Fiat",
                    model = "Argo",
                    color = "Black",
                    year = 2024,
                ),
            )
            authorization.requireCanAccessCustomer(customerId = ownerId)
        }
    }

    @Test
    fun `deactivate and reactivate delegate to use cases`() {
        val vehicleId = UUID.randomUUID()
        every { deactivateVehicle.execute(input = vehicleId) } returns Unit
        every { reactivateVehicle.execute(input = vehicleId) } returns Unit

        assertEquals(
            expected = HttpStatus.NO_CONTENT,
            actual = controller.deactivate(id = vehicleId).statusCode,
        )
        assertEquals(
            expected = HttpStatus.NO_CONTENT,
            actual = controller.reactivate(id = vehicleId).statusCode,
        )
        verify {
            deactivateVehicle.execute(input = vehicleId)
            reactivateVehicle.execute(input = vehicleId)
        }
    }
}
