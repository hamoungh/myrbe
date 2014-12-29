/*-------------------------------------------------------------------------
 * rbe.EB.java
 * Timothy Heil
 * 10/5/99
 *
 * ECE902 Fall '99
 *
 * TPC-W emulated browser.
 *------------------------------------------------------------------------
 *
 * This is part of the the Java TPC-W distribution,
 * written by Harold Cain, Tim Heil, Milo Martin, Eric Weglarz, and Todd
 * Bezenek.  University of Wisconsin - Madison, Computer Sciences
 * Dept. and Dept. of Electrical and Computer Engineering, as a part of
 * Prof. Mikko Lipasti's Fall 1999 ECE 902 course.s
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
 ************************************************************************/

package generalRBE;

import java.net.*;
import java.io.*;

import java.util.Random;
import java.util.Date;
import java.util.Vector;


import rbe.util.Debug;
import rbe.util.StringPattern;
import rbe.util.StrStrPattern;
import rbe.util.CharStrPattern;
import rbe.util.CharSetStrPattern;

public class EB extends Thread {
	// Terminate all EBs.
	public static volatile boolean terminate=false;  

	int          [/*from*/][/*to*/] transProb;      // Transition probabilities.
	EBTransition [/*from*/][/*to*/] trans;          // EB transitions.
	EBTransition curTrans;
	int curState;                                   // Current state.
	String nextReq;            // Next HTTP request.
	String html;               // Received HTML
	public String prevHTML;    // HTML from a previous page.
	//int maxTrans;
	String name;
	byte [] buffer = new byte[4096];
	String sessionID;          // SESSION_ID.  See TPC-W Spec. 
	int    shopID;             // Shopping ID.  
	String fname = null;       // Customer first name.
	String lname = null;       // Customer last name.
	public RBEGeneral rbe;
	long usmd;
	boolean toHome;
	boolean stagger = true;

	// Wait for key-stroke between transisions.
	//   Does not do think-times.
	public boolean waitKey = true;

	public Random rand = new Random();

	// Think time-scaling.
	public double tt_scale = 1.0;

	// Set this higher to see more messages. 
	public static int DEBUG =3;

	public static final int NO_TRANS = 0;
	public static final int MIN_PROB = 1;
	public static final int MAX_PROB = 9999;
	public static final int ID_UNKNOWN = -1;

	public EB(RBEGeneral rbe,
			int [][] prob, // Transition probabilities.  
			//   See TPC-W Spec. Section 5.2.2.
			EBTransition [][] trans, // Actual transitions.
			int max,     // Number of transitions. -1 implies continuous
			String name  // String name.
	)
	{
		int i,j;
		int s;
		int prev;

		// Make sure prob and trans are well-formed.
		s = prob.length;
		// Debug.assert(s>0, "No states in prob.");
		// Debug.assert(trans.length == s, "Number of states in prob (" + s + 
		//		 ") does not equal number of states in trans (" + 
		//		 trans.length + ")");

		for (j=0;j<s;j++) {
		
			prev = 0;
			for (i=0;i<s;i++) {
				if (prob[j][i]==NO_TRANS) {
	
				}
				else {
					prev = prob[j][i];
				}
			}
		}

		this.rbe       = rbe;
		this.transProb = prob;
		this.trans     = trans;
		this.name      = name;

//		maxTrans = max;    
		initialize();
	}

	public final int states() 
	{
		return(transProb.length);
	}

	public void initialize() {
		curState = 0;
		nextReq = null;
		html = null;
		prevHTML = null;
		sessionID = null;
		usmd = System.currentTimeMillis() + usmd();
		fname = null;
		lname = null;
	}

	public void run() {
		long wirt_t1;  // WIRT.T1 in TPC_W.Spec.
		long wirt_t2;  // Same as TT.T1 in TPC-W Spec.
		long wirt;     // Web Interaction Response Time (WIRT).
		long tt=0L;    // Think Time.

		wirt_t1 = System.currentTimeMillis();

		if (DEBUG>0) {
			System.out.println("usmd " + usmd);
		}

		try {
			while (true) {

				if (terminate) { 
					System.out.println("EB " + name + "commiting suicide!");
					return;
				}
				if (nextReq!=null) {

					if (nextReq.equals("")) {
						rbe.stats.error("Restarting new user session due to error.", " <???>");
						initialize();
						continue;
					}

					// 2) Send HTTP request.
					URL httpReq = new URL(nextReq);

					// 3) Receive HTML response page.
					if (DEBUG > 0) { System.out.println("" + name + "Making request."); }		
					getHTML(httpReq);
					if (DEBUG > 0) {System.out.println("" + name + "Received HTML.");}
					
					// 4) Measure absolute response time, TT.T1 = WIRT.T2.  This is a
					// time stamp just following the reception of the last byte of the
					// HTML response page, which was provided by the SUT.
					wirt_t2 = System.currentTimeMillis();

					// 5) Compute and store Web Interaction Response Time (WIRT) 
					rbe.stats.interaction(curState, wirt_t1, wirt_t2, tt);
					// curTrans.postProcess(this, html);
				}
				else {
					html = null;
					wirt_t2 = wirt_t1;
				}

				// 6) Pick the next navigation option.
				// 7) Compose HTTP request.
				nextState();

				if (nextReq != null) {
					// 8) Pick think time (TT), and compute absolute request time
					tt = thinkTime();


					wirt_t1 = wirt_t2 + tt;

					if (terminate) {
						System.out.println("EB " + name + "commiting suicide!");
						return;
					}

					// 9) Wait for absolute request time.
					try {
						if (waitKey) {
							rbe.getKey();
						}
						else {
							sleep(tt);
						}
					}
					catch (InterruptedException inte) {
						System.out.println("EB " + name + " Caught an interrupted exception!");
						return;
					}

		
				} 
				else
					System.out.println("ERROR: nextReq == null!");
			}
		}
		catch (MalformedURLException murl) {
			murl.printStackTrace();
			return;
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		}
	}

