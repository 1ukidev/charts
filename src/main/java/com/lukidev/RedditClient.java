package com.lukidev;

import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;

@Client("https://www.reddit.com")
public interface RedditClient {

    @Get("/r/{name}.json")
    @Header(name = HttpHeaders.USER_AGENT, value = Constants.USER_AGENT)
    @Cacheable("subreddit-cache")
    Subreddit getSubreddit(String name, @QueryValue int limit);
}
