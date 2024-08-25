package crawler.virtual_threads;

import crawler.common.Flag;
import crawler.common.InputListener;
import crawler.common.gui.UrlAnalyzerView;

public class Controller implements InputListener {
    private final UrlAnalyzerView view;
    private final Flag flag;

    public Controller(UrlAnalyzerView view) {
        this.view = view;
        this.flag = new Flag();
    }

    @Override
    public void start(String url, String word, int depth) {
        flag.reset();

        var master = new Master(url, word, depth, flag, view);
        master.start();
    }

    @Override
    public void stop() {
        flag.set();
    }
}
