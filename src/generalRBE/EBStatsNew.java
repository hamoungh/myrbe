package generalRBE;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

//import rbe.util.Pad;

public class EBStatsNew implements EBStatsAbs { 
	long measurementInrvl= 1000L;
	double ttsum = 0;
	int num_interactions = 0;
	public RBEGeneral rbe;
	public long start = 0;		// start of measurement period in miliseconds
	public long end = 0;		// end of measurement period in miliseconds
	public int num_users = 0;
	public int WebUtil;
	public int DBUtil;
	double overal_rt = 0;
	
//	public final List<Integer> throughput = new ArrayList<Integer>();
//	public final List<Integer> tt = new ArrayList<Integer>();
//	public final Map<Integer,List<Integer>> rt = new HashMap<Integer,List<Integer>>();

	// List of retries/errors encount.
	  public final List<EBError> errors = new ArrayList<EBError>(); 
	
	  // from Url to sum of time
	public final Map<Integer,Double> rt = new HashMap<Integer,Double>();
	public Map<Integer,Integer> num_interactions_4_url = new HashMap<Integer,Integer>();
	
	
	// Times are all expressed in milliseconds.
	  public EBStatsNew(RBEGeneral rbe,long start, long end, int num_users){ 
			 this.rbe = rbe;
			 this.start =  start;
			 this.end =  end;
			 this.num_users = num_users;
	  }
	  

	  
		@Override
		public void updateWebCPUUtil(int util) {
			this.WebUtil = util;
			
		}


		@Override
		public void updateDBCPUUtil(int util) {
			this.DBUtil = util;			
		}
		
	public void interaction(int state, long wirt_t1, long wirt_t2, long itt) {
			//throughput.add(throughput.remove(throughput.size())+1);

			double rt_ = (double)(wirt_t2-wirt_t1)/1000d;
			if (rt.containsKey(state))				
				rt.put(state,rt.get(state) + rt_);
			else
				rt.put(state, rt_);
			 
			overal_rt  += (double)(rt_);
			
			if (num_interactions_4_url.containsKey(state))
				num_interactions_4_url.put(state,num_interactions_4_url.get(state) + 1);
			else
				num_interactions_4_url.put(state , 1);
				
			num_interactions++;
			// System.out.println("num_interactions:"+num_interactions);
			
			ttsum  += (double)(itt)/1000d;

	}

	public void error(String message, String url) {
//		System.out.println(message);
//		System.out.println(url);
		EBError error = new EBError(message, url);
		errors.add(error);
	}

	public void print(PrintStream out) {
	    double measurementIntrvl = (double)(end-start)/1000d;
	    double throughput = (double)(num_interactions) / (measurementIntrvl);  
	   
	    DecimalFormat df = new DecimalFormat("#.##");
	    DecimalFormat df2 = new DecimalFormat("#.#####");
	    
	    String str = "";
	    // per url stuff
	    //	    for (Integer url:rt.keySet()){
//	    	str = str + "," + url +":"+ 
//	    		df2.format(rt.get(url)/(double)(num_interactions_4_url.get(url))) +"/"+ 		// the average response time
//	    		df.format((double)(num_interactions_4_url.get(url))/measurementIntrvl);		// the average throughput
//	    }

	    str = str +"rt:"+df.format(overal_rt/(double)num_interactions) ;
	    
	    RBEGeneral.oFile.println(
	    		str +","+ 
	    		"through:"+df.format(throughput) + "," + 
	    		"tt:"+df.format(ttsum/(double)num_interactions) + "," + 
	    		"num_interact:"+df.format(num_interactions) +","+ 
	    		"Intrvl:"+df.format(measurementIntrvl) +","+
	    		"num_users:"+num_users 
	    		// uncomment if the server is local
	    		// +"," + "Webutil:"+ WebUtil +","+
	    		// "DButil:"+ DBUtil
	    ); 	  
	  
	    System.out.println(
		str +","+ 
		"through:"+df.format(throughput) + "," + 
		"tt:"+df.format(ttsum/(double)num_interactions) + "," + 
		"num_interact:"+df.format(num_interactions) +","+ 
		"Intrvl:"+df.format(measurementIntrvl) +","+
		"num_users:"+num_users +","+
		"errors:" + errors.size());
	  
	}



	@Override
	public void transition(int cur, int next) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void waitForRampDown() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void waitForStart() throws InterruptedException {
		// TODO Auto-generated method stub
		
	}




}
