import java.util.*;

/**
 * 最佳化決策演算法 - Optimization Decision Algorithms
 * 涵蓋：BFS、Dijkstra、Bellman-Ford、0/1 背包問題 (Knapsack DP)
 *
 * 時間複雜度摘要：
 *   1. BFS          → O(V + E)
 *   2. Dijkstra     → O((V + E) log V)  使用 Priority Queue
 *   3. Bellman-Ford → O(V × E)
 *   4. Knapsack DP  → O(N × W)
 */
public class DecisionAlgorithms {

    // =========================================================
    // 1. BFS — 廣度優先搜尋 (Breadth-First Search)
    //    情境：人際網路 — 透過最少的人認識對方
    //    節點：Sunny, Amy, James, Marshall, Cara, John, Uriah, Yummy, Andy, Bella, Eric, May
    //    目標：從 Marshall 出發，找到認識每個人所需的最少中間人數
    //    時間複雜度：O(V + E)
    // =========================================================
    static class BFS {
        private int V;
        private String[] names;
        private List<List<Integer>> adj;

        BFS(String[] names) {
            this.V = names.length;
            this.names = names;
            adj = new ArrayList<>();
            for (int i = 0; i < V; i++) adj.add(new ArrayList<>());
        }

        void addEdge(int u, int v) {
            adj.get(u).add(v);
            adj.get(v).add(u);
        }

        /**
         * 從 src 出發，回傳到每個節點的最短跳數距離，並印出逐步過程
         * @return dist[] 陣列，dist[i] = src 到 i 的最少認識人數；-1 表示不相識
         */
        int[] shortestHops(int src) {
            int[] dist = new int[V];
            int[] prev = new int[V];   // 記錄前驅，用於印路徑
            Arrays.fill(dist, -1);
            Arrays.fill(prev, -1);
            dist[src] = 0;

            Queue<Integer> queue = new LinkedList<>();
            queue.offer(src);

            int step = 0;
            System.out.println("  [過程] 初始化：將 " + names[src] + " 加入佇列");
            System.out.println("         佇列：[" + names[src] + "]");
            System.out.println();

            while (!queue.isEmpty()) {
                int node = queue.poll();
                step++;
                System.out.printf("  [步驟 %d] 取出 %-10s（距起點 %d 步）%n",
                        step, names[node], dist[node]);

                List<String> newVisited = new ArrayList<>();
                for (int neighbor : adj.get(node)) {
                    if (dist[neighbor] == -1) {
                        dist[neighbor] = dist[node] + 1;
                        prev[neighbor] = node;
                        queue.offer(neighbor);
                        newVisited.add(names[neighbor]);
                    }
                }
                if (!newVisited.isEmpty()) {
                    System.out.println("         發現新鄰居：" + newVisited
                            + " → 距起點 " + (dist[node] + 1) + " 步");
                } else {
                    System.out.println("         無新鄰居");
                }

                // 印出目前佇列狀態
                List<String> queueState = new ArrayList<>();
                for (int n : queue) queueState.add(names[n]);
                System.out.println("         佇列：" + queueState);
                System.out.println();
            }

            // 儲存 prev 供路徑回溯
            this.prevArr = prev;
            return dist;
        }

        private int[] prevArr;

        /** 回溯從 src 到 dst 的認識路徑 */
        String pathStr(int src, int dst) {
            if (prevArr == null || prevArr[dst] == -1 && dst != src) return "無路徑";
            List<String> path = new ArrayList<>();
            for (int cur = dst; cur != -1; cur = prevArr[cur])
                path.add(names[cur]);
            Collections.reverse(path);
            return String.join(" → ", path);
        }

