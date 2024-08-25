package crawler.event_driven;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import org.jsoup.nodes.Document;
import crawler.common.Report;
import crawler.common.lib.WebPageAnalyzerLib;

import java.util.ArrayList;
import java.util.List;

public class EdWebPageAnalyzerLib implements WebPageAnalyzerLib<Future<Report>> {
    private final Vertx vertx;
    private String url;
    private String word;
    private int depth;
    private String seekerId;
    private String crawlerId;

    public EdWebPageAnalyzerLib(Vertx vertx) {
        this.vertx = vertx;
        vertx.eventBus().registerDefaultCodec(Document.class,
                new GenericCodec<Document>(Document.class));
        vertx.eventBus().registerDefaultCodec(Report.class,
                new GenericCodec<Report>(Report.class));
    }

    public Future<Report> getWordOccurrences(String startingUrl, String word, int depth) {
        initializeParameters(startingUrl, word, depth);
        Promise<Report> p = Promise.promise();
        deploySeeker();
        deployCrawler();
        receiveResult(p);
        return p.future();
    }

    private void initializeParameters(String startingUrl, String word, int depth) {
        this.url = startingUrl;
        this.word = word;
        this.depth = depth;
    }

    private void receiveResult(Promise<Report> p) {
        EventBus eb = vertx.eventBus();
        eb.consumer(EventBusProtocol.REPORT_COMPLETE, ev -> {
            Report report = (Report) ev.body();
            p.complete(report);

            List<Future<Void>> futureList = new ArrayList<>();
            futureList.add(vertx.undeploy(seekerId));
            futureList.add(vertx.undeploy(crawlerId));

            Future.all(futureList).onSuccess(res -> {
                log("Discoverer undeployed.");
                log("Analyser undeployed.");
            });
        });
    }

    private void deployCrawler() {
        vertx.deployVerticle(new DocumentCrawlerAgent(word),
                res -> {
                    if (res.succeeded()) {
                        log("Crawler deployed");
                        this.crawlerId = res.result();
                    }
                });
    }

    private void deploySeeker() {
        vertx.deployVerticle(new DocumentSeekerAgent(url, depth),
                res -> {
                    if (res.succeeded()) {
                        log("Seeker deployed");
                        this.seekerId = res.result();
                    }
                });
    }

    private void log(String msg) {
        System.out.println("[" + getClass().getName() + "] " + msg);
    }

}
