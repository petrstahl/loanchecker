package cz.rev.zonky.loanchecker.service;

import cz.rev.zonky.loanchecker.LoanCheckerApplication;
import cz.rev.zonky.loanchecker.exception.DataFetchException;
import cz.rev.zonky.loanchecker.model.Loan;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestGatewaySupport;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Basic tests for fetching loans.
 *
 * @author Petr Stahl [petr.stahl@gmail.com]
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LoanCheckerApplication.class)
public class DataFetcherServiceTest {
    @Value("${app.fetcher.url}")
    private String apiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DataFetcherService service;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        RestGatewaySupport gateway = new RestGatewaySupport();
        gateway.setRestTemplate(restTemplate);
        mockServer = MockRestServiceServer.createServer(gateway);
    }

    @Test
    public void emptyResult() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total", "0");

        mockServer.expect(once(), header("X-Page", "0"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON).headers(headers));

        Collection<Loan> loans = service.readLoans(null);
        assertEquals(0, loans.size());
    }

    @Test(expected = HttpClientErrorException.class)
    public void invalidResult404() {
        mockServer.expect(once(), anything())
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Collection<Loan> loans = service.readLoans(null);
    }

    @Test(expected = HttpServerErrorException.class)
    public void invalidResult500() {
        mockServer.expect(once(), anything())
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        Collection<Loan> loans = service.readLoans(null);
    }

    @Test(expected = DataFetchException.class)
    public void missingXTotalHeader() {
        mockServer.expect(once(), header("X-Page", "0"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Collection<Loan> loans = service.readLoans(null);
    }

    @Test
    public void singleRecord() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total", "1");

        String responseBody = makeJson(
                "[{`id`:1, `name`:`name1`, `rating`:`AA`, `amount`:1, `interestRate`:1, `datePublished`:`2019-04-04T23:45:13.874+02:00`}]");
        mockServer.expect(once(), header("X-Page", "0"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON).headers(headers));

        Collection<Loan> loans = service.readLoans(null);
        assertEquals(1, loans.size());
        assertEquals(new Loan(1L, "name1", "AA", BigDecimal.ONE, BigDecimal.ONE, ZonedDateTime.parse("2019-04-04T23:45:13.874+02:00")),
                loans.iterator().next());
    }


    @Test
    public void singlePage() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total", "5");

        String responseBody = makeJson("["
                + "{`id`:1, `name`:`name1`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:2, `name`:`name2`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:3, `name`:`name3`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:4, `name`:`name4`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:5, `name`:`name5`, `rating`:`AA`, `amount`:1, `interestRate`:1}"
                + "]");
        mockServer.expect(once(), header("X-Page", "0"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON).headers(headers));

        Collection<Loan> loans = service.readLoans(null);
        assertEquals(5, loans.size());
    }

    @Test
    public void twoPages() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total", "6");

        String responseBody = makeJson("["
                + "{`id`:1, `name`:`name1`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:2, `name`:`name2`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:3, `name`:`name3`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:4, `name`:`name4`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:5, `name`:`name5`, `rating`:`AA`, `amount`:1, `interestRate`:1}"
                + "]");
        mockServer.expect(once(), header("X-Page", "0"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON).headers(headers));

        String responseBody2 = makeJson("[{`id`:1, `name`:`name1`, `rating`:`AA`, `amount`:1, `interestRate`:1}]");
        mockServer.expect(once(), header("X-Page", "1"))
                .andRespond(withSuccess(responseBody2, MediaType.APPLICATION_JSON).headers(headers));

        Collection<Loan> loans = service.readLoans(null);
        assertEquals(6, loans.size());
    }

    @Test
    public void pagesCountOverLimit() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total", "11");

        String responseBody = makeJson("["
                + "{`id`:1, `name`:`name1`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:2, `name`:`name2`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:3, `name`:`name3`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:4, `name`:`name4`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:5, `name`:`name5`, `rating`:`AA`, `amount`:1, `interestRate`:1}"
                + "]");
        mockServer.expect(once(), header("X-Page", "0"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON).headers(headers));

        String responseBody2 = makeJson("["
                + "{`id`:6, `name`:`name6`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:7, `name`:`name7`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:8, `name`:`name8`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:9, `name`:`name9`, `rating`:`AA`, `amount`:1, `interestRate`:1},"
                + "{`id`:10, `name`:`name10`, `rating`:`AA`, `amount`:1, `interestRate`:1}"
                + "]");
        mockServer.expect(once(), header("X-Page", "1"))
                .andRespond(withSuccess(responseBody2, MediaType.APPLICATION_JSON).headers(headers));

        Collection<Loan> loans = service.readLoans(null);
        assertEquals(10, loans.size());
    }

    private Collection<Loan> createLoanBatch(long startId, int count) {
        Collection<Loan> result = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            Loan loan = new Loan(i + startId, "name" + i, "AA", BigDecimal.valueOf(i), BigDecimal.valueOf(i), ZonedDateTime.now());
            result.add(loan);
        }
        return result;
    }

    /**
     * Helper method to avoid using \" in json strings.
     *
     * @param jsonToProcess json string with back apostrophes instead of double quotes. Cannot be null.
     * @return string with replaced back apostrophes
     */
    private String makeJson(String jsonToProcess) {
        return jsonToProcess.replace('`', '"');
    }


}