        void printResult(int src) {
            System.out.println("=== BFS 廣度優先搜尋 — 人際網路 (O(V+E)) ===");
            System.out.println("起點：" + names[src] + "（透過最少人數認識對方）");
            System.out.println();
            int[] dist = shortestHops(src);
            System.out.println("  ── 最終結果 ──");
            for (int i = 0; i < V; i++) {
                if (i == src) continue;
                String hops = dist[i] == -1 ? "無法認識" :
                              dist[i] == 1  ? "直接認識（0 個中間人）" :
                                              "需透過 " + (dist[i] - 1) + " 個中間人";
                String path = dist[i] == -1 ? "-" : pathStr(src, i);
                System.out.printf("  %-10s → %-10s：%-22s  路徑：%s%n",
                        names[src], names[i], hops, path);
            }
        }
    }

    // =========================================================
    // 2. Dijkstra — 貪心最短路徑
    //    情境：交通路線 — 找兩站之間的最短距離（公里）
    //    節點：A B C D E F G H I J L（對應 PDF Slide 2 右圖）
    //    目標：從 C 出發，找到抵達每個車站的最短距離
    //    時間複雜度：O((V + E) log V)
    // =========================================================
    static class Dijkstra {
        private int V;
        private String[] stations;
        private List<List<int[]>> adj;

        Dijkstra(String[] stations) {
            this.V = stations.length;
            this.stations = stations;
            adj = new ArrayList<>();
            for (int i = 0; i < V; i++) adj.add(new ArrayList<>());
        }

        void addEdge(int u, int v, int w) {
            adj.get(u).add(new int[]{v, w});
            adj.get(v).add(new int[]{u, w});
        }

        /**
         * 核心：distance[] + done[] 雙陣列協作（如 PDF Slide 5）
         * 每輪從未鎖定節點中取最小距離者鎖定，再對其鄰居進行鬆弛 (Relax)
         */
        int[] shortestPath(int src) {
            int[] distance = new int[V];
            int[] prev     = new int[V];
            boolean[] done = new boolean[V];
            Arrays.fill(distance, Integer.MAX_VALUE);
            Arrays.fill(prev, -1);
            distance[src] = 0;

            PriorityQueue<int[]> pq =
                    new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
            pq.offer(new int[]{0, src});

            System.out.println("  [過程] 初始化：" + stations[src]
                    + " 距離=0，其餘=∞");
            printDistTable(distance, done);
            System.out.println();

            int round = 0;
            while (!pq.isEmpty()) {
                int[] curr = pq.poll();
                int u = curr[1];
                if (done[u]) continue;
                done[u] = true;
                round++;

                System.out.printf("  [第 %d 輪] 鎖定站點：%s（距離 = %d）%n",
                        round, stations[u], distance[u]);

                boolean relaxed = false;
                for (int[] edge : adj.get(u)) {
                    int v = edge[0], w = edge[1];
                    if (!done[v] && distance[u] + w < distance[v]) {
                        int old = distance[v];
                        distance[v] = distance[u] + w;
                        prev[v] = u;
                        pq.offer(new int[]{distance[v], v});
                        String oldStr = old == Integer.MAX_VALUE ? "∞" : String.valueOf(old);
                        System.out.printf("         鬆弛 %s→%s：%s + %d = %d（原 %s，更新）%n",
                                stations[u], stations[v],
                                distance[u], w, distance[v], oldStr);
                        relaxed = true;
                    }
                }
                if (!relaxed) System.out.println("         無鄰居需要更新");
                printDistTable(distance, done);
                System.out.println();
            }

            this.prevArr = prev;
            return distance;
        }

        private int[] prevArr;

        void printDistTable(int[] distance, boolean[] done) {
            StringBuilder header = new StringBuilder("         距離表 |");
            StringBuilder values = new StringBuilder("                |");
            StringBuilder locked = new StringBuilder("                |");
            for (int i = 0; i < V; i++) {
                header.append(String.format(" %-4s", stations[i]));
                values.append(String.format(" %-4s",
                        distance[i] == Integer.MAX_VALUE ? "∞" : distance[i]));
                locked.append(String.format(" %-4s", done[i] ? "✓" : "·"));
            }
            System.out.println(header);
            System.out.println(values);
            System.out.println(locked + "  (✓=已鎖定)");
        }

