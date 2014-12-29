package generalRBE;

import java.io.PrintStream;

public interface EBStatsAbs {
	  
	public abstract void transition(int cur, int next);

	public abstract void interaction(int state, long wirt_t1, long wirt_t2,
			long itt);

	public abstract void error(String message, String url);

	public abstract void print(PrintStream out);

	public abstract void waitForRampDown();

	public abstract void waitForStart() throws InterruptedException;

	 public void updateWebCPUUtil(int util);
	 public void updateDBCPUUtil(int util);
}