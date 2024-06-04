package sample.dividend.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sample.dividend.model.Company;
import sample.dividend.model.ScrapedResult;
import sample.dividend.model.constants.CacheKey;
import sample.dividend.persist.CompanyRepository;
import sample.dividend.persist.DividendRepository;
import sample.dividend.persist.entity.CompanyEntity;
import sample.dividend.persist.entity.DividendEntity;
import sample.dividend.scraper.Scraper;

import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    private final Scraper yahooFinanaceScraper;

    // 일정 주기마다 수행
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true) // finance에 해당하는 데이터 모두 포함
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is now active");

        // 저장된 회사 목록 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();

        // 각 회사별 배당 정보를 새로 스크래핑
        for (var company : companies) {
            log.info("scraping scheduler is running -> " + company.getName());
            ScrapedResult scrapedResult = this.yahooFinanaceScraper.scrape(new Company(company.getTicker(), company.getName()));

            // 배당금 정보 중 DB에 없는 값을 저장
            scrapedResult.getDividends().stream()
                    // Dividend 모델을 Dividend 엔티티로 맵핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // 엘리먼트를 순회하면서 없는 경우에는 레파지토리에 삽입
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(company.getId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("successfully inserted new dividends: {}", e.toString());
                        }
                    });

            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000); // 3초 동안 일시 정지
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 현재 스레드를 다시 인터럽트 상태로 설정
                log.warn("Thread was interrupted!");
            } // 현재 쓰레드가 다른 이유로 중단되는 상황을 안전하게 처리하기 위해 필요
        }
    }
}