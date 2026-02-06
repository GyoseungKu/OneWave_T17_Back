package org.syu_likelion.OneWave.startup;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Service
public class KstartupClientService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final WebClient webClient;
    private final String serviceKey;
    private final int defaultPerPage;

    public KstartupClientService(
        WebClient.Builder builder,
        @Value("${kstartup.base-url}") String baseUrl,
        @Value("${kstartup.service-key}") String serviceKey,
        @Value("${kstartup.per-page:50}") int defaultPerPage
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.serviceKey = serviceKey;
        this.defaultPerPage = defaultPerPage;
    }

    public List<KstartupItem> fetchAnnouncements(Integer page, Integer perPage) {
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "K-Startup API key not configured");
        }

        int safePage = page == null || page < 1 ? 1 : page;
        int safePerPage = perPage == null || perPage < 1 ? defaultPerPage : Math.min(perPage, 200);

        String xml = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/getAnnouncementInformation01")
                .queryParam("serviceKey", serviceKey)
                .queryParam("page", safePage)
                .queryParam("perPage", safePerPage)
                .build())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        if (xml == null || xml.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "K-Startup response empty");
        }

        return parseItems(xml);
    }

    private List<KstartupItem> parseItems(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            NodeList items = doc.getElementsByTagName("item");

            List<KstartupItem> results = new ArrayList<>();
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                Map<String, String> values = extractValues(item);
                KstartupItem parsed = toItem(values);
                if (parsed != null) {
                    results.add(parsed);
                }
            }
            return results;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "K-Startup response parse failed");
        }
    }

    private Map<String, String> extractValues(Element item) {
        NodeList cols = item.getElementsByTagName("col");
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < cols.getLength(); i++) {
            Element col = (Element) cols.item(i);
            String name = col.getAttribute("name");
            if (name == null || name.isBlank()) {
                continue;
            }
            String value = col.getTextContent();
            values.put(name, value == null ? "" : value.trim());
        }
        return values;
    }

    private KstartupItem toItem(Map<String, String> values) {
        KstartupItem item = new KstartupItem();
        item.setPbancSn(get(values, "pbanc_sn"));
        item.setBizPbancNm(get(values, "biz_pbanc_nm"));
        item.setSuptBizClsfc(get(values, "supt_biz_clsfc"));
        item.setPbancCtnt(get(values, "pbanc_ctnt"));
        item.setAplyTrgt(get(values, "aply_trgt"));
        item.setAplyTrgtCtnt(get(values, "aply_trgt_ctnt"));
        item.setBizEnyy(get(values, "biz_enyy"));
        item.setSuptRegin(get(values, "supt_regin"));
        item.setDetlPgUrl(get(values, "detl_pg_url"));
        item.setAplyMthdOnliRcptIstc(get(values, "aply_mthd_onli_rcpt_istc"));
        item.setBizGdncUrl(get(values, "biz_gdnc_url"));
        item.setPbancNtrpNm(get(values, "pbanc_ntrp_nm"));
        item.setRcrtPrgsYn(get(values, "rcrt_prgs_yn"));
        item.setPbancRcptBgngDt(parseDate(get(values, "pbanc_rcpt_bgng_dt")));
        item.setPbancRcptEndDt(parseDate(get(values, "pbanc_rcpt_end_dt")));
        return item;
    }

    private String get(Map<String, String> values, String key) {
        return values.getOrDefault(key, "");
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (Exception ex) {
            return null;
        }
    }
}
