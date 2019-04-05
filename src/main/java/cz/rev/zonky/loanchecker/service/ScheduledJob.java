package cz.rev.zonky.loanchecker.service;

import cz.rev.zonky.loanchecker.model.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Represents periodical job for checking new loans.
 *
 * @author Petr Stahl [petr.stahl@gmail.com]
 */
@Component
@ConditionalOnProperty(value = "app.fetcher.enabled", havingValue = "true")
public class ScheduledJob {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ZonedDateTime lastLoanTime;

    @Autowired
    private DataFetcherService dataFetcherService;

    @Autowired
    private PrinterService printerService;

    /**
     * Run loan check job periodically.
     */
    @Scheduled(fixedDelayString = "${app.fetcher.period}")
    public void showJobs() {
        logger.debug("Starting update job");
        final List<Loan> loans = dataFetcherService.readLoans(lastLoanTime);
        if (!loans.isEmpty()) {
            lastLoanTime = loans.get(0).getDatePublished();
            logger.debug("Last loan time {}", lastLoanTime);

            printerService.printLoans(loans, System.out);
        }
    }
}
