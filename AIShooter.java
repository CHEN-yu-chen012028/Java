import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

// ==================== ENTRY POINT ====================
public class AIShooter {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameFrame());
    }
}

// ==================== GAME FRAME ====================
class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("AI Shooter Challenge");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        GamePanel panel = new GamePanel();
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        panel.startGameLoop();
    }
}

// ==================== NODE (BFS) ====================
class Node {
    int col, row;
    Node parent;

    Node(int col, int row, Node parent) {
        this.col = col;
        this.row = row;
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Node n)) return false;
        return col == n.col && row == n.row;
    }

    @Override
    public int hashCode() {
        return col * 1000 + row;
    }
}

// ==================== MAP ====================
class Map {
    static final int TILE = 40;
    static final int COLS = 20;
    static final int ROWS = 16;
    static final int W = COLS * TILE;
    static final int H = ROWS * TILE;

    private final boolean[][] grid = new boolean[ROWS][COLS];

    Map() {
        Random r = new Random(99);
        for (int row = 3; row < ROWS - 3; row++)
            for (int col = 0; col < COLS; col++)
                if (r.nextDouble() < 0.10) grid[row][col] = true;
        // Keep player spawn clear
        for (int col = COLS/2 - 2; col <= COLS/2 + 2; col++)
            grid[ROWS - 2][col] = false;
    }

    boolean isWall(int col, int row) {
        if (col < 0 || col >= COLS || row < 0 || row >= ROWS) return true;
        return grid[row][col];
    }

    void draw(Graphics g) {
        g.setColor(new Color(5, 5, 20));
        g.fillRect(0, 0, W, H);

        // Stars
        Random r = new Random(7);
        g.setColor(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            int x = r.nextInt(W), y = r.nextInt(H), s = r.nextInt(2) + 1;
            g.fillOval(x, y, s, s);
        }

        // Grid
        g.setColor(new Color(20, 40, 60, 60));
        for (int c = 0; c <= COLS; c++) g.drawLine(c * TILE, 0, c * TILE, H);
        for (int row = 0; row <= ROWS; row++) g.drawLine(0, row * TILE, W, row * TILE);

        // Obstacles
        for (int row = 0; row < ROWS; row++)
            for (int col = 0; col < COLS; col++)
                if (grid[row][col]) drawAsteroid(g, col * TILE, row * TILE);
    }

    private void drawAsteroid(Graphics g, int x, int y) {
        g.setColor(new Color(70, 70, 80));
        g.fillOval(x + 4, y + 4, TILE - 8, TILE - 8);
        g.setColor(new Color(50, 50, 60));
        g.fillOval(x + 10, y + 10, TILE - 20, TILE - 20);
        g.setColor(new Color(100, 100, 110));
        g.drawOval(x + 4, y + 4, TILE - 8, TILE - 8);
    }
}

// ==================== BFS PATHFINDER ====================
class BFSPathfinder {
    private final Map map;
    private static final int[] DC = {0, 0, -1, 1};
    private static final int[] DR = {-1, 1, 0, 0};

    BFSPathfinder(Map map) { this.map = map; }

    // Returns the next Node to step toward the target, or null
    Node findNextStep(int sc, int sr, int tc, int tr) {
        if (sc == tc && sr == tr) return null;

        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        Node start = new Node(sc, sr, null);
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            for (int i = 0; i < 4; i++) {
                int nc = cur.col + DC[i], nr = cur.row + DR[i];
                if (map.isWall(nc, nr)) continue;
                Node next = new Node(nc, nr, cur);
                if (visited.contains(next)) continue;
                if (nc == tc && nr == tr) return firstStep(next);
                visited.add(next);
                queue.add(next);
            }
        }
        return null;
    }

    private Node firstStep(Node n) {
        while (n.parent != null && n.parent.parent != null) n = n.parent;
        return n;
    }
}

// ==================== BULLET ====================
class Bullet {
    float x, y;
    final float speed;
    final boolean fromPlayer;
    boolean active = true;

