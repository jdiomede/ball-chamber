import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.Random;

public class BallChamber extends JPanel implements ActionListener,Runnable{

    private JButton start = new JButton("start");
    private JButton pause = new JButton("pause");
    private JButton stop = new JButton("stop");
    private JButton addBall = new JButton("add thread");

	public boolean threadPause = true;
	private boolean threadStop = false;
    private Thread displayThread = null;
    public ArrayList<ThreadedBalls> workerThreads = new ArrayList<ThreadedBalls>();

    public BallQueue ballQueue = null;
    public ArrayList<ThreadBallPair> balls = null;

    public int localCount = 0;
    public int sameColor = 0;
    public int bothSmall = 0;
    public int bothMedium = 0;
    public int bothLarge = 0;
    public int annihilation = 0;
    public int escaped = 0;

    private int height = 400;
    private int width = 600;

    private int maxLimitOfBalls = 100;

    public Image buffer = null;

    private boolean debounceAdd = false;
    private int debounceCount = 0;

	public int arb = 100;
	public int sizeH = 30;
	public int origX = 0;
	public int origY = 0;
	public int break1 = 0;
	public int break2 = 0;

	private JLabel result = new JLabel("balls in play: "+localCount);
    private JLabel color = new JLabel("balls of same color, different size: "+sameColor);
    private JLabel small = new JLabel("both balls small, different color: "+sameColor);
    private JLabel medium = new JLabel("both balls medium, different color: "+sameColor);
    private JLabel large = new JLabel("both balls large, different color: "+sameColor);
    private JLabel ann = new JLabel("annihilation of smaller ball: "+sameColor);
    private JLabel esc = new JLabel("escaped balls: ");
    private JLabel load = new JLabel("thread count: 0, balls in each thread: 0");

	public void addBall(int newSize, Color newColor){
		if(workerThreads.size() > 0){
			ThreadedBalls lowestCountThread = workerThreads.get(0);
			int lowestCount = lowestCountThread.getNumberOfBalls();
			for (int i = 0;i < workerThreads.size(); i++){
		    	if(workerThreads.get(i).getNumberOfBalls() < lowestCount){
		    		lowestCountThread = workerThreads.get(i);
		    	}
		    }


		    if(lowestCount > 10 && workerThreads.size() <= 25){
		    	debounceCount++;
		    	if(debounceCount >= 20){
		    		debounceAdd = false;
		    	}
		    	if(!debounceAdd){
			    	ThreadedBalls temp = new ThreadedBalls(buffer.getGraphics(), this, this.getBackground(), width, height, false);

					temp.start();
					workerThreads.add(temp);
			    	lowestCountThread = temp;
			    	debounceAdd = true;
			    	debounceCount = 0;
		    	}
		    }

		    lowestCountThread.addBall(newSize, newColor);
		}
	}

    private void collisionDetectionWithBalls(Ball b, ThreadedBalls t){
    	for(int j = 0;j<balls.size();j++){
    		Ball c = balls.get(j).pBall;
    		if(b != c){
    			//double distance = Math.sqrt(Math.pow((b.centerPointX-c.centerPointX),2)+Math.pow((b.centerPointY-c.centerPointY),2));
				//if(distance <= (b.radius+c.radius+b.velocityVect) || distance <= (b.radius+c.radius+c.velocityVect)){
				if(Math.abs(b.centerPointX-c.centerPointX) <= (b.sizeX/2+c.sizeX/2)){
					if(Math.abs(b.centerPointY-c.centerPointY) <= (b.sizeY/2+c.sizeY/2)){

						t.collideBalls(b,c);

					}
				}
    		}
    	}
    }

