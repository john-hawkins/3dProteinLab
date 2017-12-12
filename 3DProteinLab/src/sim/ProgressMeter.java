package sim;

import java.io.PrintStream;

/*
 * A general class for keeping track of progress through a set of tasks
 * 
 * It can be used by setting the number of tasks to begin with and then
 * updating after each task is complete.
 * 
 * Or by simply updating by giving the percentage so far complete.
 */

public class ProgressMeter {
	
	private double percentageProcessed;
	private double lastPercentageProcessed;
	private int numProcessed = 0;
	private int numToProcess = 0;
	
	private long startTime;
	private long endTime;
	
	private double increment = 10.0;
	private double meterThreshold = increment;
	private int numPrinted = 0;
	
	PrintStream theStream;
	
	public ProgressMeter() {
	}
	
	public ProgressMeter(int num, PrintStream stream) {
		numToProcess= num;
		theStream = stream;
	}
	
	public ProgressMeter(double meterIncrement, PrintStream stream) {
		this.increment =  meterIncrement;
		theStream = stream;
	}
	
	public double getPercentageProcessed() {
		return percentageProcessed;
	}

	public void initializeMeter(int num) {
		numToProcess= num;
		initializeMeter();
	}
	
	public void initializeMeter() {
		this.startTimer();
		percentageProcessed = 0.0;
		if(theStream != null)
			initializeMeterPrint();
	}

	public void updateMeter() {
		numProcessed++;
		lastPercentageProcessed = percentageProcessed;
		percentageProcessed = ( (double)numProcessed/(double)numToProcess ) * 100;
		if(theStream != null)
			updateMeterPrint();
	}
	
	public void updateMeter(double percent) {
		lastPercentageProcessed = percentageProcessed;
		percentageProcessed = percent;
		if(theStream != null)
			updateMeterPrint();
	}
	
	public void finaliseMeter() {
		this.endTimer();
		percentageProcessed = 100.0;
		if(theStream != null)
			updateMeterPrint();
	}
	
	public void startTimer() {
		startTime = System.currentTimeMillis();
	}

	public void endTimer() {
		endTime = System.currentTimeMillis();
	}
	
	public void initializeMeterPrint() {
		theStream.print("0|");
		for(int i=0;i<100;i+=increment) {
			theStream.print("_");
		}
		theStream.println("|100% ");
		theStream.print(" |");
	}
	
	public void updateMeterPrint() {
		int numToBePrinted = (int) ( percentageProcessed/increment );
		int wePrint= 0;
		while(numPrinted < numToBePrinted) {
			theStream.print("*");
			numPrinted++;
			wePrint++;
		}
		if(percentageProcessed == 100 && wePrint > 0)
			theStream.print("|\n");
	}

	public void printTimerResults() {
		theStream.println("   Execution time :"+ ((double)(endTime-startTime)/1000) + " seconds");
		if(numToProcess > 0)
			theStream.println("     Average time :"+ ((double)(endTime-startTime)/numToProcess)/1000 + " seconds" );
	}

}