    Bullet(float x, float y, float speed, boolean fromPlayer) {
        this.x = x; this.y = y; this.speed = speed; this.fromPlayer = fromPlayer;
    }

    void update() { y += speed; }
    boolean outOfBounds() { return y < -20 || y > Map.H + 20; }
    Rectangle bounds() { return new Rectangle((int)x - 3, (int)y - 8, 6, 16); }

    void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (fromPlayer) {
            g2.setColor(new Color(100, 200, 255, 80));
            g2.fillOval((int)x - 5, (int)y - 14, 10, 22);
            g2.setColor(new Color(160, 230, 255));
            g2.fillRect((int)x - 2, (int)y - 10, 4, 16);
            g2.setColor(Color.WHITE);
            g2.fillRect((int)x - 1, (int)y - 8, 2, 10);
        } else {
            g2.setColor(new Color(255, 60, 60, 80));
            g2.fillOval((int)x - 5, (int)y - 4, 10, 22);
            g2.setColor(new Color(255, 100, 50));
            g2.fillRect((int)x - 2, (int)y, 4, 14);
            g2.setColor(new Color(255, 220, 100));
            g2.fillRect((int)x - 1, (int)y + 2, 2, 8);
        }
    }
}

// ==================== PLAYER ====================
class Player {
    static final int MAX_HP = 5;
    int col, row, hp = MAX_HP;
    private int invFrames = 0, moveDelay = 0;
    private final Map map;
    private final Set<Integer> keys = new HashSet<>();

    Player(Map map) {
        this.map = map;
        col = Map.COLS / 2;
        row = Map.ROWS - 2;
    }

    void keyPressed(int k)  { keys.add(k); }
    void keyReleased(int k) { keys.remove(k); }
    boolean isKeyDown(int k){ return keys.contains(k); }

    void update() {
        if (invFrames > 0) invFrames--;
        if (--moveDelay > 0) return;

        int dc = 0, dr = 0;
        if (keys.contains(KeyEvent.VK_LEFT)  || keys.contains(KeyEvent.VK_A)) dc = -1;
        else if (keys.contains(KeyEvent.VK_RIGHT) || keys.contains(KeyEvent.VK_D)) dc =  1;
        if (keys.contains(KeyEvent.VK_UP)    || keys.contains(KeyEvent.VK_W)) dr = -1;
        else if (keys.contains(KeyEvent.VK_DOWN)  || keys.contains(KeyEvent.VK_S)) dr =  1;

        if ((dc != 0 || dr != 0) && !map.isWall(col + dc, row + dr)) {
            col += dc; row += dr;
        }
        moveDelay = 8;
    }

    void hit() {
        if (invFrames > 0) return;
        hp--;
        invFrames = 60;
    }

    boolean alive()      { return hp > 0; }
    boolean invincible() { return invFrames > 0; }
    int px() { return col * Map.TILE; }
    int py() { return row * Map.TILE; }
    Rectangle bounds() { return new Rectangle(px() + 5, py() + 5, Map.TILE - 10, Map.TILE - 10); }

    void draw(Graphics g) {
        if (invFrames > 0 && (invFrames / 5) % 2 == 0) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int x = px(), y = py(), t = Map.TILE;

        // Engine glow
        g2.setColor(new Color(255, 140, 0, 180));
        g2.fillOval(x + t/2 - 5, y + t - 8, 10, 12);

        // Body
        int[] bx = {x+t/2, x+t/2-10, x+t/2-8, x+t/2, x+t/2+8, x+t/2+10};
        int[] by = {y+4,   y+t-8,    y+t-4,   y+t-4, y+t-4,   y+t-8};
        g2.setColor(new Color(80, 150, 255));
        g2.fillPolygon(bx, by, 6);

        // Wings
        g2.setColor(new Color(50, 90, 200));
        g2.fillPolygon(new int[]{x+t/2-8, x+4,    x+t/2-6}, new int[]{y+t/2, y+t-6, y+t-4}, 3);
        g2.fillPolygon(new int[]{x+t/2+8, x+t-4,  x+t/2+6}, new int[]{y+t/2, y+t-6, y+t-4}, 3);

        // Cockpit
        g2.setColor(new Color(200, 240, 255));
        g2.fillOval(x + t/2 - 4, y + 8, 8, 10);
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillOval(x + t/2 - 2, y + 10, 3, 5);
    }
}

