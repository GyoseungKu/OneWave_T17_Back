package org.syu_likelion.OneWave.feed.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedPositionStatusResponse {
    private String stack;
    private int capacity;
    private int filled;
    private int remaining;
}
