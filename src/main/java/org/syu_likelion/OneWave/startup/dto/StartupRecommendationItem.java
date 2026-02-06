package org.syu_likelion.OneWave.startup.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartupRecommendationItem {
    private String pbancSn;
    private String bizPbancNm;
    private String suptBizClsfc;
    private String pbancCtnt;
    private String aplyTrgt;
    private String aplyTrgtCtnt;
    private String bizEnyy;
    private String suptRegin;
    private String detlPgUrl;
    private String aplyMthdOnliRcptIstc;
    private String bizGdncUrl;
    private String pbancNtrpNm;
    private LocalDate pbancRcptBgngDt;
    private LocalDate pbancRcptEndDt;
    private int score;
    private List<String> reasons;
}