// ==================== ENEMY ====================
class Enemy {
    int col, row;
    boolean active = true;
    private final Map map;
    private final BFSPathfinder bfs;
    private int moveDelay, shootDelay, moveSpeed, shootCooldown;
    private final int colorType;
    private static final Random R = new Random();

    Enemy(int col, int row, Map map, int level) {
        this.col = col; this.row = row;
        this.map = map;
        this.bfs = new BFSPathfinder(map);
        this.moveSpeed    = Math.max(10, 30 - level * 3);
        this.shootCooldown = Math.max(40, 120 - level * 10);
        this.shootDelay   = R.nextInt(shootCooldown);
        this.colorType    = R.nextInt(3);
    }

    void update(Player player, List<Bullet> bullets) {
        // BFS movement
        if (--moveDelay <= 0) {
            Node next = bfs.findNextStep(col, row, player.col, player.row);
            if (next != null) { col = next.col; row = next.row; }
            moveDelay = moveSpeed;
        }
        // Shoot downward
        if (--shootDelay <= 0) {
            bullets.add(new Bullet(px() + Map.TILE / 2f, py() + Map.TILE, 5, false));
            shootDelay = shootCooldown;
        }
    }

    int px() { return col * Map.TILE; }
    int py() { return row * Map.TILE; }
    Rectangle bounds() { return new Rectangle(px() + 5, py() + 5, Map.TILE - 10, Map.TILE - 10); }

    void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int x = px(), y = py(), t = Map.TILE;

        Color main, wing, glow;
        switch (colorType) {
            case 0 -> { main = new Color(220, 60, 60);  wing = new Color(160,20,20);  glow = new Color(255,100,0,160); }
            case 1 -> { main = new Color(180, 60, 220); wing = new Color(120,20,160); glow = new Color(200,80,255,160); }
            default-> { main = new Color(60, 200, 100); wing = new Color(20,130,40);  glow = new Color(80,255,100,160); }
        }

        // Engine glow (top, enemy faces down)
        g2.setColor(glow);
        g2.fillOval(x + t/2 - 5, y + 2, 10, 12);

        // Body (inverted — nose points down)
        int[] bx = {x+t/2, x+t/2-10, x+t/2-8, x+t/2, x+t/2+8, x+t/2+10};
        int[] by = {y+t-4, y+8,       y+4,      y+4,   y+4,      y+8};
        g2.setColor(main);
        g2.fillPolygon(bx, by, 6);

        // Wings
        g2.setColor(wing);
        g2.fillPolygon(new int[]{x+t/2-8, x+4,   x+t/2-6}, new int[]{y+t/2, y+6, y+4}, 3);
        g2.fillPolygon(new int[]{x+t/2+8, x+t-4, x+t/2+6}, new int[]{y+t/2, y+6, y+4}, 3);

        // Cockpit
        g2.setColor(new Color(255, 200, 200));
        g2.fillOval(x + t/2 - 4, y + t - 16, 8, 10);
    }
}

// ==================== SCOREBOARD ====================
class ScoreBoard {
    int score = 0, level = 1;
    private int surviveTimer = 0;

    void enemyKilled()  { score += 100 * level; }
    void levelUp(int totalKills) { level = 1 + totalKills / 5; }
    void update() {
        if (++surviveTimer % 300 == 0) score += 10; // survival bonus
    }

    void draw(Graphics g, int panelX, int panelW, int panelH, Player player) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(new Color(8, 15, 30));
        g2.fillRect(panelX, 0, panelW, panelH);
        g2.setColor(new Color(40, 100, 160));
        g2.drawRect(panelX, 0, panelW - 1, panelH - 1);

