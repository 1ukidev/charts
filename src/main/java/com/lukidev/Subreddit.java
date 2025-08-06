package com.lukidev;

import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Value;

@Serdeable
@Value
public final class Subreddit {

    private final Data data;

    @Serdeable
    @Value
    public static final class Data {

        private final List<Children> children;

        @Serdeable
        @Value
        public static final class Children {

            private final ChildData data;

            @Serdeable
            @Value
            public static final class ChildData {

                private final String title;
                private final Boolean stickied;
            }
        }
    }
}
