import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CartoonApp extends JFrame {
    // 儲存影像的變數
    private BufferedImage originalImage;
    private BufferedImage edgeImage;
    private BufferedImage quantizedImage;
    private BufferedImage finalCartoonImage;
    
    // 介面元件：顯示三個結果的標籤
    private JLabel edgeImageLabel;      // 結果1：微分邊緣
    private JLabel quantizedImageLabel; // 結果2：像素簡單化色彩
    private JLabel finalCartoonLabel;    // 結果3：最終合併卡通圖
    
    private JSlider edgeSlider;     // 步驟一：微分邊緣閥值滑桿
    private JSlider colorSlider;    // 步驟二：像素簡單化滑桿
    private JButton loadButton;     // 按鈕：載入圖片
    private JButton saveButton;     // 按鈕：儲存結果

    public CartoonApp() {
        // 初始化視窗設定 (寬度拉長以容納三個結果畫面)
        setTitle("Traditional Cartoon Filter App - Three Stage Results");
        setSize(1350, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 視窗居中
        setLayout(new BorderLayout());

        initControlPanel();
        initImageDisplayPanel();
    }

    // ==========================================
    // 介面建立：上方控制面板 (按鈕與滑桿保持不變)
    // ==========================================
    private void initControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(245, 245, 245));
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 12));

        loadButton = new JButton("選擇並載入影像");
        saveButton = new JButton("儲存最終卡通影像");
        saveButton.setEnabled(false);

        JLabel edgeLabel = new JLabel("步驟一：微分邊緣閥值:");
        edgeSlider = new JSlider(30, 180, 90); 
        edgeSlider.setPaintTicks(true);

        JLabel colorLabel = new JLabel("步驟二：像素簡單化 (色彩數):");
        colorSlider = new JSlider(2, 10, 4);
        colorSlider.setMajorTickSpacing(2);
        colorSlider.setPaintTicks(true);
        colorSlider.setPaintLabels(true);

        // 監聽滑桿數值變化 -> 即時更新三個結果
        ChangeListener sliderListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                applyCartoonFilterPipeline();
            }
        };
        edgeSlider.addChangeListener(sliderListener);
        colorSlider.addChangeListener(sliderListener);

        // 載入圖片事件
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(".");
                if (chooser.showOpenDialog(CartoonApp.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        BufferedImage img = ImageIO.read(chooser.getSelectedFile());
                        // 配合 3 欄位排版，將原圖等比例縮放到適合寬度
                        originalImage = resizeImage(img, 400, 400);
                        
                        saveButton.setEnabled(true);
                        applyCartoonFilterPipeline(); // 自動跑濾鏡
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(CartoonApp.this, "讀取影像失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // 儲存圖片事件 (儲存最終的第三個卡通結果)
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (finalCartoonImage == null) return;
                JFileChooser chooser = new JFileChooser(".");
                if (chooser.showSaveDialog(CartoonApp.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        File outFile = chooser.getSelectedFile();
                        if (!outFile.getName().toLowerCase().endsWith(".jpg")) {
                            outFile = new File(outFile.getAbsolutePath() + ".jpg");
                        }
                        ImageIO.write(finalCartoonImage, "jpg", outFile);
                        JOptionPane.showMessageDialog(CartoonApp.this, "卡通影像已成功儲存！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(CartoonApp.this, "儲存影像失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        controlPanel.add(loadButton);
        controlPanel.add(edgeLabel);
        controlPanel.add(edgeSlider);
        controlPanel.add(colorLabel);
        controlPanel.add(colorSlider);
        controlPanel.add(saveButton);

        add(controlPanel, BorderLayout.NORTH);
    }

    // ==========================================
    // 介面建立：中央影像顯示區域 (改成三個結果欄位)
    // ==========================================
    private void initImageDisplayPanel() {
        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new GridLayout(1, 3, 15, 0)); // 改為 1 列 3 行 的佈局
        displayPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 結果一：微分邊緣框
        edgeImageLabel = new JLabel("請載入影像...", SwingConstants.CENTER);
        edgeImageLabel.setBorder(BorderFactory.createTitledBorder("結果一：微分邊緣 (Sobel Edges)"));
        edgeImageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // 結果二：像素簡單化色彩框
        quantizedImageLabel = new JLabel("請載入影像...", SwingConstants.CENTER);
        quantizedImageLabel.setBorder(BorderFactory.createTitledBorder("結果二：像素簡單化 (Quantized)"));
        quantizedImageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // 結果三：最終合併卡通圖
        finalCartoonLabel = new JLabel("請載入影像...", SwingConstants.CENTER);
        finalCartoonLabel.setBorder(BorderFactory.createTitledBorder("結果三：最終卡通合併 (Renderer)"));
        finalCartoonLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        displayPanel.add(edgeImageLabel);
        displayPanel.add(quantizedImageLabel);
        displayPanel.add(finalCartoonLabel);

        add(displayPanel, BorderLayout.CENTER);
    }

    // ==========================================
    // 核心處理流水線 與 三個結果的即時更新
    // ==========================================
    private void applyCartoonFilterPipeline() {
        if (originalImage == null) return;

        int threshold = edgeSlider.getValue();
        int bins = colorSlider.getValue();

        // 1. 計算結果一：對影像作微分 (Sobel) 取得邊緣
        edgeImage = detectEdges(originalImage, threshold);
        
        // 2. 計算結果二：將像素簡單化 (色彩量化)
        quantizedImage = quantizeColors(originalImage, bins);
        
        // 3. 計算結果三：將前兩者合併
        finalCartoonImage = combine(quantizedImage, edgeImage);

        // 將三個影像結果同步刷新至 GUI 的三個 Label 上
        edgeImageLabel.setIcon(new ImageIcon(edgeImage));
        edgeImageLabel.setText("");

        quantizedImageLabel.setIcon(new ImageIcon(quantizedImage));
        quantizedImageLabel.setText("");

        finalCartoonLabel.setIcon(new ImageIcon(finalCartoonImage));
        finalCartoonLabel.setText("");
    }

    // ==========================================
    // 傳統影像處理演算法 (保持不變)
    // ==========================================

    // 第一步：影像一階微分運算 (Sobel)
    private BufferedImage detectEdges(BufferedImage src, int threshold) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        int[][] gray = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                gray[x][y] = (int)(0.299 * r + 0.587 * g + 0.114 * b);
            }
        }

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int gx = 0, gy = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int pixelGray = gray[x + i][y + j];
                        gx += pixelGray * sobelX[i + 1][j + 1];
                        gy += pixelGray * sobelY[i + 1][j + 1];
                    }
                }
                double gradient = Math.sqrt(gx * gx + gy * gy);
                int edgeColor = (gradient > threshold) ? 0x000000 : 0xFFFFFF;
                result.setRGB(x, y, edgeColor);
            }
        }
        return result;
    }

    // 第二步：像素簡單化 (色彩量化)
    private BufferedImage quantizeColors(BufferedImage src, int bins) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int step = 256 / bins;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int qR = Math.min(255, Math.max(0, (r / step) * step + (step / 2)));
                int qG = Math.min(255, Math.max(0, (g / step) * step + (step / 2)));
                int qB = Math.min(255, Math.max(0, (b / step) * step + (step / 2)));

                result.setRGB(x, y, (qR << 16) | (qG << 8) | qB);
            }
        }
        return result;
    }

    // 第三步：混合兩者
    private BufferedImage combine(BufferedImage colorImg, BufferedImage edgeImg) {
        int width = colorImg.getWidth();
        int height = colorImg.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if ((edgeImg.getRGB(x, y) & 0xFFFFFF) == 0x000000) {
                    result.setRGB(x, y, 0x000000);
                } else {
                    result.setRGB(x, y, colorImg.getRGB(x, y));
                }
            }
        }
        return result;
    }

    // 工具方法：調整 BufferedImage 尺寸
    private BufferedImage resizeImage(BufferedImage src, int maxWidth, int maxHeight) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        double ratio = Math.min((double) maxWidth / srcWidth, (double) maxHeight / srcHeight);
        
        if (ratio >= 1.0) return src;

        int targetWidth = (int) (srcWidth * ratio);
        int targetHeight = (int) (srcHeight * ratio);

        Image imgScale = src.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(imgScale, 0, 0, null);
        g2d.dispose();
        
        return dimg;
    }

    // 主程式進入點
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CartoonApp().setVisible(true);
            }
        });
    }
}