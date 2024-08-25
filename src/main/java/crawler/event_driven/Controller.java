package crawler.event_driven;

import io.vertx.core.Vertx;
import crawler.common.InputListener;
import crawler.common.gui.UrlAnalyzerView;

public class Controller implements InputListener {
    private UrlAnalyzerView view;
    private Vertx vertx;
    private String masterId;

    public Controller(UrlAnalyzerView view) {
        this.view = view;
    }

    @Override
    public void start(String url, String word, int depth) {
        this.vertx = Vertx.vertx();
        vertx.deployVerticle(new Master(url, word, depth, view, this),
                res -> {
                    masterId = res.result();
                    log("Master deployed");
                });
    }

    @Override
    public void stop() {
        vertx.eventBus().publish(EventBusProtocol.STOPPED, "stop");
    }

    public void done() {
        vertx.undeploy(masterId)
                .onFailure(Throwable::printStackTrace)
                .onSuccess(res -> {
                    log("Master undeployed.");
                    vertx.close();
                });
    }

    private void log(String msg) {
        System.out.println("[" + getClass().getName() + "] " + msg);
    }
}
