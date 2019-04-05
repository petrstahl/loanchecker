package cz.rev.zonky.loanchecker.service;

import cz.rev.zonky.loanchecker.model.Loan;

import java.io.PrintStream;
import java.util.List;

/**
 * Service for printing data.
 *
 * @author Petr Stahl [petr.stahl@gmail.com]
 */
public interface PrinterService {

    /**
     * Print human readable representation of loans.
     * @param loans given loans
     * @param output the output to print result
     */
    void printLoans(List<Loan> loans, PrintStream output);
}
