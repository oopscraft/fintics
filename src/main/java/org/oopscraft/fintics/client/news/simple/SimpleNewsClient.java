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
        return getNewses(asset.getAssetName());
    }

    @Override
    public List<News> getIndiceNewses(Indice indice) {
        return getNewses(indice.getIndiceName());
    }

    List<News> getNewses(String keyword) {
        try {
            RestTemplate restTemplate = RestTemplateBuilder.create()
                    .insecure(true)
                    .build();
            String url = "https://news.google.com/rss/search";
            url = UriComponentsBuilder.fromUriString(url)
                    .queryParam("q", keyword)
                    .queryParam("hl", "en-US")
                    .queryParam("gl", "US")
                    .queryParam("ceid", "US:en")
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
                        String newsId = IdGenerator.md5(newsUrl);
                        return News.builder()
                                .dateTime(dateTime)
                                .newsId(newsId)
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

    String getContent(String newsUrl) {
        RestTemplate restTemplate = RestTemplateBuilder.create()
                .insecure(true)
                .build();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(newsUrl, String.class);
        String responseBody = responseEntity.getBody();
        Document doc = Jsoup.parse(responseBody);
        return doc.text();
    }

    void analyzeNews(News news, String content) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,parse,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = new Annotation(content);
        pipeline.annotate(annotation);

        int totalSentimentScore = 0;
        int sentenceCount = 0;
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            int sentimentScore = convertSentimentToScore(sentiment);
            totalSentimentScore += sentimentScore;
            sentenceCount++;
        }

        // 평균 감정 점수 계산
        double averageSentimentScore = (double) totalSentimentScore / sentenceCount;
        String overallSentiment = determineOverallSentiment(averageSentimentScore);

        log.debug("Overall Sentiment: {}", overallSentiment);
        log.debug("Average Sentiment Score: {}", averageSentimentScore);
    }

    private static int convertSentimentToScore(String sentiment) {
        switch (sentiment) {
            case "Very positive":
                return 4;
            case "Positive":
                return 3;
            case "Neutral":
                return 2;
            case "Negative":
                return 1;
            case "Very negative":
                return 0;
            default:
                return 2; // 중립
        }
    }

    private static String determineOverallSentiment(double averageSentimentScore) {
        if (averageSentimentScore >= 3.5) {
            return "Very positive";
        } else if (averageSentimentScore >= 2.5) {
            return "Positive";
        } else if (averageSentimentScore >= 1.5) {
            return "Neutral";
        } else if (averageSentimentScore >= 0.5) {
            return "Negative";
        } else {
            return "Very negative";
        }
    }

}
