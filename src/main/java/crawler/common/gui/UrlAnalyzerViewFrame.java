package crawler.common.gui;

import crawler.common.InputListener;
import crawler.common.ReportRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;

public class UrlAnalyzerViewFrame extends JFrame implements ActionListener {

    public static final String PROTOCOL = "https://";
    private final JTextArea state;
    private String url;
    private String word;
    private int depth;
    private final JTextField urlField;
    private final JTextField wordField;
    private final JTextField depthField;
    private final JButton stopButton;
    private final JButton startButton;
    private final JTextArea analyzedUrlList;
    private final ArrayList<InputListener> inputListeners;

    public UrlAnalyzerViewFrame(String url, String word, int depth, String title) {
        inputListeners = new ArrayList<>();

        this.url = url;
        this.word = word;
        this.depth = depth;

        urlField = new JTextField(url);
        wordField = new JTextField(word);
        depthField = new JTextField("" + depth);

        startButton = new JButton("Start");
        stopButton = new JButton("Stop");

        JPanel textControls = new JPanel();
        textControls.add(Box.createRigidArea(new Dimension(40, 0)));
        textControls.add(new JLabel("https://"));
        textControls.add(urlField);
        textControls.add(Box.createRigidArea(new Dimension(20, 0)));
        textControls.add(new JLabel("Word to Search: "));
        textControls.add(wordField);
        textControls.add(Box.createRigidArea(new Dimension(20, 0)));
        textControls.add(new JLabel("Max depth: "));
        textControls.add(depthField);

        JPanel buttonControls = new JPanel();
        buttonControls.add(startButton);
        buttonControls.add(stopButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(textControls);
        controlPanel.add(buttonControls);

        JPanel analyzedUrlPanel = new JPanel();
        analyzedUrlList = new JTextArea(20, 50);
        analyzedUrlPanel.add(analyzedUrlList);
        analyzedUrlList.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(analyzedUrlPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel updatePanel = new JPanel();
        state = new JTextArea(3, 50);
        updatePanel.add(state);
        state.setEditable(false);

        JPanel cp = new JPanel();
        LayoutManager layout = new BorderLayout();
        cp.setLayout(layout);
        cp.add(BorderLayout.NORTH, controlPanel);
        cp.add(BorderLayout.CENTER, scrollPane);
        cp.add(BorderLayout.SOUTH, updatePanel);
        setContentPane(cp);

        setSize(700, 500);

        startButton.addActionListener(this);
        stopButton.addActionListener(this);

        this.startButton.setEnabled(true);
        this.stopButton.setEnabled(false);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle(title);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == startButton) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            String text = urlField.getText();
            if (text.contains(PROTOCOL)) {
                url = text;
            } else url = PROTOCOL + text;
            word = wordField.getText();
            depth = Integer.parseInt(depthField.getText());
            this.notifyStarted(url, word, depth);
        } else if (src == stopButton) {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            this.notifyStopped();
        }
    }

    private void notifyStopped() {
        inputListeners.forEach(InputListener::stop);
    }

    private void notifyStarted(String url, String word, int depth) {
        state.setText("Started.");
        inputListeners.forEach(l -> l.start(url, word, depth));
    }

    public void display() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
        });
    }

    public void reset() {
        SwingUtilities.invokeLater(() -> {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        });
    }

    public void addListener(InputListener controller) {
        inputListeners.add(controller);
    }

    public void done() {
        SwingUtilities.invokeLater(() -> {
            this.startButton.setEnabled(true);
            this.stopButton.setEnabled(false);
            state.append("Done.");
        });
    }

    public void update(Set<ReportRecord> report) {
        SwingUtilities.invokeLater(() -> {
            analyzedUrlList.setText("");
            report.forEach((rec) -> analyzedUrlList.append(rec.getSrcUrl() + " - " + rec.getCounter() + "\n"));
            state.setText("Started.\n");
            state.append("# web pages: " + report.size() + "\n");
        });
    }
}
