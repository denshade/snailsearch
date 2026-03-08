package search.ui;

import search.ContextMap;
import search.Hosts;
import search.IndexRecord;
import search.store.HostDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Swing JFrame that shows all host indices: index name, update start/end, and a progress bar for completion.
 * Uses SQLite host store so that indices (scan progress) are available.
 */
public final class HostsFrame extends JFrame {

    private static final String DEFAULT_DATA_DIR = "data";

    public HostsFrame(List<IndexRecord> indices) {
        super("Hosts");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(520, 400);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.setBackground(UIManager.getColor("Panel.background"));

        if (indices.isEmpty()) {
            content.add(new JLabel("No indices (hosts) found. Use SQLite store and run an index scan to see progress."));
        } else {
            for (IndexRecord idx : indices) {
                content.add(createCard(idx));
                content.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(content.getBackground());
        setContentPane(scroll);
    }

    private JPanel createCard(IndexRecord idx) {
        JPanel card = new JPanel();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(8, 10, 10, 10)));
        card.setBackground(Color.WHITE);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 0, 2, 8);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        card.add(new JLabel("Index:"), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        card.add(new JLabel(idx.getIndexName()), c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        card.add(new JLabel("Start:"), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        card.add(new JLabel(idx.getUpdateStart()), c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        card.add(new JLabel("End:"), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        card.add(new JLabel(idx.getUpdateEnd().isEmpty() ? "—" : idx.getUpdateEnd()), c);

        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        card.add(new JLabel("Complete:"), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        int pct = (int) Math.round(idx.getPctComplete());
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(Math.min(100, Math.max(0, pct)));
        bar.setStringPainted(true);
        bar.setString(pct + "%");
        card.add(bar, c);

        return card;
    }

    /**
     * Loads indices from the default data directory (SQLite) and shows the frame.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        ContextMap ctx = new ContextMap();
        ctx.useCsv = true;
        new Hosts().loadHosts(ctx);
        HostDAO dao = ctx.hostDAO;
        List<IndexRecord> indices = dao.getIndices();

        SwingUtilities.invokeLater(() -> {
            HostsFrame frame = new HostsFrame(indices);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
