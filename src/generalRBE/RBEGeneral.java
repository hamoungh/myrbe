/*-------------------------------------------------------------------------
 * rbe.RBE.java
 * Timothy Heil
 * ECE902 Fall '99
 *
 * TPC-W Remote Browser Emulator.
 *
 * Main program.
 *------------------------------------------------------------------------
 *
 * This is part of the the Java TPC-W distribution,
 * written by Harold Cain, Tim Heil, Milo Martin, Eric Weglarz, and Todd
 * Bezenek.  University of Wisconsin - Madison, Computer Sciences
 * Dept. and Dept. of Electrical and Computer Engineering, as a part of
 * Prof. Mikko Lipasti's Fall 1999 ECE 902 course.
 *
 * Copyright (C) 1999, 2000 by Harold Cain, Timothy Heil, Milo Martin, 
 *                             Eric Weglarz, Todd Bezenek.
 *
 * This source code is distributed "as is" in the hope that it will be
 * useful.  It comes with no warranty, and no author or distributor
 * accepts any responsibility for the consequences of its use.
 *
 * Everyone is granted permission to copy, modify and redistribute
 * this code under the following conditions:
 *
 * This code is distributed for non-commercial use only.
 * Please contact the maintainer for restrictions applying to 
 * commercial use of these tools.
 *
 * Permission is granted to anyone to make or distribute copies
 * of this code, either as received or modified, in any
 * medium, provided that all copyright notices, permission and
 * nonwarranty notices are preserved, and that the distributor
 * grants the recipient permission for further redistribution as
 * permitted by this document.
 *
 * Permission is granted to distribute this code in compiled
 * or executable form under the same conditions that apply for
 * source code, provided that either:
 *
 * A. it is accompanied by the corresponding machine-readable
 *    source code,
 * B. it is accompanied by a written offer, with no time limit,
 *    to give anyone a machine-readable copy of the corresponding
 *    source code in return for reimbursement of the cost of
 *    distribution.  This written offer must permit verbatim
 *    duplication by anyone, or
 * C. it is distributed by someone who received only the
 *    executable form, and is accompanied by a copy of the
 *    written offer of source code that they received concurrently.
 *
 * In other words, you are welcome to use, share and improve this codes.
 * You are forbidden to forbid anyone else to use, share and improve what
 * you give them.
 *
 ************************************************************************
 *
 * Changed 2003 by Jan Kiefer.
 *
 ************************************************************************/

package generalRBE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

//import rbe.Array2StrConverter;
//import rbe.EB;
//import rbe.EBFactory;
//import rbe.EBStats;
//import rbe.EBStatsAbs;
//import rbe.EBTPCW1Factory;
//import rbe.EBTPCW2Factory;
//import rbe.EBTPCW3Factory;
//import rbe.args.Arg;
//import rbe.args.ArgDB;
//import rbe.args.IntArg;
//import rbe.args.PrintStreamArg;
//import rbe.args.DateArg;
//import rbe.args.DoubleArg;
//import rbe.args.StringArg;
//import rbe.args.BooleanArg;



import rbe.util.StrStrPattern;
import rbe.util.CharSetStrPattern;
import rbe.util.CharRangeStrPattern;
import rbe.util.Pad;

public class RBEGeneral {
	// properties
	protected static final String TT_SCALE = "tt_scale";
	protected static final String STEP = "step";
	protected static final String WWW1 = "www1";
	protected static final String WWW2 = "www2";
	protected static final String OUTPUT_FILE = "outputFile";
	protected static final String CONTEXT = "context";
	protected static final String WORKLOAD_FRACTION = "workloadFraction";
	protected static final String IF_GET_IMAGE = "ifGetImage";
	protected static final String WORKLOAD = "workload"; 
	protected static final String DEBUG = "debug";
	protected static final String LOOP_NUM = "loopnum";
	// end properties

	static String rbe_properties_address = "/rbe.properties";
	
	public static final String field_sessionID = ";jsessionid=";
	
