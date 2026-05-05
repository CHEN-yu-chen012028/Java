import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;

public class MeerkatSegmentation {

    static BufferedImage original;
    static BufferedImage grayscale;
    static BufferedImage segmented;

    static JLabel origLabel    = new JLabel();
    static JLabel grayLabel    = new JLabel();
    static JLabel segLabel     = new JLabel();
    static JLabel histLabel    = new JLabel();

    static JSlider sliderT1    = new JSlider(0, 255, 175);
    static JSlider sliderT2    = new JSlider(0, 255, 228);
    static JLabel  lblT1       = new JLabel("T1 = 175");
    static JLabel  lblT2       = new JLabel("T2 = 228");

    public static void main(String[] args) throws Exception {
        // ---------- load image ----------
        original  = ImageIO.read(new File("3.jpg"));
        grayscale = toGrayscale(original);
        segmented = segment(grayscale, sliderT1.getValue(), sliderT2.getValue());

        // ---------- build GUI ----------
        JFrame frame = new JFrame("Multi-Threshold Segmentation — Assignment 1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(15, 15, 15));
        frame.setLayout(new BorderLayout(10, 10));

        // ----- top: controls -----
        JPanel ctrl = new JPanel(new GridLayout(2, 3, 12, 6));
        ctrl.setBackground(new Color(20, 20, 20));
        ctrl.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel hdr1 = makeLabel("LOW THRESHOLD  T1  (dark features → BLACK)", new Color(0, 180, 255));
        JLabel hdr2 = makeLabel("HIGH THRESHOLD T2  (bright BG → BLACK)",     new Color(180, 240, 60));
        JLabel hdr3 = makeLabel("LEGEND", new Color(160, 160, 160));

        JPanel p1 = new JPanel(new BorderLayout(6,0));
        p1.setBackground(new Color(20,20,20));
        sliderT1.setBackground(new Color(20,20,20));
        sliderT1.setForeground(new Color(0,180,255));
        lblT1.setForeground(new Color(0,180,255));
        lblT1.setFont(new Font("Courier New", Font.BOLD, 14));
        p1.add(sliderT1, BorderLayout.CENTER);
        p1.add(lblT1,   BorderLayout.EAST);

        JPanel p2 = new JPanel(new BorderLayout(6,0));
        p2.setBackground(new Color(20,20,20));
        sliderT2.setBackground(new Color(20,20,20));
        sliderT2.setForeground(new Color(180,240,60));
        lblT2.setForeground(new Color(180,240,60));
        lblT2.setFont(new Font("Courier New", Font.BOLD, 14));
        p2.add(sliderT2, BorderLayout.CENTER);
        p2.add(lblT2,    BorderLayout.EAST);

        JTextArea legend = new JTextArea(
            "< T1  →  Eyes/Nose (Black)\nT1 ~ T2  →  Fur (White)\n> T2  →  Background (Black)");
        legend.setFont(new Font("Courier New", Font.PLAIN, 11));
        legend.setForeground(new Color(180,180,180));
        legend.setBackground(new Color(20,20,20));
        legend.setEditable(false);

        ctrl.add(hdr1); ctrl.add(hdr2); ctrl.add(hdr3);
        ctrl.add(p1);   ctrl.add(p2);   ctrl.add(legend);
        frame.add(ctrl, BorderLayout.NORTH);

        // ----- center: 4 images -----
        JPanel imgs = new JPanel(new GridLayout(1, 4, 8, 0));
        imgs.setBackground(new Color(10, 10, 10));
        imgs.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        imgs.add(panel("ORIGINAL",  origLabel));
        imgs.add(panel("GRAYSCALE", grayLabel));
        imgs.add(panel("SEGMENTED (Multi-threshold)", segLabel));
        imgs.add(panel("HISTOGRAM", histLabel));
        frame.add(imgs, BorderLayout.CENTER);

        // ----- bottom: status -----
        JLabel status = makeLabel(
            "Assignment 1: Multi-threshold Segmentation  |  黃祥睿 — NYCU / NPU",
            new Color(80,80,80));
        status.setBorder(BorderFactory.createEmptyBorder(4,16,8,16));
        status.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(status, BorderLayout.SOUTH);

        // refresh images
        refresh();

        // slider listeners
        ChangeListener cl = e -> {
            int t1 = sliderT1.getValue();
            int t2 = sliderT2.getValue();
            // keep T1 < T2
            if (t1 >= t2) {
                if (e.getSource() == sliderT1) sliderT1.setValue(t2 - 1);
                else                           sliderT2.setValue(t1 + 1);
            }
            lblT1.setText("T1 = " + sliderT1.getValue());
            lblT2.setText("T2 = " + sliderT2.getValue());
            segmented = segment(grayscale, sliderT1.getValue(), sliderT2.getValue());
            refresh();
        };
        sliderT1.addChangeListener(cl);
        sliderT2.addChangeListener(cl);

        frame.setSize(1300, 620);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // ----- save result -----
        segmented = segment(grayscale, sliderT1.getValue(), sliderT2.getValue());
        ImageIO.write(segmented, "PNG",
            new File("/mnt/user-data/outputs/segmented_output.png"));
        System.out.println("Saved: /mnt/user-data/outputs/segmented_output.png");
    }

    // ── helpers ──────────────────────────────────────────

    static BufferedImage toGrayscale(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    static BufferedImage segment(BufferedImage gray, int t1, int t2) {
        int w = gray.getWidth(), h = gray.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        int[] pixels = gray.getRaster().getPixels(0, 0, w, h, (int[]) null);
        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            int g = pixels[i];
            // pixel < t1  → 0 (dark features: eyes, nose)
            // t1 <= pixel <= t2 → 255 (rabbit fur)
            // pixel > t2  → 0 (white background)
            result[i] = (g >= t1 && g <= t2) ? 255 : 0;
        }
        out.getRaster().setPixels(0, 0, w, h, result);
        return out;
    }

    static BufferedImage buildHistogram(BufferedImage gray, int t1, int t2) {
        // compute histogram
        int[] hist = new int[256];
        int w = gray.getWidth(), h = gray.getHeight();
        int[] pixels = gray.getRaster().getPixels(0, 0, w, h, (int[]) null);
        for (int p : pixels) hist[p]++;
        int maxV = 1;
        for (int v : hist) if (v > maxV) maxV = v;

        int HW = 512, HH = 200;
        BufferedImage img = new BufferedImage(HW, HH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(18, 18, 18));
        g2.fillRect(0, 0, HW, HH);

        for (int i = 0; i < 256; i++) {
            int barH = (int)((double) hist[i] / maxV * (HH - 20));
            int x = i * 2;
            // colour by region
            if (i < t1)       g2.setColor(new Color(0, 80, 130));
            else if (i <= t2) g2.setColor(new Color(80, 130, 20));
            else              g2.setColor(new Color(50, 50, 50));
            g2.fillRect(x, HH - barH, 2, barH);
        }
        // threshold lines
        g2.setColor(new Color(0, 180, 255));
        g2.drawLine(t1 * 2, 0, t1 * 2, HH);
        g2.setColor(new Color(180, 240, 60));
        g2.drawLine(t2 * 2, 0, t2 * 2, HH);
        // labels
        g2.setFont(new Font("Courier New", Font.BOLD, 11));
        g2.setColor(new Color(0, 180, 255));
        g2.drawString("T1=" + t1, t1 * 2 + 2, 14);
        g2.setColor(new Color(180, 240, 60));
        g2.drawString("T2=" + t2, Math.min(t2 * 2 + 2, HW - 60), 28);
        g2.dispose();
        return img;
    }

    static void refresh() {
        origLabel.setIcon(scaled(original));
        grayLabel.setIcon(scaled(grayscale));
        segLabel .setIcon(scaled(segmented));
        histLabel.setIcon(new ImageIcon(
            buildHistogram(grayscale, sliderT1.getValue(), sliderT2.getValue())));
    }

    static ImageIcon scaled(BufferedImage img) {
        int maxW = 280, maxH = 340;
        double ratio = Math.min((double) maxW / img.getWidth(),
                                (double) maxH / img.getHeight());
        int nw = (int)(img.getWidth()  * ratio);
        int nh = (int)(img.getHeight() * ratio);
        Image scaled = img.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    static JPanel panel(String title, JLabel content) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(new Color(18, 18, 18));
        p.setBorder(BorderFactory.createLineBorder(new Color(35, 35, 35)));
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(new Font("Courier New", Font.BOLD, 10));
        lbl.setForeground(new Color(100, 100, 100));
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));
        content.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lbl,     BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    static JLabel makeLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(new Font("Courier New", Font.PLAIN, 11));
        return l;
    }
}