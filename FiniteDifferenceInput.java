import java.util.Scanner;
import java.util.function.DoubleFunction;

public class FiniteDifferenceInput {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=== 數值微分 (中心差分法) 計算機 ===");

        // 1. 讓使用者輸入 x (計算點)
        System.out.print("請輸入欲計算的點 x (例如 2.0 或 1.5708): ");
        double x = sc.nextDouble();

        // 2. 讓使用者輸入 h (極小位移量)
        System.out.print("請輸入位移量 h (建議輸入極小值，例如 0.0001): ");
        double h = sc.nextDouble();

        // 定義函數
        DoubleFunction<Double> f1 = val -> val * val; // f(x) = x^2
        DoubleFunction<Double> f2 = Math::sin;        // f(x) = sin(x)

        // 3. 計算並顯示結果
        double deriv1 = (f1.apply(x + h) - f1.apply(x - h)) / (2 * h);
        double deriv2 = (f2.apply(x + h) - f2.apply(x - h)) / (2 * h);

        System.out.println("\n--- 計算結果 ---");
        System.out.printf("當 x = %.4f, h = %.6f 時：\n", x, h);
        System.out.printf("1. f(x)=x^2   的導數逼近值為: %.8f (理論值為 %.2f)\n", deriv1, 2 * x);
        System.out.printf("2. f(x)=sin(x) 的導數逼近值為: %.8f (理論值為 %.4f)\n", deriv2, Math.cos(x));

        sc.close();
    }
}