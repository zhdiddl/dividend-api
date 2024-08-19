package sample.dividend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sample.dividend.exception.imple.AlreadyExistTickerException;
import sample.dividend.exception.imple.NoCompanyException;
import sample.dividend.model.Company;
import sample.dividend.model.ScrapedResult;
import sample.dividend.persist.CompanyRepository;
import sample.dividend.persist.DividendRepository;
import sample.dividend.persist.entity.CompanyEntity;
import sample.dividend.persist.entity.DividendEntity;
import sample.dividend.scraper.Scraper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie<String, String> trie;
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
        boolean exists = this.companyRepository.existsByTicker(ticker);
        if (exists) {
            log.warn("Ticker {} already exists", ticker);
            throw new AlreadyExistTickerException();
        }
        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker) {
        // ticker로 매칭되는 회사를 스크래핑
        Company company = this.yahooFinanceScraper.scrapeCompanyByTicker(ticker);

        // 해당 회사가 존재하면, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrape(company);

        // 스크래핑 결과 저장
        // 1. 회사 엔티티 생성
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        // 2. 배당 엔티티 목록 생성
        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream() // 배당 정보 리스트를 스트림으로
                .map(e -> new DividendEntity(companyEntity.getId(), e)) // 회사 id와 맵핑
                .collect(Collectors.toList()); // 다시 리스트로 변환

        this.dividendRepository.saveAll(dividendEntities); // id + 배당 정보 리스트를 DB에 저장

        return company;
    }

    public List<String> getCompanyNamesStartingWithKeyword(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntities.stream().map(e -> e.getName()).sorted().collect(Collectors.toList()); // 오름차순 정렬
    }

    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword, "");
    }

    public List<String> autocompleteKeyword(String keyword) {
        return new ArrayList<>(this.trie.prefixMap(keyword).keySet());
//        return this.trie.prefixMap(keyword).keySet().stream()
//                .collect(Collectors.toList());
    }

    void deleteAutocompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }

    @Transactional
    public String deleteCompany(String ticker) {
        var company = this.companyRepository.findByTicker(ticker)
                .orElseThrow(() -> {
                    log.warn("company not found with ticker {}", ticker);
                    return new NoCompanyException();
                });

        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);

        this.deleteAutocompleteKeyword(company.getName()); // 자동 완성 데이터 삭제
        return company.getName();
    }
}
