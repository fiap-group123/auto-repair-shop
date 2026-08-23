package br.com.autorepairshop.shared.domain.valueobject.carplate

import br.com.autorepairshop.shared.domain.ValueObject
import br.com.autorepairshop.shared.domain.exception.InvalidCarPlateException

@JvmInline
value class CarPlate private constructor(private val value: String) : ValueObject {
    companion object {
        fun of(input: String): CarPlate {
            val normalized = input.uppercase().filter { it.isLetterOrDigit() }

            if (!normalized.matches(CarPlateType.NATIONAL.regex) && !normalized.matches(CarPlateType.MERCOSUL.regex)) {
                throw InvalidCarPlateException("Invalid car plate: $input")
            }

            return CarPlate(value = normalized)
        }
    }
}