   	public void paint(Graphics chamber){
   		super.paint(chamber);

		if(!threadStop){
	   		if(buffer != null){
				//chamber.drawImage(buffer, 0,0, null);
				if(ballQueue != null){
					balls = ballQueue.getBalls();
					if(balls != null){
						localCount = balls.size();
						if(localCount > 0){
							for(int i = 0; i<balls.size();i++){
								Ball b = balls.get(i).pBall;
								ThreadedBalls t = balls.get(i).pThread;
								//if(b.collided == false){
									collisionDetectionWithBalls(b, t);
								//}
							}

							for(int i = 0; i<balls.size();i++){
								Ball b = balls.get(i).pBall;
								chamber.setColor(b.shade);
								chamber.fillArc(b.positionX,b.positionY,b.sizeX,b.sizeY, 0, 360);
								//b.collided = false;
							}

							if(balls.size() > maxLimitOfBalls){
								for (int i = 0;i < workerThreads.size(); i++){
							    	workerThreads.get(i).maxLimitReached = true;
							    }
							}
							else if(balls.size() < maxLimitOfBalls){
								for (int i = 0;i < workerThreads.size(); i++){
							    	workerThreads.get(i).maxLimitReached = false;
							    }
							}
							balls.clear();
							ballQueue.donePainting();
						}
					}
				}
	   		}
	   		else{
	   			buffer = createImage(getWidth(), getHeight());
	   		}
		}

		origX = (getWidth()/2)-width/2;
		origY = (getHeight()/2)-height/2;
		break1 = origX+arb;
		break2 = break1+sizeH;

		chamber.setColor(Color.BLACK);
   		chamber.drawRect(origX,origY,width,height);

   		chamber.setColor(this.getBackground());
   		chamber.drawLine(break1+1,origY,break2,origY);



   		try{
			Thread.sleep(1);
        }catch(InterruptedException ie){}

		repaint();
   	}

    private void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("Ball Chamber");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container cp = frame.getContentPane();
		cp.setLayout(new BorderLayout());

		this.setLayout(new FlowLayout());
		cp.add(this, BorderLayout.CENTER);

		JPanel resultsSuper = new JPanel();
		resultsSuper.setLayout(new GridLayout(2,1));

		JPanel threads = new JPanel();
		threads.setLayout(new FlowLayout());

		JPanel control = new JPanel();
		control.setLayout(new FlowLayout());
		control.add(start);
		control.add(pause);
		control.add(stop);
		control.add(addBall);

		JPanel results = new JPanel();
		results.setLayout(new GridLayout(7,1));

		results.add(result);
		results.add(color);
		results.add(small);
		results.add(medium);
		results.add(large);
		results.add(ann);
		results.add(esc);

		control.add(results);

		threads.add(load);

		resultsSuper.add(control);
		resultsSuper.add(threads);

		cp.add(resultsSuper, BorderLayout.SOUTH);

		start.addActionListener(this);
		pause.addActionListener(this);
		stop.addActionListener(this);
		addBall.addActionListener(this);

		//Display the window.
		frame.setSize(800,700);
		frame.setVisible(true);

