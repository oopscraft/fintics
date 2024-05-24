package org.oopscraft.fintics.client.news.simple;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.core.support.RestTemplateBuilder;
import org.oopscraft.fintics.client.news.NewsClient;
import org.oopscraft.fintics.client.news.NewsClientProperties;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.Indice;
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
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "fintics", name = "news-client.class-name", havingValue="org.oopscraft.fintics.client.news.simple.SimpleNewsClient")
@Slf4j
public class SimpleNewsClient extends NewsClient {

    public SimpleNewsClient(NewsClientProperties newsClientProperties) {
        super(newsClientProperties);
    }

    @Override
    public List<News> getAssetNewses(Asset asset) {
        Locale locale = parseLocale(asset);
        return getNewses(asset.getAssetName(), locale);
    }

    @Override
    public List<News> getIndiceNewses(Indice indice) {
        Locale locale = new Locale("en", "US");
        return getNewses(indice.getIndiceName(), locale);
    }

    private Locale parseLocale(Asset asset) {
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
            String url = "https://news.google.com/rss/search";
            String query = String.format("intitle:%s", keyword);
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
            ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
            String responseBody = responseEntity.getBody();
            Document doc = Jsoup.parse(responseBody,"", Parser.xmlParser());
            Elements items = doc.select("item");
            List<News> newses = items.stream()
                    .map(it -> {
                        String pubDate = it.select("pubDate").text();
                        String title = it.select("title").text();
                        String link = it.select("link").text();
                        LocalDateTime dateTime = LocalDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME);
                        String newsUrl = extractNewsUrl(link);
                        return News.builder()
                                .dateTime(dateTime)
                                .newsUrl(newsUrl)
                                .title(title)
                                .build();
                    })
                    .collect(Collectors.toList());

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
