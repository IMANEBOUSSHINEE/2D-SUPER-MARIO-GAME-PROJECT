package com.TETOSOFT.tilegame;

import java.awt.*;
import java.util.Iterator;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.TETOSOFT.graphics.*;
import com.TETOSOFT.input.*;
import com.TETOSOFT.test.GameCore;
import com.TETOSOFT.tilegame.sprites.*;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

/**
 * GameManager manages all parts of the game.
 */
public class GameEngine extends GameCore {

	public static void main(String[] args) {

		GameEngine game = new GameEngine();
		game.init();
		while ( start == false ) {
			game.showSplashScreen(); 
		}
		game.run();
		
		
		

	}

	public static final float GRAVITY = 0.002f;

	private Point pointCache = new Point();
	private TileMap map;
	private MapLoader mapLoader;
	private InputManager inputManager;
	private TileMapDrawer drawer;
	private SoundManager soundManager;

	private GameAction moveLeft;
	private GameAction moveRight;
	private GameAction jump;
	private GameAction exit;
	private GameAction enter;
	private int collectedStars = 0;
	private int numLives = 3;

	private boolean pause = false;
	private static boolean start = false ;

	public void init() {
		super.init();

		// set up input manager
		initInput();

		// start resource manager
		mapLoader = new MapLoader(screen.getFullScreenWindow().getGraphicsConfiguration());

		// load resources
		drawer = new TileMapDrawer();

		drawer.setBackground(mapLoader.loadImage("background.jpg"));

		// load first map
		map = mapLoader.loadNextMap();
		try {
			soundManager = new SoundManager();
			soundManager.sound("audio/background.wav");
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Closes any resources used by the GameManager.
	 */
	public void stop() {
		super.stop();

	}

	private void initInput() {
		moveLeft = new GameAction("moveLeft");
		moveRight = new GameAction("moveRight");
		jump = new GameAction("jump", GameAction.DETECT_INITAL_PRESS_ONLY);
		exit = new GameAction("exit", GameAction.DETECT_INITAL_PRESS_ONLY);

		enter = new GameAction("enter");

		inputManager = new InputManager(screen.getFullScreenWindow());
		inputManager.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
		inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
		inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
		inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
		inputManager.mapToKey(enter, KeyEvent.VK_ENTER);


	}
	
	
	public void showSplashScreen() {

		JFrame  frame = screen.getFullScreenWindow();
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		JLabel background = new JLabel();
		
		
		JButton begin = new JButton("Start the game");
		JButton quit = new JButton("Quit");
	
		ActionListener beginListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				begin.setVisible(false);
				background.setVisible(false);
				quit.setVisible(false);
				start = true;
			}
		};
		
		ActionListener quitListener = new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
		        System.exit(0);
			
			}
		};
	
		
		Icon icon = new ImageIcon("images/splashscreen.png");
		
		background.setIcon(icon);
	
		background.setSize(icon.getIconWidth(), icon.getIconHeight());
		background.setFocusable(true);
		
		
		begin.setSize(150, 70);
		begin.setFont(new Font(Font.SANS_SERIF, StyleConstants.ALIGN_CENTER, 16));
		begin.setFocusable(false);
		begin.setLocation(screen.getWidth() /2-100 , screen.getHeight() /2-70);
		begin.setBackground(Color.DARK_GRAY);
		begin.setForeground(Color.white);
		
		begin.addActionListener(beginListener);
		
		
		
		quit.setSize(150, 70);
		quit.setFont(new Font(Font.SANS_SERIF, StyleConstants.ALIGN_CENTER, 16));
		quit.setFocusable(false);
		quit.setLocation(screen.getWidth() /2 - 100 , screen.getHeight() /2 + 10);
		quit.setBackground(Color.DARK_GRAY);
		quit.setForeground(Color.white);
		
		quit.addActionListener(quitListener);
		
		background.add(quit);
		background.add(begin);
		frame.add(panel);
		
		panel.add(background);
		
		frame.setVisible(true);
	}

	private void setScore(int score) {
	
		try {
			
			 FileWriter fw = new FileWriter("scores.txt", true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    bw.write(new String(score + ""));
			    bw.newLine();
			    bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void checkInput(long elapsedTime) {

		if (exit.isPressed()) {
			stop();
		}

		Player player = (Player) map.getPlayer();
		if (player.isAlive()) {
			float velocityX = 0;
			if (moveLeft.isPressed()) {
				velocityX -= player.getMaxSpeed();
			}
			if (moveRight.isPressed()) {
				velocityX += player.getMaxSpeed();
			}
			if (jump.isPressed()) {
				player.jump(false);
			}
			player.setVelocityX(velocityX);
		} else {

			if (enter.isPressed()) {
				map = mapLoader.reloadMap();
			}
		}

	}

	ActionListener exitListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			setScore(collectedStars);
			stop();
		}
	};

	ActionListener pauseListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
		
			pause = true;
		}
	};

	ActionListener resumeListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			pause = false;
		}
	};

	public void draw(Graphics2D g) {

		if (g != null) {

			Creature player = (Creature) map.getPlayer();
			drawer.draw(g, map, screen.getWidth(), screen.getHeight());

			JFrame frame = screen.getFullScreenWindow();

			Icon exitIcon = new ImageIcon("images/exit.png");
			JButton exit = new JButton(exitIcon);
			exit.setSize(40, 40);
			exit.setBackground(Color.WHITE);
			exit.setOpaque(true);
			exit.setIgnoreRepaint(true);
			exit.setFocusable(false);
			exit.setLocation(50, 20);
			exit.addActionListener(exitListener);

			Icon pauseIcon = new ImageIcon("images/pause.png");
			JButton pause = new JButton(pauseIcon);
			pause.setSize(40, 40);
			pause.setBackground(Color.WHITE);
			pause.setOpaque(true);
			pause.setIgnoreRepaint(true);
			pause.setFocusable(false);
			pause.setLocation(120, 20);
			pause.addActionListener(pauseListener);

			Icon resumeIcon = new ImageIcon("images/resume.png");
			JButton resume = new JButton(resumeIcon);
			resume.setSize(40, 40);
			resume.setBackground(Color.WHITE);
			resume.setOpaque(true);
			resume.setIgnoreRepaint(true);
			resume.setFocusable(false);
			resume.setLocation(190, 20);
			resume.addActionListener(resumeListener);

			frame.add(exit);
			frame.add(pause);
			frame.add(resume);
			frame.getContentPane().paintComponents(g);

			g.setColor(Color.WHITE);
			g.drawString("Press ESC for EXIT.", 10.0f, 100.0f);
			g.setColor(Color.GREEN);

			g.drawString("Coins: " + collectedStars, 300.0f, 100.0f);
			g.setColor(Color.YELLOW);
			g.drawString("Lives: " + numLives, 500.0f, 100.0f);
			g.setColor(Color.WHITE);
			g.drawString("Home: " + mapLoader.currentMap, 700.0f, 100.0f);

			if (player.getState() == Creature.STATE_DEAD) {

				if (numLives == 0) {
					Image transparentImage = loadImage("images/loser.png");
					g.drawImage(transparentImage, screen.getWidth() / 2, screen.getHeight() / 2 - 100, null);
					g.setColor(Color.blue);
					g.drawString("Your score : " + collectedStars, screen.getWidth() / 2, screen.getHeight() / 2 + 20);
					g.drawString("Press Enter to try again", screen.getWidth() / 2, screen.getHeight() / 2 + 40);
					if ( enter.isPressed()) {
						numLives = 3;
					}
				} else {
					map = mapLoader.reloadMap();
				}

			}
		} else {
			mapLoader = null;
		}

	}

	/*
	 * public void drawImage(Graphics g, Image image, int x, int y, String caption)
	 * { g.drawImage(image, x, y, null); g.drawString(caption, x + 5, y + FONT_SIZE
	 * + image.getHeight(null)); }
	 */

	/**
	 * Gets the current map.
	 */
	public TileMap getMap() {
		return map;
	}

	/**
	 * Gets the tile that a Sprites collides with. Only the Sprite's X or Y should
	 * be changed, not both. Returns null if no collision is detected.
	 */
	public Point getTileCollision(Sprite sprite, float newX, float newY) {
		float fromX = Math.min(sprite.getX(), newX);
		float fromY = Math.min(sprite.getY(), newY);
		float toX = Math.max(sprite.getX(), newX);
		float toY = Math.max(sprite.getY(), newY);

		// get the tile locations
		int fromTileX = TileMapDrawer.pixelsToTiles(fromX);
		int fromTileY = TileMapDrawer.pixelsToTiles(fromY);
		int toTileX = TileMapDrawer.pixelsToTiles(toX + sprite.getWidth() - 1);
		int toTileY = TileMapDrawer.pixelsToTiles(toY + sprite.getHeight() - 1);

		// check each tile for a collision
		for (int x = fromTileX; x <= toTileX; x++) {
			for (int y = fromTileY; y <= toTileY; y++) {
				if (x < 0 || x >= map.getWidth() || map.getTile(x, y) != null) {
					// collision found, return the tile
					pointCache.setLocation(x, y);
					return pointCache;
				}
			}
		}

		// no collision found
		return null;
	}

	/**
	 * Checks if two Sprites collide with one another. Returns false if the two
	 * Sprites are the same. Returns false if one of the Sprites is a Creature that
	 * is not alive.
	 */
	public boolean isCollision(Sprite s1, Sprite s2) {
		// if the Sprites are the same, return false
		if (s1 == s2) {
			return false;
		}

		// if one of the Sprites is a dead Creature, return false
		if (s1 instanceof Creature && !((Creature) s1).isAlive()) {
			return false;
		}
		if (s2 instanceof Creature && !((Creature) s2).isAlive()) {
			return false;
		}

		// get the pixel location of the Sprites
		int s1x = Math.round(s1.getX());
		int s1y = Math.round(s1.getY());
		int s2x = Math.round(s2.getX());
		int s2y = Math.round(s2.getY());

		// check if the two sprites' boundaries intersect
		return (s1x < s2x + s2.getWidth() && s2x < s1x + s1.getWidth() && s1y < s2y + s2.getHeight()
				&& s2y < s1y + s1.getHeight());
	}

	/**
	 * Gets the Sprite that collides with the specified Sprite, or null if no Sprite
	 * collides with the specified Sprite.
	 */
	public Sprite getSpriteCollision(Sprite sprite) {

		// run through the list of Sprites
		Iterator i = map.getSprites();
		while (i.hasNext()) {
			Sprite otherSprite = (Sprite) i.next();
			if (isCollision(sprite, otherSprite)) {
				// collision found, return the Sprite
				return otherSprite;
			}
		}

		// no collision found
		return null;
	}

	/**
	 * Updates Animation, position, and velocity of all Sprites in the current map.
	 */
	public void update(long elapsedTime) {
		Creature player = (Creature) map.getPlayer();

	
		if (pause == false) {
			// player is dead! start map over
			if (player.getState() == Creature.STATE_DEAD) {
				// map = mapLoader.reloadMap();
				// return;
			}
			// get keyboard/mouse input
			checkInput(elapsedTime);
			// update player
			updateCreature(player, elapsedTime);
			player.update(elapsedTime);
			// update other sprites
			Iterator i = map.getSprites();
			while (i.hasNext()) {
				Sprite sprite = (Sprite) i.next();
				if (sprite instanceof Creature) {
					Creature creature = (Creature) sprite;
					if (creature.getState() == Creature.STATE_DEAD) {
						i.remove();
					} else {
						updateCreature(creature, elapsedTime);
					}
				}
				// normal update
				sprite.update(elapsedTime);
			}

		} else {
			if (player.getState() == Creature.STATE_DEAD) {
				// map = mapLoader.reloadMap();

				// return;
			}

			// get keyboard/mouse input
			checkInput(elapsedTime);

			// update player
			updateCreature(player, 0);
			player.update(0);

			// update other sprites
			Iterator i = map.getSprites();
			while (i.hasNext()) {
				Sprite sprite = (Sprite) i.next();
				if (sprite instanceof Creature) {
					Creature creature = (Creature) sprite;
					if (creature.getState() == Creature.STATE_DEAD) {
						i.remove();
					} else {
						updateCreature(creature, 0);
					}
				}
				// normal update
				sprite.update(0);
			}
		}
		
		
	}

	/**
	 * Updates the creature, applying gravity for creatures that aren't flying, and
	 * checks collisions.
	 */
	private void updateCreature(Creature creature, long elapsedTime) {

		// apply gravity
		if (!creature.isFlying()) {
			creature.setVelocityY(creature.getVelocityY() + GRAVITY * elapsedTime);
		}

		// change x
		float dx = creature.getVelocityX();
		float oldX = creature.getX();
		float newX = oldX + dx * elapsedTime;
		Point tile = getTileCollision(creature, newX, creature.getY());
		if (tile == null) {
			creature.setX(newX);
		} else {
			// line up with the tile boundary
			if (dx > 0) {
				creature.setX(TileMapDrawer.tilesToPixels(tile.x) - creature.getWidth());
			} else if (dx < 0) {
				creature.setX(TileMapDrawer.tilesToPixels(tile.x + 1));
			}
			creature.collideHorizontal();
		}
		if (creature instanceof Player) {
			checkPlayerCollision((Player) creature, false);
		}

		// change y
		float dy = creature.getVelocityY();
		float oldY = creature.getY();
		float newY = oldY + dy * elapsedTime;
		tile = getTileCollision(creature, creature.getX(), newY);
		if (tile == null) {
			creature.setY(newY);
		} else {
			// line up with the tile boundary
			if (dy > 0) {
				creature.setY(TileMapDrawer.tilesToPixels(tile.y) - creature.getHeight());
			} else if (dy < 0) {
				creature.setY(TileMapDrawer.tilesToPixels(tile.y + 1));
			}
			creature.collideVertical();
		}
		if (creature instanceof Player) {
			boolean canKill = (oldY < creature.getY());
			checkPlayerCollision((Player) creature, canKill);
		}

	}

	/**
	 * Checks for Player collision with other Sprites. If canKill is true,
	 * collisions with Creatures will kill them.
	 */
	public void checkPlayerCollision(Player player, boolean canKill) {
		if (!player.isAlive()) {

			return;
		}

		// check for player collision with other sprites
		Sprite collisionSprite = getSpriteCollision(player);
		if (collisionSprite instanceof PowerUp) {
			acquirePowerUp((PowerUp) collisionSprite);
		} else if (collisionSprite instanceof Creature) {
			Creature badguy = (Creature) collisionSprite;
			if (canKill) {
				try {
					soundManager.sound("audio/NPC_Death.wav");;
				} catch (UnsupportedAudioFileException e) {
				} catch (IOException e) {} catch (LineUnavailableException e) {};



				// kill the badguy and make player bounce
				badguy.setState(Creature.STATE_DYING);
				player.setY(badguy.getY() - player.getHeight());
				player.jump(true);
			} else {
				// player dies!
				player.setState(Creature.STATE_DYING);
				numLives--;
				if (numLives == 0) {
					for (int x = 0; x <= 6; x++)
						soundManager.stop();
					try {
						soundManager.sound("audio/Game_Over.wav");
					} catch (UnsupportedAudioFileException e) {
					} catch (IOException e) {
					} catch (LineUnavailableException e) {
					}
					try {
						Thread.sleep(3000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
					// stop();
				}
				else
					try {
						soundManager.sound("audio/1&2Death.wav");
					} catch (UnsupportedAudioFileException e) {
					} catch (IOException e) {
					} catch (LineUnavailableException e) {
					}
			}
		}
	}

	/**
	 * Gives the player the specified power up and removes it from the map.
	 */
	public void acquirePowerUp(PowerUp powerUp) {
		// remove it from the map
		map.removeSprite(powerUp);

		if (powerUp instanceof PowerUp.Star) {
			try {
				soundManager.sound("audio/Coin.wav");;
			} catch (UnsupportedAudioFileException e) {
			} catch (IOException e) {} catch (LineUnavailableException e) {};
			// do something here, like give the player points
			collectedStars++;
			if (collectedStars == 100) {
				numLives++;
				collectedStars = 0;
			}

		} else if (powerUp instanceof PowerUp.Music) {
			// change the music

		} else if (powerUp instanceof PowerUp.Goal) {
			// advance to next map

			map = mapLoader.loadNextMap();

		}
	}

}