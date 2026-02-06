package org.syu_likelion.OneWave.startup;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KstartupItem {
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
    private String rcrtPrgsYn;
    private LocalDate pbancRcptBgngDt;
    private LocalDate pbancRcptEndDt;
}
