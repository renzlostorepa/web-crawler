package crawler.event_driven;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DocumentSeekerAgent extends AbstractVerticle {
    private final String url;
    private final int depth;
    private WebClient client;
    private boolean stopSeeker;

    public DocumentSeekerAgent(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    public void start(Promise<Void> startPromise) {
        startPromise.complete();
        stopSeeker = false;
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(EventBusProtocol.STOPPED, ev -> stopSeeker = true);
        initializeWebClient();
        getWordOccurrences(url, depth)
                .onComplete(v -> eventBus.publish(EventBusProtocol.SEEKER_DONE, "seeker-done"));
    }

    private void initializeWebClient() {
        WebClientOptions options = new WebClientOptions();
        options.setKeepAlive(false);
        options.setConnectTimeout(5000);
        options.setHttp2MaxPoolSize(30);
        this.client = WebClient.create(this.vertx, options);
    }

    public Future<Void> getWordOccurrences(String url, int depth) {
        Promise<Void> p = Promise.promise();
        if (depth != 0 && UrlValidator.getInstance().isValid(url) && !stopSeeker) {
            this.client.getAbs(url).send().onSuccess(response -> {
                if (Objects.nonNull(response.body()) && !stopSeeker) {
                    Document doc = Jsoup.parse(response.body().toString());
                    doc.setBaseUri(url);
                    vertx.eventBus().send(EventBusProtocol.NEW_DOC_FOUND, doc);
                    if (depth - 1 != 0 && !stopSeeker) {
                        Elements links = doc.select("a[href]");
                        List<Future<Void>> futureList = new ArrayList<>();
                        for (Element link : links) {
                            if (!stopSeeker) {
                                futureList.add(getWordOccurrences(link.attr("abs:href"), depth - 1));
                            }
                        }
                        Future.all(futureList).onComplete((ar) -> p.complete());
                    } else p.complete();
                } else p.complete();
            }).onFailure(err -> {
                log(err.getMessage());
                p.complete();
            });
        } else p.complete();
        return p.future();
    }

    private void log(String msg) {
        System.out.println("[" + getClass().getName() + "] " + msg);
    }
}
