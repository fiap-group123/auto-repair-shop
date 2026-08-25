package br.com.autorepairshop.customer.domain.valueobject.document

import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class DocumentId private constructor(val value: String) : ValueObject {

    val type: DocumentType
        get() = if (value.length == CPF_LENGTH) DocumentType.CPF else DocumentType.CNPJ

    fun formatted(): String = when (type) {
        DocumentType.CPF ->
            "${value.substring(startIndex = 0, endIndex = 3)}." +
                "${value.substring(startIndex = 3, endIndex = 6)}." +
                "${value.substring(startIndex = 6, endIndex = 9)}-" +
                value.substring(startIndex = 9)

        DocumentType.CNPJ ->
            "${value.substring(startIndex = 0, endIndex = 2)}." +
                "${value.substring(startIndex = 2, endIndex = 5)}." +
                "${value.substring(startIndex = 5, endIndex = 8)}/" +
                "${value.substring(startIndex = 8, endIndex = 12)}-" +
                value.substring(startIndex = 12)
    }

    /** Only representation allowed in logs, error messages and API responses. */
    fun masked(): String = when (type) {
        DocumentType.CPF ->
            "***.${value.substring(startIndex = 3, endIndex = 6)}." +
                "${value.substring(startIndex = 6, endIndex = 9)}-**"

        DocumentType.CNPJ ->
            "**.${value.substring(startIndex = 2, endIndex = 5)}." +
                "${value.substring(startIndex = 5, endIndex = 8)}/****-**"
    }

    override fun toString(): String = masked()

    companion object {
        private const val CPF_LENGTH = 11
        private const val CNPJ_LENGTH = 14

        private val CPF_FIRST_WEIGHTS = (10 downTo 2).toList()
        private val CPF_SECOND_WEIGHTS = (11 downTo 2).toList()
        private val CNPJ_FIRST_WEIGHTS = listOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        private val CNPJ_SECOND_WEIGHTS = listOf(element = 6).plus(elements = CNPJ_FIRST_WEIGHTS)

        fun of(raw: String): DocumentId {
            val normalized = normalize(raw = raw)
            if (!isValidNormalized(candidate = normalized)) {
                throw CustomerException.InvalidDocument(
                    message = "Invalid document id: ${redact(candidate = normalized)}",
                )
            }
            return DocumentId(value = normalized)
        }

        fun ofOrNull(raw: String): DocumentId? = normalize(raw = raw)
            .takeIf { isValidNormalized(candidate = it) }
            ?.let { DocumentId(value = it) }

        fun isValid(raw: String): Boolean = isValidNormalized(candidate = normalize(raw = raw))

        private fun normalize(raw: String) = raw.uppercase().filter { it.isLetterOrDigit() }

        private fun isValidNormalized(candidate: String) = when (candidate.length) {
            CPF_LENGTH -> isValidCpf(cpf = candidate)
            CNPJ_LENGTH -> isValidCnpj(cnpj = candidate)
            else -> false
        }

        private fun isValidCpf(cpf: String): Boolean {
            if (!cpf.all { it.isDigit() }) return false
            if (cpf.all { it == cpf[0] }) return false

            val values = cpf.map { valueOf(it) }
            return checkDigit(values = values.take(n = 9), weights = CPF_FIRST_WEIGHTS) == values[9] &&
                checkDigit(values = values.take(n = 10), weights = CPF_SECOND_WEIGHTS) == values[10]
        }

        private fun isValidCnpj(cnpj: String): Boolean {
            val root = cnpj.take(n = 12)
            val verifiers = cnpj.drop(n = 12)

            // Root accepts A-Z and 0-9 since July 2026; the two check digits stay numeric.
            if (!root.all { it.isDigit() || it in 'A'..'Z' }) return false
            if (!verifiers.all { it.isDigit() }) return false
            if (root.all { it == root[0] }) return false

            val values = cnpj.map { valueOf(it) }
            return checkDigit(values = values.take(n = 12), weights = CNPJ_FIRST_WEIGHTS) == values[12] &&
                checkDigit(values = values.take(n = 13), weights = CNPJ_SECOND_WEIGHTS) == values[13]
        }

        // IN RFB 2.229/2024: ASCII code minus 48. Digits keep face value, A becomes 17.
        private fun valueOf(character: Char) = character.code - 48

        private fun checkDigit(
            values: List<Int>,
            weights: List<Int>,
        ): Int {
            val remainder = values.zip(other = weights)
                .sumOf { (value, weight) -> value * weight } % 11
            return if (remainder < 2) 0 else 11 - remainder
        }

        private fun redact(candidate: String) = if (candidate.length < 5) "***" else "***${candidate.takeLast(n = 2)}"
    }
}
