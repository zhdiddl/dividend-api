package sample.dividend.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sample.dividend.service.FinanceService;

@Slf4j
@RestController
@RequestMapping("/finance")
@AllArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("dividend/{companyName}")
    public ResponseEntity<?> searchFinance(@PathVariable("companyName") String companyName) {
        var result = this.financeService.getDividendByCompanyName(companyName);
        log.info("successfully retrieved dividend data for company: {}", companyName);
        return ResponseEntity.ok(result);
    }
}
