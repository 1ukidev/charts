package com.lukidev;

import java.io.File;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.views.View;

@Controller
public class PageController {

    private final Logger log = LoggerFactory.getLogger(PageController.class);

    private final RedditClient redditClient;

    private final Charts charts;

    public PageController(RedditClient redditClient, Charts charts) {
        this.redditClient = redditClient;
        this.charts = charts;
    }

    @Get("/")
    @Produces(MediaType.TEXT_HTML)
    @View("layout")
    @ExecuteOn(TaskExecutors.BLOCKING)
    public HttpResponse<?> index() {
        log.debug("Carregando...");
        long start = System.currentTimeMillis();

        try {
            String name = "brasil";
            Subreddit subreddit = redditClient.getSubreddit(name, 98);
            JFreeChart chart = charts.generateBarChart(subreddit, "r/" + name, 20);

            File outputFile = new File("output/barchart.png");
            outputFile.getParentFile().mkdirs();
            ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);

            long end = System.currentTimeMillis();
            log.debug("Carregado com sucesso. Tempo levado: {} ms", (end - start));

            return HttpResponse.ok();
        } catch (Exception e) {
            log.error("Ocorreu um erro: {}", e.getMessage(), e);
            return HttpResponse.serverError();
        }
    }
}
