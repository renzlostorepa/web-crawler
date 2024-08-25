package crawler.event_driven;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import org.jsoup.nodes.Document;
import crawler.common.Report;
import crawler.common.ReportRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DocumentCrawlerAgent extends AbstractVerticle {
    private final String word;

    private boolean stopCrawler;

    private List<Future> taskInProgress;
    private Report report;

    public DocumentCrawlerAgent(String word) {
        this.word = word;
        this.report = new Report();
    }

    public void start(Promise<Void> startPromise) {
        stopCrawler = false;
        taskInProgress = new ArrayList<>();

        EventBus eb = vertx.eventBus();
        eb.consumer(EventBusProtocol.NEW_DOC_FOUND, docMsg -> {
            if (!stopCrawler) {
                vertx.executeBlocking((future) -> {
                    var p = Promise.promise();
                    this.taskInProgress.add(p.future());
                    Document doc = (Document) docMsg.body();
                    String text = doc.text();
                    long countWordOccurrence = Arrays.stream(text.split(" ")).
                            filter(s -> s.toLowerCase().contains(word.toLowerCase())).count();
                    this.report.addRecord(new ReportRecord(doc.baseUri(), countWordOccurrence));
                    vertx.eventBus().publish(EventBusProtocol.UPDATE, report);
                    p.complete();
                });
            }
        });

        eb.consumer(EventBusProtocol.STOPPED, ev -> stopCrawler = true);

        eb.consumer(EventBusProtocol.SEEKER_DONE, ev -> {
            CompositeFuture.all(taskInProgress).onSuccess(arg -> {
                vertx.eventBus().publish(EventBusProtocol.REPORT_COMPLETE, report);
            });
        });

        startPromise.complete();
    }

}