		this.addComponentListener(new ComponentListener(){
		    public void componentResized(ComponentEvent evt) {
		    	//System.out.println("resized!");
		    	BallChamber c = (BallChamber)evt.getSource();
		    	for (int i = 0;i < c.workerThreads.size(); i++){
			    	c.workerThreads.get(i).updateWindowSize(c, width, height);
			    }
		    }
		    public void componentHidden(ComponentEvent evt){
		    }
		    public void componentShown(ComponentEvent evt){
		    }
		    public void componentMoved(ComponentEvent evt){
		    }
		});
    }

    public void actionPerformed(ActionEvent ae){
		if (ae.getSource() == start){
			for(int t=0;t<workerThreads.size();t++){
				ThreadedBalls temp = workerThreads.get(t);
				//System.out.println(temp.getState());
			}
			if(threadStop || displayThread == null){
				threadStop = false;
				threadPause = false;

				//repaint();

				ballQueue = new BallQueue(this);

				displayThread = new Thread(this);
			    displayThread.start();
			    ballQueue.updateLocalCount(2);
				if(buffer != null){
				    workerThreads.add(new ThreadedBalls(buffer.getGraphics(), this, this.getBackground(), width, height, true));
				    workerThreads.add(new ThreadedBalls(buffer.getGraphics(), this, this.getBackground(), width, height, true));
				    //workerThreads.add(new ThreadedBalls(buffer.getGraphics(), this, this.getBackground(), width, height, true));
				}
			    for (int i = 0;i < workerThreads.size(); i++){
			    	workerThreads.get(i).start();
			    }
			    //ballQueue.localCount = workerThreads.size();

			    //System.out.println("stop: "+threadStop+" pause: "+threadPause);
			}
			else if(threadPause){
				threadPause = false;
				for (int i = 0;i < workerThreads.size(); i++){
			    	workerThreads.get(i).togglePauseBalls();
			    }

			    //System.out.println("stop: "+threadStop+" pause: "+threadPause);
			}
		}
		else if (ae.getSource() == pause){
			if(displayThread != null && !threadPause){
				threadPause = true;

				for (int i = 0;i < workerThreads.size(); i++){
			    	workerThreads.get(i).togglePauseBalls();
			    }

			    //System.out.println("stop: "+threadStop+" pause: "+threadPause);
			}

		}
		else if (ae.getSource() == stop){
		    if(displayThread != null && !threadStop){
		    	threadStop = true;
		    	//try{
		    		displayThread.interrupt();
			    	//displayThread.join();
		    	//}
		    	//catch(InterruptedException ie){System.out.println(ie);}

			    for (int i = 0;i < workerThreads.size(); i++){
			    	//try{
			    		workerThreads.get(i).interrupt();
			    		//workerThreads.get(i).join();
			    	//}
			    	//catch(InterruptedException ie){System.out.println(ie);}
			    }
				workerThreads.clear();
			    //give worker thread some time to exit before reset localCount
			    try{
					Thread.sleep(50);
				}
				catch(InterruptedException ie){}
			    localCount = 0;
			    sameColor = 0;
			    bothSmall = 0;
			    bothMedium = 0;
			    bothLarge = 0;
			    annihilation = 0;
			    escaped = 0;

			    //System.out.println("stop: "+threadStop+" pause: "+threadPause);
			}

		}
		else if (ae.getSource() == addBall){
			if(!threadPause && !threadStop){
				ThreadedBalls temp = new ThreadedBalls(buffer.getGraphics(), this, this.getBackground(), width, height, true);

					temp.start();
				workerThreads.add(temp);
				ballQueue.updateLocalCount(1);

				//System.out.println("stop: "+threadStop+" pause: "+threadPause);
			}
		}
    }

    public void stop(){
    	 if(displayThread != null){
    	 	try{
	    		displayThread.interrupt();
		    	displayThread.join();
	    	}
	    	catch(InterruptedException ie){System.out.println(ie);}

		    for (int i = 0;i < workerThreads.size(); i++){
		    	try{
		    		workerThreads.get(i).interrupt();
		    		workerThreads.get(i).join();
		    	}
		    	catch(InterruptedException ie){System.out.println(ie);}
		    }
			workerThreads.clear();
		    //give worker thread some time to exit before reset localCount
		    try{
				Thread.sleep(50);
			}
			catch(InterruptedException ie){}

		    localCount = 0;
		    sameColor = 0;
		    bothSmall = 0;
		    bothMedium = 0;
		    bothLarge = 0;
		    annihilation = 0;

		    balls.clear();
			ballQueue.donePainting();

		}
    }

    public void run(){
		threadStop = false;
		while(true){
			if (displayThread.isInterrupted()){
				threadStop = true;
			}
			try{
				Thread.sleep(100);
			}
			catch(InterruptedException ie){
				//System.out.println(ie);
				//System.out.println(displayThread.getState());
				threadStop = true;
			}

			result.setText("balls in play: "+localCount);
		    color.setText("balls of same color, different size: "+sameColor);
		    small.setText("both balls small, different color: "+bothSmall);
		    medium.setText("both balls medium, different color: "+bothMedium);
		    large.setText("both balls large, different color: "+bothLarge);
		    ann.setText("annihilation of smaller ball: "+annihilation);
		    esc.setText("escaped balls: "+escaped);
			StringBuffer temp = new StringBuffer("thread count: ");

			temp.append(workerThreads.size()+", ");

			temp.append("balls in each thread");

			if(workerThreads.size() == 0){
				temp.append(": 0");
			}
			else{
				for (int i = 0;i < workerThreads.size(); i++){
					if(i == 0){
						temp.append(": ");
					}
					else{
						temp.append(", ");
					}
					temp.append(workerThreads.get(i).getNumberOfBalls());
				}
			}

		    load.setText(temp.toString());

			if(threadStop){
				//pause until interrupted
				return;
			}
		}
    }

    BallChamber(){
    	super();
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    new BallChamber().createAndShowGUI();
			}
		});
    }
}

class ThreadedBalls extends Thread {
	private Graphics chamber = null;
	private BallChamber view = null;
	private Color background;

	static final int SMALL = 0;
	static final int MEDIUM = 1;
	static final int LARGE = 2;

	static final int NOP = 3;

	private int uBoundY;
	private int lBoundY;
	private int lBoundX;
	private int rBoundX;

