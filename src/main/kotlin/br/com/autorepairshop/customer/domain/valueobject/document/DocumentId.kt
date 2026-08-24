package br.com.autorepairshop.customer.domain.valueobject.document

import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class DocumentId private constructor(val value: String) : ValueObject {

    val type: DocumentType
        get() = if (value.length == CPF_LENGTH) DocumentType.CPF else DocumentType.CNPJ

    fun formatted(): String = when (type) {
        DocumentType.CPF ->
            "${value.substring(0, 3)}.${value.substring(3, 6)}.${value.substring(6, 9)}-${value.substring(9)}"
        DocumentType.CNPJ ->
            "${value.substring(0, 2)}.${value.substring(2, 5)}.${value.substring(5, 8)}/${value.substring(8, 12)}-${value.substring(12)}"
    }

    /** Only representation allowed in logs, error messages and API responses. */
    fun masked(): String = when (type) {
        DocumentType.CPF -> "***.${value.substring(3, 6)}.${value.substring(6, 9)}-**"
        DocumentType.CNPJ -> "**.${value.substring(2, 5)}.${value.substring(5, 8)}/****-**"
    }

    override fun toString(): String = masked()

    companion object {
        private const val CPF_LENGTH = 11
        private const val CNPJ_LENGTH = 14

        private val CPF_FIRST_WEIGHTS = (10 downTo 2).toList()
        private val CPF_SECOND_WEIGHTS = (11 downTo 2).toList()
        private val CNPJ_FIRST_WEIGHTS = listOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        private val CNPJ_SECOND_WEIGHTS = listOf(6) + CNPJ_FIRST_WEIGHTS

        fun of(raw: String): DocumentId {
            val normalized = normalize(raw)
            if (!isValidNormalized(normalized)) {
                throw CustomerException.InvalidDocument("Invalid document id: ${redact(normalized)}")
            }
            return DocumentId(normalized)
        }

        fun ofOrNull(raw: String): DocumentId? =
            normalize(raw).takeIf(::isValidNormalized)?.let(::DocumentId)

        fun isValid(raw: String): Boolean = isValidNormalized(normalize(raw))

        private fun normalize(raw: String) = raw.uppercase().filter(Char::isLetterOrDigit)

        private fun isValidNormalized(candidate: String) = when (candidate.length) {
            CPF_LENGTH -> isValidCpf(candidate)
            CNPJ_LENGTH -> isValidCnpj(candidate)
            else -> false
        }

        private fun isValidCpf(cpf: String): Boolean {
            if (!cpf.all(Char::isDigit)) return false
            if (cpf.all { it == cpf[0] }) return false

            val values = cpf.map(::valueOf)
            return checkDigit(values.take(9), CPF_FIRST_WEIGHTS) == values[9] &&
                    checkDigit(values.take(10), CPF_SECOND_WEIGHTS) == values[10]
        }

        private fun isValidCnpj(cnpj: String): Boolean {
            val root = cnpj.take(12)
            val verifiers = cnpj.drop(12)

            // Root accepts A-Z and 0-9 since July 2026; the two check digits stay numeric.
            if (!root.all { it.isDigit() || it in 'A'..'Z' }) return false
            if (!verifiers.all(Char::isDigit)) return false
            if (root.all { it == root[0] }) return false

            val values = cnpj.map(::valueOf)
            return checkDigit(values.take(12), CNPJ_FIRST_WEIGHTS) == values[12] &&
                    checkDigit(values.take(13), CNPJ_SECOND_WEIGHTS) == values[13]
        }

        // IN RFB 2.229/2024: ASCII code minus 48. Digits keep face value, A becomes 17.
        private fun valueOf(character: Char) = character.code - 48

        private fun checkDigit(values: List<Int>, weights: List<Int>): Int {
            val remainder = values.zip(weights).sumOf { (value, weight) -> value * weight } % 11
            return if (remainder < 2) 0 else 11 - remainder
        }

        private fun redact(candidate: String) =
            if (candidate.length < 5) "***" else "***${candidate.takeLast(2)}"
    }
}
