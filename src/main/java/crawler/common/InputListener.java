package crawler.common;

public interface InputListener {
    void start(String url, String word, int depth);

    void stop();
}
