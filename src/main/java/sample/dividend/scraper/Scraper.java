package sample.dividend.scraper;

import sample.dividend.model.Company;
import sample.dividend.model.ScrapedResult;

public interface Scraper {
    Company scrapeCompanyByTicker(String ticker);

    ScrapedResult scrape(Company company);
}
