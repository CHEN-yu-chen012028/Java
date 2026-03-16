import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class game2 {
    // 1. 類別與封裝 (Classes & Encapsulation)
    static class Player {
        private int id;
        private String role;
        private String camp; // Good, Bad, Neutral
        private boolean alive;
        private boolean hasHeal = true; // 女巫專用
        private boolean hasPoison = true; // 女巫專用

        public Player(int id, String role, String camp) {
            this.id = id;
            this.role = role;
            this.camp = camp;
            this.alive = true;
        }

        // Getters
        public int getId() { return id; }
        public String getRole() { return role; }
        public String getCamp() { return camp; }
        public boolean isAlive() { return alive; }
        
        // Night Abilities (夜晚技能)
        public void kill() { this.alive = false; }
        
        public String getStatus() {
            return "Player " + id + (alive ? " [Alive]" : " [Dead]");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        // 2. 可配置角色數量 (Configurable role counts)
        System.out.println("=== Advanced Werewolf System ===");
        System.out.print("Enter number of Villagers: ");
        int vCount = sc.nextInt();
        System.out.print("Enter number of Werewolves: ");
        int wCount = sc.nextInt();
        System.out.print("Include Seer? (1 for Yes, 0 for No): ");
        int hasSeer = sc.nextInt();
        System.out.print("Include Witch? (1 for Yes, 0 for No): ");
        int hasWitch = sc.nextInt();
        System.out.print("Include Hunter? (1 for Yes, 0 for No): ");
        int hasHunter = sc.nextInt();

        List<Player> rolePool = new ArrayList<>();
        int total = vCount + wCount + hasSeer + hasWitch + hasHunter;
        
        // 分配陣營與角色
        for (int i = 0; i < vCount; i++) rolePool.add(new Player(0, "Villager", "Good"));
        for (int i = 0; i < wCount; i++) rolePool.add(new Player(0, "Werewolf", "Bad"));
        if (hasSeer == 1) rolePool.add(new Player(0, "Seer", "Good"));
        if (hasWitch == 1) rolePool.add(new Player(0, "Witch", "Good"));
        if (hasHunter == 1) rolePool.add(new Player(0, "Hunter", "Good"));

        Collections.shuffle(rolePool); // 洗牌隨機分配
        Player[] players = new Player[total];
        for (int i = 0; i < total; i++) {
            players[i] = rolePool.get(i);
            players[i].id = i + 1; // 重新編號
        }

        boolean gameOver = false;
        while (!gameOver) {
            // --- 夜晚階段 (Night Abilities) ---
            System.out.println("\n--- Night Phase ---");
            int victimId = -1;

            // 狼人殺人
            System.out.println("Werewolves, choose someone to kill:");
            printAlive(players);
            victimId = sc.nextInt();
            
            // 預言家驗人
            int seerIdx = findAliveRole(players, "Seer");
            if (seerIdx != -1) {
                System.out.println("Seer, choose someone to check:");
                int checkId = sc.nextInt();
                System.out.println("Role is: " + players[checkId-1].getCamp());
            }

            // 女巫救人/毒人
            int witchIdx = findAliveRole(players, "Witch");
            if (witchIdx != -1 && (players[witchIdx].hasHeal || players[witchIdx].hasPoison)) {
                System.out.println("Witch, Player " + victimId + " is dead. Use heal? (1:Yes/0:No)");
                if (sc.nextInt() == 1 && players[witchIdx].hasHeal) {
                    victimId = -1;
                    players[witchIdx].hasHeal = false;
                }
            }

            // 結算夜晚死亡
            if (victimId != -1) {
                players[victimId - 1].kill();
                System.out.println("Morning: Player " + victimId + " died.");
                // 獵人發動技能 (簡化版)
                if (players[victimId-1].getRole().equals("Hunter")) {
                    System.out.println("Hunter died! Choose someone to shoot:");
                    int shootId = sc.nextInt();
                    players[shootId-1].kill();
                }
            } else {
                System.out.println("Morning: It was a peaceful night.");
            }

            if (checkWin(players)) break;

            // --- 白天投票 (Day Voting) ---
            System.out.println("\n--- Day Voting Phase ---");
            printAlive(players);
            int[] votes = new int[total + 1];
            for (int i = 0; i < total; i++) {
                if (players[i].isAlive()) {
                    System.out.print("Player " + (i + 1) + " vote for (ID): ");
                    int v = sc.nextInt();
                    votes[v]++;
                }
            }
            // 找出最高票
            int maxVoteId = 1;
            for (int i = 2; i <= total; i++) {
                if (votes[i] > votes[maxVoteId]) maxVoteId = i;
            }
            System.out.println("Player " + maxVoteId + " is executed.");
            players[maxVoteId - 1].kill();

            gameOver = checkWin(players);
        }
    }

    // --- 輔助方法 ---
    public static int findAliveRole(Player[] players, String role) {
        for (int i = 0; i < players.length; i++) {
            if (players[i].isAlive() && players[i].getRole().equals(role)) return i;
        }
        return -1;
    }

    public static void printAlive(Player[] players) {
        for (Player p : players) if (p.isAlive()) System.out.println(p.getStatus());
    }

    // 5. 勝負判定 (Win condition detection)
    public static boolean checkWin(Player[] players) {
        int badCount = 0;
        int goodCount = 0;
        for (Player p : players) {
            if (p.isAlive()) {
                if (p.getCamp().equals("Bad")) badCount++;
                else goodCount++;
            }
        }
        if (badCount == 0) {
            System.out.println("GOOD CAMP WINS!");
            return true;
        }
        if (badCount >= goodCount) {
            System.out.println("BAD CAMP WINS!");
            return true;
        }
        return false;
    }
}