	//private ThreadBallPair paired = new ThreadBallPair(this, null);
	private ArrayList<ThreadBallPair> pairs = new ArrayList<ThreadBallPair>();
	private ArrayList<Ball> balls = new ArrayList<Ball>();
	private ArrayList<Ball> remove = new ArrayList<Ball>();

	private boolean threadStop = false;

	public boolean maxLimitReached = false;

	public void updateWindowSize(BallChamber view, int chamberWidth, int chamberHeight){
		this.view = view;

		uBoundY = (view.getHeight()/2)-chamberHeight/2;
		lBoundY = (view.getHeight()/2)+chamberHeight/2;
		lBoundX = (view.getWidth()/2)-chamberWidth/2;
		rBoundX = (view.getWidth()/2)+chamberWidth/2;
	}

	public int getNumberOfBalls(){
		return balls.size();
	}

	public void collideBalls(Ball b, Ball c){

		b.collided = true;
		b.debounce = true;

		//validate that this thread owns this ball
		if(balls.contains(b) && !b.wallBall){
			if(b.shade == c.shade){
				if(c.debounce != true && !view.threadPause){
					view.sameColor += 1;
				}
			}
			else if((b.size == c.size) && (b.shade != c.shade)){
				if(b.size == SMALL){
					//1 new small red ball is created
					if(c.debounce != true && !view.threadPause){
						view.bothSmall += 1;
						//addBall = SMALL;
						//System.out.println("ball: "+b+" is spawning a new ball");
						view.addBall(SMALL, Color.RED);
						//view.ballQueue.updateLocalCount(1);
					}
				}
				else if(b.size == MEDIUM){
					//2 new small red balls are created
					if(c.debounce != true && !view.threadPause){
						view.bothMedium += 1;
						//addBall = MEDIUM;
						view.addBall(SMALL, Color.RED);
						view.addBall(SMALL, Color.RED);
						//view.ballQueue.updateLocalCount(2);
					}
				}
				else{
					//2 new small red balls and 1 blue ball are created
					if(c.debounce != true && !view.threadPause){
						view.bothLarge += 1;
						//addBall = LARGE;
						view.addBall(SMALL, Color.RED);
						view.addBall(SMALL, Color.RED);
						view.addBall(SMALL, Color.BLUE);
						//view.ballQueue.updateLocalCount(3);
					}
				}

			}
			else{
				//the bigger ball annihilates the smaller one
				if(c.debounce != true && !view.threadPause){
					//view.annihilation += 1;
				}
				if(b.size < c.size){
					remove.add(b);
					view.annihilation += 1;
				}
			}

			//b.velocityY *= (-1);
			//b.velocityX *= (-1);

			//determine position of collision
			double distXrel = Math.abs(b.centerPointX-c.centerPointX);
			double distYrel = Math.abs(b.centerPointY-c.centerPointY);
			//double distXrel = (b.centerPointX-c.centerPointX);
			//double distYrel = (b.centerPointY-c.centerPointY);
			double thetaA = Math.atan(distXrel/distYrel);

			if(thetaA >= 42.5 || thetaA <= 47.5){
				thetaA += (10*Math.random());
			}

			//find approach of collision
			double pointOfCollisionX = 0;
			double pointOfCollisionY = 0;
			if(b.centerPointX < c.centerPointX){
				if(b.centerPointY < c.centerPointY){
					//from northwest
					//System.out.println("from NW: "+b);
					pointOfCollisionX = b.centerPointX + Math.sin(thetaA)*b.radius/2;
					pointOfCollisionY = b.centerPointY + Math.cos(thetaA)*b.radius/2;
				}
				else if(b.centerPointY > c.centerPointY){
					//from southwest
					//System.out.println("from SW: "+b);
					pointOfCollisionX = b.centerPointX + Math.sin(thetaA)*b.radius/2;
					pointOfCollisionY = b.centerPointY - Math.cos(thetaA)*b.radius/2;
				}
				else{
					//from west
					//System.out.println("from W: "+b);
					pointOfCollisionX = b.centerPointX + Math.sin(thetaA)*b.radius/2;
					pointOfCollisionY = b.centerPointY;
				}
			}
			else if(b.centerPointX > c.centerPointX){
				if(b.centerPointY < c.centerPointY){
					//from northeast
					//System.out.println("from NE: "+b);
					pointOfCollisionX = b.centerPointX - Math.sin(thetaA)*b.radius/2;
					pointOfCollisionY = b.centerPointY + Math.cos(thetaA)*b.radius/2;
				}
				else if(b.centerPointY > c.centerPointY){
					//from southeast
					//System.out.println("from SE: "+b);
					pointOfCollisionX = b.centerPointX - Math.sin(thetaA)*b.radius/2;
					pointOfCollisionY = b.centerPointY - Math.cos(thetaA)*b.radius/2;
				}
				else{
					//from east
					//System.out.println("from E: "+b);
					pointOfCollisionX = b.centerPointX - Math.sin(thetaA)*b.radius/2;
					pointOfCollisionY = b.centerPointY;
				}
			}
			else{
				if(b.centerPointY < c.centerPointY){
					//from north
					//System.out.println("from N: "+b);
					pointOfCollisionX = b.centerPointX;
					pointOfCollisionY = b.centerPointY + Math.cos(thetaA)*b.radius/2;
				}
				else if(b.centerPointY > c.centerPointY){
					//from south
					//System.out.println("from S: "+b);
					pointOfCollisionX = b.centerPointX;
					pointOfCollisionY = b.centerPointY - Math.cos(thetaA)*b.radius/2;
				}
				else{
					//bogus
					//System.out.println("from bogus: "+b);
					pointOfCollisionX = b.centerPointX;
					pointOfCollisionY = b.centerPointY;
				}

			}

			//find angle theta between plane of collision and vertical intersection
			double xPrime = (b.centerPointX-pointOfCollisionX);
			double yPrime = (b.centerPointY-pointOfCollisionY);

			double xyLength = Math.sqrt(Math.pow(xPrime,2)+Math.pow(yPrime,2));

			xPrime /= xyLength;
			yPrime /= xyLength;

			//determine velocity relative and parallel to normal plane n
			double u = -(Math.abs(b.velocityX)*xPrime+Math.abs(b.velocityY)*yPrime);
			double w = Math.sqrt(Math.pow(b.velocityX,2)+Math.pow(b.velocityY,2))-Math.abs(u);

			double tvelocityX = Math.cos(thetaA)*(w-u);
			double tvelocityY = Math.sin(thetaA)*(w-u);

			//scale velocity
			double velocityNorm = Math.sqrt(Math.pow(b.velocityDX,2)+Math.pow(b.velocityDY,2));
			double normalizeMult = velocityNorm/Math.sqrt(Math.pow(tvelocityX,2)+Math.pow(tvelocityY,2));

			if(tvelocityX > b.velocityDX || tvelocityX < -b.velocityDX){
				tvelocityX *= normalizeMult;
			}
			else if(tvelocityY > b.velocityDY || tvelocityY < -b.velocityDY){
				tvelocityY *= normalizeMult;;
			}

			//thetaA *= 180/Math.PI;
			//System.out.println("ball: "+b+" "+tvelocityX+" "+tvelocityY+" "+thetaA+" "+b.centerPointX+" "+b.centerPointY);

			b.bounce(tvelocityX,tvelocityY);

			//System.out.println("ball: "+b+" vx: "+b.velocityX+" vy: "+b.velocityY+" "+thetaF);
			//System.out.println("ball: "+b+" "+this.getState());
		}

	}

