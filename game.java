import java.util.Random;
import java.util.Scanner;

public class game {
    static class Player {
        private int id;
        private String role;
        private boolean alive;

        // 修正建構子，接收 id 與 role
        public Player(int id, String role) {
            this.id = id;
            this.role = role;
            this.alive = true;
        }

        public int getId() { return id; }
        public String getRole() { return role; }
        public boolean isAlive() { return alive; }
        
        public void kill() {
            this.alive = false;
        }

        public String getPublicInfo() {
            if (alive) {
                return "Player " + id + " [Alive]";
            } else {
                return "Player " + id + " [Dead]";
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        System.out.println("=== Wolf Game ===");
        System.out.println("Enter number of players (4-10):");
        int n = sc.nextInt();
        sc.nextLine();

        while (n < 4 || n > 10) {
            System.out.println("Invalid number. Try again:");
            n = sc.nextInt();
            sc.nextLine();
        }

        Player[] players = new Player[n];
        int wolfIndex = rand.nextInt(n);

        // 初始化玩家
        for (int i = 0; i < n; i++) {
            if (i == wolfIndex) {
                players[i] = new Player(i + 1, "Werewolf");
            } else {
                players[i] = new Player(i + 1, "Villager");
            }
        }

        System.out.println("\nRole assignment start.");
        System.out.println("Each player takes turns to see their role.");

        for (int i = 0; i < n; i++) {
            System.out.println("\nPlayer " + (i + 1) + ", please press Enter to see your role.");
            sc.nextLine();
            System.out.println("Your Role : " + players[i].getRole());
            System.out.println("Memorize your role, then press Enter to clear screen.");
            sc.nextLine();
            // 簡易清空螢幕
            for (int line = 0; line < 30; line++) System.out.println();
        }

        boolean gameOver = false;
        int round = 1;

        while (!gameOver) {
            System.out.println("\n--- Round " + round + " ---");
            System.out.println("Night falls. Werewolf wakes up.");

            int wolfIdx = findAliveWolf(players);
            if (wolfIdx != -1) {
                System.out.println("Werewolf, choose a player to kill:");
                printAlivePlayers(players);
                
                int targetId;
                while (true) {
                    targetId = sc.nextInt();
                    if (targetId > 0 && targetId <= n && players[targetId - 1].isAlive() && targetId != (wolfIdx + 1)) {
                        break;
                    }
                    System.out.println("Invalid target. Choose an alive villager:");
                }
                players[targetId - 1].kill();
                System.out.println("\nMorning comes... Player " + targetId + " has been killed.");
            }

            // 檢查殺完人後狼人是否獲勝
            if (checkGameOver(players)) break;

            // 白天投票環節
            System.out.println("\nDaytime. Discussion and Voting.");
            printAlivePlayers(players);
            System.out.println("Enter the Player ID to vote out:");
            int voteId;
            while (true) {
                voteId = sc.nextInt();
                if (voteId > 0 && voteId <= n && players[voteId - 1].isAlive()) {
                    break;
                }
                System.out.println("Invalid ID. Vote again:");
            }

            players[voteId - 1].kill();
            System.out.println("Player " + voteId + " was voted out. They were a " + players[voteId - 1].getRole());

            if (checkGameOver(players)) break;

            round++;
        }
        System.out.println("Game Over!");
    }

    // 輔助方法：找活著的狼人索引
    public static int findAliveWolf(Player[] players) {
        for (int i = 0; i < players.length; i++) {
            if (players[i].isAlive() && players[i].getRole().equals("Werewolf")) {
                return i;
            }
        }
        return -1;
    }

    // 輔助方法：列出活著的玩家
    public static void printAlivePlayers(Player[] players) {
        for (Player p : players) {
            if (p.isAlive()) {
                System.out.println(p.getPublicInfo());
            }
        }
    }

    // 輔助方法：檢查勝負
    public static boolean checkGameOver(Player[] players) {
        int wolfCount = 0;
        int villagerCount = 0;

        for (Player p : players) {
            if (p.isAlive()) {
                if (p.getRole().equals("Werewolf")) wolfCount++;
                else villagerCount++;
            }
        }

        if (wolfCount == 0) {
            System.out.println("Villagers win! All werewolves are gone.");
            return true;
        }
        if (wolfCount >= villagerCount) {
            System.out.println("Werewolves win! They have overrun the village.");
            return true;
        }
        return false;
    }
}
