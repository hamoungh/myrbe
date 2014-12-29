/*-------------------------------------------------------------------------
 * rbe.EBTPCW2Factory.java
 * Timothy Heil
 * 10/20/99
 *
 * Trey Cain
 * 2/26/2000
 * 
 * Produces TPCW EBs for transistion subset 1 (The Browsing Mix - WIPSb).
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
 ************************************************************************/

package generalRBE;

//import rbe.EB;
//import rbe.EBFactory;
//import rbe.EBTransition;
//import rbe.EBWInitTrans;

public class EBGeneralFactory extends EBFactory {
  private static final EBTransition init = 
    new EBWInitTrans();

  private static final EBTransition [] trans = {
    init,
  };

  private static final int [][] transProb = 
  {
    //           INIT 
    /*INIT  */ {  9999}
  };

  private static final EBTransition [][] transArray = 
  {
    //           INIT 
    /*INIT  */ { null}
  };

  private int count = 1;
  private boolean [] stateEnabled = new boolean[trans.length];
  private final int [][] cTransProb = new int[transProb.length][transProb.length];
  private final EBTransition [][] cTransArray = new EBTransition[transArray.length][transArray.length];

  public EB getEB(RBEGeneral rbe)
  {
    return(new EB(rbe, cTransProb, cTransArray, 1000000,
		  "TPCW Broswing Mix EB #" + (count++)));
  }

//  };

  public void initialize() {
	    int i;
	      for (i=0;i<stateEnabled.length;i++) {
	    	  stateEnabled[i] = true;
	      }
	      initTransArray();
	   };
  private void initTransArray()
  {
    int i,j;
    int max, maxCol;

    // Copy probabilities, disabling states.
    for (i=0;i<transProb.length;i++) {
      for (j=0;j<transProb.length;j++) {
	if (stateEnabled[i] && stateEnabled[j]) {
	  cTransProb[i][j] = transProb[i][j];
	  // System.out.println("CC " + i + " " + j);
	}
	else {
	  transArray[i][j] = null;
	}
      }
    }

    // Clean up any broken probabilities.
    for (i=0;i<transProb.length;i++) {
      max = cTransProb[i][0];maxCol = 0;
      for (j=1;j<transProb.length;j++) {
	if (cTransProb[i][j] > max) {
	  max = cTransProb[i][j];
	  maxCol = j;
	}
      }
      if (max < 9999) {
	cTransProb[i][maxCol] = 9999;
      }
    }

    // Copy in transitions.
    for (i=0;i<transProb.length;i++) {
      for (j=0;j<transProb.length;j++) {
	cTransArray[i][j] = transArray[i][j];
	if ((cTransProb[i][j]>0) && (cTransArray[i][j]==null)) {
	  cTransArray[i][j] = trans[j];
	}
      }
    }
  }
}
