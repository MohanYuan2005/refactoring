package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {

    // Fields must be private and final
    private final Invoice invoice;
    private final Map<String, Play> plays;

    /**
     * Creates a StatementPrinter for the given invoice and play map.
     *
     * @param invoice the invoice to print
     * @param plays   a mapping from play ID to Play
     */
    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted plaintext statement of the invoice associated with this printer.
     *
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;

        final StringBuilder result =
                new StringBuilder("Statement for " + invoice.getCustomer()
                        + System.lineSeparator());

        final NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance p : invoice.getPerformances()) {

            final Play play = plays.get(p.getPlayID());
            final int thisAmount = amountFor(p, play);

            // Volume credits
            volumeCredits += Math.max(
                    p.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);

            // Extra credit for every five comedy attendees
            if ("comedy".equals(play.getType())) {
                volumeCredits += p.getAudience()
                        / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
            }

            // Print line for this order
            result.append(String.format(
                    "  %s: %s (%s seats)%n",
                    play.getName(),
                    frmt.format(thisAmount / Constants.PERCENT_FACTOR),
                    p.getAudience()));

            totalAmount += thisAmount;
        }

        result.append(String.format("Amount owed is %s%n",
                frmt.format(totalAmount / Constants.PERCENT_FACTOR)));

        result.append(String.format("You earned %s credits%n", volumeCredits));

        return result.toString();
    }

    /**
     * Calculates the price of a given performance based on the play type.
     *
     * @param performance the performance information
     * @param play        the play associated with the performance
     * @return the calculated amount in cents
     */
    private int amountFor(Performance performance, Play play) {
        int result;

        switch (play.getType()) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;

                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;

            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;

                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }

                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE
                        * performance.getAudience();
                break;

            default:
                throw new RuntimeException(
                        String.format("unknown type: %s", play.getType()));
        }

        return result;
    }
}
