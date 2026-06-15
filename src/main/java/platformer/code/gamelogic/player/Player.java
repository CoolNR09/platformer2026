package platformer.code.gamelogic.player;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import platformer.code.gameengine.PhysicsObject;
import platformer.code.gameengine.graphics.MyGraphics;
import platformer.code.gameengine.hitbox.RectHitbox;
import platformer.code.gamelogic.Main;
import platformer.code.gamelogic.level.Level;
import platformer.code.gamelogic.tiles.Tile;
import platformer.code.gameengine.input.KeyboardInputManager;
import platformer.code.gamelogic.GameResources;
import platformer.code.gamelogic.tiles.Gas;
import platformer.code.gamelogic.tiles.Water;

public class Player extends PhysicsObject{
	public float walkSpeed = 400;
	public float jumpPower = 1350;

	private boolean isJumping = false;
	private boolean jumpKeyHeld = false; 
	
	// double jump params
	private boolean canDoubleJump = false;
	private boolean hasDoubleJumped = false;
	private long doubleJumpActiveTime = 0;
	private final long DOUBLE_JUMP_DURATION = 5000;

	public Player(float x, float y, Level level) {
		super(x, y, level.getLevelData().getTileSize(), level.getLevelData().getTileSize(), level);
		int offset =(int)(level.getLevelData().getTileSize()*0.1);
		this.hitbox = new RectHitbox(this, offset,offset, width -offset, height - offset);
	}

	@Override
	public void update(float tslf) {
		super.update(tslf);
		
		// water check (bypasses collisionMatrix since water isn't solid)
		boolean inWater = false;
		for (int i = 0; i < getLevel().getMap().getWidth(); i++) {
			for (int j = 0; j < getLevel().getMap().getHeight(); j++) {
				Tile t = getLevel().getMap().getTiles()[i][j];
				if (t instanceof Water && t.getHitbox() != null) {
					if (this.hitbox.isIntersecting(t.getHitbox())) {
						inWater = true;
						break;
					}
				}
			}
			if (inWater) break;
		}

		float currentSpeed = walkSpeed;
		float currentJump = jumpPower;

		if (inWater) {
			currentSpeed = 120f;
			currentJump = 750f;
		}

		boolean leftPressed = PlayerInput.isLeftKeyDown();
		boolean rightPressed = PlayerInput.isRightKeyDown();
		
		// applies affects when player is in water
		if (inWater) {
			boolean temp = leftPressed;
			leftPressed = rightPressed;
			rightPressed = temp;
		}
		
		// base movement
		movementVector.x = 0;
		if (leftPressed) {
			movementVector.x = -currentSpeed;
		}
		if (rightPressed) {
			movementVector.x = +currentSpeed;
		}

		// double jump powerup - to activate
		if (KeyboardInputManager.isKeyDown(KeyEvent.VK_E) && doubleJumpActiveTime == 0) {
			doubleJumpActiveTime = System.currentTimeMillis();
			canDoubleJump = true;
		}
		
		// expire timer for powerup active
		if (doubleJumpActiveTime != 0) {
			if (System.currentTimeMillis() - doubleJumpActiveTime >= 5000) {
				canDoubleJump = false;
				doubleJumpActiveTime = 0;
			}
		}

		// single fire check
		boolean currentJumpInput = PlayerInput.isJumpKeyDown();
		
		if (currentJumpInput) {
			if (!jumpKeyHeld) {
				if (!isJumping) {
					movementVector.y = -currentJump;
					isJumping = true;
				} else if (canDoubleJump && !hasDoubleJumped) {
					movementVector.y = -currentJump * 0.9f;
					hasDoubleJumped = true;
				}
			}
			jumpKeyHeld = true;
		} else {
			jumpKeyHeld = false;
		}

		// reset statements
		if (collisionMatrix[BOT] != null) {
			isJumping = false;
			hasDoubleJumped = false;
		} else {
			isJumping = true;
		}
	}
	@Override
	public void draw(Graphics g) {
		g.setColor(Color.YELLOW);
		MyGraphics.fillRectWithOutline(g, (int)getX(), (int)getY(), width, height);
		
		//visual feedback
		if (doubleJumpActiveTime != 0) {
			g.setColor(Color.CYAN);
			g.drawRect((int) getX() - 2, (int) getY() - 2, (int) width + 4, (int) height + 4);
		}
		if(Main.DEBUGGING) {
			for (int i = 0; i < closestMatrix.length; i++) {
				Tile t = closestMatrix[i];
				if(t != null) {
					g.setColor(Color.RED);
					g.drawRect((int)t.getX(), (int)t.getY(), t.getSize(), t.getSize());
				}
			}
		}
		
		hitbox.draw(g);
	}
}