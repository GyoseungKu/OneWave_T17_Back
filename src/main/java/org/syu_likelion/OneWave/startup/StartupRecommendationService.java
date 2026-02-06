package org.syu_likelion.OneWave.startup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.syu_likelion.OneWave.analysis.AiAnalysis;
import org.syu_likelion.OneWave.analysis.AiAnalysisRepository;
import org.syu_likelion.OneWave.idea.Idea;
import org.syu_likelion.OneWave.idea.IdeaCategory;
import org.syu_likelion.OneWave.idea.IdeaRepository;
import org.syu_likelion.OneWave.idea.IdeaStage;
import org.syu_likelion.OneWave.startup.dto.StartupRecommendationItem;
import org.syu_likelion.OneWave.startup.dto.StartupRecommendationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StartupRecommendationService {
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;

    private final IdeaRepository ideaRepository;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final KstartupClientService kstartupClientService;
    private final Map<IdeaCategory, List<String>> categoryKeywords;

    public StartupRecommendationService(
        IdeaRepository ideaRepository,
        AiAnalysisRepository aiAnalysisRepository,
        KstartupClientService kstartupClientService
    ) {
        this.ideaRepository = ideaRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.kstartupClientService = kstartupClientService;
        this.categoryKeywords = buildCategoryKeywords();
    }

    public StartupRecommendationResponse recommend(String email, Long ideaId, Integer limit, Integer perPage) {
        Idea idea = ideaRepository.findByIdeaIdAndUserEmail(ideaId, email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));

        AiAnalysis analysis = aiAnalysisRepository.findByIdeaIdeaId(ideaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis not found"));

        int safeLimit = limit == null || limit < 1 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        List<KstartupItem> candidates = kstartupClientService.fetchAnnouncements(1, perPage);
        LocalDate today = LocalDate.now();

        List<ScoredItem> scored = new ArrayList<>();
        for (KstartupItem item : candidates) {
            if (!isRecruiting(item)) {
                continue;
            }
            if (item.getPbancRcptEndDt() != null && item.getPbancRcptEndDt().isBefore(today)) {
                continue;
            }
            scored.add(score(item, idea, analysis));
        }

        scored.sort(Comparator
            .comparingInt(ScoredItem::score).reversed()
            .thenComparing(scoredItem -> safeDate(scoredItem.item.getPbancRcptEndDt())));

        StartupRecommendationResponse response = new StartupRecommendationResponse();
        response.setIdeaId(idea.getIdeaId());
        response.setAnalysisId(analysis.getAnalysisId());
        response.setCandidateCount(scored.size());
        response.setItems(scored.stream()
            .limit(safeLimit)
            .map(this::toResponseItem)
            .toList());
        return response;
    }

    private boolean isRecruiting(KstartupItem item) {
        String flag = safe(item.getRcrtPrgsYn());
        return flag.equalsIgnoreCase("Y");
    }

    private ScoredItem score(KstartupItem item, Idea idea, AiAnalysis analysis) {
        String text = buildText(item);
        List<String> reasons = new ArrayList<>();
        int score = 0;

        List<String> stageTokens = stageTokens(idea.getStage());
        if (containsAny(text, stageTokens)) {
            score += 15;
            reasons.add("Stage match");
        }

        List<String> categoryTokens = categoryKeywords.getOrDefault(idea.getCategory(), List.of());
        int categoryHits = countHits(text, categoryTokens);
        if (categoryHits > 0) {
            int categoryScore = Math.min(15, categoryHits * 3);
            score += categoryScore;
            reasons.add("Category keywords matched");
        }

        String clsfc = safe(item.getSuptBizClsfc());
        if (analysis.getFeasibilityScore() < 50 && containsAny(clsfc, List.of("멘토링", "컨설팅", "교육"))) {
            score += 12;
            reasons.add("Low feasibility → mentoring/education");
        }
        if (analysis.getMarketScore() < 50
            && containsAny(clsfc, List.of("멘토링", "컨설팅", "교육", "글로벌", "판로", "마케팅"))) {
            score += 8;
            reasons.add("Low market → market support");
        }
        if (analysis.getInnovationScore() < 50 && containsAny(clsfc, List.of("멘토링", "컨설팅", "교육"))) {
            score += 8;
            reasons.add("Low innovation → mentoring/education");
        }
        if (analysis.getTotalScore() >= 75
            && containsAny(clsfc, List.of("글로벌", "정책자금", "사업화", "투자"))) {
            score += 10;
            reasons.add("High score → scale-up support");
        }

        return new ScoredItem(item, score, reasons);
    }

    private StartupRecommendationItem toResponseItem(ScoredItem scored) {
        KstartupItem item = scored.item;
        StartupRecommendationItem response = new StartupRecommendationItem();
        response.setPbancSn(item.getPbancSn());
        response.setBizPbancNm(item.getBizPbancNm());
        response.setSuptBizClsfc(item.getSuptBizClsfc());
        response.setPbancCtnt(item.getPbancCtnt());
        response.setAplyTrgt(item.getAplyTrgt());
        response.setAplyTrgtCtnt(item.getAplyTrgtCtnt());
        response.setBizEnyy(item.getBizEnyy());
        response.setSuptRegin(item.getSuptRegin());
        response.setDetlPgUrl(item.getDetlPgUrl());
        response.setAplyMthdOnliRcptIstc(item.getAplyMthdOnliRcptIstc());
        response.setBizGdncUrl(item.getBizGdncUrl());
        response.setPbancNtrpNm(item.getPbancNtrpNm());
        response.setPbancRcptBgngDt(item.getPbancRcptBgngDt());
        response.setPbancRcptEndDt(item.getPbancRcptEndDt());
        response.setScore(scored.score);
        response.setReasons(scored.reasons);
        return response;
    }

    private String buildText(KstartupItem item) {
        return String.join(" ",
            safe(item.getBizPbancNm()),
            safe(item.getPbancCtnt()),
            safe(item.getAplyTrgt()),
            safe(item.getAplyTrgtCtnt()),
            safe(item.getBizEnyy()),
            safe(item.getSuptBizClsfc())
        );
    }

    private List<String> stageTokens(IdeaStage stage) {
        if (stage == null) {
            return List.of();
        }
        return switch (stage) {
            case IDEA -> List.of("예비", "예비창업", "대학생", "청년", "청소년");
            case PROTOTYPE -> List.of("1년미만", "2년미만", "3년미만", "초기", "창업 초기");
            case MVP -> List.of("3년미만", "5년미만", "7년미만", "사업화");
            case LAUNCHED -> List.of("5년미만", "7년미만", "10년미만", "성장", "사업화");
        };
    }

    private boolean containsAny(String text, List<String> tokens) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            if (lower.contains(token.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private int countHits(String text, List<String> tokens) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        int hits = 0;
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            if (lower.contains(token.toLowerCase(Locale.ROOT))) {
                hits++;
            }
        }
        return hits;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private LocalDate safeDate(LocalDate date) {
        return date == null ? LocalDate.MAX : date;
    }

    private Map<IdeaCategory, List<String>> buildCategoryKeywords() {
        Map<IdeaCategory, List<String>> map = new EnumMap<>(IdeaCategory.class);
        map.put(IdeaCategory.HEALTHCARE, List.of("헬스", "의료", "바이오", "health", "medical", "clinic"));
        map.put(IdeaCategory.FINTECH, List.of("핀테크", "금융", "결제", "payment", "finance", "bank"));
        map.put(IdeaCategory.EDUTECH, List.of("에듀", "교육", "학습", "edtech", "school", "academy"));
        map.put(IdeaCategory.ECOMMERCE, List.of("이커머스", "커머스", "유통", "marketplace", "commerce"));
        map.put(IdeaCategory.SAAS, List.of("saas", "b2b", "클라우드", "cloud", "subscription"));
        map.put(IdeaCategory.SOCIAL, List.of("사회", "공공", "임팩트", "복지", "social", "impact"));
        map.put(IdeaCategory.OTHER, List.of());
        return map;
    }

    private record ScoredItem(KstartupItem item, int score, List<String> reasons) {}
}
