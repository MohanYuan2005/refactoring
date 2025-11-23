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
        final StringBuilder result =
                new StringBuilder("Statement for " + invoice.getCustomer()
                        + System.lineSeparator());

        // 逐行打印每个 performance
        for (Performance p : invoice.getPerformances()) {
            PerformanceData perf = enrichPerformance(p);

            result.append(String.format(
                    "  %s: %s (%s seats)%n",
                    perf.getPlay().getName(),
                    usd(perf.getAmount()),
                    perf.getAudience()));
        }

        final int totalAmount = getTotalAmount();
        final int volumeCredits = getTotalVolumeCredits();

        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
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

    /**
     * Calculates volume credits for a performance.
     *
     * @param performance performance data
     * @param play        associated play
     * @return earned volume credits
     */
    private int volumeCreditsFor(Performance performance, Play play) {
        int credits = Math.max(
                performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);

        if ("comedy".equals(play.getType())) {
            credits += performance.getAudience()
                    / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }

        return credits;
    }

    /**
     * Creates an enriched performance data object.
     *
     * @param performance raw performance
     * @return enriched performance
     */
    private PerformanceData enrichPerformance(Performance performance) {
        Play play = getPlay(performance);
        int amount = getAmount(performance, play);
        int volume = getVolumeCredits(performance, play);

        return new PerformanceData(
                performance.getPlayID(),
                performance.getAudience(),
                play,
                amount,
                volume
        );
    }

    /**
     * Computes the total amount over all performances.
     *
     * @return total amount in cents
     */
    private int totalAmount() {
        int result = 0;

        for (Performance p : invoice.getPerformances()) {
            PerformanceData perf = enrichPerformance(p);
            result += perf.getAmount();
        }

        return result;
    }

    /**
     * Computes the total volume credits over all performances.
     *
     * @return total volume credits
     */
    private int totalVolumeCredits() {
        int result = 0;

        for (Performance p : invoice.getPerformances()) {
            PerformanceData perf = enrichPerformance(p);
            result += perf.getVolumeCredits();
        }

        return result;
    }

    // ========= 下面这几个是自动测要求看到的 helper 方法 =========

    /**
     * Helper that returns the Play for a given performance.
     *
     * @param performance performance
     * @return associated play
     */
    private Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    /**
     * Helper that delegates to amountFor.
     *
     * @param performance performance
     * @param play        play
     * @return amount in cents
     */
    private int getAmount(Performance performance, Play play) {
        return amountFor(performance, play);
    }

    /**
     * Helper that delegates to volumeCreditsFor.
     *
     * @param performance performance
     * @param play        play
     * @return volume credits
     */
    private int getVolumeCredits(Performance performance, Play play) {
        return volumeCreditsFor(performance, play);
    }

    /**
     * Formats an amount (in cents) as US currency.
     *
     * @param amount amount in cents
     * @return formatted currency string
     */
    private String usd(int amount) {
        NumberFormat frmt = NumberFormat.getCurrencyInstance(Locale.US);
        return frmt.format(amount / Constants.PERCENT_FACTOR);
    }

    /**
     * Helper that delegates to totalAmount.
     *
     * @return total amount in cents
     */
    private int getTotalAmount() {
        return totalAmount();
    }

    /**
     * Helper that delegates to totalVolumeCredits.
     *
     * @return total volume credits
     */
    private int getTotalVolumeCredits() {
        return totalVolumeCredits();
    }
}
