package org.oopscraft.fintics.client.news;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.News;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(prefix = "fintics", name = "news-client.class-name", havingValue="org.oopscraft.fintics.client.news.SimpleNewsClient")
@Slf4j
public class SimpleNewsClient extends NewsClient {

    private final static Object LOCK_OBJECT = new Object();

    public SimpleNewsClient(NewsClientProperties newsClientProperties) {
        super(newsClientProperties);
    }

    /**
     * force sleep
     */
    private static synchronized void sleep() {
        synchronized (LOCK_OBJECT) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.warn(e.getMessage());
            }
        }
    }

    @Override
    public List<News> getNewses(Asset asset) {
        // keyword
        String keyword = switch (Optional.ofNullable(asset.getMarket()).orElse("")) {
            case "US" -> asset.getSymbol() + "+" + asset.getAssetName().split("\\s+")[0];
            default -> asset.getAssetName();
        };
        // locale
        Locale locale = parseLocale(asset);
        return getNewses(keyword, locale);
    }

    Locale parseLocale(Asset asset) {
        String market = Optional.ofNullable(asset.getMarket()).orElse("US");
        return switch (market) {
            case "KR" -> new Locale("ko", "KR");
            default -> new Locale("en", "US");
        };
    }

    List<News> getNewses(String keyword, Locale locale) {
        try {
            RestTemplate restTemplate = RestTemplateBuilder.create()
                    .insecure(true)
                    .build();
            String url = "https://news.google.com/search";
            String query = String.format("intitle:%s+when:1h", keyword);
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("q", query)
                    .queryParam("hl", String.format("%s-%s", locale.getLanguage(), locale.getCountry()))  // en-US
                    .queryParam("gl", locale.getCountry())     // US
                    .queryParam("ceid", String.format("%s:%s", locale.getCountry(), locale.getLanguage()))    // US:en
                    .build()
                    .toUriString();
            HttpHeaders headers = new HttpHeaders();
            RequestEntity<Void> requestEntity = RequestEntity.get(url)
                    .headers(headers)
                    .build();
            sleep();
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            String responseBody = responseEntity.getBody();
            Document doc = Jsoup.parse(responseBody);

            // find article elements
            Elements articleElements = doc.getElementsByTag("article");
            List<News> newses = new ArrayList<>();
            for (Element articleElement : articleElements) {
                Element aElement = articleElement.getElementsByTag("a").stream()
                        .filter(it -> StringUtils.isNotBlank(it.text()))
                        .findFirst()
                        .orElse(null);
                String newsUrl = extractNewsUrl(aElement.attr("href"));
                String title = aElement.text();
                String datetimeString = articleElement.getElementsByTag("time").first().attr("datetime");
                LocalDateTime dateTime = LocalDateTime.parse(datetimeString, DateTimeFormatter.ISO_DATE_TIME);
                News news = News.builder()
                        .dateTime(dateTime)
                        .newsUrl(newsUrl)
                        .title(title)
                        .build();
                newses.add(news);
            }

            // sort
            newses.sort(Comparator.comparing(News::getDateTime)
                    .reversed());

            // return
            return newses;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String extractNewsUrl(String googleNewsUrl) {
        Pattern pattern = Pattern.compile("/articles/([A-Za-z0-9_-]+)");
        Matcher matcher = pattern.matcher(googleNewsUrl);
        if (matcher.find()) {
            String encodedUrl = matcher.group(1);
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedUrl.getBytes(StandardCharsets.UTF_8));
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            // extract url
            Pattern urlPattern = Pattern.compile("(https?://[\\w\\-._~:/?#[\\\\]@!$&'()*+,;=%]+)");
            Matcher urlMatcher = urlPattern.matcher(decodedString);
            if (urlMatcher.find()) {
                return urlMatcher.group(1);
            } else {
                return decodedString;
            }
        }
        return null;
    }

}