        int x = panelX + 15, y = 30;

        // Score
        label(g2, "SCORE", x, y); y += 5;
        value(g2, String.valueOf(score), x, y + 20); y += 50;
        divider(g2, panelX, panelW, y); y += 20;

        // HP
        label(g2, "HP", x, y); y += 20;
        for (int i = 0; i < Player.MAX_HP; i++) {
            g2.setColor(i < player.hp ? new Color(255, 60, 60) : new Color(60, 60, 80));
            drawHeart(g2, x + i * 22, y);
        }
        y += 35;
        divider(g2, panelX, panelW, y); y += 20;

        // Level
        label(g2, "LEVEL", x, y); y += 18;
        g2.setColor(new Color(20, 50, 80));
        g2.fillRoundRect(x, y, panelW - 30, 10, 6, 6);
        g2.setColor(new Color(60, 140, 255));
        g2.fillRoundRect(x, y, (int)((panelW - 30) * Math.min(level / 10.0, 1.0)), 10, 6, 6);
        y += 20;
        value(g2, String.valueOf(level), x, y + 22); y += 50;
        divider(g2, panelX, panelW, y); y += 20;

        // Controls
        label(g2, "CONTROLS", x, y); y += 18;
        String[][] ctrl = {{"WASD/←→↑↓","Move"},{"SPACE","Shoot"},{"R","Restart"},{"P","Pause"}};
        for (String[] c : ctrl) {
            g2.setColor(new Color(255, 200, 60));
            g2.setFont(new Font("Monospaced", Font.BOLD, 11));
            g2.drawString(c[0], x, y);
            g2.setColor(new Color(170, 200, 220));
            g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g2.drawString(c[1], x, y + 13);
            y += 30;
        }
    }

    private void label(Graphics2D g, String s, int x, int y) {
        g.setColor(new Color(100, 180, 255));
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.drawString(s, x, y);
    }
    private void value(Graphics2D g, String s, int x, int y) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString(s, x, y);
    }
    private void divider(Graphics2D g, int px, int pw, int y) {
        g.setColor(new Color(40, 80, 120));
        g.drawLine(px + 10, y, px + pw - 10, y);
    }
    private void drawHeart(Graphics2D g, int x, int y) {
        g.fillOval(x, y, 8, 8);
        g.fillOval(x + 5, y, 8, 8);
        g.fillPolygon(new int[]{x, x + 13, x + 6}, new int[]{y + 5, y + 5, y + 14}, 3);
    }
}

// ==================== COLLISION MANAGER ====================
class CollisionManager {
    // Returns number of enemies killed
    int resolve(Player player, List<Enemy> enemies, List<Bullet> bullets) {
        int kills = 0;

        for (Bullet b : bullets) {
            if (!b.active) continue;
            if (b.fromPlayer) {
                for (Enemy e : enemies) {
                    if (!e.active) continue;
                    if (b.bounds().intersects(e.bounds())) {
                        b.active = false; e.active = false; kills++;
                    }
                }
            } else {
                if (!player.invincible() && b.bounds().intersects(player.bounds())) {
                    player.hit(); b.active = false;
                }
            }
        }

        // Enemy body touches player
        for (Enemy e : enemies) {
            if (!e.active) continue;
            if (e.bounds().intersects(player.bounds())) {
                player.hit(); e.active = false; kills++;
            }
        }

        bullets.removeIf(b -> !b.active || b.outOfBounds());
        enemies.removeIf(e -> !e.active);
        return kills;
    }
}

// ==================== GAME PANEL ====================
class GamePanel extends JPanel implements KeyListener {
    static final int SIDE_W = 160;
    static final int FPS = 60;

    private Map map;
    private Player player;
    private List<Enemy> enemies;
    private List<Bullet> bullets;
    private ScoreBoard scoreboard;
    private CollisionManager collision;

    private boolean gameOver = false, paused = false;
    private int shootCooldown = 0, spawnTimer = 0, totalKills = 0;
    private javax.swing.Timer loop;