	public void collisionDetectionWithWalls(Ball b){
		int bounceX = b.sizeX+b.velocityX+1;
		int bounceY = b.sizeY+b.velocityY+1;
		b.wallBall = false;
		if(b.positionY+b.velocityY <= (uBoundY + 1)){

			if(b.positionX > view.break1-15 && (b.positionX+b.sizeX) < view.break2+15 && b.size == SMALL){
				//let ball escape
				//remove.add(b);
			}
			else{
				b.wallBall = true;
				b.velocityY *= (-1);
			}
		}
		else if(b.positionY+b.velocityY >= (lBoundY - bounceY)){
			b.wallBall = true;
			b.velocityY *= (-1);
		}

		if(b.positionX+b.velocityX <= (lBoundX + 1)){
			b.wallBall = true;
			b.velocityX *= (-1);
		}
		else if(b.positionX+b.velocityX >= (rBoundX - bounceX)){
			b.wallBall = true;
			b.velocityX *= (-1);
		}

		if(b.positionY < uBoundY){
			remove.add(b);
			view.escaped += 1;
		}
	}

	public void addBall(int newSize, Color newColor){
		if(!maxLimitReached){
			Random randomGenerator = new Random();
	      	int randomInt = randomGenerator.nextInt(100);
	      	int randomDir = randomGenerator.nextInt(4);

			int startX = 0;
	    	int startY = 0;

			switch(randomDir){
				case 0:
					startX = (view.getWidth()/2)+randomInt;
	    			startY = (view.getHeight()/2)+randomInt;
				break;
				case 1:
					startX = (view.getWidth()/2)+randomInt;
	    			startY = (view.getHeight()/2)-randomInt;
				break;
				case 2:
					startX = (view.getWidth()/2)-randomInt;
	    			startY = (view.getHeight()/2)+randomInt;
				break;
				case 3:
					startX = (view.getWidth()/2)-randomInt;
	    			startY = (view.getHeight()/2)-randomInt;
				break;
			}

			this.balls.add(new Ball(newSize, newColor, startX, startY));
			pairs.add(new ThreadBallPair(this, null));
			view.ballQueue.updateLocalCount(1);
		}
	}

