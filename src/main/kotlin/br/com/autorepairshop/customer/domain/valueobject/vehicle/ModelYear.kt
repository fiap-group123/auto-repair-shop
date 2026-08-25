package br.com.autorepairshop.customer.domain.valueobject.vehicle

import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.shared.domain.ValueObject
import java.time.Year

@JvmInline
value class ModelYear private constructor(val value: Int) : ValueObject {

    companion object {
        private const val MIN_YEAR = 1900

        fun of(
            year: Int,
            currentYear: Int = Year.now().value,
        ): ModelYear {
            val maxYear = currentYear + 1
            if (year !in MIN_YEAR..maxYear) {
                throw VehicleException.InvalidModelYear(
                    message = "Model year must be between $MIN_YEAR and $maxYear.",
                )
            }
            return ModelYear(value = year)
        }
    }
}
