package com.main19.server.follow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class FollowDto {
    @Getter
    @AllArgsConstructor
    public static class Response {
        private long followId;
        private long followerId;
        private long followingId;
    }

    @Getter
    @AllArgsConstructor
    public static class FollowerResponse {
        private long followId;
        private long followerId;
    }

    @Getter
    @AllArgsConstructor
    public static class FollowingResponse {
        private long followId;
        private long followingId;
    }
}
