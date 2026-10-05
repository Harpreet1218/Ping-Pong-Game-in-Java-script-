import java.awt.*;

import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;



public class GamePanel extends JPanel implements Runnable{



	static final int GAME_WIDTH = 1000;

	static final int GAME_HEIGHT = (int)(GAME_WIDTH * (0.5555));

	static final Dimension SCREEN_SIZE = new Dimension(GAME_WIDTH,GAME_HEIGHT);

	static final int BALL_DIAMETER = 20;

	static final int PADDLE_WIDTH = 25;

	static final int PADDLE_HEIGHT = 100;

	static final int SCORE_LIMIT = 15;

	Thread gameThread;

	Image image;

	Graphics graphics;

	Random random;

	Paddle paddle1;

	Paddle paddle2;

	Ball ball;

	Score score;

	private GameState state;
	private int winner;

	private long lastSpacePressTime =0;
	private final long DOUBLE_CLICK_THRESHOLD_MS = 500;


	private int gameOverFontSize = 10;
	private final int GAME_OVER_MAX_FONT_SIZE =120;
	private int gameOverAlpha = 0;




	public enum GameState{
		PLAYING,
		GAME_OVER
	}

	

	GamePanel(){

		newPaddles();

		newBall();

		score = new Score(GAME_WIDTH,GAME_HEIGHT);

		this.setFocusable(true);

		this.addKeyListener(new AL());

		this.setPreferredSize(SCREEN_SIZE);

		

		gameThread = new Thread(this);

		gameThread.start();

		state = GameState.PLAYING;

	}

	

	public void newBall() {

		random = new Random();

		ball = new Ball((GAME_WIDTH/2)-(BALL_DIAMETER/2),random.nextInt(GAME_HEIGHT-BALL_DIAMETER),BALL_DIAMETER,BALL_DIAMETER);

	}

	public void newPaddles() {

		paddle1 = new Paddle(0,(GAME_HEIGHT/2)-(PADDLE_HEIGHT/2),PADDLE_WIDTH,PADDLE_HEIGHT,1);

		paddle2 = new Paddle(GAME_WIDTH-PADDLE_WIDTH,(GAME_HEIGHT/2)-(PADDLE_HEIGHT/2),PADDLE_WIDTH,PADDLE_HEIGHT,2);

	}

	public void paint(Graphics g) {

		image = createImage(getWidth(),getHeight());

		graphics = image.getGraphics();

		draw(graphics);

		g.drawImage(image,0,0,this);

	}

	public void draw(Graphics g) {

		paddle1.draw(g);

		paddle2.draw(g);

		ball.draw(g);

		score.draw(g);

		if (state ==GameState.GAME_OVER) {
			// -- updating animation variables
			// run on every draw call making the numbers grow

			if(gameOverFontSize < GAME_OVER_MAX_FONT_SIZE){
				gameOverFontSize +=2;
			}

			if(gameOverAlpha<255){
				gameOverAlpha +=5;
				if(gameOverAlpha >255) gameOverAlpha =255;

			}

			String winnerMsg = "";
			Color winnerColor = Color.WHITE;

			if (winner == 1) {
        winnerMsg = "RED WINS";
        winnerColor = new Color(204, 0, 0); // A nice bright red
    } else if (winner == 2) {
        winnerMsg = "BLUE WINS";
        winnerColor = new Color(0, 0, 204); // A nice bright blue
    }
    
    // --- 3. Draw the animation! ---
    
    // Set the color, using the alpha for the fade-in effect
    g.setColor(new Color(winnerColor.getRed(), winnerColor.getGreen(), winnerColor.getBlue(), gameOverAlpha));
    
    // Set the font, using the growing font size
    g.setFont(new Font("Consolas", Font.BOLD, gameOverFontSize));
    
    // Center the text on the screen
    FontMetrics metrics = getFontMetrics(g.getFont());
    int x = (GAME_WIDTH - metrics.stringWidth(winnerMsg)) / 2;
    int y = (GAME_HEIGHT / 2) + (metrics.getAscent() / 3);
    
    g.drawString(winnerMsg, x, y);
	}

	}

	public void move() {

		paddle1.move();

		paddle2.move();

		ball.move();

	}

