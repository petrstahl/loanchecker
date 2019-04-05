package cz.rev.zonky.loanchecker.service;

import cz.rev.zonky.loanchecker.exception.DataFetchException;
import cz.rev.zonky.loanchecker.model.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

/**
 * Represents service for reading data from endpoint.
 *
 * @author Petr Stahl [petr.stahl@gmail.com]
 */
@Service
public class DataFetcherServiceImpl implements DataFetcherService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${app.fetcher.url}")
    private String apiUrl;

    @Value("${app.fetcher.page_size}")
    private int pageSize;

    @Value("${app.fetcher.max_pages}")
    private int maxPages;

    @Value("${app.fetcher.default_offset}")
    private long defaultTimeOffset;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public List<Loan> readLoans(ZonedDateTime startTime) {
        logger.debug("Going to read loans from API");

        List<Loan> loans = new ArrayList<>();
        int currentPage = 0;
        boolean hasNextPage = false;
        do {
            //avoid overload of API
            if ((currentPage + 1) > maxPages) {
                logger.warn("Requested more page than allowed ({})", maxPages);
                break;
            }

            hasNextPage = readLoanPage(loans, calculateStartTime(startTime), currentPage);
            currentPage++;
        }
        while (hasNextPage);

        logger.debug("{} loan(s) from API read", loans.size());
        return loans;
    }

    private int readTotalRecords(ResponseEntity<List<Loan>> response) {
        int totalRecords;
        List<String> totalRecordsHeaders = response.getHeaders().get("X-Total");
        if ((totalRecordsHeaders != null) && !totalRecordsHeaders.isEmpty()) {
            String totalRecordsHeaderValue = totalRecordsHeaders.get(0);
            totalRecords = Integer.parseInt(totalRecordsHeaderValue);
        } else {
            throw new DataFetchException("Response from server does not contain X-Total header");
        }

        return totalRecords;
    }

    private boolean readLoanPage(Collection<Loan> result, ZonedDateTime startTime, int page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Order", "-datePublished");
        headers.add("X-Page", Integer.toString(page));
        headers.add("X-Size", Integer.toString(pageSize));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String startTimeParam = startTime.format(ISO_OFFSET_DATE_TIME).replaceAll("\\+", "%2b");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(apiUrl);
        builder.queryParam("datePublished__gt", startTimeParam);
        URI uri = builder.build(true).toUri();
        ResponseEntity<List<Loan>> response = restTemplate.exchange(uri,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Loan>>(){});

        boolean hasNextPage = false;
        Collection<Loan> loansPage = response.getBody();

        if (loansPage != null) {
            int totalRecords = readTotalRecords(response);
            logger.trace("Read {} records, page: {}, total records: {}", loansPage.size(), page, totalRecords);

            result.addAll(loansPage);
            hasNextPage = result.size() < totalRecords;
        }

        return hasNextPage;

    }

    private ZonedDateTime calculateStartTime(ZonedDateTime startTime) {
        final ZonedDateTime result;
        if (startTime == null) {
            result = ZonedDateTime.now().minus(defaultTimeOffset, ChronoUnit.SECONDS);
        } else {
            result = startTime;
        }

        return result;
    }
}
