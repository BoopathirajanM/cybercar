import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public class CyberCar extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // image
    Image backgroundImg;
    Image carImg;
    Image topPipeImg;
    Image bottomPipeImg;
    Image gamestartImg;
    Image gameoverImg;

    // car
    int carX = boardWidth / 8;
    int carY = boardHeight / 2;
    int carWidth = 50;
    int carHeight = 40;

    class Car {
        int x = carX;
        int y = carY;
        int Width = carWidth;
        int Heigth = carHeight;
        Image img;

        Car(Image img) {
            this.img = img;
        }
    }

    // pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64; // scaled by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int Width = pipeWidth;
        int Heigth = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // game logic
    Car car;
    int velocityX = -4; // move pipe to the left speed
    int velocityY = 0; // move car up/down speed
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gamestarted = false;
    double score = 0;

    boolean gameOver = false; 

    private double highestScore = 0;

    private Clip musicClip;

    // constructor
    CyberCar() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.BLUE);
        setFocusable(true);
        addKeyListener(this);

        // load images
        backgroundImg = new ImageIcon(getClass().getResource("./background.png")).getImage();
        carImg = new ImageIcon(getClass().getResource("./flyingcar.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();
        gamestartImg = new ImageIcon(getClass().getResource("./gamestart.png")).getImage();
        gameoverImg = new ImageIcon(getClass().getResource("./gameover.png")).getImage();

        // car
        car = new Car(carImg);
        pipes = new ArrayList<Pipe>();

        // place pipes timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        //placePipesTimer.start();

        // game timer
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();

        // Start the background music
        playMusic();
    }

    
    // New method to handle playing a sound file
    private void playMusic() {
        try {
            File musicPath = new File("./BujjiTheme.wav");
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                
                // If a clip already exists, stop and close it before creating a new one
                if (musicClip != null) {
                    musicClip.stop();
                    musicClip.close();
                }

                musicClip = AudioSystem.getClip();
                musicClip.open(audioInput);
                musicClip.start();
                musicClip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the music indefinitely
            } else {
                System.out.println("Can't find the audio file: ./game_music.wav");
            }
        } catch (UnsupportedAudioFileException | LineUnavailableException | java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void placePipes() {

        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {

        // background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // car
        g.drawImage(car.img, car.x, car.y, car.Width, car.Heigth, null);

        // pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.Width, pipe.Heigth, null);
        }
        // score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 28));

        if(!gamestarted){
            //game start
            g.drawImage(gamestartImg, 20, 150,320,320, null);
        } else if (gameOver) {
            g.drawString("Your Score:" + String.valueOf((int) score), 85, 130);
            //game over
            g.drawImage(gameoverImg, 20, 150, 320,320, null);
        } else {
            g.drawString("Score:" + String.valueOf((int) score), 10, 65);
            g.drawString("Highest Score:" + String.valueOf((int)highestScore), 10, 35);
        }
    }

    public void move() {
        if(!gamestarted || gameOver){
            return;
        }

        // car
        velocityY += gravity;
        car.y += velocityY;
        car.y = Math.max(car.y, 0);

        // pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && car.x > pipe.x + pipe.Width) {
                pipe.passed = true;
                score += 0.5; // there are 2 pipes 0.5 * 2 = 1 point
            }

            if (collision(car, pipe)) {
                gameOver = true;
            }
        }

        if (car.y > boardHeight) {
            gameOver = true;
        }
    }

    public boolean collision(Car a, Pipe b) {
        return a.x < b.x + b.Width && // a's top left corner does n't reach b's top right corner
                a.x + a.Width > b.x && // a's top right corner passed b's top left corner
                a.y < b.y + b.Heigth && // a's top left corner does n't reach b's bottom left corner
                a.y + a.Heigth > b.y; // a's bottom left corner passed b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
            if(musicClip != null){
                musicClip.stop();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if(!gamestarted){
                gamestarted = true;
                gameLoop.start();
                placePipesTimer.start();
                if(musicClip != null){
                    musicClip.setFramePosition(0);
                    musicClip.start();
                }
            }

            velocityY = -9;

            if (gameOver) {
                if(score > highestScore){
                    highestScore = score;
                }
                // restart
                car.y = carY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gamestarted = false;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}