    GamePanel() {
        setPreferredSize(new Dimension(Map.W + SIDE_W, Map.H));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        init();
    }

    private void init() {
        map        = new Map();
        player     = new Player(map);
        enemies    = new ArrayList<>();
        bullets    = new ArrayList<>();
        scoreboard = new ScoreBoard();
        collision  = new CollisionManager();
        gameOver   = false;
        paused     = false;
        totalKills = 0;
        spawnWave(3);
    }

    private void spawnWave(int count) {
        Random r = new Random();
        int spawned = 0, tries = 0;
        while (spawned < count && tries++ < 200) {
            int c = r.nextInt(Map.COLS), row = r.nextInt(3);
            if (!map.isWall(c, row)) {
                enemies.add(new Enemy(c, row, map, scoreboard.level));
                spawned++;
            }
        }
    }

    void startGameLoop() {
        loop = new javax.swing.Timer(1000 / FPS, evt -> { if (!paused && !gameOver) update(); repaint(); });
        loop.start();
    }

    private void update() {
        player.update();

        // Player shooting
        if (--shootCooldown < 0) shootCooldown = 0;
        if (player.isKeyDown(KeyEvent.VK_SPACE) && shootCooldown == 0) {
            bullets.add(new Bullet(player.px() + Map.TILE / 2f, player.py(), -9, true));
            shootCooldown = 12;
        }

        bullets.forEach(Bullet::update);
        enemies.forEach(e -> e.update(player, bullets));

        int kills = collision.resolve(player, enemies, bullets);
        totalKills += kills;
        for (int i = 0; i < kills; i++) scoreboard.enemyKilled();

        scoreboard.update();
        scoreboard.levelUp(totalKills);

        // Spawn more enemies
        int interval = Math.max(100, 300 - scoreboard.level * 20);
        if (++spawnTimer >= interval) {
            if (enemies.size() < 3 + scoreboard.level) spawnWave(1);
            spawnTimer = 0;
        }

        if (!player.alive()) gameOver = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        map.draw(g);
        bullets.forEach(b -> b.draw(g));
        enemies.forEach(e -> e.draw(g));
        player.draw(g);
        scoreboard.draw(g, Map.W, SIDE_W, Map.H, player);
        if (paused)   drawPause(g);
        if (gameOver) drawGameOver(g);
    }

    private void drawPause(Graphics g) {
        g.setColor(new Color(0,0,0,150));
        g.fillRect(0, 0, Map.W, Map.H);
        drawCentered(g, "PAUSED", Map.W/2, Map.H/2, 36, new Color(255,220,60));
        drawCentered(g, "Press P to resume", Map.W/2, Map.H/2+40, 16, new Color(150,200,255));
    }

    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0,0,0,180));
        g.fillRect(0, 0, Map.W, Map.H);
        drawCentered(g, "GAME OVER",              Map.W/2, Map.H/2-40, 42, new Color(255,60,60));
        drawCentered(g, "Score: " + scoreboard.score, Map.W/2, Map.H/2+5,  22, Color.WHITE);
        drawCentered(g, "Kills: " + totalKills,   Map.W/2, Map.H/2+35, 16, new Color(150,200,255));
        drawCentered(g, "Press R to Restart",     Map.W/2, Map.H/2+70, 18, new Color(255,200,60));
    }

    private void drawCentered(Graphics g, String s, int cx, int cy, int size, Color c) {
        g.setFont(new Font("Monospaced", Font.BOLD, size));
        g.setColor(c);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, cx - fm.stringWidth(s)/2, cy);
    }

    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R) { init(); return; }
        if (e.getKeyCode() == KeyEvent.VK_P) { paused = !paused; return; }
        if (!gameOver) player.keyPressed(e.getKeyCode());
    }
    @Override public void keyReleased(KeyEvent e) { player.keyReleased(e.getKeyCode()); }
    @Override public void keyTyped(KeyEvent e) {}
}