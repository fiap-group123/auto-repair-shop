package br.com.autorepairshop.shared.domain

import br.com.autorepairshop.shared.domain.exception.DomainException
import java.math.BigDecimal
import java.math.RoundingMode

@JvmInline
value class Money private constructor(val amount: BigDecimal) :
    ValueObject,
    Comparable<Money> {

    operator fun plus(other: Money): Money = Money(amount = amount.add(other.amount))

    override fun compareTo(other: Money): Int = amount.compareTo(other.amount)

    operator fun times(quantity: Int): Money = of(raw = amount.multiply(BigDecimal.valueOf(quantity.toLong())))

    override fun toString(): String = amount.toPlainString()

    companion object {
        private const val SCALE = 2

        val ZERO: Money = Money(amount = BigDecimal.ZERO.setScale(SCALE))

        fun of(raw: BigDecimal): Money {
            if (raw.signum() < 0) {
                throw DomainException(message = "Amount cannot be negative.")
            }
            return Money(amount = raw.setScale(SCALE, RoundingMode.HALF_UP))
        }
    }
}
