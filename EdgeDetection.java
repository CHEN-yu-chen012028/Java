import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class EdgeDetection {

    public static void main(String[] args) {
        try {
            // 1. 讀取彩色原始影像
            File input = new File("C:\\Users\\User\\Downloads\\1.jpg"); // 👈 確保檔名正确
            if (!input.exists()) {
                System.out.println("找不到原始圖片檔案，請檢查檔名和路徑。");
                return;
            }
            BufferedImage src = ImageIO.read(input);
            int w = src.getWidth();
            int h = src.getHeight();

            // 建立三個臨時影像，分別儲存灰階原圖、Ix、Iy
            // 使用 TYPE_BYTE_GRAY 以便正確顯示灰階
            BufferedImage graySrc = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            BufferedImage imgIx = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            BufferedImage imgIy = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

            // 先把原圖轉成灰階
            Graphics2D gGray = graySrc.createGraphics();
            gGray.drawImage(src, 0, 0, null);
            gGray.dispose();

            // 2. 計算梯度 (Ix 和 Iy)
            // 跳過邊緣1像素以防越界
            for (int y = 1; y < h - 1; y++) {
                for (int x = 1; x < w - 1; x++) {
                    
                    // --- 計算 Ix (水平) ---
                    // 公式: (f(x+1, y) - f(x-1, y)) / 2
                    // 我們先算差值，最後再視覺化
                    int grayRight = getGrayValue(src, x + 1, y);
                    int grayLeft = getGrayValue(src, x - 1, y);
                    int gradX = grayRight - grayLeft;

                    // --- 計算 Iy (垂直) ---
                    // 公式: (f(x, y+1) - f(x, y-1)) / 2
                    int grayDown = getGrayValue(src, x, y + 1);
                    int grayUp = getGrayValue(src, x, y - 1);
                    int gradY = grayDown - grayUp;

                    // --- 映射到 0-255 範圍 (浮雕效果視覺化) ---
                    // 原始差值範圍約在 -255 到 255
                    // 先除以2 -> -127 到 127
                    // 加上 128 (Offset) -> 1 到 255 (中性灰是128)
                    int finalIx = clamp((gradX / 2) + 128);
                    int finalIy = clamp((gradY / 2) + 128);

                    // 寫入對應的灰階像素
                    // 灰階影像的 setRGB 只需要傳入亮度值 (0-255) 的特殊編碼
                    imgIx.setRGB(x, y, grayToRGB(finalIx));
                    imgIy.setRGB(x, y, grayToRGB(finalIy));
                }
            }

            // 3. 合併影像
            // 建立一個新的超寬影像 (寬度 x 3)
            BufferedImage finalResult = new BufferedImage(w * 3, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D gCombined = finalResult.createGraphics();

            // 依序畫入：原圖(灰階版)、Ix、Iy
            gCombined.drawImage(graySrc, 0, 0, null);
            gCombined.drawImage(imgIx, w, 0, null);
            gCombined.drawImage(imgIy, w * 2, 0, null);
            
            gCombined.dispose();

            // 4. 儲存結果
            ImageIO.write(finalResult, "jpg", new File("zebra_edge_comparison.jpg"));
            System.out.println("成功！已生成合併影像: zebra_edge_comparison.jpg");

        } catch (Exception e) {
            System.out.println("發生錯誤: " + e.getMessage());
        }
    }

    // --- 輔助方法 ---

    // 1. 獲取像素的標準亮度灰階值
    private static int getGrayValue(BufferedImage img, int x, int y) {
        int rgb = img.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        // 使用 ITU-R BT.601 亮度公式
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }

    // 2. 限制數值在 0-255 之間
    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    // 3. 將灰階值 (0-255) 轉為 Java setRGB 接受的 24位元格式
    private static int grayToRGB(int gray) {
        return (gray << 16) | (gray << 8) | gray;
    }
}