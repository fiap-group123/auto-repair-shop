package br.com.autorepairshop.shared.domain

import br.com.autorepairshop.shared.domain.exception.DomainException
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class MoneyTest {

    @Test
    fun `normalizes the scale to two decimals`() {
        assertEquals(
            expected = "10.00",
            actual = Money.of(raw = BigDecimal("10")).toString(),
        )
        assertEquals(
            expected = "10.13",
            actual = Money.of(raw = BigDecimal("10.125")).toString(),
        )
    }

    @Test
    fun `rejects a negative amount`() {
        assertFailsWith<DomainException> {
            Money.of(raw = BigDecimal("-0.01"))
        }
    }

    @Test
    fun `accepts zero`() {
        assertEquals(
            expected = Money.ZERO,
            actual = Money.of(raw = BigDecimal.ZERO),
        )
    }

    @Test
    fun `adds two amounts`() {
        val total = Money.of(raw = BigDecimal("99.90")).plus(Money.of(raw = BigDecimal("0.10")))

        assertEquals(
            expected = "100.00",
            actual = total.toString(),
        )
    }

    @Test
    fun `multiplies by a quantity`() {
        val total = Money.of(raw = BigDecimal("120.50")).times(quantity = 3)

        assertEquals(
            expected = "361.50",
            actual = total.toString(),
        )
    }

    @Test
    fun `multiplying by zero yields zero`() {
        assertEquals(
            expected = Money.ZERO,
            actual = Money.of(raw = BigDecimal("120.50")).times(quantity = 0),
        )
    }

    @Test
    fun `rejects multiplication by a negative quantity`() {
        assertFailsWith<DomainException> {
            Money.of(raw = BigDecimal("10.00")).times(quantity = -1)
        }
    }
}
