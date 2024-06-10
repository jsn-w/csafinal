import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Spirit {
    private final double MOVE_AMT = 0.4;
    private final int DEATH_FRAMES = 30;
    private final int SPAWN_FRAMES = 30;
    private final int IDLE_FRAMES = 50;

    private BufferedImage spiritAnimations[][], spiritAnimationsLeft[][];
    private BufferedImage spiritSpritesheet;

    private double xCoord;
    private double yCoord;
    private boolean isLeft;
    private int i;
    private int health;

    private boolean spawnDone;

    public Spirit(int x, int y){
        xCoord = x;
        yCoord = y;
        i = 0;
        health = 300;
        spawnDone = false;

        loadImages();
    }

    public int getxCoord(){
        return (int) (xCoord + GraphicsPanel.backgroundPosition);
    }

    public int getyCoord(){
        return (int) yCoord;
    }

    public void render(Graphics g, Player p, ArrayList<Bullet> b){
        if (p.getxCoord() < getxCoord()){
            isLeft = true;
        } else{
            isLeft = false;
        }

        if (!spawnDone){
            spawnAnimation(g);
        }
        if (health > 0) {
            int rando = (int)(Math.random() * 400);
            if (rando == 1){
                shoot(p,b);
            }
            idleAnimation(g);
        } else {
            deathAnimation(g);
        }
    }

    private void loadImages(){
        try{
            spiritSpritesheet = ImageIO.read(new File("src/assets/Death/spirit.png"));
        } catch (IOException e){
            System.out.println(e);
        }
        loadAnimations();
    }

    private void loadAnimations(){
        spiritAnimations = new BufferedImage[3][6]; // row 1: spawn, row 2: death, row 3: idle
        spiritAnimationsLeft = new BufferedImage[3][6];

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 6; j++){
                spiritAnimations[i][j] = spiritSpritesheet.getSubimage(spiritSpritesheet.getWidth() / 6 * j, spiritSpritesheet.getHeight() / 3 * i, 53, 53);
            }
        }
        spiritAnimationsLeft = Utility.flipEvery(spiritAnimations);
    }

    private void shoot(Player p, ArrayList<Bullet> b){
        double angle = Math.atan2( (p.getyCoord() - getyCoord() + 50) , (p.getxCoord() - getxCoord() + 50)); // adjust these values accordingly to player size
        b.add(new Bullet(getxCoord(), getyCoord(), angle, 0.5));
    }

    private void spawnAnimation(Graphics g){
        i++;
        if (i < SPAWN_FRAMES * 6){
            if (isLeft) {
                g.drawImage(spiritAnimationsLeft[0][i / SPAWN_FRAMES], getxCoord(), getyCoord(), null);
            } else {
                g.drawImage(spiritAnimations[0][i / SPAWN_FRAMES], getxCoord(), getyCoord(), null);
            }
        } else {
            spawnDone = true;
            i = 0;
        }
    }

    private void idleAnimation(Graphics g){
        i++;
        if (i <= 4 * IDLE_FRAMES){
            if (isLeft){
                g.drawImage(spiritAnimationsLeft[2][i / IDLE_FRAMES], getxCoord(), getyCoord(), null);
            } else{
                g.drawImage(spiritAnimations[2][i / IDLE_FRAMES], getxCoord(), getyCoord(), null);
            }
        } else {
            i = 0;
        }
    }

    private void deathAnimation(Graphics g){
        i++;
        if (i < DEATH_FRAMES * 5){
            if (isLeft){
                g.drawImage(spiritAnimationsLeft[1][i / DEATH_FRAMES], getxCoord(), getyCoord(), null);
            } else {
                g.drawImage(spiritAnimations[1][i / DEATH_FRAMES], getxCoord(), getyCoord(), null);
            }
        }
    }
}