	public void togglePauseBalls(){
		for(int i=0;i<balls.size();i++){
			Ball b = balls.get(i);
			if(b.pause == true){
				b.pause = false;
			}
			else{
				b.pause = true;
			}
		}
	}

	public void run(){
		while(true){
			if (this.isInterrupted()){
				threadStop = true;
			}

			if(balls.size() == 0){
				threadStop = true;
				view.workerThreads.remove(this);
			}
			else{

				for(int i=0;i<balls.size();i++){
					Ball b = balls.get(i);
					ThreadBallPair temp = pairs.get(i);
					temp.pBall = b;

					boolean accel = false;
					//chamber.setColor(background);
			   		//chamber.fillArc(b.positionX,b.positionY,b.sizeX,b.sizeY, 0, 360);
			   		if(!remove.contains(b)){
			   			accel = view.ballQueue.addBalls(temp);
				   		if(accel){
				   			b.accelerate();
				   			collisionDetectionWithWalls(b);
				   		}
			   		}
			   		else{
			   			balls.remove(b);
			   			pairs.remove(temp);
						view.ballQueue.updateLocalCount(-1);
						remove.remove(b);
						i--;
						//System.out.println("threadedBall: "+balls.size());
		   			}
			   		//chamber.setColor(Color.BLACK);
			   		//chamber.fillArc(b.positionX,b.positionY,b.sizeX,b.sizeY, 0, 360);
				}
			}

	   		try{
				Thread.sleep(1);
	        }
	        catch(InterruptedException ie){
	        	//System.out.println(ie);
				//System.out.println(this.getState());
				threadStop = true;
			}

			if(threadStop){
				//pause until interrupted
				//balls.clear();
				//view.ballQueue.balls.clear();
				//view.ballQueue.donePainting();
				return;
			}
		}
    }

	ThreadedBalls(Graphics chamber, BallChamber view, Color background, int chamberWidth, int chamberHeight, boolean newBall){
    	//super();

    	//this.chamber = chamber;
    	this.chamber = view.buffer.getGraphics();
    	this.view = view;
    	this.background = background;

    	Random randomGenerator = new Random();
      	int randomInt = randomGenerator.nextInt(100);
      	int randomDir = randomGenerator.nextInt(4);
      	int randomSiz = randomGenerator.nextInt(4);
      	int randomCol = randomGenerator.nextInt(2);

		int startX = 0;
    	int startY = 0;
		switch(randomDir){
			case 0:
				startX = (view.getWidth()/2)+randomInt;
    			startY = (view.getHeight()/2)+randomInt;
			break;
			case 1:
				startX = (view.getWidth()/2)-randomInt;
    			startY = (view.getHeight()/2)+randomInt;
			break;
			case 2:
				startX = (view.getWidth()/2)+randomInt;
    			startY = (view.getHeight()/2)-randomInt;
			break;
			case 3:
				startX = (view.getWidth()/2)-randomInt;
    			startY = (view.getHeight()/2)-randomInt;
			break;
		}

		Color newColor = Color.BLACK;
		switch(randomCol){
			case 0:
				newColor = Color.RED;
			break;
			case 1:
				newColor = Color.BLUE;
			break;
		}

		if(newBall){
			switch(randomSiz){
				case 0:
				case 1:
					balls.add(new Ball(SMALL, newColor, startX, startY));
				break;
				case 2:
					balls.add(new Ball(MEDIUM, newColor, startX, startY));
				break;
				case 3:
					balls.add(new Ball(LARGE, newColor, startX, startY));
				break;
			}
		}

    	uBoundY = (view.getHeight()/2)-chamberHeight/2;
		lBoundY = (view.getHeight()/2)+chamberHeight/2;
		lBoundX = (view.getWidth()/2)-chamberWidth/2;
		rBoundX = (view.getWidth()/2)+chamberWidth/2;

		pairs.add(new ThreadBallPair(this, null));
    }
}

