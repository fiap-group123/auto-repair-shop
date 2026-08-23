package br.com.autorepairshop.shared.domain.valueobject.carplate

enum class CarPlateType(val regex: Regex) {
    NATIONAL(regex = Regex(
        pattern = "^[A-Z]{3}\\\\d{4}$")
    ),
    MERCOSUL(regex = Regex(
        pattern = "^[A-Z]{3}\\\\d[A-Z]\\\\d{2}$")
    )
}