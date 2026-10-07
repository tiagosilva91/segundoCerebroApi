package com.segundoCerebroApi.service.bible;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.segundoCerebroApi.config.AppProperties;
import com.segundoCerebroApi.exception.ExternalServiceException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Cliente HTTP do provedor bolls.life. As respostas são cacheadas (Caffeine),
 * já que o texto bíblico é imutável.
 */
@Component
public class BollsBibleClient {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BollsVerse(Integer book, Integer chapter, int verse, String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BollsSearchResponse(List<BollsVerse> results, Integer total) {}

    private final RestClient restClient;

    public BollsBibleClient(AppProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(15_000);
        this.restClient = RestClient.builder()
                .baseUrl(props.bible().baseUrl())
                .requestFactory(factory)
                .build();
    }

    @Cacheable(cacheNames = "bibleChapters", key = "#translation + ':' + #bookId + ':' + #chapter")
    public List<BollsVerse> getChapter(String translation, int bookId, int chapter) {
        try {
            List<BollsVerse> verses = restClient.get()
                    .uri("/get-text/{tr}/{book}/{chapter}/", translation, bookId, chapter)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return verses == null ? List.of() : verses;
        } catch (RestClientException e) {
            throw new ExternalServiceException("Não foi possível carregar o texto bíblico no momento.", e);
        }
    }

    @Cacheable(cacheNames = "bibleSearch", key = "#translation + ':' + #query.toLowerCase() + ':' + #limit")
    public BollsSearchResponse search(String translation, String query, int limit) {
        try {
            BollsSearchResponse res = restClient.get()
                    .uri(uri -> uri.path("/v2/find/{tr}")
                            .queryParam("search", query)
                            .queryParam("match_case", false)
                            .queryParam("match_whole", false)
                            .queryParam("limit", limit)
                            .queryParam("page", 1)
                            .build(translation))
                    .retrieve()
                    .body(BollsSearchResponse.class);
            return res == null ? new BollsSearchResponse(List.of(), 0) : res;
        } catch (RestClientException e) {
            throw new ExternalServiceException("Não foi possível pesquisar na Bíblia no momento.", e);
        }
    }
}
