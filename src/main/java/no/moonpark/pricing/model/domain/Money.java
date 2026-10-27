package no.moonpark.pricing.model.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * An amount of money, always stored with two decimals.
 */
public record Money(BigDecimal amount, Currency currency) {

    public static final Currency NOK = Currency.getInstance("NOK");

    public Money {
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(currency, "currency is required");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money nok(BigDecimal amount) {
        return new Money(amount, NOK);
    }
}
