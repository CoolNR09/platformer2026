package platformer.code.gamelogic.level;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import platformer.code.gameengine.PhysicsObject;
import platformer.code.gameengine.graphics.Camera;
import platformer.code.gameengine.loaders.Mapdata;
import platformer.code.gameengine.loaders.Tileset;
import platformer.code.gamelogic.GameResources;
import platformer.code.gamelogic.Main;
import platformer.code.gamelogic.enemies.Enemy;
import platformer.code.gamelogic.player.Player;
import platformer.code.gamelogic.tiledMap.Map;
import platformer.code.gamelogic.tiles.Flag;
import platformer.code.gamelogic.tiles.Flower;
import platformer.code.gamelogic.tiles.Gas;
import platformer.code.gamelogic.tiles.SolidTile;
import platformer.code.gamelogic.tiles.Spikes;
import platformer.code.gamelogic.tiles.Tile;
import platformer.code.gamelogic.tiles.Water;

public class Level {

	private LevelData leveldata;
	private Map map;
	private Enemy[] enemies;
	public static Player player;
	private Camera camera;

	private boolean active;
	private boolean playerDead;
	private boolean playerWin;

	private ArrayList<Enemy> enemiesList = new ArrayList<>();
	private ArrayList<Flower> flowers = new ArrayList<>();

	private List<PlayerDieListener> dieListeners = new ArrayList<>();
	private List<PlayerWinListener> winListeners = new ArrayList<>();

	private Mapdata mapdata;
	private int width;
	private int height;
	private int tileSize;
	private Tileset tileset;
	public static float GRAVITY = 70;
	
	// gas timer params
	private long gasTimerStart = 0;
	private final long GAS_MAX_TIME = 3000;

	public Level(LevelData leveldata) {
		this.leveldata = leveldata;
		mapdata = leveldata.getMapdata();
		width = mapdata.getWidth();
		height = mapdata.getHeight();
		tileSize = mapdata.getTileSize();
		restartLevel();
	}

	public LevelData getLevelData(){
		return leveldata;
	}

