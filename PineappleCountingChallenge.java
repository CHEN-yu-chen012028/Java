import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class PineappleCountingChallenge {

    public static void main(String[] args) throws Exception {

        // 1. 讀取圖片 (請確保路徑正確)
        String path = "C:\\Users\\User\\Pictures\\0001.jpg";
        BufferedImage img = ImageIO.read(new File(path));

        int width = img.getWidth();
        int height = img.getHeight();
        int count = 0;

        // --- 🎯 新增：取得畫筆，準備在圖片上畫大標記 ---
        Graphics2D g2d = img.createGraphics();
        // -------------------------------------------

        boolean[][] visited = new boolean[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (visited[x][y]) continue;

                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // 🎯 判斷紅紫色（鳳梨中心）
                if (r > 120 && b > 120 && g < 100) {

                    // 找到一顆 → 標記附近區域避免重複計算
                    markArea(img, visited, x, y);

                    count++;                   
                    int diameter = 30; // 圓圈的大小 (直徑加大到 30，超級明顯)
                    
                    // 1. 先畫一個黑色的外圈 (陰影效果，讓紅圈在深色背景更跳脫)
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(5)); // 線條寬度加到 5，超級粗
                    g2d.drawOval(x - diameter/2, y - diameter/2, diameter, diameter);

                    // 2. 畫亮紅色的主圓圈 (在黑色外圈裡面)
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(3)); // 主紅圈粗度 3
                    g2d.drawOval(x - diameter/2, y - diameter/2, diameter, diameter);

                    // 3. 在中心畫一個亮黃色的實心小點 (更增加專業感)
                    g2d.setColor(Color.YELLOW);
                    g2d.fillOval(x - 5, y - 5, 10, 10);
                    
                    // ===================================================
                }
            }
        }

        // --- 🎯 新增：釋放畫筆資源並儲存結果圖 ---
        g2d.dispose();
        File outputFile = new File("result_0001_labeled.jpg");
        ImageIO.write(img, "jpg", outputFile);
        // -------------------------------------------

        System.out.println("處理圖片: " + path);
        System.out.println("鳳梨總計數量: " + count + " 顆");
        System.out.println("標記圖片已儲存至: " + outputFile.getAbsolutePath());
    }

    // 用 for 迴圈標記一整塊區域（避免重複計算）- 保持不變
    public static void markArea(BufferedImage img, boolean[][] visited, int cx, int cy) {
        int range = 35; // 半徑
        for (int y = cy - range; y <= cy + range; y++) {
            for (int x = cx - range; x <= cx + range; x++) {
                if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
                    visited[x][y] = true;
                }
            }
        }
    }
}
