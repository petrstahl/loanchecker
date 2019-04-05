package cz.rev.zonky.loanchecker.service;

import cz.rev.zonky.loanchecker.exception.DataFetchException;
import cz.rev.zonky.loanchecker.model.Loan;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Service for reading data from endpoint.
 *
 * @author Petr Stahl [petr.stahl@gmail.com]
 */
public interface DataFetcherService {

    /**
     * Read source data and convert it to internal representation.
     * @param startTime time of oldest requested loan, if null now - {app.fetcher.default_offset} will be used
     * @return converted loans
     * @throws DataFetchException when reading source data was not successful
     */
    List<Loan> readLoans(ZonedDateTime startTime) throws DataFetchException;
}
