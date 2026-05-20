package platformer.code.gamelogic.tiles;

import platformer.code.gameengine.hitbox.RectHitbox;
import platformer.code.gameengine.loaders.Tileset;
import platformer.code.gamelogic.GameResources;
import platformer.code.gamelogic.level.Level;

public class Spikes extends Tile{

	public static final int HORIZONTAL_DOWNWARDS = 0;
	public static final int HORIZONTAL_UPWARDS = 1;
	public static final int VERTICAL_LEFTWARDS = 2;
	public static final int VERTICAL_RIGHTWARDS = 3;
	
	private int offsetX = 25;
	private int offsetY = 70;
	
	public Spikes(float x, float y, int size, int orientation, Level level) {
		super(x, y, size, null, true, level);
		offsetX = (int)(level.getLevelData().getTileSize()*0.1);
		offsetY = (int)(level.getLevelData().getTileSize()/2);
		Tileset tileset = GameResources.tileset;

		switch (orientation) {
		case HORIZONTAL_UPWARDS:
			hitbox = new RectHitbox(x*size , y*size, offsetX, offsetY, size - offsetX, size);
			image = tileset.getImage("Spikes_upwards");
			break;
		case HORIZONTAL_DOWNWARDS:
			hitbox = new RectHitbox(x*size , y*size, offsetX, 0, size - offsetX, size - offsetY);
			image = tileset.getImage("Spikes_downwards");;
			break;
		case VERTICAL_LEFTWARDS:
			hitbox = new RectHitbox(x*size , y*size, offsetY, offsetX, size, size - offsetX);
			image = tileset.getImage("Spikes_leftwards");;
			break;
		case VERTICAL_RIGHTWARDS:
			hitbox = new RectHitbox(x*size , y*size, 0, offsetX, size - offsetY, size - offsetX);
			image = tileset.getImage("Spikes_rightwards");;
			break;

		default:
			break;
		}
	}
}