	public void checkCollision() {

		

		//bounce ball off top & bottom window edges

		if(ball.y <=0) {

			ball.setYDirection(-ball.yVelocity);
			playSound("../sounds/mixkit-light-impact-on-the-ground-2070.wav");

		}

		if(ball.y >= GAME_HEIGHT-BALL_DIAMETER) {

			ball.setYDirection(-ball.yVelocity);
			playSound("../sounds/mixkit-light-impact-on-the-ground-2070.wav");


		}

		//bounce ball off paddles

		if(ball.intersects(paddle1)) {

			ball.xVelocity = Math.abs(ball.xVelocity);

			ball.xVelocity++; //optional for more difficulty

			if(ball.yVelocity>0)

				ball.yVelocity++; //optional for more difficulty

			else

				ball.yVelocity--;

			ball.setXDirection(ball.xVelocity);

			ball.setYDirection(ball.yVelocity);
			playSound("../sounds/mixkit-game-ball-tap-2073.wav");


		}

		if(ball.intersects(paddle2)) {

			ball.xVelocity = Math.abs(ball.xVelocity);

			ball.xVelocity++; //optional for more difficulty

			if(ball.yVelocity>0)

				ball.yVelocity++; //optional for more difficulty

			else

				ball.yVelocity--;

			ball.setXDirection(-ball.xVelocity);

			ball.setYDirection(ball.yVelocity);
			playSound("../sounds/mixkit-game-ball-tap-2073.wav");


		}

		//stops paddles at window edges

		if(paddle1.y<=0)

			paddle1.y=0;

		if(paddle1.y >= (GAME_HEIGHT-PADDLE_HEIGHT))

			paddle1.y = GAME_HEIGHT-PADDLE_HEIGHT;

		if(paddle2.y<=0)

			paddle2.y=0;

		if(paddle2.y >= (GAME_HEIGHT-PADDLE_HEIGHT))

			paddle2.y = GAME_HEIGHT-PADDLE_HEIGHT;

		//give a player 1 point and creates new paddles & ball

		if(ball.x <=0) {

			score.player2++;
			playSound("../sounds/game-over-417465.wav");


			if(score.player2 == SCORE_LIMIT){
				state = GameState.GAME_OVER;
				winner =1;
			}

			newPaddles();

			newBall();

			System.out.println("Player 2: "+score.player2);

		}

		if(ball.x >= GAME_WIDTH-BALL_DIAMETER) {

			score.player1++;
			playSound("../sounds/game-over-417465.wav");


			if(score.player1 == SCORE_LIMIT){
				state = GameState.GAME_OVER;
				winner =2;
			}

			newPaddles();

			newBall();

			System.out.println("Player 1: "+score.player1);

		}

		}

		public void resetGame(){
			score.player1 = 0;
			score.player2= 0;

			newPaddles();
			newBall();

			gameOverAlpha=0;
			gameOverFontSize=10;

			state = GameState.PLAYING;

			
		}

	public void run() {

		//game loop

		long lastTime = System.nanoTime();

		double amountOfTicks =60.0;

		double ns = 1000000000 / amountOfTicks;

		double delta = 0;

		while(true) {

			long now = System.nanoTime();

			delta += (now -lastTime)/ns;

			lastTime = now;

			if(delta >=1) {

				if (state ==GameState.PLAYING){

				move();

				checkCollision();

				}

				repaint();

				delta--;

			}

		}

	}

	public class AL extends KeyAdapter{
		public void keyPressed(KeyEvent e) {
        
        // --- THIS IS THE NEW LOGIC ---
        // Check if the Space Bar was pressed
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            
            // Only do this if the game is OVER
            if (state == GameState.GAME_OVER) {
                long currentTime = System.currentTimeMillis();
                
                // Check if this press is "close" to the last one
                if (currentTime - lastSpacePressTime < DOUBLE_CLICK_THRESHOLD_MS) {
                    // This is a DOUBLE-CLICK!
                    resetGame(); // Call our new reset method
                    lastSpacePressTime = 0; // Reset the timer
                } else {
                    // This is just the FIRST click
                    lastSpacePressTime = currentTime;
                }
            }
        } else {
            // --- END OF NEW LOGIC ---
            
            // This is your old code. We put it in an 'else' block
            // so the paddles don't move when we press space.
            paddle1.keyPressed(e);
            paddle2.keyPressed(e);
        }
    }

    public void keyReleased(KeyEvent e) {
        // This method does not need to change
        paddle1.keyReleased(e);
        paddle2.keyReleased(e);
    }
}


	public void playSound(String soundFilePath){
		try{
			File soundFile = new File(soundFilePath);
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
			Clip clip = AudioSystem.getClip();
			clip.open(audioIn);
			clip.start();
			} catch (Exception e){
				e.printStackTrace();
				System.out.println("Error: Could not play sound file"+ soundFilePath);
			}





	}

}