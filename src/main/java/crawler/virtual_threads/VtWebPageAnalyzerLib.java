package crawler.virtual_threads;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import crawler.common.Flag;
import crawler.common.Report;
import crawler.common.ReportRecord;
import crawler.common.lib.WebPageAnalyzerLib;

import java.util.*;
import java.util.concurrent.*;

public class VtWebPageAnalyzerLib extends Thread implements WebPageAnalyzerLib<Void> {
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    private final String url;
    private final String word;
    private final int depth;
    private final Report report;
    private final RestTemplate restTemplate;
    private final ExecutorService executorService;
    private final Flag flag;

    public VtWebPageAnalyzerLib(String url, String word, int depth, Flag flag, Report report) {
        this.url = url;
        this.word = word;
        this.depth = depth;
        this.flag = flag;
        this.report = report;
        this.restTemplate = new RestTemplate();
        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(5000);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void run() {
        getWordOccurrences(url, word, depth);
        flag.set();
        log("Finished.");
    }

    @Override
    public Void getWordOccurrences(String url, String word, int depth) {
        List<WebSeekerTask> taskList = new ArrayList<>();
        taskList.add(new WebSeekerTask(url, depth));
        scheduleOnVtPool(word, depth, taskList);
        return null;
    }

    private void scheduleOnVtPool(String word, int depth, List<WebSeekerTask> taskList) {
        try {
            List<Future<Optional<Document>>> futureList = executorService.invokeAll(taskList);
            futureList.forEach(future -> extractDocsAndCount(word, future));
            nextIteration(word, depth, taskList, futureList);
            executorService.shutdown();
        } catch (InterruptedException e) {
            log(e.getMessage());
        }
    }

    private void nextIteration(String word, int depth, List<WebSeekerTask> taskList, List<Future<Optional<Document>>> futureList) throws InterruptedException {
        List<Future<Optional<Document>>> countFuture = new ArrayList<>();
        while (depth != 0 && !flag.isSet()) {
            depth--;
            taskList.clear();
            int finalDepth = depth;
            /* Populate Task Pool */
            futureList.forEach(future -> populateTaskList(taskList, future, finalDepth));
            /* Clear Future Pool */
            futureList.clear();
            List<List<WebSeekerTask>> partitions = ListUtils.partition(taskList, 100);
            for (List<WebSeekerTask> chunk : partitions) {
                if (flag.isSet())
                    break;
                /* Execute WebSeekerTasks */
                countFuture.addAll(executorService.invokeAll(chunk));
                /* Count word occurence */
                countFuture.forEach(future -> extractDocsAndCount(word, future));
                /* Copy and clear iteration future*/
                futureList.addAll(countFuture);
                countFuture.clear();
            }
        }
    }

    private void populateTaskList(List<WebSeekerTask> taskList, Future<Optional<Document>> future, int finalDepth) {
        try {
            Optional<Document> documentOptional = future.get();
            if (!documentOptional.isEmpty()) {
                Elements links = documentOptional.get().select("a[href]");
                links.forEach(link -> taskList.add(new WebSeekerTask(link.attr("abs:href"), finalDepth)));
            }
        } catch (InterruptedException | ExecutionException e) {
            log(e.getMessage());
        }
    }

    private void extractDocsAndCount(String word, Future<Optional<Document>> future) {
        try {
            Optional<Document> documentOptional = future.get();
            if (!documentOptional.isEmpty()) {
                Document doc = documentOptional.get();
                countWords(word, doc);
            }
        } catch (InterruptedException | ExecutionException e) {
            log(e.getMessage());
        }
    }

    private void countWords(String word, Document doc) {
        String text = doc.text();
        long countWordOccurrence = Arrays.stream(text.split(" ")).
                filter(s -> s.toLowerCase().contains(word.toLowerCase())).count();
        this.report.addRecord(new ReportRecord(doc.baseUri(), countWordOccurrence));
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private class WebSeekerTask implements Callable<Optional<Document>> {
        private String url;
        private int depth;

        public WebSeekerTask(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }

        @Override
        public Optional<Document> call() {
            if (isValid(depth, url)) {
                try {
                    ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
                    if (Objects.nonNull(entity.getBody())) {
                        Document doc = Jsoup.parse(entity.getBody());
                        doc.setBaseUri(url);
                        return Optional.of(doc);
                    }
                } catch (RestClientException e) {
                    print(ANSI_RED + "Error fetching %s", url + ANSI_RESET);
                }
            }
            return Optional.empty();
        }

        private boolean isValid(int depth, String url) {
            return depth != 0 && UrlValidator.getInstance().isValid(url);
        }
    }

    private void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }
}
