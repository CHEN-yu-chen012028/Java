import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class PineappleCountingChallenge {

    public static void main(String[] args) throws Exception {

        BufferedImage img = ImageIO.read(
    new File("C:/Users/User/Downloads/0001.jpg")
);

        int width = img.getWidth();
        int height = img.getHeight();

        int count = 0;

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
                }
            }
        }

        System.out.println("鳳梨數量: " + count);
    }

    // 用 for 迴圈標記一整塊區域（避免重複計算）
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