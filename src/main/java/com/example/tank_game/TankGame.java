package com.example.tank_game;



import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TankGame extends Application {

    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final int TANK_SIZE = 50;
    private static final int PLAYER_TANK_SPEED = 8;
    private static final int ENEMY_TANK_SPEED = 1;
    private static final int BULLET_SIZE = 10;
    private static final double BULLET_SPEED = 5.0;
    private static final int ENEMY_BULLET_SIZE = 10;
    private static final double ENEMY_BULLET_SPEED = 2.0;

    private int tankX = SCREEN_WIDTH / 2 - TANK_SIZE / 2;
    private int tankY = SCREEN_HEIGHT / 2 - TANK_SIZE / 2;
    private boolean isFiring = false;
    private int tankDirection = 1; // Başlangıçta sağa bakıyor

    private List<Bullet> bullets = new ArrayList<>();
    private List<EnemyTank> enemyTanks = new ArrayList<>();
    private List<EnemyBullet> enemyBullets = new ArrayList<>();

    private boolean gameOver = false;

    private static final long PLAYER_BULLET_COOLDOWN = 500_000_000; // 500 milliseconds
    private long lastPlayerBulletTime = 0;
    private int playerScore = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tank Game");

        Canvas canvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);

        for (int i = 0; i < 3; i++) {
            EnemyTank enemyTank = new EnemyTank();
            enemyTanks.add(enemyTank);
        }

        scene.setOnKeyPressed(e -> {
            if (!gameOver) {
                if (e.getCode() == KeyCode.LEFT && tankX > 0) {
                    tankX -= PLAYER_TANK_SPEED;
                    tankDirection = 2; // Sola
                } else if (e.getCode() == KeyCode.RIGHT && tankX < SCREEN_WIDTH - TANK_SIZE) {
                    tankX += PLAYER_TANK_SPEED;
                    tankDirection = 1; // Sağa
                } else if (e.getCode() == KeyCode.UP && tankY > 0) {
                    tankY -= PLAYER_TANK_SPEED;
                    tankDirection = 3; // Yukarı
                } else if (e.getCode() == KeyCode.DOWN && tankY < SCREEN_HEIGHT - TANK_SIZE) {
                    tankY += PLAYER_TANK_SPEED;
                    tankDirection = 0; // Aşağı
                } else if (e.getCode() == KeyCode.SPACE) {
                    isFiring = true;
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                isFiring = false;
            }
        });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameOver) {
                    updateBullets();
                    updateEnemyTanks();
                    updateEnemyBullets();
                    checkCollisions();
                }
                draw(gc);
            }
        }.start();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameOver) {
                    updateBullets();
                    updateEnemyTanks();
                    updateEnemyBullets();
                    checkCollisions();
                }
                draw(gc);
            }
        }.start();

        primaryStage.setScene(scene);
        primaryStage.show();


    }

    private void draw(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.fillRect(tankX, tankY, TANK_SIZE, TANK_SIZE);


        gc.setFill(Color.RED);
        for (Bullet bullet : bullets) {
            gc.fillRect(bullet.getX(), bullet.getY(), BULLET_SIZE, BULLET_SIZE);
        }

        gc.setFill(Color.GREEN);
        for (EnemyTank enemyTank : enemyTanks) {
            if (!enemyTank.isDestroyed()) {
                gc.fillRect(enemyTank.getX(), enemyTank.getY(), TANK_SIZE, TANK_SIZE);
            }
        }

        gc.setFill(Color.BLUE);
        for (EnemyBullet enemyBullet : enemyBullets) {
            gc.fillRect(enemyBullet.getX(), enemyBullet.getY(), ENEMY_BULLET_SIZE, ENEMY_BULLET_SIZE);
        }

        if (gameOver) {
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial", 36));
            gc.fillText("OYUN BİTTİ!", SCREEN_WIDTH / 2 - 120, SCREEN_HEIGHT / 2);

            gc.setFont(new Font("Arial", 20));
            gc.fillText("Toplam Puan: " + playerScore, SCREEN_WIDTH / 2 - 80, SCREEN_HEIGHT / 2 + 40);
        }



        double namluGenislik = 10.0;
        double namluUzunluk = 10.0;

        gc.setFill(Color.GRAY); // Namlu rengini ayarla

        // Tankın yönüne bağlı olarak namlu pozisyonunu hesapla
        double namluX = 0;
        double namluY = 0;

        switch (tankDirection) {
            case 0: // Aşağı
                namluX = tankX + TANK_SIZE / 2 - namluGenislik / 2;
                namluY = tankY + TANK_SIZE;
                break;
            case 1: // Sağa
                namluX = tankX + TANK_SIZE;
                namluY = tankY + TANK_SIZE / 2 - namluUzunluk / 2;
                break;
            case 2: // Sola
                namluX = tankX - namluGenislik;
                namluY = tankY + TANK_SIZE / 2 - namluUzunluk / 2;
                break;
            case 3: // Yukarı
                namluX = tankX + TANK_SIZE / 2 - namluGenislik / 2;
                namluY = tankY - namluUzunluk;
                break;
        }

        gc.fillRect(namluX, namluY, namluGenislik, namluUzunluk);


        // Düşman tanklarını namlu ile çiz
        gc.setFill(Color.GREEN);
        for (EnemyTank enemyTank : enemyTanks) {
            if (!enemyTank.isDestroyed()) {
                gc.fillRect(enemyTank.getX(), enemyTank.getY(), TANK_SIZE, TANK_SIZE);

                // Düşman tankı namlu pozisyonunu hesapla
                double enemyNamluX = 0;
                double enemyNamluY = 0;

                switch (enemyTank.getDirection()) {
                    case 0: // Aşağı
                        enemyNamluX = enemyTank.getX() + TANK_SIZE / 2 - namluGenislik / 2;
                        enemyNamluY = enemyTank.getY() + TANK_SIZE;
                        break;
                    case 1: // Sağa
                        enemyNamluX = enemyTank.getX() + TANK_SIZE;
                        enemyNamluY = enemyTank.getY() + TANK_SIZE / 2 - namluUzunluk / 2;
                        break;
                    case 2: // Sola
                        enemyNamluX = enemyTank.getX() - namluGenislik;
                        enemyNamluY = enemyTank.getY() + TANK_SIZE / 2 - namluUzunluk / 2;
                        break;
                    case 3: // Yukarı
                        enemyNamluX = enemyTank.getX() + TANK_SIZE / 2 - namluGenislik / 2;
                        enemyNamluY = enemyTank.getY() - namluUzunluk;
                        break;
                }

                gc.fillRect(enemyNamluX, enemyNamluY, namluGenislik, namluUzunluk);
            }
        }

        // Puan göstergesini çiz
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 20));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Puan: " + playerScore, 20, 30);

    }



    private void updateBullets() {
        long currentTime = System.nanoTime();

        if (isFiring && currentTime - lastPlayerBulletTime > PLAYER_BULLET_COOLDOWN) {
            Bullet bullet = new Bullet(
                    tankX + TANK_SIZE / 2 - BULLET_SIZE / 2,
                    tankY + TANK_SIZE / 2 - BULLET_SIZE / 2,
                    BULLET_SPEED,
                    tankDirection
            );

            bullets.add(bullet);

            lastPlayerBulletTime = currentTime; // Update the last firing time
        }

        List<Bullet> bulletsToRemove = new ArrayList<>();
        for (Bullet bullet : bullets) {
            bullet.update();

            for (EnemyTank enemyTank : enemyTanks) {
                if (bullet.collidesWith(enemyTank)) {
                    bulletsToRemove.add(bullet);
                    enemyTank.destroy();
                    playerScore += 10;
                }
            }

            if (isBulletOutsideScreen(bullet)) {
                bulletsToRemove.add(bullet);
            }
        }



        bullets.removeAll(bulletsToRemove);
    }

    private boolean isBulletOutsideScreen(Bullet bullet) {
        if (bullet.getDirection() == 1) { // Bullet is moving right
            return bullet.getX() > SCREEN_WIDTH;
        } else if (bullet.getDirection() == 2) { // Bullet is moving left
            return bullet.getX() + BULLET_SIZE < 0;
        } else {
            // Handle other directions if necessary
            return false;
        }
    }

    private void updateEnemyTanks() {
        for (EnemyTank enemyTank : enemyTanks) {
            enemyTank.update();
            enemyTank.fixBounds();
            if (!enemyTank.isDestroyed() && Math.random() < 0.01) {
                enemyTank.fireBullet();
            }
        }

        for (EnemyTank enemyTank : enemyTanks) {
            if (tankX < enemyTank.getX() + TANK_SIZE &&
                    tankX + TANK_SIZE > enemyTank.getX() &&
                    tankY < enemyTank.getY() + TANK_SIZE &&
                    tankY + TANK_SIZE > enemyTank.getY()) {
                gameOver = true;
            }
        }

        // Düşman tanklarından biri yok olduğunda yeni bir düşman tankı oluştur
        List<EnemyTank> destroyedTanks = new ArrayList<>();
        for (EnemyTank enemyTank : enemyTanks) {
            if (enemyTank.isDestroyed()) {
                destroyedTanks.add(enemyTank);
            }
        }

        for (EnemyTank destroyedTank : destroyedTanks) {
            enemyTanks.remove(destroyedTank);
            createNewEnemyTank();
        }
    }
    private void createNewEnemyTank() {
        EnemyTank newEnemyTank = new EnemyTank();
        enemyTanks.add(newEnemyTank);
    }

    private void updateEnemyBullets() {
        List<EnemyBullet> bulletsToRemove = new ArrayList<>();
        for (EnemyBullet bullet : enemyBullets) {
            bullet.update();

            if (bullet.collidesWith(tankX, tankY, TANK_SIZE, TANK_SIZE)) {
                gameOver = true;
            }

            if (bullet.getX() < 0 || bullet.getX() > SCREEN_WIDTH ||
                    bullet.getY() < 0 || bullet.getY() > SCREEN_HEIGHT) {
                bulletsToRemove.add(bullet);
            }
        }

        enemyBullets.removeAll(bulletsToRemove);
    }

    private void checkCollisions() {
        for (EnemyTank enemyTank : enemyTanks) {
            if (tankX < enemyTank.getX() + TANK_SIZE &&
                    tankX + TANK_SIZE > enemyTank.getX() &&
                    tankY < enemyTank.getY() + TANK_SIZE &&
                    tankY + TANK_SIZE > enemyTank.getY()) {
                 // Düşman tank ve oyuncu tank çarpıştığında oyun bitmesin
                 // gameOver = true;

            }
        }
    }

    private static class Bullet {
        private double x;
        private double y;
        private double speed;
        private int direction;

        public Bullet(double x, double y, double speed, int direction) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.direction = direction;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void update() {
            switch (direction) {
                case 0:
                    y += speed; // Aşağı
                    break;
                case 1:
                    x += speed; // Sağa
                    break;
                case 2:
                    x -= speed; // Sola
                    break;
                case 3:
                    y -= speed; // Yukarı
                    break;
            }
        }

        public boolean collidesWith(EnemyTank enemyTank) {
            return x < enemyTank.getX() + TANK_SIZE &&
                    x + BULLET_SIZE > enemyTank.getX() &&
                    y < enemyTank.getY() + TANK_SIZE &&
                    y + BULLET_SIZE > enemyTank.getY();
        }

        private int getDirection() {
            return direction;
        }
    }

    private static class EnemyBullet {
        private double x;
        private double y;
        private double speedX;
        private double speedY;
        private double speed;

        public EnemyBullet(double x, double y, double speedX, double speedY, double speed) {
            this.x = x;
            this.y = y;
            this.speedX = speedX;
            this.speedY = speedY;
            this.speed = speed;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void update() {
            x += speedX * speed;
            y += speedY * speed;
        }

        public boolean collidesWith(double otherX, double otherY, double otherWidth, double otherHeight) {
            return x < otherX + otherWidth &&
                    x + ENEMY_BULLET_SIZE > otherX &&
                    y < otherY + otherHeight &&
                    y + ENEMY_BULLET_SIZE > otherY;
        }
    }

    private class EnemyTank {
        private double x;
        private double y;
        private double speedX;
        private double speedY;
        private boolean destroyed;

        private static final long ENEMY_DIRECTION_CHANGE_COOLDOWN = 1_000_000_000; // 1 second
        private long lastDirectionChangeTime = 0;

        private int getDirection() {
            if (speedX > 0) {
                return 1; // Sağa
            } else if (speedX < 0) {
                return 2; // Sola
            } else if (speedY > 0) {
                return 0; // Aşağı
            } else if (speedY < 0) {
                return 3; // Yukarı
            } else {
                return 0; // Varsayılan olarak aşağıya bakıyor
            }
        }

        public EnemyTank() {
            setRandomPosition();
            setRandomDirection();
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public boolean isDestroyed() {
            return destroyed;
        }

        public void update() {
            if (!destroyed) {
                long currentTime = System.nanoTime();

                if (currentTime - lastDirectionChangeTime > ENEMY_DIRECTION_CHANGE_COOLDOWN) {
                    setRandomDirection();
                    lastDirectionChangeTime = currentTime;
                }

                x += speedX;
                y += speedY;
            }
        }

        public void fixBounds() {
            if (x < 0) {
                x = 0;
                setRandomDirection();
            } else if (x > SCREEN_WIDTH - TANK_SIZE) {
                x = SCREEN_WIDTH - TANK_SIZE;
                setRandomDirection();
            }

            if (y < 0) {
                y = 0;
                setRandomDirection();
            } else if (y > SCREEN_HEIGHT - TANK_SIZE) {
                y = SCREEN_HEIGHT - TANK_SIZE;
                setRandomDirection();
            }
        }

        public void destroy() {
            destroyed = true;
        }

        public void fireBullet() {
            enemyBullets.add(new EnemyBullet(
                    x + TANK_SIZE / 2 - ENEMY_BULLET_SIZE / 2,
                    y + TANK_SIZE / 2 - ENEMY_BULLET_SIZE / 2,
                    speedX,
                    speedY,
                    ENEMY_BULLET_SPEED
            ));
        }

        private void setRandomPosition() {
            Random random = new Random();
            x = random.nextInt(SCREEN_WIDTH - TANK_SIZE);
            y = random.nextInt(SCREEN_HEIGHT - TANK_SIZE);
        }

        private void setRandomDirection() {
            Random random = new Random();
            int direction = random.nextInt(4);

            switch (direction) {
                case 0:
                    speedX = ENEMY_TANK_SPEED;
                    speedY = 0;
                    break;
                case 1:
                    speedX = -ENEMY_TANK_SPEED;
                    speedY = 0;
                    break;
                case 2:
                    speedX = 0;
                    speedY = ENEMY_TANK_SPEED;
                    break;
                case 3:
                    speedX = 0;
                    speedY = -ENEMY_TANK_SPEED;
                    break;
            }
        }
    }
}