	void getHTML(URL url) 
	{

		// System.out.println("Begin reading HTML. " + name);
		html = "";
		int r;
		BufferedInputStream in=null;
		BufferedInputStream imgIn=null;
		boolean retry;
		Vector imageRd = new Vector(0);

		do {
			retry = false;
			try {
				in = new BufferedInputStream(url.openStream(), 4096);
			}
			catch (IOException ioe) {
				rbe.stats.error("Unable to open URL." , url.toExternalForm());
				ioe.printStackTrace();
				retry=true;
				continue;
			}
			try {
				while ((r = in.read(buffer, 0, buffer.length))!=-1) {
					if (r>0) {
						html = html + new String(buffer, 0, r);
					}
				}
			}
			catch (IOException ioe) {
				rbe.stats.error("Unable to read HTML from URL." , 
						url.toExternalForm());
				retry=true;
				continue;
			}

			if (retry) {
				try {
					if (waitKey) {
						rbe.getKey();
					}
					else {
						sleep(1000L);
					}
				}
				catch (InterruptedException inte) {
					System.out.println("In getHTML, caught interrupted exception!");
					return;
				}	  
			}
		} while (retry);

		try {
			in.close();
		}
		catch (IOException ioe) {
			rbe.stats.error("Unable to close URL." , url.toExternalForm());
		}


		if (DEBUG>10) {
			System.out.println(html);
		}

	}


	long thinkTime()
	{
		if (stagger) {
			long r = rand.nextInt(20000)+100;
			stagger = false;
			if (DEBUG > 0) {
				System.out.println("Think time staggering to " + r + "ms.");
			}
			return((long) (r*tt_scale));
		}
		else {
			long r = rbe.negExp(rand, 7000L, 0.36788, 70000L, 4.54e-5, 7000.0);

			r = (long) (tt_scale*r);

			if (DEBUG>0) {
				// r =100; // For testing...
				//System.out.println("Think time of " + r + "ms.");
			}

			return(r);
		}
	}

	long usmd()
	{
		return(rbe.negExp(rand, 0L, 1.0, 3600000L /*60 minutes*/, 0.0183156, 
				900000.0 /* 15 minutes */));
	}

	void nextState()
	{
		int i = nextInt(MAX_PROB-MIN_PROB + 1) + MIN_PROB;
		int j;

		// System.out.println(transProb);

		for (j=0;j<transProb[curState].length;j++) {
			if (transProb[curState][j]>=i) {

				rbe.stats.transition(curState,j);
				curTrans = trans[curState][j];
				nextReq = curTrans.request(this, html);
				toHome = trans[curState][j].toHome();
				curState = j;
				return;
			}
		}
	}

	// Needed, because Java 1.1 did not have Random.nextInt(int range)
	public int nextInt(int range) {
		int i = Math.abs(rand.nextInt());
		return (i % (range));
	}

	// Adds CUSTOMER_ID and SHOPPING_ID fields to HTTP request,
	//  if they are known.
	String addIDs(String i) {

		if (sessionID != null) {
			i = rbe.addSession(i,rbe.field_sessionID, ""+sessionID);
		}

		i = rbe.addField(i,rbe.field_count, ""+RBEGeneral.getProperty(RBEGeneral.LOOP_NUM));

		return(i);
	}

	public int findID(String html, StrStrPattern tag) {
		int id;

		// NOTE: StringPattern.first/last are not thread-safe. 

		// Find the tag string.
		int i = tag.find(html);
		if (i==-1) {
			return(EB.ID_UNKNOWN);
		}
		i = i + tag.length();

		// Find the digits following the tag string.
		int j = CharSetStrPattern.digit.find(html.substring(i));
		if (j==-1) {
			return(EB.ID_UNKNOWN);
		}

		// Find the end of the digits.
		j = j + i;
		int k = CharSetStrPattern.notDigit.find(html.substring(j));
		if (k==-1) {
			k = html.length();
		}
		else {
			k = k + j;
		}	 

		id =  Integer.parseInt(html.substring(j, k));

		return(id);
	}

	public String findSessionID(String html, StrStrPattern tag, 
			StrStrPattern etag) {
		int id;

		// NOTE: StringPattern.first/last are not thread-safe. 

		// Find the tag string.
		int i = tag.find(html);
		if (i==-1) {
			return(null);
		}
		i = i + tag.length();

		// Find end of the digits.
		int j = etag.find(html, i);
		if (j==-1) {
			return(null);
		}

		return(html.substring(i,j));
	}
}
