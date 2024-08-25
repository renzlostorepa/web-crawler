package crawler.common.lib;

public interface WebPageAnalyzerLib<T> {

    T getWordOccurrences(String url, String word, int depth);
}
