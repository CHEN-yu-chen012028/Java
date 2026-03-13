import java.util.Scanner;

public class MatrixMultiplication {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 1. 輸入矩陣 A 的大小
        System.out.println("--- 設定矩陣 A ---");
        System.out.print("請輸入矩陣 A 的列數 (Rows): ");
        int rowsA = sc.nextInt();
        System.out.print("請輸入矩陣 A 的行數 (Cols): ");
        int colsA = sc.nextInt();

        // 2. 輸入矩陣 B 的大小
        System.out.println("\n--- 設定矩陣 B ---");
        System.out.print("請輸入矩陣 B 的列數 (Rows): ");
        int rowsB = sc.nextInt();
        System.out.print("請輸入矩陣 B 的行數 (Cols): ");
        int colsB = sc.nextInt();

        // 3. 檢查維度是否符合規律：A 的行數必須等於 B 的列數
        if (colsA != rowsB) {
            System.out.println("\n錯誤：矩陣 A 的行數 (" + colsA + ") 必須等於矩陣 B 的列數 (" + rowsB + ") 才能相乘！");
            return;
        }

        // 4. 輸入矩陣 A 的數值
        int[][] A = new int[rowsA][colsA];
        System.out.println("\n請輸入矩陣 A 的元素：");
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsA; j++) {
                System.out.printf("A[%d][%d] = ", i, j);
                A[i][j] = sc.nextInt();
            }
        }

        // 5. 輸入矩陣 B 的數值
        int[][] B = new int[rowsB][colsB];
        System.out.println("\n請輸入矩陣 B 的元素：");
        for (int i = 0; i < rowsB; i++) {
            for (int j = 0; j < colsB; j++) {
                System.out.printf("B[%d][%d] = ", i, j);
                B[i][j] = sc.nextInt();
            }
        }

        // 6. 執行計算 (核心邏輯)
        int[][] C = new int[rowsA][colsB];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }

        // 7. 輸出結果
        System.out.println("\n--- 計算結果矩陣 C ---");
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                System.out.print(C[i][j] + "\t");
            }
            System.out.println();
        }
        
        sc.close();
    }
}