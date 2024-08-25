package crawler.event_driven;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import crawler.common.Report;
import crawler.common.gui.UrlAnalyzerView;

public class Master extends AbstractVerticle {
    private final String url;
    private final String word;
    private final int depth;
    private final UrlAnalyzerView view;
    private final Controller controller;

    public Master(String url, String word, int depth, UrlAnalyzerView view, Controller controller) {
        this.url = url;
        this.word = word;
        this.depth = depth;
        this.view = view;
        this.controller = controller;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        long t0 = System.currentTimeMillis();
        EdWebPageAnalyzerLib edWebPageAnalyzerLib = new EdWebPageAnalyzerLib(vertx);
        Future<Report> reportFuture = edWebPageAnalyzerLib.getWordOccurrences(url, word, depth);
        reportFuture.onSuccess(rep -> {
            controller.done();
            view.update(rep.getRecords());
            view.done();
            log("Done in " + (System.currentTimeMillis() - t0) + " ms");
        });

        EventBus eb = vertx.eventBus();
        eb.consumer(EventBusProtocol.UPDATE, ev -> {
            view.update(((Report) ev.body()).getRecords());
        });

        startPromise.complete();
    }

    private void log(String msg) {
        System.out.println("[" + getClass().getName() + "] " + msg);
    }
}
