import java.util.Arrays;

public class AlgorithmCollection {

    // 代表無限大（用於圖形演算法的初始化）
    private static final int INF = Integer.MAX_VALUE;

    // 定義圖形的邊結構（供 Bellman-Ford 使用）
    static class Edge {
        int source, destination, weight;
        Edge(int source, int destination, int weight) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }
    }

    // ==========================================
    // 1. Dijkstra 演算法
    // ==========================================
    /**
     * @param adjacencyMatrix 鄰接矩陣，若兩點不相連則填入 INF
     * @param source 起點節點
     */
    public static void dijkstra(int[][] adjacencyMatrix, int source) {
        int totalNodes = adjacencyMatrix.length;
        int[] distance = new int[totalNodes]; // 追蹤目前的最短距離
        boolean[] done = new boolean[totalNodes];  // 標記該節點是否已鎖定最短距離

        // Phase 1: 悲觀初始化
        Arrays.fill(distance, INF);
        distance[source] = 0; // 起點到自己的距離為 0

        System.out.println("--- Dijkstra 演算法執行過程 ---");

        for (int i = 0; i < totalNodes - 1; i++) {
            // 步驟一：從尚未鎖定的節點中，選出 distance 最小的節點
            int currentMinNode = -1;
            int minDistance = INF;
            for (int node = 0; node < totalNodes; node++) {
                if (!done[node] && distance[node] < minDistance) {
                    minDistance = distance[node];
                    currentMinNode = node;
                }
            }

            // 如果找不到可到達的節點，則提早結束
            if (currentMinNode == -1) break;

            // 步驟二：鎖定該節點
            done[currentMinNode] = true;
            System.out.println("鎖定節點: " + currentMinNode + "，當前確定最短距離: " + distance[currentMinNode]);

            // 步驟三：鬆弛 (Relax) 更新鄰居的距離
            for (int neighbor = 0; neighbor < totalNodes; neighbor++) {
                // 必須有邊相連，且鎖定節點本身可達
                if (adjacencyMatrix[currentMinNode][neighbor] != INF) {
                    int newDistance = distance[currentMinNode] + adjacencyMatrix[currentMinNode][neighbor];
                    if (newDistance < distance[neighbor]) {
                        distance[neighbor] = newDistance; // 更新為更短的距離
                    }
                }
            }
        }

        // 輸出最後確立的結果
        System.out.println("\n[Dijkstra 結果] 從起點 " + source + " 到各節點的最短距離：");
        for (int i = 0; i < totalNodes; i++) {
            System.out.println("到節點 " + i + " 的距離: " + (distance[i] == INF ? "無法到達" : distance[i]));
        }
        System.out.println();
    }


    // ==========================================
    // 2. Bellman-Ford 演算法
    // ==========================================
    /**
     * @param edges 圖中所有的邊集合
     * @param totalNodes 總節點數
     * @param source 起點節點
     */
    public static void bellmanFord(Edge[] edges, int totalNodes, int source) {
        int[] distance = new int[totalNodes];

        // Phase 1: 悲觀初始化
        Arrays.fill(distance, INF);
        distance[source] = 0;

        System.out.println("--- Bellman-Ford 演算法執行過程 ---");

        // Phase 2: 邊的無差別疊加鬆弛，最多進行 totalNodes - 1 輪
        for (int round = 1; round < totalNodes; round++) {
            boolean anyUpdateInThisRound = false;

            for (Edge edge : edges) {
                // 只有當起點是可達時，才進行邊的鬆弛更新
                if (distance[edge.source] != INF) {
                    int newDistance = distance[edge.source] + edge.weight;
                    if (newDistance < distance[edge.destination]) {
                        distance[edge.destination] = newDistance;
                        anyUpdateInThisRound = true;
                    }
                }
            }

            System.out.println("第 " + round + " 輪全局邊鬆弛掃描完畢。");

            // Phase 3: 智慧提早結束機制
            if (!anyUpdateInThisRound) {
                System.out.println("-> 偵測到本輪未更新任何數值，系統已達全局最佳解，煞車提早結束！");
                break;
            }
        }

        // 檢查是否存在負權重迴圈（Negative Weight Cycle）
        for (Edge edge : edges) {
            if (distance[edge.source] != INF && distance[edge.source] + edge.weight < distance[edge.destination]) {
                System.out.println("[警告] 圖形中存在負權重迴圈！最短路徑邏輯已崩潰。");
                return;
            }
        }

        // 輸出最後確立的結果
        System.out.println("\n[Bellman-Ford 結果] 從起點 " + source + " 到各節點的最短距離：");
        for (int i = 0; i < totalNodes; i++) {
            System.out.println("到節點 " + i + " 的距離: " + (distance[i] == INF ? "無法到達" : distance[i]));
        }
        System.out.println();
    }


    // ==========================================
    // 3. 0/1 背包問題 (動態規劃)
    // ==========================================
    /**
     * @param weights 物品的重量陣列
     * @param values 物品的價值陣列
     * @param capacity 背包的最大限重容量
     */
    public static void zeroOneKnapsack(int[] weights, int[] values, int capacity) {
        int totalItems = weights.length;
        // dp[i][w] 代表前 i 個物品，在容量為 w 時的最大總價值
        int[][] dp = new int[totalItems + 1][capacity + 1];

        // 建立動態規劃決策矩陣
        for (int i = 1; i <= totalItems; i++) {
            for (int w = 1; w <= capacity; w++) {
                // 如果當前物品的重量大於背包剩餘容量，則無法放入，沿用不放該物品的最佳解
                if (weights[i - 1] > w) {
                    dp[i][w] = dp[i - 1][w];
                } else {
                    // 決策：選擇 [不放入該物品] 或 [放入該物品並騰出相應空間]，取價值較大者
                    int excludeItemValue = dp[i - 1][w];
                    int includeItemValue = values[i - 1] + dp[i - 1][w - weights[i - 1]];
                    dp[i][w] = Math.max(excludeItemValue, includeItemValue);
                }
            }
        }

        System.out.println("--- 0/1 背包問題結果 ---");
        System.out.println("背包可裝入的最大總價值為: " + dp[totalItems][capacity]);
        System.out.println();
    }


    // ==========================================
    // 主要測試入口 (Main Method)
    // ==========================================
    public static void main(String[] args) {
        
        // ------------------------------------------
        // 測試一：Dijkstra 演算法（不可有負權重）
        // ------------------------------------------
        // 建立一個 4 個節點的圖（0, 1, 2, 3）
        int[][] dijkstraGraph = {
            {0, 5, 10, INF},
            {INF, 0, 3, INF},
            {INF, INF, 0, 1},
            {INF, INF, INF, 0}
        };
        dijkstra(dijkstraGraph, 0);

        // ------------------------------------------
        // 測試二：Bellman-Ford 演算法（包容負權重）
        // ------------------------------------------
        int totalNodes = 3;
        // 對應講義：A(0) -> B(1) 權重為 5; A(0) -> C(2) 權重為 10; C(2) -> B(1) 權重為 -50
        Edge[] bellmanFordEdges = {
            new Edge(0, 1, 5),
            new Edge(0, 2, 10),
            new Edge(2, 1, -50)
        };
        bellmanFord(bellmanFordEdges, totalNodes, 0);

        // ------------------------------------------
        // 測試三：0/1 背包問題
        // ------------------------------------------
        int[] weights = {10, 20, 30}; // 物品重量
        int[] values = {60, 100, 120}; // 物品價值
        int capacity = 50;            // 背包限重上限
        zeroOneKnapsack(weights, values, capacity);
    }
}