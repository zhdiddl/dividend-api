package sample.dividend.scraper;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import sample.dividend.exception.imple.NoCompanyException;
import sample.dividend.exception.imple.ScrapingFailedException;
import sample.dividend.model.Company;
import sample.dividend.model.Dividend;
import sample.dividend.model.ScrapedResult;
import sample.dividend.model.constants.Month;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class YahooFinanceScraper implements Scraper{
    private static final String STATICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&frequency=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s";

    private static final long START_TIME = 86400; // 60 초 * 60 분 * 24 시간 = 1 일

    // 지정된 회사의 배당금 정보 스크래핑
    @Override
    public ScrapedResult scrape(Company company) {
        var scrapedResult = new ScrapedResult();
        scrapedResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000; // 밀리초를 초 단위로 변경

            String url = String.format(STATICS_URL, company.getTicker(), START_TIME, now);
            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            Document document = connection.get();

            Elements parsingDivs = document.getElementsByAttributeValue("class", "table svelte-ewueuo");
            Element tableEle = parsingDivs.get(0);

            // children 데이터가 없을 경우를 위해 방어 로직 추가
            Elements children = tableEle.children();
            if (children == null || children.isEmpty()) {
                throw new RuntimeException("The table element does not have enough children to access tbody");
            }

            Element tbody = tableEle.children().get(1);// Thead는 0, Tbody는 1, Tfoot은 2

            List<Dividend> dividends = new ArrayList<>();
            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] splits = txt.split(" "); // 공백 기준으로 잘라 배열로 담아 준다
                int month = Month.strToNumber(splits[0]);
                int day = Integer.parseInt(splits[1].replace(",", ""));
                int year = Integer.parseInt(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));
            }
            scrapedResult.setDividends(dividends);

        } catch (IOException e) {
            log.error("failed to scrape data for company : {}", company.getTicker(), e);
            throw new ScrapingFailedException();
        }

        return scrapedResult;
    }

    // 지정된 티커의 회사 정보 스크래핑
    @Override
    public Company scrapeCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker);
        try {
            Document document = Jsoup.connect(url).get();
            Element parsingName = document.select("h1.svelte-3a2v0c").get(0);
            String title = parsingName.text().split(" \\(")[0];

            return new Company(ticker, title);

        } catch (IOException e) {
            log.error("failed to scrape company info for ticker : {}", ticker, e);
            throw new NoCompanyException();
        }
    }
}
