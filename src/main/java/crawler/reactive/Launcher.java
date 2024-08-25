package crawler.reactive;


import crawler.common.InputListener;
import crawler.common.gui.UrlAnalyzerView;

public class Launcher {
    public static void main(String[] args) {
        String url = "corrieredellosport.it";
        String word = "Roma";
        int depth = 2;

        UrlAnalyzerView view = new UrlAnalyzerView(url, word, depth, "::Reactive::");
        InputListener controller = new Controller(view);
        view.addListener(controller);
        view.display();
    }

}
