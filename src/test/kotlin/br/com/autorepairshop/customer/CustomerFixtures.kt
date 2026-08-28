package br.com.autorepairshop.customer

import br.com.autorepairshop.customer.domain.aggregate.Customer
import br.com.autorepairshop.customer.domain.aggregate.Vehicle
import br.com.autorepairshop.customer.domain.valueobject.contact.ContactInfo
import br.com.autorepairshop.customer.domain.valueobject.customer.PersonName
import br.com.autorepairshop.customer.domain.valueobject.document.DocumentId
import br.com.autorepairshop.customer.domain.valueobject.vehicle.LicensePlate
import br.com.autorepairshop.customer.domain.valueobject.vehicle.ModelYear

object CustomerFixtures {
    const val VALID_CPF = "529.982.247-25"
    const val OTHER_CPF = "111.444.777-35"
    const val VALID_CNPJ = "11.222.333/0001-81"
    const val INVALID_CPF = "111.111.111-11"
    const val MERCOSUL_PLATE = "ABC1D23"
    const val NATIONAL_PLATE = "ABC-1234"
    const val NAME = "John Doe"
    const val EMAIL = "john.doe@email.com"
    const val PHONE = "11987654321"
    const val PLATE = MERCOSUL_PLATE
    const val BRAND = "Fiat"
    const val MODEL = "Argo"
    const val COLOR = "Preto"
    const val YEAR = 2024

    fun activeCustomer(
        documentId: String = VALID_CPF,
        name: String = NAME,
        email: String = EMAIL,
        phone: String = PHONE,
    ): Customer = Customer.register(
        documentId = DocumentId.of(raw = documentId),
        name = PersonName.of(raw = name),
        contact = ContactInfo.of(
            email = email,
            phone = phone,
        ),
    )

    fun inactiveCustomer(): Customer = activeCustomer().apply { deactivate() }

    fun inactiveVehicle(): Vehicle = vehicle().apply { deactivate() }

    fun vehicle(
        owner: Customer = activeCustomer(),
        plate: String = PLATE,
        brand: String = BRAND,
        model: String = MODEL,
        color: String = COLOR,
        year: Int = YEAR,
    ): Vehicle = Vehicle.register(
        ownerId = owner.id,
        plate = LicensePlate.of(raw = plate),
        brand = brand,
        model = model,
        color = color,
        year = ModelYear.of(
            year = year,
            currentYear = 2026,
        ),
    )
}
