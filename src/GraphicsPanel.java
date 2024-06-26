import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GraphicsPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener, ActionListener {

    public static double backgroundPosition;

    private BufferedImage menuBackground, buttonBackground, background, deathBackground;
    private Player player;
    private NightBorne nightBorne;
    private Death death;

    private ArrayList<NightBorne> enemies;
    public static ArrayList<Bullet> bullets;
    public static ArrayList<Spirit> spirits;

    private boolean[] pressedKeys;

    private BufferedImage buttons;
    private BufferedImage[][] buttonAnimations;
    private int[] buttonState;
    private int menuStartYPos = 330;
    private int menuButtonGap = 40;

    private Timer loadingTimer;
    private int loadingAnimationAngle;
    private Boss b;
    private boolean bossSpawned;
    private ArrayList<Object> mobs;

    private enum State {
        MENU, LOADING, GAME, DEAD, WIN
    }
    private State state;

    private Clip songClip;
    private boolean songPlayed;

    public GraphicsPanel() {
        pressedKeys = new boolean[128];
        mobs = new ArrayList<>();
        bullets = new ArrayList<>();
        spirits = new ArrayList<>();
        backgroundPosition = (double) -MainFrame.screenWidth / 2;
        buttonState = new int[3];

        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        requestFocusInWindow();
        loadAssets();

        bossSpawned = false;
        loadingAnimationAngle = 0;
        loadingTimer = new Timer(1, this);
        state = State.MENU;
        songPlayed = false;
    }

    private void loadAssets() {
        try {
            background = ImageIO.read(new File("src/assets/background.png"));
            menuBackground = ImageIO.read(new File("src/assets/menuBackground.png"));
            buttonBackground = ImageIO.read(new File("src/assets/buttonBackground.png"));
            deathBackground = ImageIO.read(new File("src/assets/deathscreen.jpg"));
            buttons = ImageIO.read(new File("src/assets/buttons.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        buttonAnimations = new BufferedImage[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 1) {
                    continue;
                }
                buttonAnimations[i][j] = buttons.getSubimage(140 * j, 56 * i, 140, 56);
            }
        }

        player = new Player("src/assets/playerAnimations.png", 640, 350);
        nightBorne = new NightBorne("src/assets/NightBorne.png", 100, 220);
        // e = new Spirit(500, 200);

        death = new Death(500, 200);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        switch (state) {
            case MENU -> {
                renderMenu(g);
                if (!songPlayed) {
                    playMenu();
                    songPlayed = true;
                }
            }
            case LOADING -> renderLoading(g);
            case GAME -> {
                renderGame(g);
                if (!songPlayed && !b.isPhaseTwoBeat()){
                    playBattle();
                    songPlayed = true;
                }
            }
            case DEAD -> renderDead(g);
            case WIN -> renderWin(g);
        }
    }

    private void renderMenu(Graphics g) {
        g.drawImage(menuBackground, 0, 0, null);
        g.drawImage(buttonBackground, (MainFrame.screenWidth - buttonBackground.getWidth())/2, (MainFrame.screenHeight - buttonBackground.getHeight())/2, null);

        int buttonX = (MainFrame.screenWidth - buttonAnimations[0][0].getWidth()) / 2;

        for (int i = 0; i < 3; i++) {
            g.drawImage(buttonAnimations[i][buttonState[i]], buttonX, menuStartYPos + i * menuButtonGap, null);
        }

    }

    private void renderLoading(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.translate(MainFrame.screenWidth / 2, MainFrame.screenHeight / 2);
        g2d.rotate(Math.toRadians(loadingAnimationAngle));
        g2d.fillRect(-25, -25, 50, 50);
        g2d.rotate(-Math.toRadians(loadingAnimationAngle));
        g2d.translate(-MainFrame.screenWidth / 2, -MainFrame.screenHeight / 2);
    }

    private void renderGame(Graphics g) {
        if (!bossSpawned){
            b = new Boss(player,g);
            bossSpawned = true;
        }
        g.drawImage(background, (int) backgroundPosition, 0, null);
        boolean win = !(nightBorne.getHealth() > 0) && !(death.getHealth() > 0);

        for (Spirit spirit : spirits) {
            if (spirit.getHealth() > 0) {
                win = false;
                break;
            }
        }

        if (win) {
            state = State.WIN;
        }

        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).move(g);
            if ((bullets.get(i).getyCoord() > 550 || bullets.get(i).getyCoord() < -200) || bullets.get(i).enemyRect().intersects(player.playerRect())){
                if (bullets.get(i).enemyRect().intersects(player.playerRect())) {
                    if (bullets.get(i) instanceof Bloodsword) {
                        player.takeDamage(50);
                    } else {
                        player.takeDamage(5);
                    }
                }
                bullets.remove(i);
                i--;
            }
        }
        for (int i = 0; i < spirits.size(); i++){
            spirits.get(i).render(g, player,bullets, spirits);
        }
        player.render(g, this);

        if (player.getHp() <= 0) {
            state = State.DEAD;
        }
        if(!b.isWin()) {
            if (!b.isPhaseOneBeat()) {
                b.phaseOne(g, player);
            } else if (!b.isPhaseTwoBeat()) {
                b.phaseTwo(g, player);
                if (b.getGrowNumber() == 1){
                    songClip.stop();
                    songClip.close();
                    playFinale();
                }
            } else {
                b.phaseThree(g, player);
            }
        }else{

            state = State.WIN;
        }
    }

    private void renderDead(Graphics g) {
        g.drawImage(background, (int) backgroundPosition, 0, null);
//        g.drawImage(deathBackground, 0, 0, null);
        g.setFont(new Font("Source Code Pro", Font.BOLD, 40));
        g.setColor(new Color(240, 240, 240));
        g.drawString("You have LOST!", 490, 300);
        g.drawString("Thanks for playing!!!", 450, 350);
        player.render(g, this);

    }

    private void renderWin(Graphics g) {
        g.drawImage(background, (int) backgroundPosition, 0, null);
        g.setFont(new Font("Source Code Pro", Font.BOLD, 40));
        g.setColor(new Color(240, 240, 240));
        g.drawString("You have WON!", 490, 300);
        g.drawString("Thanks for playing!!!", 450, 350);

    }

    private void playMenu() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("src/assets/audios/menu.wav").getAbsoluteFile());
            songClip = AudioSystem.getClip();
            songClip.open(audioInputStream);
            songClip.loop(Clip.LOOP_CONTINUOUSLY);  // song repeats when finished
            songClip.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void playBattle() {
        try{
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("src/assets/audios/battle.wav").getAbsoluteFile());
            songClip = AudioSystem.getClip();
            songClip.open(audioInputStream);
            songClip.loop(Clip.LOOP_CONTINUOUSLY);
            songClip.start();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void playFinale(){
        try{
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("src/assets/audios/phase3.wav").getAbsoluteFile());
            songClip = AudioSystem.getClip();
            songClip.open(audioInputStream);
            songClip.loop(Clip.LOOP_CONTINUOUSLY);
            songClip.start();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (state == State.MENU) {
            if (playRect().contains(e.getPoint())) {
                songClip.stop();
                songClip.close();
                songPlayed = false;
                state = State.LOADING;
                loadingTimer.start();
            } else if (quitRect().contains(e.getPoint())) {
                System.exit(0);
            }
        } else if (state == State.GAME) {
            player.setState();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (state == State.MENU) {
            boolean insideButton = false;
            for (int i = 0; i < 3; i++) {
                if (getButtonRect(i).contains(e.getPoint())) {
                    buttonState[i] = 1;
                    insideButton = true;
                } else {
                    buttonState[i] = 0;
                }
            }

            if (!insideButton) {
                for (int i = 0; i < 3; i++) {
                    buttonState[i] = 0;
                }
            }

            repaint();
        }
    }

    private Rectangle getButtonRect(int index) {
        int imageHeight = buttonAnimations[0][0].getHeight();
        int imageWidth = buttonAnimations[0][0].getWidth();
        int x = (MainFrame.screenWidth - imageWidth) / 2;
        int y = menuStartYPos + index * menuButtonGap;  // Adjusted Y coordinate with spacing
        return new Rectangle(x, y, imageWidth, imageHeight);
    }

    private Rectangle playRect() {
        return getButtonRect(0);
    }

    private Rectangle optionRect() {
        return getButtonRect(1);
    }

    private Rectangle quitRect() {
        return getButtonRect(2);
    }

    // KeyListener methods
    public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        pressedKeys[key] = true;
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        pressedKeys[key] = false;
    }

    // MouseListener methods
    public void mouseClicked(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    // MouseMotionListener methods
    public void mouseDragged(MouseEvent e) {}

    public boolean[] getPressedKeys() {
        return pressedKeys;
    }

    public ArrayList<NightBorne> getEnemies() {
        return enemies;
    }

    // ActionListener method for the loading animation
    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == State.LOADING) {
            loadingAnimationAngle += 5;
            repaint();
            if (loadingAnimationAngle >= 360) {
                loadingAnimationAngle = 0;
                state = State.GAME;
                loadingTimer.stop();
            }
        }
    }

    public void playerDied() {
        state = State.DEAD;
    }
}