        String pathStr(int src, int dst) {
            if (prevArr == null) return "-";
            List<String> path = new ArrayList<>();
            for (int cur = dst; cur != -1; cur = prevArr[cur])
                path.add(stations[cur]);
            Collections.reverse(path);
            return String.join(" → ", path);
        }

        void printResult(int src) {
            System.out.println("\n=== Dijkstra 演算法 — 交通路線 (O((V+E) log V)) ===");
            System.out.println("起點：" + stations[src] + " 站  ※ 僅適用非負權重（距離 ≥ 0）");
            System.out.println();
            int[] dist = shortestPath(src);
            System.out.println("  ── 最終結果 ──");
            for (int i = 0; i < V; i++) {
                String result = dist[i] == Integer.MAX_VALUE ? "不可達" : dist[i] + " 公里";
                String path   = dist[i] == Integer.MAX_VALUE ? "-" : pathStr(src, i);
                System.out.printf("  %s → %-2s：%-8s  路徑：%s%n",
                        stations[src], stations[i], result, path);
            }
        }
    }

    // =========================================================
    // 3. Bellman-Ford — 全局鬆弛，支援負權重
    //    適用：含負權重邊的有向圖；可偵測負環
    //    時間複雜度：O(V × E)
    // =========================================================
    static class BellmanFord {
        private int V;
        private List<int[]> edges;
        private String[] nodeNames = {"A", "B", "C"};

        BellmanFord(int v) {
            this.V = v;
            edges = new ArrayList<>();
        }

        void addEdge(int u, int v, int w) {
            edges.add(new int[]{u, v, w});
        }

        /**
         * Phase 1: 悲觀初始化（起點=0，其餘=∞）
         * Phase 2: 反覆掃描所有邊 V-1 輪，逐步鬆弛
         * Phase 3: 智慧提早結束（若某輪無更新即停止）
         */
        int[] shortestPath(int src) {
            int[] distance = new int[V];
            int[] prev     = new int[V];
            Arrays.fill(distance, Integer.MAX_VALUE);
            Arrays.fill(prev, -1);
            distance[src] = 0;

            System.out.println("  [Phase 1] 悲觀初始化");
            printDist(distance);

            for (int round = 1; round <= V - 1; round++) {
                boolean updated = false;
                System.out.printf("%n  [Phase 2 — 第 %d 輪] 掃描所有 %d 條邊%n", round, edges.size());
                for (int[] edge : edges) {
                    int u = edge[0], v = edge[1], w = edge[2];
                    String uName = nodeNames[u], vName = nodeNames[v];
                    if (distance[u] != Integer.MAX_VALUE
                            && distance[u] + w < distance[v]) {
                        String oldStr = distance[v] == Integer.MAX_VALUE ? "∞"
                                : String.valueOf(distance[v]);
                        distance[v] = distance[u] + w;
                        prev[v] = u;
                        updated = true;
                        System.out.printf("         鬆弛 %s→%s：%d + (%d) = %d（原 %s，更新）%n",
                                uName, vName, distance[u], w, distance[v], oldStr);
                    } else {
                        String du = distance[u] == Integer.MAX_VALUE ? "∞"
                                : String.valueOf(distance[u]);
                        System.out.printf("         檢查 %s→%s：%s + (%d)，無需更新%n",
                                uName, vName, du, w);
                    }
                }
                printDist(distance);
                if (!updated) {
                    System.out.println("  [Phase 3] 本輪無更新，提早結束！");
                    break;
                }
            }

            // 偵測負環
            System.out.println("\n  [負環偵測] 再掃描一輪...");
            for (int[] edge : edges) {
                int u = edge[0], v = edge[1], w = edge[2];
                if (distance[u] != Integer.MAX_VALUE
                        && distance[u] + w < distance[v]) {
                    System.out.println("  ⚠ 警告：圖中含負環，無最短路徑！");
                    return null;
                }
            }
            System.out.println("  無負環，結果有效。");

            this.prevArr = prev;
            return distance;
        }

        private int[] prevArr;