class Ball {
	static final int SMALL = 0;
	static final int MEDIUM = 1;
	static final int LARGE = 2;

	public int size;
	public int sizeX;
	public int sizeY;

	public double radius;
	public double centerPointX;
	public double centerPointY;

	public Color shade = Color.BLACK;

	public boolean pause = false;

	public int positionX;
	public int positionY;

	public int velocityX = 1;
	public int velocityY = 1;

	public int velocityDX = 2;
	public int velocityDY = 2;

	public double velocityVect = 0;

	public boolean collided = false;
	public boolean debounce = false;
	public boolean wallBall = false;

	private int threshHoldC = 0;
	private int threshHoldD = 0;

	public void bounce(double tvelocityX, double tvelocityY){

		if(tvelocityY < 0){
			velocityY = (int)(Math.floor(tvelocityY));
		}
		else if(tvelocityY > 0){
			velocityY = (int)(Math.ceil(tvelocityY));
		}

		if(tvelocityX < 0){
			velocityX = (int)(Math.floor(tvelocityX));
		}
		else if(tvelocityX > 0){
			velocityX = (int)(Math.ceil(tvelocityX));
		}

		velocityVect = Math.sqrt(Math.pow(velocityX,2)+Math.pow(velocityY,2));
	}

	public void accelerate(){
		if(!pause){
			positionX = positionX+velocityX;
			positionY = positionY+velocityY;

			centerPointX = positionX + sizeX/2;
			centerPointY = positionY + sizeY/2;
		}

		if(debounce){
			threshHoldD += 1;
			if(threshHoldD == 10){
				debounce = false;
				threshHoldD = 0;
			}
		}

		if(collided){
			threshHoldC += 1;
			if(threshHoldC == 5){
				collided = false;
				threshHoldC = 0;
			}
		}
	}

	Ball(int size, Color shade,int startX, int startY){
		switch(size){
			case SMALL:
				this.size = SMALL;
				sizeX = 15;
				sizeY = 15;
			break;
			case MEDIUM:
				this.size = MEDIUM;
				sizeX = 25;
				sizeY = 25;
			break;
			case LARGE:
				this.size = LARGE;
				sizeX = 50;
				sizeY = 50;
			break;
		}

		/*this.size = SMALL;
		sizeX = 30;
		sizeY = 30;*/

		this.shade = shade;

		radius = sizeX/2;

		positionX = startX;
		positionY = startY;

		centerPointX = positionX + sizeX/2;
		centerPointY = positionY + sizeX/2;

		Random randomGenerator = new Random();
      	int randomDir = randomGenerator.nextInt(4);

      	double randomX = Math.random();
      	double randomY = Math.random();

		switch(randomDir){
			case 0:
				velocityX = -(int)(Math.ceil(randomX*velocityDX));
				velocityY = -(int)(Math.ceil(randomY*velocityDY));
			break;
			case 1:
				velocityX = +(int)(Math.ceil(randomX*velocityDX));
				velocityY = -(int)(Math.ceil(randomY*velocityDY));
			break;
			case 2:
				velocityX = -(int)(Math.ceil(randomX*velocityDX));
				velocityY = +(int)(Math.ceil(randomY*velocityDY));
			break;
			case 3:
				velocityX = +(int)(Math.ceil(randomX*velocityDX));
				velocityY = +(int)(Math.ceil(randomY*velocityDY));
			break;
		}

		velocityVect = Math.sqrt(Math.pow(velocityX,2)+Math.pow(velocityY,2));
	}
}

class ThreadBallPair{
	public ThreadedBalls pThread = null;
	public Ball pBall = null;

	ThreadBallPair(ThreadedBalls pThread, Ball pBall){
		this.pThread = pThread;
		this.pBall = pBall;
	}
}