	public static String www1; //= "http://localhost:8080";
	public static String www2;
	public static Date startTime;

	// Name of matlab .m output file for results.
	public static PrintStream oFile = null;

	// URLs
	// static {www1 = getProperty(WWW);}
	//public static String www;

	public static final StrStrPattern yourSessionID = 
		//new StrStrPattern("JIGSAW-SESSION-ID=");
		//      new StrStrPattern("JServSessionIdroot=");
		new StrStrPattern(";jsessionid=");
	public static final StrStrPattern endSessionID =
		new StrStrPattern("?");

	public static String homeURL1;
	public static String homeURL2;
	
	static {
		www1 = getProperty(WWW1) + getProperty(CONTEXT)+"/";
		www2 = getProperty(WWW2) + getProperty(CONTEXT)+"/";
		//tring wwwTPCW = www1 + "/tpcw";
		homeURL1 = www1+"HelloWorldServlet";
		homeURL2 = www2+"HelloWorldServlet";
	}
	
	public static void setURLs()
	{
		www1 = getProperty(WWW1) + getProperty(CONTEXT)+"/";
		www2 = getProperty(WWW2) + getProperty(CONTEXT)+"/";
		//tring wwwTPCW = www1 + "/tpcw";
		homeURL1 = www1+"HelloWorldServlet";
		homeURL2 = www2+"HelloWorldServlet";
	}

	static {
		setURLs();
	}

	// FIELD NAMES
	public static final String field_count = "count";
	//
	
	public static boolean getImage; // Whether to fetch images.
	public static boolean monitor; //Whether or not to do monitoring
	public static boolean incremental;  

