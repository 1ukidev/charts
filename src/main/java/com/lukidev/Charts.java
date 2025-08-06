package com.lukidev;

import java.awt.Color;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.cache.annotation.Cacheable;
import jakarta.inject.Singleton;

@Singleton
public class Charts {

    private final Logger log = LoggerFactory.getLogger(Charts.class);

    private DefaultCategoryDataset generateDataset(Subreddit subreddit, int limit) {
        String[] words = subreddit.getData().getChildren().parallelStream()
            .filter(child -> !child.getData().getStickied())
            .map(child -> child.getData().getTitle())
            .filter(text -> text != null && !text.isBlank())
            .collect(Collectors.joining(" "))
            .toLowerCase()
            .replaceAll("(?<!\\p{L})-(?!\\p{L})", "")
            .replaceAll("[^\\p{L}\\p{N}-\\s]", "")
            .split("\\s+");

        log.debug("Total de palavras encontradas: {}", words.length);

        Map<String, Integer> wordCounts = new HashMap<>();
        for (String word : words) {
            if (Constants.STOPWORDS_PTBR.contains(word)) continue;
            wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
        }

        List<Map.Entry<String, Integer>> topWords = wordCounts.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(limit)
            .toList();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : topWords) {
            dataset.addValue(entry.getValue(), "Frequência", entry.getKey());
        }

        return dataset;
    }

    @Cacheable("barchart-cache")
    public JFreeChart generateBarChart(Subreddit subreddit, String name, int limit) {
        DefaultCategoryDataset dataset = generateDataset(subreddit, limit);

        LocalDateTime dateTime = LocalDateTime.now();
        String dateFormatted = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm a"));
        String title = new StringBuilder(name)
            .append(" | Palavras mais frequentes")
            .append(" | Últimas ")
            .append(subreddit.getData().getChildren().size())
            .append(" postagens")
            .append(" | ")
            .append(dateFormatted)
            .toString();

        JFreeChart chart = ChartFactory.createBarChart(
            title, "Palavra", "Ocorrências", dataset,
            PlotOrientation.HORIZONTAL, true, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.DARK_GRAY);

        Font font = new Font("SansSerif", Font.BOLD, 16);
        Font font2 = new Font("SansSerif", Font.PLAIN, 14);

        chart.getTitle().setFont(font);
        plot.getDomainAxis().setLabelFont(font2);
        plot.getDomainAxis().setTickLabelFont(font2);
        plot.getRangeAxis().setLabelFont(font2);
        plot.getRangeAxis().setTickLabelFont(font2);

        return chart;
    }
}
