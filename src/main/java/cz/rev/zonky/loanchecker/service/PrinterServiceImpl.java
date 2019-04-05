package cz.rev.zonky.loanchecker.service;

import cz.rev.zonky.loanchecker.model.Loan;
import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.springframework.stereotype.Service;

import java.io.PrintStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

/**
 * Represents service for printing data.
 *
 * @author Petr Stahl [petr.stahl@gmail.com]
 */
@Service
public class PrinterServiceImpl implements PrinterService {

    @Override
    public void printLoans(List<Loan> loans, PrintStream output) {

        ReverseListIterator<Loan> iter = new ReverseListIterator(loans);

        while (iter.hasNext()) {
            Loan loan = iter.next();
            output.printf("Published: %s, Id: %s, Name: %s, Rating: %s, Amount: %f, Interest Rate: %f%n",
                    loan.getDatePublished()
                            .withZoneSameInstant(ZoneId.of("Europe/Prague"))
                            .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
                    loan.getId(),
                    loan.getName(),
                    loan.getRating(),
                    loan.getAmount(),
                    loan.getInterestRate());
        }
    }
}