	public static String getCurPath(){
		File dir1 = new File (".");
		String path = "";
		try {
			path =  dir1.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}

	static Properties properties = new Properties();	
	static {
		try {			
			properties.load(new FileInputStream(getCurPath() + rbe_properties_address));
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}
	public static String getProperty(String key) {
		if (properties == null) properties = new Properties(); 
		if(properties.isEmpty()){
			try {			
				properties.load(new FileInputStream(getCurPath() + rbe_properties_address));
			} catch (Exception e) {
				e.printStackTrace();
			} 		
		}
		String prop= properties.getProperty(key);
		if (prop==null || prop.isEmpty()) System.out.print("prop "+ key + " has no value");
		return prop;
	}

	public double slowDown;         // Time slow down factor.
	//  1000 means one thousand real seconds equals one simulated second.
	public double speedUp;          // 1/slowDown.

	public int maxImageRd=10;       // Maximum number of images to read at once.
	public EBStatsAbs stats;
	public static final BufferedReader bin = 
		new BufferedReader(new InputStreamReader(System.in));
	
	public static Integer[] concat(Integer[] A, Integer[] B) {
		   Integer[] C= new Integer[A.length+B.length];
		   System.arraycopy(A, 0, C, 0, A.length);
		   System.arraycopy(B, 0, C, A.length, B.length);
		   return C;
	}
	
	public static void main(String [] args) {
		final RBEGeneral rbe = new RBEGeneral();
		// EBTestFactory ebtf = new EBTestFactory();
		int i;		

		Vector<EB> ebs = new Vector<EB>(0);

		System.out.println("Remote Browser Emulator for TPC-W.");
		System.out.println("  ECE 902  Fall '99");
		System.out.println("  Version 1.5");
		System.out.println("System's current path is "+getCurPath());
		//rbe_properties_address = "/rbe.properties"; //*l
		
		startTime = new Date();

		EBGeneralFactory[] factory =   {new EBGeneralFactory()};

		for (EBGeneralFactory ebf:factory){
			ebf.initialize();
		}


		// jus to fix the bug
		// int maxState= factory.getEB(rbe).states();
		int maxState= factory[0].getEB(rbe).states();

		// Create EBs
		// number of EBs to create with this factory.
		//int[] num_seq=Workload.rolledup_workload;

		//----------- load the workload file ---------------------
		String str = getProperty(WORKLOAD);	
		Integer[] num_seq = new Array2StrConverter().stringtoArray( str );
//		Integer[] num_seq = {};
//		try {
//			String thisLine;
//			String filename = getCurPath() + "/" + workloadFile;
//			BufferedReader br = new BufferedReader(new FileReader(filename));
//			while ((thisLine = br.readLine()) != null) { // while loop begins here
//				Integer[] temp = new Array2StrConverter().stringtoArray( thisLine );
//				num_seq = concat(num_seq,temp);
//			} 
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			//"prop file not found, \""+workloadFile+"\"\n";
//			// System.exit(0);
//		}
			

		//------------------------------------------------	
		for(i=0;i<num_seq.length;i++){
			num_seq[i] = num_seq[i] / Integer.parseInt(getProperty(WORKLOAD_FRACTION)); 
		}

		int cur_num = 0; 

		try{
			oFile = new PrintStream(new FileOutputStream(getCurPath()+"/"+getProperty(OUTPUT_FILE)));
		} catch(Exception e){
			System.out.println(e);
		}

		//  Starting time for ramp-up, Time (such as Nov 2, 1999 11:30:00 AM CST) at which to start ramp-up.
		//  Useful for synchronizing multiple RBEs.
		Date st = new Date(System.currentTimeMillis()+2000L);

		//  Ramp-up time: Seconds used to warm-up the simulator.
		int ru = 1;

		// Measurement interval: Seconds used for measuring SUT performance.
		int mi = 1 * 60;

		// Ramp-down time: Seconds of steady-state operation following measurment interval.
		int rd = 1;

		final int step = Integer.parseInt(getProperty(STEP)); 

		//  Slow-down factor: 1000 means one thousand real seconds equals one simulated second.
		//  Accepts factional values and E notation.
		double slow = 1.0;

		// Think time multiplication: Used to increase (>1.0) or decrease (<1.0) think time. In addition to slow-down factor.	    
		//double tt_scale = 0.03;
		double tt_scale = Double.parseDouble(getProperty(TT_SCALE));
		
		// interactive control: Require user to hit RETURN before every interaction.  Overrides think time.
		boolean key = false;

		// Request images: True will cause RBE to request images.  False suppresses image requests.
		boolean getImage = Boolean.parseBoolean(getProperty(IF_GET_IMAGE));

		// Image connections: Maximum number of images downloaded at once.
		int img = 4;

		// Number of customers: Number of customers in the database.   
		// Used to generated random CIDs.
		int cust = 1000;

		// CID NURand A: sed to generate random CIDs. 
		// See TPC-W Spec. Clause 2.3.2. -1 means use TPC-W spec. value.// 
		int custa = -1;

		// Number of items: Number of items in the database. 
		// Used to generate random searches. 
		int item = 10000;

		// Item NURand A: Used to generate random searches.  
		// See TPC-W Spec. Clause 2.10.5.1. -1 means use TPC-W spec. value.  
		int itema = -1;

		// Debug message: Increase this to see more debug messages ~1 to 10.  
		int debug = Integer.parseInt(getProperty(DEBUG));  

		// Maximum errors allowed: RBE will terminate after this many errors.  0 implies no limit. 
		//int maxErr = 1;
		int maxErr = 0; 
		
		// Base URL: The root URL for the TPC-W pages. 
		//String www = RBE.www1;

		// Do utilization monitoring: TRUE=do monitoring, FALSE=Don't do monitoring 
		boolean mon = false; 

		// Start EBs Incrementally: TRUE=do them in increments, FALSE=Do them all at once
		boolean incr = true;

		// Copy in parameters.
		rbe.maxImageRd  = img;
		//RBE2.www1        = www;
		RBEGeneral.getImage    = getImage; 
		RBEGeneral.monitor     = mon;
		RBEGeneral.incremental = incr;
		RBEGeneral.setURLs();   
		EB.DEBUG = debug;
	//	EBStats.maxError = maxErr;


		// Correct ramp-up time by start time.
		long start = st.getTime();
		long addRU = start - System.currentTimeMillis();

		if (addRU < 0L) {
			System.out.println("Warning: start time " + (((double) addRU)/1000.0) + 
					" seconds before current time.\n" +
			"Resetting to current time.");
			start = System.currentTimeMillis();
		}

		rbe.slowDown = slow;
		rbe.speedUp  = 1/rbe.slowDown;

		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		for (int num:num_seq){
			// Create statistics object.
			rbe.stats = new EBStatsNew(
					rbe,			// RBE rbe,	
					0, 			// start
					step*1000, 			// end
					num			// number of users	

			);

			int to_be_added = num - cur_num;   
			if (to_be_added>0) {
				// System.out.println("I want to add "+to_be_added);
				for (i=0;i<to_be_added;i++) {
					// int indx = (int)(Math.floor(Math.random()*3));
					int indx = 0;
					EB e = factory[indx].getEB(rbe);
					if (e.states()>maxState) {
						maxState = e.states();
					}
					ebs.addElement(e);
					e.initialize();
					e.tt_scale = tt_scale;
					e.waitKey = key;
					e.setName("TPC-W Emulated Broswer " + (i+1));
					e.setDaemon(true);
					//try {
					//		Thread.currentThread().sleep(10000L);
					//}catch(java.lang.Exception ex){
					//	System.out.println("Unable to sleep");
					//}
					e.start();
				}
			} else if (to_be_added<0) { // to be deleted 
				// System.out.println("I want to delete "+to_be_added);
				for (i=0;i<Math.min(Math.abs(to_be_added),ebs.size());i++) {
					EB eb = ebs.get(ebs.size()-1);
					ebs.remove(eb);
					eb.stop();
				}
			}

			try {
				Thread.currentThread().sleep(step*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			rbe.stats.print(oFile);

			cur_num = num;
		}

		System.out.println("Terminating EBs...");
		// Terminate all EBs
		EB.terminate = true;
		//		try {
		//			Thread.currentThread().sleep(10000L);
		//		}
		//		catch (InterruptedException ie) { System.out.println("Unable to sleep!");}

		for (i=0;i<ebs.size();i++) {
			EB e = (EB) ebs.elementAt(i);
			//e.interrupt();
			e.stop();
			try {
				e.join();
			}
			catch (InterruptedException inte) {
				inte.printStackTrace();
				return;
			}
		}


		System.out.println("EBs finished.");

		oFile.println("% Start time: " + startTime);
		// oFile.println("% System under test: " + www);
		Date endTime = new Date();
		oFile.println("% End time: " + endTime);

		oFile.close();
		System.out.println("Really finishing RBE!.");
	}

	public final long slow(long t) { return((long) (slowDown*t+0.5)); }
	public final long speed(long t) { return((long) (speedUp*t+0.5)); }

	public static void getKey() {
		System.out.println("Type RETURN to continue...");
		try {
			bin.readLine();
		}
		catch (java.io.IOException e) {
			e.printStackTrace();		
		}
	}

	// Negative exponential distribution used by 
	//  TPC-W spec for Think Time (Clause 5.3.2.1) and USMD (Clause 6.1.9.2)
	public final long negExp(Random rand, long min, double lMin,
			long max, double lMax,
			double mu)
	{
		double r = rand.nextDouble();

		if (r < lMax) {
			return(slow(max));
		}

		return(slow((long) (-mu*Math.log(r))));
	}

	public static String addSession(String i, String f, String v){
		StringTokenizer tok = new StringTokenizer(i, "?");
		String return_val = null;
		try {
			return_val = tok.nextToken();
			return_val = return_val + f + v;
			return_val = return_val + "?" + tok.nextToken();
		}
		catch (NoSuchElementException e) { 
		}

		return(return_val);
	}

	public static String addField(String i, String f, String v)
	{
		if (i.indexOf((int) '?')==-1) {
			// First field
			i = i + '?';
		}
		else {
			// Another additional field.
			i = i + '&';
		}
		i = i + f + "=" + v;

		return(i);
	}

}
