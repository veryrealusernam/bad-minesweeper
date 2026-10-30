import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Minesweeper {
    private static int n;
    private static int m;
    private static String[][] mineFieldData;
    private static boolean[][] revealed, flagged;
    private static boolean firstClick = true;
    private static boolean gameEnded = false;
    static int revealedCount = 0;
    static int totalSafeCells;
    static JFrame window;
    public static void main(String[] args) throws InterruptedException {
        while (true) {

            firstClick = true;
            String rowInput = JOptionPane.showInputDialog("How many rows do you want your minefield to be?");
            if (rowInput == null) {
                System.exit(0);
            }
            String colInput = JOptionPane.showInputDialog("How many columns do you want your minefield to be?");
            if (colInput == null) {
                System.exit(0);
            }
            try {
                n = Integer.parseInt(rowInput);
                m = Integer.parseInt(colInput);
            }
            catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "no");
                return;
            }

            if (n > 0 && m > 0) {
                if (n * m >= 10000) {
                    int safetyCheck = JOptionPane.showConfirmDialog(null, "This is a very big minefield, and may cause extreme lag to some devices. Do you wish to continue?", "Warning", JOptionPane.YES_NO_OPTION);
                    if (safetyCheck == JOptionPane.NO_OPTION) {
                        JOptionPane.showMessageDialog(null, "Good choice :)");
                        System.exit(0);
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "ok...");
                    }
                }
                mineFieldData = new String[n][m];
                revealed = new boolean[n][m];
                flagged = new boolean[n][m];
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < m; j++)
                        mineFieldData[i][j] = "?";
                String[][] original = new String[n][m];

                for (int i = 0; i < n; i++) {
                    if (m >= 0) System.arraycopy(mineFieldData[i], 0, original[i], 0, m);
                }

                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < m; j++) {
                        if (original[i][j].equals(".")) {
                            int minesAroundSquare = 0;
                            for (int checkDirectionX = -1; checkDirectionX <= 1; checkDirectionX++) {
                                for (int checkDirectionY = -1; checkDirectionY <= 1; checkDirectionY++) {
                                    if (checkDirectionX == 0 && checkDirectionY == 0) {
                                        continue;
                                    }
                                    int idkWhatToNameThis = i + checkDirectionX;
                                    int idkWhatToNameThis2 = j + checkDirectionY;
                                    if (idkWhatToNameThis >= 0 && idkWhatToNameThis < n && idkWhatToNameThis2 >= 0 && idkWhatToNameThis2 < m) {
                                        if (original[idkWhatToNameThis][idkWhatToNameThis2].equals("*")) {
                                            minesAroundSquare++;
                                        }
                                    }
                                }
                            }
                            mineFieldData[i][j] = Integer.toString(minesAroundSquare);
                        }
                    }
                }

                int mineCount = 0;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < m; j++) {
                        if (original[i][j].equals("*")) {
                            mineCount++;
                        }
                    }
                }
                totalSafeCells = n * m - mineCount;
                revealedCount = 0;

                window = new JFrame("Minesweeper Field");
                particleStuff particles = new particleStuff();
                window.setGlassPane(particles);
                window.getGlassPane().setVisible(true);
                window.setLayout(new GridLayout(n, m));
                window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                AtomicBoolean windowClosed = new AtomicBoolean(false);
                window.addWindowListener(new WindowAdapter() {
                    public void windowClosed(WindowEvent e) {
                        windowClosed.set(true);
                    }
                });
                Container container = window.getContentPane();
                /*Image cursorImage = new ImageIcon("shovel.png").getImage();
                Point hotSpot = new Point (0,0);
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Cursor customCursor = toolkit.createCustomCursor(cursorImage, hotSpot, "shovel.png");
                window.setCursor(customCursor);
                window.setVisible(true);*/
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < m; j++) {
                        JButton tile = new JButton();
                        tile.setName(i + " " + j);
                        if ((i + j) % 2 == 0) tile.setBackground(new Color(0,255,0));
                        else tile.setBackground(new Color(0, 240, 0));
                        tile.setPreferredSize(new Dimension(60, 60));
                        tile.setBorderPainted(false);
                        int finalJ = j;
                        int finalI = i;
                        tile.addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent e) {
                                if (!gameEnded) {
                                    JButton clickedButton = (JButton) e.getSource();
                                    String[] parts = clickedButton.getName().split(" ");
                                    if (parts.length != 2) return;
                                    int x = Integer.parseInt(parts[0]), y = Integer.parseInt(parts[1]);
                                    if (SwingUtilities.isRightMouseButton(e)) {
                                        Point clickPoint = clickedButton.getLocationOnScreen();
                                        SwingUtilities.convertPointFromScreen(clickPoint, window.getContentPane());
                                        if (!revealed[x][y]) {
                                            flagged[x][y] = !flagged[x][y];
                                            if (flagged[x][y]) {
                                                particles.spawnExplosion(clickPoint.x + 30, clickPoint.y + 30, new Color(190, 190, 0), 20);
                                                clickedButton.setIcon(new flag(26, new Color(220, 0, 0), new Color(100, 50, 0)));
                                            } else {
                                                particles.spawnExplosion(clickPoint.x + 30, clickPoint.y + 30, new Color(0, 190, 0), 20);
                                                clickedButton.setIcon(null);
                                            }

                                            if ((finalI + finalJ) % 2 == 0) {
                                                clickedButton.setBackground(flagged[x][y] ? Color.YELLOW : new Color(0, 255, 0));
                                            } else {
                                                clickedButton.setBackground(flagged[x][y] ? Color.YELLOW : new Color(0, 240, 0));
                                            }
                                        }
                                        return;
                                    }
                                    if (SwingUtilities.isLeftMouseButton(e) && !flagged[x][y]) {
                                        if (firstClick) {
                                            firstClick = false;
                                            placeMines(x, y);
                                        }
                                        Point clickPoint = clickedButton.getLocationOnScreen();
                                        SwingUtilities.convertPointFromScreen(clickPoint, window.getContentPane());
                                        if (mineFieldData[x][y].equals("*")) {
                                            particles.spawnExplosion(clickPoint.x + 30, clickPoint.y + 30, Color.RED, 20);
                                            gameEnded = true;
                                            revealAllMines();
                                            JOptionPane.showMessageDialog(null, "Game over! You hit a mine at (" + x + ", " + y + ")");
                                            new Timer(3000, evt -> {
                                                ((Timer) evt.getSource()).stop();
                                            }).start();
                                            JOptionPane.showMessageDialog(null, "Click 'OK' to continue");
                                            window.dispose();
                                        } else {
                                            particles.spawnExplosion(clickPoint.x + 30, clickPoint.y + 30, new Color(236, 225, 97), 20);
                                            revealCell(window, x, y);
                                        }
                                    }
                                }
                            }
                        });

                        container.add(tile);
                    }
                }
                window.pack();
                window.setVisible(true);
                while (!windowClosed.get()) {
                    Thread.sleep(100);
                }
            } else {
                JOptionPane.showMessageDialog(null, "ok");
                return;
            }

            int result = JOptionPane.showConfirmDialog(null, "Play again?", "Continue", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) {
                JOptionPane.showMessageDialog(null, "Thanks for playing!");
                System.exit(0);
                break;
            }
            else {
                firstClick = true;
                gameEnded = false;
            }
        }
    }

    private static void revealCell(JFrame window, int x, int y) {
        if (flagged[x][y]) return;
        if (x < 0 || x >= n || y < 0 || y >= m || revealed[x][y]) {
            return;
        }
        revealed[x][y] = true;
        revealedCount++;

        for (Component comp : window.getContentPane().getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (btn.getName().equals(x + " " + y)) {
                    Point buttonPosition = btn.getLocationOnScreen();
                    SwingUtilities.convertPointFromScreen(buttonPosition, window.getContentPane());
                    window.getContentPane().remove(btn);
                    particleStuff revealParticles = (particleStuff) window.getGlassPane();
                    String value = mineFieldData[x][y];
                    JLabel label;
                    if (value.equals("*")) {
                        label = new JLabel();
                        label.setIcon(new mine(26, new Color(128, 128, 128), new Color(70, 70, 70)));
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                        label.setFont(new Font("SansSerif", Font.BOLD, 24));
                        label.setOpaque(true);
                        label.setBackground(new Color(73, 0, 124));
                    } else {
                        label = new JLabel(value.equals("0") ? "" : value, SwingConstants.CENTER);
                        label.setFont(new Font("SansSerif", Font.BOLD, 24));
                        label.setOpaque(true);
                        label.setBorder(BorderFactory.createEmptyBorder());
                        if (value.equals("0")) {
                            revealParticles.spawnExplosion(buttonPosition.x + 30, buttonPosition.y + 30, new Color(236, 225, 97), 5);
                            if ((x + y) % 2 == 0) label.setBackground(new Color(236, 225, 97));
                            else label.setBackground(new Color(255, 243, 101));
                        }
                        else {
                            revealParticles.spawnExplosion(buttonPosition.x + 30, buttonPosition.y + 30, new Color(236, 225, 97), 20);
                            if ((x + y) % 2 == 0) label.setBackground(new Color(230, 222, 120));
                            else label.setBackground(new Color(250, 241, 131));
                            switch (value) {
                                case "1": label.setForeground(Color.BLUE); break;
                                case "2": label.setForeground(Color.GREEN.darker()); break;
                                case "3": label.setForeground(Color.RED); break;
                                case "4": label.setForeground(new Color(0, 0, 128)); break;
                                case "5": label.setForeground(new Color(128, 0, 0)); break;
                                case "6": label.setForeground(new Color(0, 128, 128)); break;
                                case "7": label.setForeground(Color.ORANGE); break;
                                case "8": label.setForeground(Color.PINK.darker()); break;
                            }
                        }
                    }

                    window.getContentPane().add(label, getComponentIndex(x, y));
                    window.revalidate();
                    window.repaint();
                    break;
                }
            }
        }

        if (revealedCount == totalSafeCells) {
            JOptionPane.showMessageDialog(window, "Congratulations, you win :D");
            revealAllMines();
            new Timer(3000, evt -> {
                ((Timer) evt.getSource()).stop();
                window.dispose();
            }).start();
            return;
        }

        if (mineFieldData[x][y].equals("0")) {
            List<Point> neighbors = new ArrayList<>();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = x + dx;
                    int ny = y + dy;
                    if (nx >= 0 && nx < n && ny >= 0 && ny < m && !revealed[nx][ny] && !flagged[nx][ny]) {
                        neighbors.add(new Point(nx, ny));
                    }
                }
            }

            final int[] index = {0};
            Timer cascadeTimer = new Timer(67, null);
            cascadeTimer.addActionListener(evt -> {
                if (index[0] >= neighbors.size()) {
                    ((Timer) evt.getSource()).stop();
                    return;
                }
                Point p = neighbors.get(index[0]++);
                revealCell(window, p.x, p.y);
            });
            cascadeTimer.start();
        }
    }

    private static int getComponentIndex(int x, int y) {
        return x * m + y;
    }

    private static void revealAllMines() {
        for (Component comp : window.getContentPane().getComponents()) {
            if (comp instanceof JButton button) {
                String[] parts = button.getName().split(" ");
                int i = Integer.parseInt(parts[0]);
                int j = Integer.parseInt(parts[1]);
                if (mineFieldData[i][j].equals("*")) {
                    JLabel mineLabel = new JLabel();
                    mineLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    mineLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
                    mineLabel.setOpaque(true);
                    mineLabel.setBackground(new Color(129, 129, 255));
                    mineLabel.setBorder(BorderFactory.createEmptyBorder());
                    mineLabel.setIcon(new mine(26, new Color(82, 82, 197), new Color(70, 70, 70)));
                    window.getContentPane().remove(button);
                    window.getContentPane().add(mineLabel, i * m + j);
                }
            }
        }
        window.revalidate();
        window.repaint();
    }

    private static void placeMines(int safeX, int safeY) {
        int totalCells = n * m;
        int mineGoal = (int)(totalCells * 0.15);
        int minReveal = (int)((totalCells - mineGoal) * 0.05);

        Random rnd = new Random();
        while (true) {
            for (int i = 0; i < n; i++)
                for (int j = 0; j < m; j++)
                    mineFieldData[i][j] = ".";
            int placed = 0;
            while (placed < mineGoal) {
                int i = rnd.nextInt(n), j = rnd.nextInt(m);
                if ((i == safeX && j == safeY) || mineFieldData[i][j].equals("*"))
                    continue;
                mineFieldData[i][j] = "*";
                placed++;
            }

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    if (mineFieldData[i][j].equals("*")) continue;
                    int cnt = 0;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int ni = i + dx, nj = j + dy;
                            if (ni >= 0 && ni < n && nj >= 0 && nj < m
                                    && mineFieldData[ni][nj].equals("*"))
                                cnt++;
                        }
                    }
                    mineFieldData[i][j] = Integer.toString(cnt);
                }
            }

            boolean[][] seen = new boolean[n][m];
            int revealCount = revealSurrounding(safeX, safeY, seen);
            if (revealCount >= minReveal) {
                totalSafeCells = totalCells - mineGoal;
                return;
            }
        }
    }

    private static int revealSurrounding(int x, int y, boolean[][] seen) {
        if (x < 0 || x >= n || y < 0 || y >= m || seen[x][y]) return 0;
        seen[x][y] = true;
        String val = mineFieldData[x][y];
        int cnt = 1;
        if (val.equals("0")) {
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if (!(dx == 0 && dy==0))
                        cnt += revealSurrounding(x+dx, y+dy, seen);
        }
        return cnt;
    }

    private static class flag implements Icon {
        private final int size;
        private final Color flagColor;
        private final Color poleColor;

        public flag(int size, Color flagColor, Color poleColor) {
            this.size = size;
            this.flagColor = flagColor;
            this.poleColor = poleColor;
        }

        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int poleX = x + 2;
            int poleTop = y - 5;
            int poleBottom = y + size - 1;

            g2.setStroke(new BasicStroke(2));
            g2.setColor(poleColor);
            g2.drawLine(poleX, poleTop, poleX, poleBottom);

            Polygon flag = new Polygon();
            flag.addPoint(poleX, poleTop + 2);
            flag.addPoint(poleX + size, poleTop + size / 3);
            flag.addPoint(poleX, poleTop + size - 4);
            g2.setColor(flagColor);
            g2.fillPolygon(flag);
            g2.dispose();
        }
    }

    private static class mine implements Icon {
        private final int size;
        private final Color fillColor;
        private final Color shadowColor;

        public mine(int size, Color fillColor, Color shadowColor) {
            this.size = size;
            this.fillColor = fillColor;
            this.shadowColor = shadowColor;
        }

        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int pad = 3;
            int w = size - pad * 2;
            int h = size - pad * 2;
            g2.setColor(shadowColor);
            g2.fillOval(x + pad + 1, y + pad + 1, w, h);
            g2.setColor(fillColor);
            g2.fillOval(x + pad, y + pad, w, h);
            g2.dispose();
        }
    }

    static class Particle {
        double x, y;
        double xSpeed, ySpeed;
        int alpha = 255;
        Color color;

        public Particle(int x, int y, Color color, double angle) {
            this.x = x;
            this.y = y;
            Random rand = new Random();

            double speed = rand.nextDouble();

            this.xSpeed = Math.cos(angle) * speed;
            this.ySpeed = Math.sin(angle) * speed;
            this.color = color;
        }

        public boolean update() {
            x += xSpeed;
            y += ySpeed;
            //dy += 0.5;
            alpha -= (int) (Math.random() * 8);
            xSpeed /= 1.06;
            ySpeed /= 1.06;
            return alpha <= 0;
        }

        public void draw(Graphics2D g2d) {
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2d.fillRect((int)x, (int)y, 12, 12);
        }
    }

    static class particleStuff extends JPanel {
        private final List<Particle> particles = new ArrayList<>();
        private Timer animationTimer = null;

        public particleStuff() {
            setOpaque(false);
            animationTimer = new Timer(16, e -> {
                synchronized (particles) {
                    Iterator<Particle> it = particles.iterator();
                    while (it.hasNext()) {
                        if (it.next().update()) {
                            it.remove();
                        }
                    }
                }
                if (particles.isEmpty()) {
                    animationTimer.stop();
                }
                repaint();
            });
        }

        public void spawnExplosion(int startX, int startY, Color color, int amount) {
            int minCentreDist = 25;
            int maxCentreDist = 30;
            synchronized (particles) {
                for (int i = 0; i < amount; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double rad = minCentreDist + (Math.random() * (maxCentreDist - minCentreDist));
                    int updateX = startX + (int) (Math.cos(angle) * rad);
                    int updateY = startY + (int) (Math.sin(angle) * rad);
                    particles.add(new Particle(updateX, updateY, color, angle));
                }
            }
            if (!animationTimer.isRunning()) {
                animationTimer.start();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            synchronized (particles) {
                for (Particle p : particles) {
                    p.draw(g2d);
                }
            }
            g2d.dispose();
        }
    }
}
