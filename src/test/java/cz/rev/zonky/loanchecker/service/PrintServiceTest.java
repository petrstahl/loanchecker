package cz.rev.zonky.loanchecker.service;

import cz.rev.zonky.loanchecker.LoanCheckerApplication;
import cz.rev.zonky.loanchecker.model.Loan;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Basic tests of printing service.
 *
 * @author Petr Stahl [petr.stahl@gmail.com]
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LoanCheckerApplication.class)
public class PrintServiceTest {
    private static final int TEST_YEAR = 1999;

    private PrintStream output;

    private ByteArrayOutputStream bos;

    @Autowired
    private PrinterService printerService;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        bos = new ByteArrayOutputStream();
        output = new PrintStream(bos, true, "UTF-8");
    }

    @Test
    public void singleLine() {
        ZonedDateTime time = LocalDateTime.of(TEST_YEAR, Month.FEBRUARY, 1, 1, 0).atZone(ZoneId.of("Europe/Prague"));
        Loan loan = new Loan(1L, "name1", "AAA", BigDecimal.ONE, BigDecimal.ONE, time);

        List<Loan> loans = Arrays.asList(new Loan[] {loan});

        printerService.printLoans(loans, output);

        String printed = grabOutput();
        assertTrue(printed.contains("1.2.1999"));
        assertTrue(printed.contains("Id: 1"));
    }

    @Test
    public void rightOrder() {
        ZonedDateTime time = LocalDateTime.of(TEST_YEAR, Month.FEBRUARY, 1, 1, 0).atZone(ZoneId.of("Europe/Prague"));

        List<Loan> loans = new ArrayList<>();
        loans.add(new Loan(5L, "name5", "AAA", BigDecimal.ONE, BigDecimal.ONE, time
                .plus(4, ChronoUnit.HOURS)));
        loans.add(new Loan(4L, "name4", "AAA", BigDecimal.ONE, BigDecimal.ONE, time
                .plus(3, ChronoUnit.HOURS)));
        loans.add(new Loan(3L, "name3", "AAA", BigDecimal.ONE, BigDecimal.ONE, time
                .plus(2, ChronoUnit.HOURS)));
        loans.add(new Loan(2L, "name2", "AAA", BigDecimal.ONE, BigDecimal.ONE, time
                .plus(1, ChronoUnit.HOURS)));
        loans.add(new Loan(1L, "name1", "AAA", BigDecimal.ONE, BigDecimal.ONE, time));

        printerService.printLoans(loans, output);

        String printed = grabOutput();
        System.out.println(printed);

        String[] loanLines = printed.split("\\r?\\n");
        assertTrue(loanLines[0].contains("Id: 1"));
        assertTrue(loanLines[1].contains("Id: 2"));
        assertTrue(loanLines[2].contains("Id: 3"));
        assertTrue(loanLines[3].contains("Id: 4"));
        assertTrue(loanLines[4].contains("Id: 5"));

    }

    private String grabOutput() {
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }
}
