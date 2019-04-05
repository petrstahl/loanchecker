package cz.rev.zonky.loanchecker.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Represents internal entity with attributes required by the task.
 *
 * @author Petr Stahl [petr.stahl@gmail.com]
 */
public class Loan {
    /** ID of the loan. */
    private Long id;
    /** Name of the loan. */
    private String name;
    /** Rating of the loan. */
    private String rating;
    /** The amount offered to and accepted by borrower. */
    private BigDecimal amount;
    /** Interest rate for investors. */
    private BigDecimal interestRate;
    /** Date of publishing. */
    private ZonedDateTime datePublished;

    /** Main constructor for VO immutability. */
    @JsonCreator
    public Loan(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("rating") String rating,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("interestRate") BigDecimal interestRate,
            @JsonProperty("datePublished") ZonedDateTime datePublished) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.amount = amount;
        this.interestRate = interestRate;
        this.datePublished = datePublished;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRating() {
        return rating;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public ZonedDateTime getDatePublished() {
        return datePublished;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Loan loan = (Loan) other;

        return new EqualsBuilder()
                .append(id, loan.id)
                .append(name, loan.name)
                .append(rating, loan.rating)
                .append(amount, loan.amount)
                .append(interestRate, loan.interestRate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode();
    }
}
