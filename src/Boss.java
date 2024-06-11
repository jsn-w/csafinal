import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class Boss implements ActionListener{
    private boolean phaseOneBeat;
    private boolean phaseTwoBeat;
    private boolean onPhaseThree;
    private boolean win;
    private int hp;
    private boolean hitAvailability;
    private int maxHp;
    private BufferedImage[][] bossAnimationsLeft,bossAnimationsRight;
    private BufferedImage spritesheet;
    private int i;
    private boolean isLeft;
    private static final double MOVE_AMT = 1.8;
    private static final int FRAMES_PER_UPDATE = 25;
    private double xCoord;
    private double yCoord;
    private int fly;
    private boolean heavyAttack;
    private boolean attack;
    private boolean attackDone;
    private int width;
    private int height;
    private Timer flyTimer;
    private Timer bulletTimer;
    private ArrayList<BossBullet> bullets;
    private Player p;
    private boolean falling;
    private ArrayList<Object> mobs;

    public Boss(Player p, Graphics g){
        this.p = p;
        hp = 500;
        maxHp = 1000;
        phaseOneBeat = false;
        phaseTwoBeat = false;
        onPhaseThree = false;
        win = false;
        hitAvailability = true;
        i = 0;
        isLeft = true;
        xCoord = 300;
        yCoord = 400;
        fly = 0;
        bullets = new ArrayList<>();
        heavyAttack = false;
        attack = false;
        attackDone = true;
        flyTimer = new Timer(5000, this);
        flyTimer.start();
        bulletTimer = new Timer(1000,this);
        bulletTimer.start();
        bullets = new ArrayList<>();
        mobs = new ArrayList<>();
        falling = false;
        loadImages("src/assets/bossSpritesheet.png");
    }

    private void loadImages(String file) {
        try {
            spritesheet = ImageIO.read(new File(file));
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        width = spritesheet.getWidth() / 15;
        height = spritesheet.getHeight() / 7;
        loadAnimations();
    }

    private void loadAnimations() {
        bossAnimationsLeft = new BufferedImage[7][15];
        bossAnimationsRight = new BufferedImage[7][15];
        for (int i = 0; i < 7; i++){
            for (int j = 0; j < 15; j++){
                 bossAnimationsRight[i][j] = spritesheet.getSubimage(width * j, height * i, width, height);
            }
        }
        bossAnimationsLeft = Utility.flipEvery(bossAnimationsRight);
        mobs.add(new NightBorne("src/assets/NightBorne.png", 100, 220));
        mobs.add(new Spirit(500, 200));
        mobs.add(new Death(500, 200));
    }
    private void render(Graphics g, Player p) {
        drawHealthBar(g);
        int margin = -70;
        isLeft = getXCoord() + width / 2 > p.getxCoord() + 50;
        if(!hitAvailability){
            idle(g);
            if (mobs.get(0) instanceof NightBorne) {
                ((NightBorne) mobs.get(0)).render(g,p,mobs);
            }else if(mobs.get(0) instanceof Death) {
                ((Death) mobs.get(0)).render(g,p,mobs);
            }
        }else {
            if (attackDone) {
                if ((getXCoord() + 480) + margin < p.getxCoord()) {
                    isLeft = false;
                    if (fly % 2 == 1) {
                        bulletTimer.start();
                        if (onPhaseThree){
                            if (yCoord >= -100) {
                                fly(g);
                                yCoord -= MOVE_AMT / 2;
                            }
                            if (yCoord < -100) {
                                yCoord = -100;
                            }
                        }else {
                            if (yCoord >= 300) {
                                fly(g);
                                yCoord -= MOVE_AMT / 2;
                            }
                            if (yCoord < 300) {
                                yCoord = 300;
                            }
                        }
                        fly(g);
                    } else if (falling && fly % 2 == 0) {
                        if (onPhaseThree){
                            if (yCoord <= 200) {
                                fly(g);
                                yCoord += MOVE_AMT / 2;
                            }
                            if (yCoord > 200) {
                                yCoord = 200;
                                falling = false;
                            }
                        }else {
                            if (yCoord <= 400) {
                                fly(g);
                                yCoord += MOVE_AMT / 2;
                            }
                            if (yCoord > 400) {
                                yCoord = 400;
                                falling = false;
                            }
                        }
                    } else {
                        walk(g, FRAMES_PER_UPDATE);
                    }
                    xCoord += MOVE_AMT;
                } else if ((getXCoord() - 100) - margin > p.getxCoord()) {
                    isLeft = true;
                    if (fly % 2 == 1) {
                        bulletTimer.start();
                        if (onPhaseThree){
                            if (yCoord >= -100) {
                                fly(g);
                                yCoord -= MOVE_AMT / 2;
                            }
                            if (yCoord < -100) {
                                yCoord = -100;
                            }
                        }else {
                            if (yCoord >= 300) {
                                fly(g);
                                yCoord -= MOVE_AMT / 2;
                            }
                            if (yCoord < 300) {
                                yCoord = 300;
                            }
                        }
                        fly(g);
                    } else if (falling && fly % 2 == 0) {
                        if (onPhaseThree){
                            if (yCoord <= 200) {
                                fly(g);
                                yCoord += MOVE_AMT / 2;
                            }
                            if (yCoord > 200) {
                                yCoord = 200;
                                falling = false;
                            }
                        }else {
                            if (yCoord <= 400) {
                                fly(g);
                                yCoord += MOVE_AMT / 2;
                            }
                            if (yCoord > 400) {
                                yCoord = 400;
                                falling = false;
                            }
                        }
                    } else {
                        walk(g, FRAMES_PER_UPDATE);
                    }
                    xCoord -= MOVE_AMT;
                } else if ((getXCoord() - 100) - margin <= p.getxCoord() && (getXCoord() + 480) + margin >= p.getxCoord()) {
                    if (fly % 2 == 0 && falling == false) {
                        if (attackDone) {
                            if (Math.random() < .25) {
                                heavyAttack = true;
                                attackDone = false;
                                attack = false;
                            } else {
                                attack = true;
                                attackDone = false;
                                heavyAttack = false;
                            }
                        }
                        if (heavyAttack) {
                            heavyAttack(g, p);
                        } else {
                            punch(g, p);
                        }
                    } else {
                        if (fly % 2 == 1) {
                            if (onPhaseThree){
                                if (yCoord >= -100) {
                                    fly(g);
                                    yCoord -= MOVE_AMT / 2;
                                }
                                if (yCoord < -100) {
                                    yCoord = -100;
                                }
                            }else {
                                if (yCoord >= 300) {
                                    fly(g);
                                    yCoord -= MOVE_AMT / 2;
                                }
                                if (yCoord < 300) {
                                    yCoord = 300;
                                }
                            }
                            fly(g);
                        } else if (falling && fly % 2 == 0) {
                            if (onPhaseThree){
                                if (yCoord <= 200) {
                                    fly(g);
                                    yCoord += MOVE_AMT / 2;
                                }
                                if (yCoord > 200) {
                                    yCoord = 200;
                                    falling = false;
                                }
                            }else {
                                if (yCoord <= 400) {
                                    fly(g);
                                    yCoord += MOVE_AMT / 2;
                                }
                                if (yCoord > 400) {
                                    yCoord = 400;
                                    falling = false;
                                }
                            }
                        }
                    }
                } else {
                    deathAnimation(g);
                }
            } else {
                if (attack) {
                    punch(g, p);
                } else {
                    heavyAttack(g, p);
                }
            }
            for (int i = 0; i < bullets.size(); i++) {
                boolean removed = bullets.get(i).move(g, bullets);
                if (!removed) {
                    if ((bullets.get(i).getyCoord() > 550 || bullets.get(i).getyCoord() < -200) || bullets.get(i).enemyRect().intersects(p.playerRect())) {
                        if (bullets.get(i).enemyRect().intersects(p.playerRect())) {
                            p.takeDamage(1);
                        }
                        bullets.remove(i);
                        i--;
                    }
                }
            }
        }
    }
    private void deathAnimation(Graphics g){
        if (i == 11 * 40) {
            i = 0;
        }
        if (!isLeft) {
            g.drawImage(bossAnimationsRight[7][i/40], getXCoord(), getYCoord(), null);
        } else {
            g.drawImage(bossAnimationsLeft[7][i/40], getXCoord(), getYCoord(), null);
        }
        i++;
    }
    private void idle(Graphics g){
        if (i == 15 * 40) {
            i = 0;
        }
        if (!isLeft) {
            g.drawImage(bossAnimationsRight[0][i/40], getXCoord(), getYCoord(), null);
        } else {
            g.drawImage(bossAnimationsLeft[0][i/40], getXCoord(), getYCoord(), null);
        }
        i++;
    }
    private void punch(Graphics g, Player p){
        if (i >= 7 * 35) {
            i = 0;
            attackDone = true;
        }
        if (!isLeft) {
            g.drawImage(bossAnimationsRight[3][i/35], getXCoord(), getYCoord(), null);
        } else {
            g.drawImage(bossAnimationsLeft[3][i/35], getXCoord(), getYCoord(), null);
        }
        i++;
        Rectangle atkRect;
        if (isLeft){
            atkRect = new Rectangle(getXCoord(),getYCoord(),width/2,height);
        }else{
            atkRect = new Rectangle(getXCoord()+width/2,getYCoord(),width/2,height);
        }
        if (atkRect.intersects(p.playerRect())){
            p.takeDamage(.3);
        }else if (atkRect.intersects(p.playerRect())){
            p.takeDamage(.3);
        }
    }
    private void heavyAttack(Graphics g, Player p){
        if (i >= 9 * 35) {
            i = 0;
            attackDone = true;
        }
        if (!isLeft) {
            g.drawImage(bossAnimationsRight[4][i/35], getXCoord(), getYCoord(), null);
        } else {
            g.drawImage(bossAnimationsLeft[4][i/35], getXCoord(), getYCoord(), null);
        }
        i++;
        Rectangle atkRect;
        if (isLeft){
            atkRect = new Rectangle(getXCoord(),getYCoord(),width/2,height);
        }else{
            atkRect = new Rectangle(getXCoord()+width/2,getYCoord(),width/2,height);
        }
        if (atkRect.intersects(p.playerRect())){
            p.takeDamage(1);
        }else if (atkRect.intersects(p.playerRect())){
            p.takeDamage(1);
        }
    }
    private void walk(Graphics g, int frames){
        if (i >= 6 * frames) {
            i = 0;
        }
        if (!isLeft) {
            g.drawImage(bossAnimationsRight[1][i/frames], getXCoord(), getYCoord(), null);
        } else {
            g.drawImage(bossAnimationsLeft[1][i/frames], getXCoord(), getYCoord(), null);
        }
        i++;
    }
    private void fly(Graphics g){
        if (i >= 6 * 55) {
            i = 0;
        }
        if (!isLeft) {
            g.drawImage(bossAnimationsRight[2][i/55], getXCoord(), getYCoord(), null);
        } else {
            g.drawImage(bossAnimationsLeft[2][i/55], getXCoord(), getYCoord(), null);
        }
        i++;
    }
    public boolean isPhaseOneBeat(){
        return phaseOneBeat;
    }
    public boolean isPhaseTwoBeat() {
        return phaseTwoBeat;
    }
    public boolean canHit(){
        return hitAvailability;
    }

    public void drawHealthBar(Graphics g){
        g.setColor(Color.black);
        g.fillRoundRect(320, 100, 500, 40, 40, 40);
        g.setColor(Color.RED);
        g.fillRoundRect(325, 105, (int)(490 * ((double)hp/maxHp)), 30, 40, 40);
        g.setFont(new Font("Courier New", Font.BOLD, 24));
        g.setColor(Color.YELLOW);
        g.drawString("boss health: " + hp + "/" + maxHp,335,125);
    }
    public void phaseOne(Graphics g, Player p){
        render(g,p);
        if (hp <= 0) {
            phaseOneBeat = true;
            hp = 5000;
            maxHp = 5000;
        }
    }
    public void phaseTwo(ArrayList<Object> e,Graphics g, Player p){
        render(g,p);
        hitAvailability = false;
        if (e.isEmpty()) {
            for (int j = 0; j <= 1000; j++) {
                grow(g);
            }
            loadImages("src/assets/biggercthulu.png");
            yCoord -= height / 2 - 35;
            for (int j = 0; j <= 500; j++) {
                grow(g);
            }
            phaseTwoBeat = true;
            hitAvailability = true;
            onPhaseThree = true;
        }
    }
    public void phaseThree(Graphics g, Player p){
        render(g,p);
        if (hp <= 0) {
            win = true;
        }
    }
    public int getXCoord() {
        return (int) (xCoord + GraphicsPanel.backgroundPosition);
    }
    public int getYCoord() {
        return (int) (yCoord);
    }
    private void grow(Graphics g){
        if (i >= 5 * 100) {
            i = 0;
        }
        if (!isLeft) {
            g.drawImage(bossAnimationsRight[5][i / 100], getXCoord(), getYCoord(), null);
        } else {
            g.drawImage(bossAnimationsLeft[5][i / 100], getXCoord(), getYCoord(), null);
        }
        i++;
    }
    private void shoot(Player p, ArrayList<BossBullet> b){
        double angle = Math.atan2( (p.getyCoord() - (int)yCoord + 50) , (p.getxCoord() - getXCoord() + 50)); // adjust these values accordingly to player size
        if (isLeft) {
            b.add(new BossBullet(getXCoord()+width/4, (int) yCoord + height / 2, angle, 0.5));
        }else{
            b.add(new BossBullet(getXCoord() + 3 * width/4, (int) yCoord + height / 2, angle, 0.5));
        }
    }
    private Rectangle bossRect(){
        return new Rectangle(getXCoord(), getYCoord(), width, height);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Timer) {
            if (e.getSource() == flyTimer) {
                fly++;
                if (fly % 2 == 0) {
                    falling = true;
                }
            }
            if(e.getSource() == bulletTimer && fly % 2 == 1){
                shoot(p,bullets);
                System.out.println("SHOOT"); // not printing
            }
        }
    }
    public void takeDamage(double damage) {
        hp -= damage;
        if (hp < 0) {
            hp = 0;
        }
    }
}