	public void restartLevel() {
		int[][] values = mapdata.getValues();
		Tile[][] tiles = new Tile[width][height];

		for (int x = 0; x < width; x++) {
			int xPosition = x;
			for (int y = 0; y < height; y++) {
				int yPosition = y;

				tileset = GameResources.tileset;

				tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this);
				if (values[x][y] == 0)
					tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this); // Air
				else if (values[x][y] == 1)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid"), this);

				else if (values[x][y] == 2)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_DOWNWARDS, this);
				else if (values[x][y] == 3)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_UPWARDS, this);
				else if (values[x][y] == 4)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_LEFTWARDS, this);
				else if (values[x][y] == 5)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_RIGHTWARDS, this);
				else if (values[x][y] == 6)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Dirt"), this);
				else if (values[x][y] == 7)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Grass"), this);
				else if (values[x][y] == 8)
					enemiesList.add(new Enemy(xPosition*tileSize, yPosition*tileSize, this)); // TODO: objects vs tiles
				else if (values[x][y] == 9)
					tiles[x][y] = new Flag(xPosition, yPosition, tileSize, tileset.getImage("Flag"), this);
				else if (values[x][y] == 10) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower1"), this, 1);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 11) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower2"), this, 2);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 12)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_down"), this);
				else if (values[x][y] == 13)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_up"), this);
				else if (values[x][y] == 14)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_middle"), this);
				else if (values[x][y] == 15)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasOne"), this, 1);
				else if (values[x][y] == 16)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasTwo"), this, 2);
				else if (values[x][y] == 17)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasThree"), this, 3);
				else if (values[x][y] == 18)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Falling_water"), this, 0);
				else if (values[x][y] == 19)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Full_water"), this, 3);
				else if (values[x][y] == 20)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Half_water"), this, 2);
				else if (values[x][y] == 21)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Quarter_water"), this, 1);
			}

		}
		enemies = new Enemy[enemiesList.size()];
		map = new Map(width, height, tileSize, tiles);
		camera = new Camera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, 0, map.getFullWidth(), map.getFullHeight());
		for (int i = 0; i < enemiesList.size(); i++) {
			enemies[i] = new Enemy(enemiesList.get(i).getX(), enemiesList.get(i).getY(), this);
		}
		player = new Player(leveldata.getPlayerX() * map.getTileSize(), leveldata.getPlayerY() * map.getTileSize(),
				this);
		camera.setFocusedObject(player);

		active = true;
		playerDead = false;
		playerWin = false;
	}

	public void onPlayerDeath() {
		active = false;
		playerDead = true;
		throwPlayerDieEvent();
	}

	public void onPlayerWin() {
		active = false;
		playerWin = true;
		throwPlayerWinEvent();
	}

	public void update(float tslf) {
		if (active) {
			// Update the player
			player.update(tslf);

			// Player death
			if (map.getFullHeight() + 100 < player.getY())
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.BOT] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.TOP] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.LEF] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.RIG] instanceof Spikes)
				onPlayerDeath();

			// gas check (bypasses collisionMatrix since gas isn't solid)
			boolean inGas = false;
			for (int i = 0; i < map.getWidth(); i++) {
				for (int j = 0; j < map.getHeight(); j++) {
					Tile t = map.getTiles()[i][j];
					if (t instanceof Gas && t.getHitbox() != null) {
						if (player.getHitbox().isIntersecting(t.getHitbox())) {
							inGas = true;
							break;
						}
					}
				}
				if (inGas) break;
			}

			// countdown till death
			if (inGas) {
				if (gasTimerStart == 0) {
					gasTimerStart = System.currentTimeMillis();
				} else if (System.currentTimeMillis() - gasTimerStart >= GAS_MAX_TIME) {
					gasTimerStart = 0;
					onPlayerDeath();
				}
			} else {
				gasTimerStart = 0;
			}

			for (int i = 0; i < flowers.size(); i++) {
				if (flowers.get(i).getHitbox().isIntersecting(player.getHitbox())) {
					if(flowers.get(i).getType() == 1)
						water(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 3);
					else
						addGas(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 20, new ArrayList<Gas>());
					flowers.remove(i);
					i--;
				}
			}

			// Update the enemies
			for (int i = 0; i < enemies.length; i++) {
				enemies[i].update(tslf);
				if (player.getHitbox().isIntersecting(enemies[i].getHitbox())) {
					onPlayerDeath();
				}
			}

			// Update the map
			map.update(tslf);

			// Update the camera
			camera.update(tslf);
		}
	}
	
	
	/**
	 * Pre-condition: map is a valid Map init obj, col and row are in map bounds, 
	 * and fullness is between 0 and 3.
	 * Post-condition: Water is placed at the coordinate with the set fullness, 
	 * and the water flows recursive to surrounding tiles based on game object properties.
	 */
	private void water(int col, int row, Map map, int fullness) {
		// 1. checks
		if (col < 0 || col >= map.getTiles().length || row < 0 || row >= map.getTiles()[col].length) return;
		if (map.getTiles()[col][row].isSolid()) return;
		
		// 2. check for other waters so no overlapping
		if (map.getTiles()[col][row] instanceof Water) {
			Water current = (Water) map.getTiles()[col][row];
			if (current.getFullness() >= fullness) return;
		}

		// 3. find tile image based on fullness
		String fileName = "Full_water";
		switch (fullness) {
			case 0: 
				fileName = "Falling_water";
				break;
			case 1:
				fileName = "Quarter_water";
				break;
			case 2: 
				fileName = "Half_water";
				break;
			case 3: 
				fileName = "Full_water";
				break;
		}
		
		// 4. set tile at appointed xy and update map
		Water water = new Water(col, row, tileSize, tileset.getImage(fileName), this, fullness);
		map.addTile(col, row, water);

		// 5. recursive part
		
		// check if we are at the bottem edge of the map.
		if (row + 1 >= map.getTiles()[col].length) {
			return; 
		}

		// if space below, flow down
		if (!map.getTiles()[col][row + 1].isSolid()) {
			water(col, row + 1, map, 0);
		} 
		// otherwise check sides
		else {
			// If we were falling water (0) and hit the floor, convert this tile to a full water block (3)
			if (fullness == 0) {
				water(col, row, map, 3);
				return;
			}

			// if already 1 keep flowing
			int spreadFullness = (fullness > 1) ? fullness - 1 : 1;
			
			// Spread right
			if (col + 1 < map.getTiles().length && !map.getTiles()[col + 1][row].isSolid()) {
				water(col + 1, row, map, spreadFullness);
			}
			// Spread left
			if (col - 1 >= 0 && !map.getTiles()[col - 1][row].isSolid()) {
				water(col - 1, row, map, spreadFullness);
			}
		}
	}



	public void draw(Graphics g) {
		g.translate((int) -camera.getX(), (int) -camera.getY());

		// Draw the map
		for (int x = 0; x < map.getWidth(); x++) {
			for (int y = 0; y < map.getHeight(); y++) {
				Tile tile = map.getTiles()[x][y];
				if (tile == null) continue;

				// --- STEP 1: GAS ADJACENCY LOGIC ---
				if (tile instanceof Gas) {
					int adjacencyCount = 0;
					// Check surrounding 8 tiles
					for (int i = -1; i <= 1; i++) {
						for (int j = -1; j <= 1; j++) {
							if (i == 0 && j == 0) continue; // Skip self
							
							// Ensure we stay within array bounds
							if ((x + i) >= 0 && (x + i) < map.getTiles().length &&
								(y + j) >= 0 && (y + j) < map.getTiles()[x].length) {
								if (map.getTiles()[x + i][y + j] instanceof Gas) {
									adjacencyCount++;
								}
							}
						}
					}

					Gas gasTile = (Gas) tile;
					if (adjacencyCount == 8) {
						gasTile.setIntensity(2);
						gasTile.setImage(tileset.getImage("GasThree"));
					} else if (adjacencyCount > 5) {
						gasTile.setIntensity(1);
						gasTile.setImage(tileset.getImage("GasTwo"));
					} else {
						gasTile.setIntensity(0);
						gasTile.setImage(tileset.getImage("GasOne"));
					}
				}
				// -----------------------------------

				if (camera.isVisibleOnCamera(tile.getX(), tile.getY(), tile.getSize(), tile.getSize()))
					tile.draw(g);
			}
		}

		// Draw the enemies
		for (int i = 0; i < enemies.length; i++) {
			enemies[i].draw(g);
		}

		// Draw the player
		player.draw(g);

		// used for debugging
		if (Camera.SHOW_CAMERA)
			camera.draw(g);
		
		if (gasTimerStart != 0) {
			long timeElapsed = System.currentTimeMillis() - gasTimerStart;
			long timeLeft = GAS_MAX_TIME - timeElapsed;
			
			// convert to seconds for display
			float displayTime = Math.max(0, timeLeft) / 1000f;
			
			// draw at a static position on the screen
			g.setColor(java.awt.Color.RED);
			g.drawString(String.format("%.1f", displayTime) + "s", 
				(int)player.getX() + 20, (int)player.getY() + 20);
		}
		
		g.translate((int) +camera.getX(), (int) +camera.getY());
	}
	
	/**
	 * Pre-condition: map is a valid Map init obj, col and row are in map bounds, 
	 * and numSquaresToFill is greater than 0.
	 * Post-condition: Gas is placed at the coordinate, 
	 * and the gas spreads to surrounding tiles based on directional rules (i wish i could do this my dynamically).
	 */
	//Adds gas tiles until the requisite number of squares are filled or there is no more room 
	private void addGas(int col, int row, Map map, int numSquaresToFill, ArrayList<Gas> placedThisRound) {
		// 1. checks
		if (col < 0 || col >= map.getTiles().length || row < 0 || row >= map.getTiles()[col].length) return;
		if (map.getTiles()[col][row].isSolid()) return;
		
		// 2. check for other gas so no overlapping
		if (map.getTiles()[col][row] instanceof Gas) return;

		// 3. arraylist queue to hold gas to check around
		ArrayList<Gas> queue = new ArrayList<>();

		// 4. set tile at appointed xy, update map, and add to lists
		Gas origin = new Gas(col, row, tileSize, tileset.getImage("GasOne"), this, 0);
		map.addTile(col, row, origin);
		placedThisRound.add(origin);
		queue.add(origin);

		// 5. directions - needed for notes
		int[][] directions = {
			{ 0, -1}, // top
			{ 1, -1}, // top right
			{-1, -1}, // top left
			{ 1,  0}, // right
			{-1,  0}, // left
			{ 0,  1}, // bottom 
			{ 1,  1}, // bottom right
			{-1,  1}  // bottom left
		};

		int queueIndex = 0;

		// 6. iterative part thank goodness no recursion.
		while (queueIndex < queue.size() && placedThisRound.size() < numSquaresToFill) {
			Gas currentTile = queue.get(queueIndex);
			int cx = currentTile.getCol();
			int cy = currentTile.getRow();

			for (int i = 0; i < directions.length; i++) {
				if (placedThisRound.size() >= numSquaresToFill) break;

				int nx = cx + directions[i][0];
				int ny = cy + directions[i][1];

				// check if inside map bounds
				if (nx >= 0 && nx < map.getTiles().length && ny >= 0 && ny < map.getTiles()[nx].length) {
					Tile targetTile = map.getTiles()[nx][ny];

					// check if empty or possible and not already gas
					if (targetTile == null || (!targetTile.isSolid() && !(targetTile instanceof Gas))) {
						Gas newGas = new Gas(nx, ny, tileSize, tileset.getImage("GasOne"), this, 0);
						map.addTile(nx, ny, newGas);
						placedThisRound.add(newGas);
						queue.add(newGas); 
					}
				}
			}
			queueIndex++;
		}
	}
	
	// --------------------------Die-Listener
	public void throwPlayerDieEvent() {
		for (PlayerDieListener playerDieListener : dieListeners) {
			playerDieListener.onPlayerDeath();
		}
	}

	public void addPlayerDieListener(PlayerDieListener listener) {
		dieListeners.add(listener);
	}

	// ------------------------Win-Listener
	public void throwPlayerWinEvent() {
		for (PlayerWinListener playerWinListener : winListeners) {
			playerWinListener.onPlayerWin();
		}
	}

	public void addPlayerWinListener(PlayerWinListener listener) {
		winListeners.add(listener);
	}

	// ---------------------------------------------------------Getters
	public boolean isActive() {
		return active;
	}

	public boolean isPlayerDead() {
		return playerDead;
	}

	public boolean isPlayerWin() {
		return playerWin;
	}

	public Map getMap() {
		return map;
	}

	public Player getPlayer() {
		return player;
	}
}