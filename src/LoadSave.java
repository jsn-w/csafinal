import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class LoadSave {

    public static final String ENEMY_SPRITES = "NightBorne.png";

    public static BufferedImage GetSpriteAtlas(String fileName){
        BufferedImage img = null;
        InputStream is = LoadSave.class.getResourceAsStream("/" + fileName);
        try {
            img = ImageIO.read(is);
        } catch (IOException e){
            System.out.println(e);
        } finally {
            try{
                is.close();
            } catch (IOException e){
                System.out.println(e);
            }
        }
        return img;
    }
}