        void printDist(int[] distance) {
            System.out.print("         距離表：");
            for (int i = 0; i < V; i++) {
                String d = distance[i] == Integer.MAX_VALUE ? "∞" : String.valueOf(distance[i]);
                System.out.printf("%s=%-5s", nodeNames[i], d);
            }
            System.out.println();
        }

        String pathStr(int src, int dst) {
            if (prevArr == null) return "-";
            List<String> path = new ArrayList<>();
            for (int cur = dst; cur != -1; cur = prevArr[cur])
                path.add(nodeNames[cur]);
            Collections.reverse(path);
            return String.join(" → ", path);
        }

        void printResult(int src) {
            System.out.println("\n=== Bellman-Ford 演算法 (O(V×E)) ===");
            System.out.printf("起點：%s  ✓ 支援負權重邊%n", nodeNames[src]);
            System.out.println();
            int[] dist = shortestPath(src);
            if (dist == null) return;
            System.out.println("\n  ── 最終結果 ──");
            for (int i = 0; i < V; i++) {
                String result = dist[i] == Integer.MAX_VALUE ? "不可達" : String.valueOf(dist[i]);
                String path   = dist[i] == Integer.MAX_VALUE ? "-" : pathStr(src, i);
                System.out.printf("  %s → %s：%-8s  路徑：%s%n",
                        nodeNames[src], nodeNames[i], result, path);
            }
        }
    }

    // =========================================================
    // 4. 0/1 Knapsack — 動態規劃背包問題
    //    適用：給定有限容量，選物品使總價值最大
    //    時間複雜度：O(N × W)
    //    狀態轉移：dp[i][j] = max(dp[i-1][j], dp[i-1][j-w[i]] + v[i])
    // =========================================================
    static class Knapsack {

        static int solve(int[] weights, int[] values, int capacity) {
            int n = weights.length;
            int[][] dp = new int[n + 1][capacity + 1];

            System.out.println("\n=== 0/1 Knapsack 動態規劃 (O(N×W)) ===");
            System.out.println("背包容量：" + capacity);
            System.out.printf("%-8s %-8s %-8s%n", "物品", "重量", "價值");
            for (int i = 0; i < n; i++)
                System.out.printf("%-8d %-8d %-8d%n", i + 1, weights[i], values[i]);

            System.out.println();

            for (int i = 1; i <= n; i++) {
                int w = weights[i - 1];
                int v = values[i - 1];
                System.out.printf("  [過程] 考慮物品 %d（重量=%d, 價值=%d）%n", i, w, v);
                for (int j = 0; j <= capacity; j++) {
                    int notTake = dp[i - 1][j];
                    dp[i][j] = notTake;
                    if (j >= w) {
                        int take = dp[i - 1][j - w] + v;
                        if (take > notTake) {
                            dp[i][j] = take;
                            System.out.printf("         容量 %2d：裝入！dp[%d][%d]=%d（不裝=%d, 裝入=%d）%n",
                                    j, i, j, dp[i][j], notTake, take);
                        }
                    }
                }
                // 印出本輪 dp 列
                System.out.printf("         Item%d 列：", i);
                for (int j = 0; j <= capacity; j++) System.out.printf("%3d", dp[i][j]);
                System.out.println();
                System.out.println();
            }

            System.out.println("  DP 表格（行=物品，列=容量 0~" + capacity + "）：");
            System.out.print("       ");
            for (int j = 0; j <= capacity; j++) System.out.printf("%4d", j);
            System.out.println();
            for (int i = 0; i <= n; i++) {
                System.out.printf("Item%-3d", i);
                for (int j = 0; j <= capacity; j++) System.out.printf("%4d", dp[i][j]);
                System.out.println();
            }

            // 回溯
            System.out.print("\n  [回溯] 選擇路徑：");
            int j = capacity;
            List<Integer> chosen = new ArrayList<>();
            for (int i = n; i >= 1; i--) {
                if (dp[i][j] != dp[i - 1][j]) {
                    System.out.printf("%n         容量 %d：選了物品 %d（重量=%d, 價值=%d）→ 剩餘容量 %d",
                            j, i, weights[i-1], values[i-1], j - weights[i-1]);
                    chosen.add(i);
                    j -= weights[i - 1];
                }
            }
            Collections.reverse(chosen);
            System.out.println();
            System.out.println("  已選物品：" + chosen);
            System.out.println("  最大總價值：" + dp[n][capacity]);
            return dp[n][capacity];
        }
    }

