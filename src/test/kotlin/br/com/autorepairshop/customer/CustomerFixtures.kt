package br.com.autorepairshop.customer

import br.com.autorepairshop.customer.domain.aggregate.Customer
import br.com.autorepairshop.customer.domain.aggregate.Vehicle
import br.com.autorepairshop.customer.domain.valueobject.contact.ContactInfo
import br.com.autorepairshop.customer.domain.valueobject.customer.PersonName
import br.com.autorepairshop.customer.domain.valueobject.document.Document
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

    fun activeCustomer(
        documentId: String = VALID_CPF,
        name: String = NAME,
        email: String = EMAIL,
        phone: String = PHONE,
    ): Customer = Customer.register(
        documentId = Document.of(raw = documentId),
        name = PersonName.of(raw = name),
        contact = ContactInfo.of(
            email = email,
            phone = phone,
        ),
    )

    fun inactiveCustomer(): Customer = activeCustomer().apply { deactivate() }

    fun vehicle(
        owner: Customer = activeCustomer(),
        plate: String = MERCOSUL_PLATE,
        brand: String = "Fiat",
        model: String = "Argo",
        year: Int = 2024,
    ): Vehicle = Vehicle.register(
        ownerId = owner.id,
        plate = LicensePlate.of(raw = plate),
        brand = brand,
        model = model,
        year = ModelYear.of(
            year = year,
            currentYear = 2026,
        ),
    )
}
