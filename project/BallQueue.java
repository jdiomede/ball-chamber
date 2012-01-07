import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;

public class BallQueue {
	private BallChamber view = null;

    private ArrayList<ThreadBallPair> balls = new ArrayList<ThreadBallPair>();
    private ArrayList<Ball> removedBalls = new ArrayList<Ball>();

	public int localCount = 0;
	public boolean available = false;
	public boolean dead = false;

	public synchronized void updateLocalCount(int i){
		localCount += i;
		//removeCount += i;

		if(localCount == 0){
			dead = true;
		}
		else{
			dead = false;
		}

		if(balls.size() >= localCount){
			available = true;
		}
		notifyAll();
	}

	public synchronized void donePainting(){
		available = false;
		notifyAll();
	}

    public synchronized ArrayList<ThreadBallPair> getBalls(){
        while (available == false && !dead) {
            try {
                wait();
            } catch (InterruptedException e) {
            	notifyAll();
            	//view.stop();
            }
        }
        return balls;
    }

    public synchronized boolean addBalls(ThreadBallPair b) {
        while (available == true) {
            try {
                wait();
            } catch (InterruptedException e) {
            	notifyAll();
            	//view.stop();
            }
        }

		boolean accel = false;
        if(!balls.contains(b)){
        	balls.add(b);
        	accel = true;
        }
        else if(balls.size() >= localCount){
        	available = true;
        	notifyAll();
        }
        return accel;
    }

    BallQueue(BallChamber view){
    	this.view = view;
    }
}