    // =========================================================
    // 計時輔助
    // =========================================================
    static String elapsed(long startNs) {
        double sec = (System.nanoTime() - startNs) / 1_000_000_000.0;
        return String.format("執行時間：%.9f 秒", sec);
    }

    // =========================================================
    // Main
    // =========================================================
    public static void main(String[] args) {
        long t;

        // --- 1. BFS — 人際網路 ---
        t = System.nanoTime();
        String[] people = {
            "Sunny", "Amy", "James", "Marshall", "Cara",
            "John",  "Uriah", "Yummy", "Andy", "Bella", "Eric", "May"
        };
        BFS bfs = new BFS(people);
        bfs.addEdge(0, 1);  // Sunny   - Amy
        bfs.addEdge(0, 3);  // Sunny   - Marshall
        bfs.addEdge(1, 3);  // Amy     - Marshall
        bfs.addEdge(2, 3);  // James   - Marshall
        bfs.addEdge(3, 5);  // Marshall- John
        bfs.addEdge(3, 6);  // Marshall- Uriah
        bfs.addEdge(4, 5);  // Cara    - John
        bfs.addEdge(5, 9);  // John    - Bella
        bfs.addEdge(5, 10); // John    - Eric
        bfs.addEdge(6, 8);  // Uriah   - Andy
        bfs.addEdge(8, 11); // Andy    - May
        bfs.printResult(3); // 從 Marshall 出發
        System.out.println("  " + elapsed(t));

        // --- 2. Dijkstra — 交通路線 ---
        t = System.nanoTime();
        String[] stations = {"A","B","C","D","E","F","G","H","I","J","L"};
        Dijkstra dijkstra = new Dijkstra(stations);
        dijkstra.addEdge(0, 1, 5);  // A - B : 5
        dijkstra.addEdge(1, 3, 6);  // B - D : 6
        dijkstra.addEdge(2, 3, 4);  // C - D : 4
        dijkstra.addEdge(3, 4, 3);  // D - E : 3
        dijkstra.addEdge(3, 5, 2);  // D - F : 2
        dijkstra.addEdge(4, 5, 5);  // E - F : 5
        dijkstra.addEdge(4, 9, 7);  // E - J : 7
        dijkstra.addEdge(5, 6, 4);  // F - G : 4
        dijkstra.addEdge(6, 8, 10); // G - I : 10
        dijkstra.addEdge(7, 3, 9);  // H - D : 9
        dijkstra.addEdge(7, 9, 10); // H - J : 10
        dijkstra.addEdge(8, 9, 1);  // I - J : 1
        dijkstra.addEdge(8, 10, 6); // I - L : 6
        dijkstra.addEdge(9, 10, 3); // J - L : 3
        dijkstra.printResult(2);    // 從 C 站出發
        System.out.println("  " + elapsed(t));

        // --- 3. Bellman-Ford — 負權重反例 ---
        t = System.nanoTime();
        BellmanFord bf = new BellmanFord(3);
        bf.addEdge(0, 1,  5);  // A→B =  5
        bf.addEdge(0, 2, 10);  // A→C = 10
        bf.addEdge(2, 1, -50); // C→B = -50
        bf.printResult(0);
        System.out.println("  " + elapsed(t));

        // --- 4. 0/1 Knapsack ---
        t = System.nanoTime();
        int[] weights = {2, 3, 4, 5};
        int[] values  = {3, 4, 5, 6};
        Knapsack.solve(weights, values, 8);
        System.out.println("  " + elapsed(t));
    }
}