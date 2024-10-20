package com.developkim.sommelierbot.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class WineCrawler {
    private static final String BASE_URL = "https://www.winenara.com/shop/product/product_lists?sh_category1_cd=10000&sh_category2_cd=10100&sh_order_by=all&sh_sort_order_by=&sh_filter_code=&sh_rcd=";
    private static final int SIMILARITY_THRESHOLD = 4; // 유사도를 결정하는 임계값

    public List<Wine> crawlWines() throws IOException {
        List<Wine> wineList = new ArrayList<>();
        int page = 1;

        while (true) {
            Document doc = Jsoup.connect(BASE_URL + "?page=" + page).get();
            wineList.addAll(extractWineItems(doc));

            if (!isNextPageAvailable(doc)) break;
            page++;
        }

        return wineList;
    }

    // 추천된 와인 이름에 따라 크롤링
    public List<Wine> crawlWinesForNames(String[] wineNames) throws IOException {
        List<Wine> matchedWines = new ArrayList<>();

        for (String name : wineNames) {
            String searchUrl = BASE_URL + "&search=" + URLEncoder.encode(name.trim(), StandardCharsets.UTF_8);
            Document doc = Jsoup.connect(searchUrl).get(); // 검색 결과 페이지 크롤링
            List<Wine> winesFromPage = extractWineItems(doc);

            // 유사한 이름의 와인을 찾는 부분
            for (Wine wine : winesFromPage) {
                if (isWineSimilar(wine.getName(), name) && !isWineAlreadyMatched(matchedWines, wine)) {
                    matchedWines.add(wine); // 유사한 와인 리스트에 추가
                }
            }
            // 전체 추천된 와인 수가 3개가 되면 반복 종료
            if (matchedWines.size() >= 3) {
                break;
            }
        }

        log.info("Matched wines: {}", matchedWines);
        return matchedWines; // 최종 추천 와인 리스트 반환
    }

    private List<Wine> extractWineItems(Document doc) {
        List<Wine> wines = new ArrayList<>();
        Elements items = doc.select("div.item");

        for (Element item : items) {
            String name = item.select("p.prd_name a").text();
            String price = item.select("span.regular_price ins").text();
            String detailUrl = "https://www.winenara.com" + item.select("p.prd_name a").attr("href");

            Wine wine = new Wine(name, price, detailUrl);
            wines.add(wine);
        }
        return wines;
    }


    private boolean isWineSimilar(String wineNameFromPage, String recommendedWineName) {
        LevenshteinDistance distance = new LevenshteinDistance();
        int result = distance.apply(wineNameFromPage.toLowerCase(), recommendedWineName.toLowerCase());

        return result <= SIMILARITY_THRESHOLD; // 두 이름이 임계값 이하로 유사한 경우 true
    }

    // 이미 매칭된 와인인지 확인하는 메서드
    private boolean isWineAlreadyMatched(List<Wine> matchedWines, Wine newWine) {
        return matchedWines.stream().anyMatch(wine -> wine.getName().equals(newWine.getName()) &&
                wine.getUrl().equals(newWine.getUrl()));
    }

    private boolean isNextPageAvailable(Document doc) {
        return !doc.select("a.next").isEmpty(); // 다음 페이지가 있는지 확인
    }
}
