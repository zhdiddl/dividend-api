package sample.dividend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import sample.dividend.exception.imple.NoCompanyException;
import sample.dividend.model.Company;
import sample.dividend.model.Dividend;
import sample.dividend.model.ScrapedResult;
import sample.dividend.model.constants.CacheKey;
import sample.dividend.persist.CompanyRepository;
import sample.dividend.persist.DividendRepository;
import sample.dividend.persist.entity.CompanyEntity;
import sample.dividend.persist.entity.DividendEntity;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        // 회사명으로 회사 정보 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> {
                    log.warn("no company found with name {}", companyName);
                    return new NoCompanyException();
                });

        // 회사 정보에서 ID를 가져와서 배당금 정보 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());

        // 회사 정보와 배당금 정보를 조합해서 반환
//        List<Dividend> dividends = new ArrayList<>();
//        for (var entity : dividendEntities) {
//            dividends.add(Dividend.builder()
//                    .date(entity.getDate())
//                    .dividend(entity.getDividend())
//                    .build());
//        }

        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(new Company(company.getTicker(), company.getName()),
                dividends);
    }

}
