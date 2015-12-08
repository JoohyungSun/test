/**
 * 
 */
import storm.Schedulers.Scheduler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import storm.EvtContext;
import storm.Processors.Processor;
import storm.Tasks.Task;

// addddddddddddddddded by JHS. from here

import java.util.Random;
// FOR DEBUG
// for file IO
import java.io.*;
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.IOException;
// for time
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
// for array and format
import java.util.Arrays;
import java.text.DecimalFormat;
// ######################## to here

import storm.EvtContext;
import storm.Processors.*;
import storm.Tasks.Task;
//import storm.DPMS.DPM_P_LEAT;
import storm.Processors.DPMProcessor;
/* ... keep ...


		// add - 1
		FileWriter fwa;
		BufferedWriter bwa = null;
		try {
			fwa = new FileWriter("__LP__test.txt", false);
			bwa = new BufferedWriter(fwa);	

//			bwa.write(dt.toString());
			bwa.newLine();
			bwa.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// end - 1

*/


/**
 * @author amd
 * Preemptive Earliest Deadline First
 * 16/01
 *
 */


public class DRAM_Scheduler extends Scheduler {
	class LReady extends ArrayList implements Comparator {

		public int compare(Object arg0, Object arg1) {
			Task T0 = (Task) arg0;
			Task T1 = (Task) arg1;
			int d0 = T0.getOwnFieldIntValue("next_deadline");
			int d1 = T1.getOwnFieldIntValue("next_deadline");
			if (d1 > d0) return -1;
			else if (d1 == d0) return 0;
			     else return 1;
		}

		/*
		// LLF
		public int compare(Object arg0, Object arg1) {
			Task T0 = (Task) arg0;
			Task T1 = (Task) arg1;
			int d0 = T0.getOwnFieldIntValue("laxity");
			int d1 = T1.getOwnFieldIntValue("laxity");
			if (d1 > d0) return -1;
			else if (d1 == d0) return 0;
			     else return 1;
		}
*/
		
	}

	private LReady list_ready;
	private Boolean todo = false;

	// LLF
	private boolean tocompute = false;
	private int nbTicks = 0;

	// added by JHS.
	int B[], W[][], lnum[], tmp[];
	double horisum[], CapW[], varx[][];
	Double res_maxflow[][];
	int N,M,numofB;
	double sumofCapW;
	ArrayList TASKS, CPUS;
	double taud_ret;
	int taud_deadline;
	int numoftdv;
//	String pattern = ".####";
	DecimalFormat dformat = new DecimalFormat(".####");
	DecimalFormat ddformat = new DecimalFormat(".######");
	// end... jhs.

	public void init() {
		list_ready = new LReady();

		TASKS =this.Kernel.getTasksListeManager().getALLTask();          
		CPUS =this.Kernel.getTasksListeManager().getProcessors();        
		
		N = TASKS.size()+1;
		M = CPUS.size();

		B = new int[N*2];
		W = new int[N*2][2];
		lnum = new int[N*2];
		tmp = new int[N*2];

		horisum = new double[N];
		CapW = new double[N*2];
		varx = new double[N][N*2];
		res_maxflow = new Double[N][N*2];
		
		Date dt = new Date();
		System.out.println(dt.toString());

		FileWriter fwa1;
		BufferedWriter bwa1 = null;
		try {
			fwa1 = new FileWriter("__LP__testtest.txt", false);
			bwa1 = new BufferedWriter(fwa1);
	//		bwa1.write(dt.toString());
	//		bwa1.newLine();
			bwa1.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	void make_lp()
	{
		int i,j,k,l;
		String strtmp, active_table[][] = new String[N][N*2];
		int of_var[] = new int[N];
		int totalvar=0;
		String sptmp = "";
//		String sptmptmp[] = new String[2];
		String[] sptmptmp;
		Double[] taud_varx = new Double[N*2];
		Double[] tmp_maxf = new Double[N*N*2];
		boolean isnendt = true;

		// add - 1
		FileWriter fwa;
		BufferedWriter bwa = null;
		try {
			fwa = new FileWriter("__LP__test.lp", false);
			bwa = new BufferedWriter(fwa);	

			for(i=0,j=0;i<N;i++)
			{
				of_var[i]=0;
				for(k=0;k<numofB;k++)
				{
					if(varx[i][k]>=0)
					{
						of_var[i]++;
						active_table[i][k] = "x"+(j+1);
						j++;
					}
					else {
						active_table[i][k] = null;
					}
				}
				totalvar+=of_var[i];
			}

			System.out.println("totalvar: "+totalvar+", of_var["+(N-1)+"]: "+of_var[N-1]);
/*
			sumtaudvar = 0;
			for(k=0;k<numofB;k++)
			{
				sumtaudvar += of_var[N-1]
			}
*/

/*
			for(i=0;i<N;i++)
			{
				for(j=0;j<numofB;j++)
				{
					strtmp = active_table[i][j] + " ";
					System.out.print(strtmp);
				}
					System.out.println(" ");
			}

			for(i=0;i<N;i++)
			{
				strtmp = of_var[i] + " ";
				System.out.print(strtmp);
			}

			System.out.println(" ");
*/
			
//			bwa.write(" ======= At time t = "+this.Kernel.getTime()+" ======= ");
//			bwa.newLine();
			
//			bwa.write("// Objective function by DRAM ");
//			bwa.newLine();
			
			l = 1;
			strtmp = "max: "; // max#1
//			strtmp = "min: "; // min#1
			if(totalvar>0) 
			{
				if ( totalvar == of_var[N-1] )
				{
					strtmp = strtmp + " x1"; // max#2
//					strtmp = strtmp + -l +" x1"; // for different weight to give tau_d // min#2
					l++;
				}
				else
				{
					strtmp = strtmp + " x1"; // max#3
//					strtmp = strtmp + "-1 x1"; // min#3
				}
			}

			for(i=1;i<totalvar;i++)
			{
				if ( i>=totalvar-of_var[N-1] && i<totalvar )
				{
					strtmp = strtmp + " + x"+(i+1); // max#4
//					strtmp = strtmp + " + "+ -l +" x"+(i+1); // min#4
					l++;
				}
				else
					strtmp = strtmp + " + x"+(i+1); // max#5
//					strtmp = strtmp + " + -1 x"+(i+1); // min#5
			}
			strtmp = strtmp + ";";
			
			bwa.write(strtmp);
			bwa.newLine();
//			bwa.newLine();

//			bwa.write("// Variable bound by DRAM ");
//			bwa.newLine();

//			for(i=0;i<N;i++)
//			{
//				for(j=0;j<numofB;j++)
//				{
//					bwa.write(active_table[i][j]+"  ");
//				}
//				bwa.newLine();
//			}

			for(i=0;i<N;i++)
			{
				for(k=0;k<numofB;k++)
				{
					if(active_table[i][k]!=null)
					{
						strtmp = "0 <= "+active_table[i][k]+" <= "+ddformat.format(varx[i][k])+";";
						bwa.write(strtmp);
						bwa.newLine();
					}
				}
			}

			for(i=0;i<N;i++)
			{
				String tttmp = null;
				if(active_table[i][0] != null)
					tttmp = active_table[i][0];
				for(k=1;k<numofB;k++)
				{
					if(active_table[i][k]!=null)
					{
						tttmp = tttmp + " + " + active_table[i][k];
					}
				}
				if(tttmp != null)
				{
					strtmp = "0 <= "+tttmp+" <= "+horisum[i]+";";
					bwa.write(strtmp);
					bwa.newLine();
				}
			}

			for(k=0;k<numofB;k++)
			{
				String tttmp = null;
				int st=0;
				for(i=0;i<N;i++)
				{
					if(active_table[i][k] != null)
					{
						tttmp = active_table[i][k];
						st=i;
						break;
					}
				}
				for(i=0;i<N;i++)
				{
					if(active_table[i][k]!=null && i!=st)
					{
						tttmp = tttmp + " + " + active_table[i][k];
					}
				}
				if(tttmp != null)
				{
					strtmp = "0 <= "+tttmp+" <= "+ddformat.format(CapW[k])+";";
					bwa.write(strtmp);
					bwa.newLine();
				}
			}

			
			bwa.newLine();
			bwa.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// end - 1


		i = k = 0;
		try {
			String s,sttttmp;
			Process oProcess = new ProcessBuilder("lpsolver.exe", "-S3", "-lp", "__LP__test.lp").start();

			// 외부 프로그램 출력 읽기
			BufferedReader stdOut   = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(oProcess.getErrorStream()));

			// "표준 출력"과 "표준 에러 출력"을 출력
			while ((s =   stdOut.readLine()) != null) 
			{
		//		System.out.println(s);
				s = s.trim().replaceAll("  +",",");
				sptmp = sptmp+s+"\n";

				if (s.matches(".*,.*") && isnendt)
				{
//					System.out.println(s);
					sptmptmp = s.split(",");
//					System.out.println(sptmptmp[0]);
//					System.out.println(sptmptmp[1]);
					
					if ( i>=totalvar-of_var[N-1] && i<totalvar )
					{
						taud_varx[k] = Double.parseDouble(sptmptmp[1]);
						k++;
					}

					tmp_maxf[i] = Double.parseDouble(sptmptmp[1]);

					i++;					
					
					if (i>=totalvar)
						isnendt = false;	
				}

//				sttttmp = " -------- "+s;

//				System.out.println(sttttmp);
				
//				sptmptmp[0] = sptmptmp[1] = null;
//				sptmptmp = s.split(",");
//				System.out.println(sptmptmp[0]);
//				System.out.println(sptmptmp[1]);
/*				
				if( sptmptmp[0] != null ) 
				{
					sptmp = sptmptmp[0] + ":" + sptmptmp[1];
					System.out.println(sptmp);
				}
*/
			}
			while ((s = stdError.readLine()) != null) {
				s = "error t = "+this.Kernel.getTime() + ",  " + s ;
				System.err.println(s);
			}

			// 외부 프로그램 반환값 출력 (이 부분은 필수가 아님)
//			System.out.println("Exit Code: " + oProcess.exitValue());
//			System.exit(oProcess.exitValue()); // 외부 프로그램의 반환값을, 이 자바 프로그램 자체의 반환값으로 삼기

	
		} catch (IOException e) { // 에러 처리
			System.err.println("에러! 외부 명령 실행에 실패했습니다.\n" + e.getMessage());
//			System.exit(-1);
		}

		numoftdv = k;

		k = 0;
		for(l=0; l<N; l++)
		{
			for(j=0; j<of_var[l]; j++)
			{
				res_maxflow[l][j] = tmp_maxf[k];
				k++;
			}
		}

			FileWriter fwa2;
			BufferedWriter bwa2 = null;
			try {
				fwa2 = new FileWriter("__LP__testtesttest.txt", true);
				bwa2 = new BufferedWriter(fwa2);
				
				bwa2.write(" ======= At time t = "+this.Kernel.getTime()+" ======= ");
				bwa2.newLine();

				bwa2.write(" ======= taud_varx[] ======= ");
				bwa2.newLine();
				for(j=0;j<numoftdv;j++)
				{
					bwa2.write(j+": "+taud_varx[j]);
					bwa2.newLine();
				}	

				bwa2.write(" ======= res_maxflow[][] ======= ");
				bwa2.newLine();
				for(l=0;l<N;l++)
				{
					for(j=0;j<of_var[l];j++)
					{
						bwa2.write(res_maxflow[l][j]+"\t");
					}
					bwa2.newLine();
				}
				
				bwa2.newLine();
				bwa2.newLine();
				bwa2.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


		FileWriter fwa1;
		BufferedWriter bwa1 = null;
		try {
			fwa1 = new FileWriter("__LP__testtest.txt", true);
			bwa1 = new BufferedWriter(fwa1);

			
			bwa1.write(sptmp);
			bwa1.newLine();
			bwa1.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	}


	int maxinB()
	{
		int mmax, i;
		mmax = B[0];
		for(i=1;i<numofB;i++)
			if(B[i]>mmax)
				mmax=B[i];
		return mmax;
	}

	void cal_taud()
	{
		int i,k;
		double sum;
		sum = 0;
		for(i=0;i<N-1;i++)
			sum+=horisum[i];
		//horisum[N] = sumofCapW - sum; 
//		T[4].d = horisum[4] = sumofCapW - sum; 
		horisum[N-1] = sumofCapW - sum; 
		taud_ret = sumofCapW - sum; 

		for(k=0;k<numofB;k++)
			varx[N-1][k] = horisum[N-1]>CapW[k]?CapW[k]:horisum[N-1];

		taud_deadline = maxinB();
	}

	void cal_CapW()
	{
		int i,k;
		double sum;

		sumofCapW = 0;
		for(k=0;k<numofB;k++)
		{
			sum = 0;
			for(i=0;i<N-1;i++)
			{
				Task T = (Task)TASKS.get(i);

//				if( T[i].arrnext < B[k] )
				if( T.getOwnFieldIntValue("next_period") < B[k] )
					sum += (double)T.getWCET()/(double)T.getDeadline();
			}
			CapW[k] = (M-sum)*(double)lnum[k];
	//		printf(" test--------- %.4f\n", CapW[k]);
			sumofCapW += CapW[k];
		}
	}

	void cal_horisum()
	{
		int i;
		for(i=0; i<N-1; i++)
		{
			Task T = (Task)TASKS.get(i);
			horisum[i] = T.getRET();
		}
	}

	void cal_varx()
	{
		int i,k;
		double tmin;

/*
		ArrayList TASKSTT;
		TASKSTT =this.Kernel.getTasksListeManager().getALLTask();
		for(int i=0;i<TASKS.size();i++)
		{
			Task T = (Task)TASKSTT.get(i);
//			System.out.println("Task "+T.getId()+"'s isIsrunning(): "+T.isIsrunning());
//			System.out.println("Task "+T.getId()+"'s isready: "+T.isready);
			boolean isisisisready = false;
			for(int ii=0;ii<list_ready.size();ii++)
			{
				Task T0 = (Task) list_ready.get(ii);
				if( (i+1) == T0.getId() ) isisisisready = true;
			}

			if( isisisisready ) 
				System.out.println("Task "+T.getId()+"'s isisisisready: "+isisisisready);
		}
*/
		boolean isisisisready[] = new boolean[TASKS.size()];
		for(i=0;i<TASKS.size();i++)
		{
			Task T = (Task)TASKS.get(i);
//			System.out.println("Task "+T.getId()+"'s isIsrunning(): "+T.isIsrunning());
//			System.out.println("Task "+T.getId()+"'s isready: "+T.isready);
			isisisisready[i] = false;
			for(int ii=0;ii<list_ready.size();ii++)
			{

				Task T0 = (Task) list_ready.get(ii);
				if( (i+1) == T0.getId() ) isisisisready[i] = true;				
			}
//			if( isisisisready ) 
//				System.out.println("Task "+T.getId()+"'s isisisisready: "+isisisisready);
		}

		for(i=0;i<N-1;i++)
		{
			Task T = (Task)TASKS.get(i);

			for(k=0;k<numofB;k++)
			{
				if( (T.getOwnFieldIntValue("next_deadline") >= B[k]) && isisisisready[i]==true ) 
				{
//					tmin = (lnum[k]*T[i].rmax>T[i].rem)?T[i].rem:lnum[k]*T[i].rmax;
//					tmin = (tmin>CapW[k])?CapW[k]:tmin;
					double rmax = T.getOwnFieldIntValue("rate_max");
					double ret = T.getRET();
					tmin = (lnum[k]*rmax>ret)?ret:lnum[k]*rmax;
					tmin = (tmin>CapW[k])?CapW[k]:tmin;

					varx[i][k] = tmin;
				}
				else
					varx[i][k] = -1;
			}
		}
	}

	void cal_B(int t)
	{
		int i,j;

		numofB=0;
		for(i=0,j=0;i<N-1; i++)
		{
			// active tasks only
//			Task T = (Task) list_ready.get(i);
			// all tasks
			Task T = (Task)TASKS.get(i);

//			if(T[i].d<t)
			if( T.getOwnFieldIntValue("next_deadline") > t )
			{
				tmp[j] = T.getOwnFieldIntValue("next_deadline"); // D
				tmp[j+1] = T.getOwnFieldIntValue("next_period"); // P
				j+=2;
				numofB+=2;
			}
		}

//		System.out.println(" ---------------- bef sort ---------------- ");
//		for(i=0;i<numofB;i++)
//			System.out.println(" "+i+": "+tmp[i]);

//		Arrays.sort(tmp);
		selsort(tmp);

//		System.out.println(" ---------------- aft sort ---------------- ");
//		for(i=0;i<numofB;i++)
//			System.out.println(" "+i+": "+tmp[i]);
	}

	void selsort(int ttmp[])
	{
		int i, j, indexMin, temp, tmpnumb;

		// Selection sorting
		for (i = 0; i < numofB -1; i++)
		{
			indexMin = i;
			for (j = i + 1; j < numofB; j++)
				if (ttmp[j] < ttmp[indexMin])
					indexMin = j;
			temp = ttmp[indexMin];
			ttmp[indexMin] = ttmp[i];
			ttmp[i] = temp;
		}

		tmpnumb = numofB;
		for(i=0,j=0; i<numofB; i++)
		{
			if( (i != numofB-1) && (ttmp[i]==ttmp[i+1]) )
			{
				tmpnumb--;
				continue;
			}
			else
			{
				B[j++]=ttmp[i];
			}		
		}
		numofB = tmpnumb;
	}

	void tcal_W_l(int t)
	{
		int i;

		W[0][0] = t;
		W[0][1] = B[0];
		lnum[0] = W[0][1] - W[0][0];
		for(i=1; i<numofB; i++)
		{
			W[i][0] = B[i-1];
			W[i][1] = B[i];
			lnum[i] = W[i][1] - W[i][0];
		}
	}


	void printinfo()
	{
		int i,j;

		System.out.println("i   C\t\tD\t\tP\t\t\tr");
		for(i=0;i<N;i++)	
		{
			if (i == N-1)
			{
				System.out.println(i+":  - (c:"+taud_ret+")  \t-(d:"+taud_deadline+")  \t-(arrnext:-)     \t");
				continue;
			}
			
			Task T = (Task)TASKS.get(i);
			System.out.println(i+":  "+T.getWCET()+"(c:"+T.getRET()+")  \t"+T.getDeadline()+"(d:"+T.getOwnFieldIntValue("next_deadline")+")  \t"+T.getPeriod()+"(arrnext:"+T.getOwnFieldIntValue("next_period")+")     \t");
		}


		System.out.print("B={ ");
		for(i=0;i<numofB;i++)
		{
			System.out.print(B[i]+" ");		
		}
		System.out.println("}");

		System.out.print("W={ ");
		for(i=0;i<numofB;i++)
		{
			System.out.print("["+W[i][0]+","+W[i][1]+"] ");		
		}
		System.out.println("}");

		System.out.print("l={ ");
		for(i=0;i<numofB;i++)
		{
			System.out.print(lnum[i]+" ");		
		}
		System.out.println("}");

		System.out.print("CapW={ ");
		for(i=0;i<numofB;i++)
		{
			System.out.print(dformat.format(CapW[i])+" ");		
		}
		System.out.println("}");

		System.out.print("horisum={ ");
		for(i=0;i<N;i++)
		{
			System.out.print(dformat.format(horisum[i])+" ");		
		}
		System.out.println("}");

		System.out.println(" ======= T.varx ======= ");
		for(i=0;i<N;i++)
		{
			for(j=0;j<numofB;j++)
			{
				if(varx[i][j]>=0)
					System.out.print(dformat.format(varx[i][j])+" ");
				else
					System.out.print("   -   ");
			}
			System.out.println("");
		}
		System.out.println("");
	}

	public void onActivate(EvtContext c) {
		Task T = (Task)c.getCible();
		T.setOwnFieldIntValue("next_deadline", this.Kernel.getTime()+T.getDeadline());
		T.setOwnFieldIntValue("next_period", this.Kernel.getTime()+T.getPeriod());
		T.setOwnFieldIntValue("rate_max", 1);

		// Gaussian distribution with mean(m) and standard deviation(sd)
		Random r = new Random();			
		double m = (T.getBCET()+T.getWCET())/2.0;
		double sd = (T.getWCET()-T.getBCET())/6.0;
		double rn = r.nextGaussian()*sd+m; 
		// end of Gaussian
		T.setRet((int)Math.round(rn));
		// end of setRet()

		// The index of each job from a task stream
		T.setOwnFieldIntValue("job_idx", 0);
		// LLF
		// calcul de la prochaine date d'echeance
//		T.setOwnFieldIntValue("next_deadline", this.Kernel.getTime()+T.getDeadline());
		int d = T.getOwnFieldIntValue("next_deadline");
		// calcul du temps restant jusqu'a la prochaine date d'echeance
		int l = d-this.Kernel.getTime();
		// calcul de la laxite dynamique
		T.setOwnFieldIntValue("laxity", l-T.getRET());


		list_ready.add(T);		
//		int d = T.getOwnFieldIntValue("next_deadline");
//		System.out.println("* Activate time : "+ this.Kernel.getTime());
//		System.out.println("-Id = "+T.getId()+" - deadline = "+d);
		
		todo = true;
	}
	public void onUnReady(EvtContext c){
		this.onUnBlock(c);
	}

	// TASK RELEASE
	// task {c.getSource().getId()} just released
	public void onUnBlock(EvtContext c){
		Task T = (Task)c.getSource();
	
		if (T.isBegin()) {
			T.setOwnFieldIntValue("next_deadline", this.Kernel.getTime()+T.getDeadline());
			T.setOwnFieldIntValue("next_period", this.Kernel.getTime()+T.getPeriod());
			T.setOwnFieldIntValue("rate_max", 1);

			// The index of each job from a task stream
			T.setOwnFieldIntValue("job_idx", T.getOwnFieldIntValue("job_idx")+1);

			// calcul de la prochaine date d'echeance
			int d = T.getOwnFieldIntValue("next_deadline");
			// calcul du temps restant jusqu'a la prochaine date d'echeance
			int l = d-this.Kernel.getTime();
			// calcul de la laxite dynamique
			T.setOwnFieldIntValue("laxity", l-T.getRET());


//			int d = T.getOwnFieldIntValue("next_deadline");
//			System.out.println("* Unblock time : "+ this.Kernel.getTime());
//			System.out.println("-Id = "+T.getId()+" - deadline = "+d);

		}
		
		// Gaussian distribution with mean(m) and standard deviation(sd)
		Random r = new Random();			
		double m = (T.getBCET()+T.getWCET())/2.0;
		double sd = (T.getWCET()-T.getBCET())/6.0;
		double rn = r.nextGaussian()*sd+m;  
		// end of Gaussian
		T.setRet((int)Math.round(rn));
		// end of setRet()

		list_ready.add(T);
		todo = true;
	}

	// TASK FINISH
	// task {c.getSource().getId()} finished
	public void onBlock(EvtContext c){
		list_ready.remove(c.getCible());
		todo = true;
	}
	
	public void onTerminated(EvtContext c){
		list_ready.remove(c.getCible());
		todo = true;
	}
	
	public void onTick(){
		int q = this.getOwnFieldIntValue("quantum");
		nbTicks = (nbTicks+1)%q;
		if (nbTicks == 0) {
			tocompute = true;
			todo = true;
		}
	}

	public void sched(){
		System.out.println(" ======= At time t = "+this.Kernel.getTime()+" ======= ");
		cal_B(this.Kernel.getTime());
		tcal_W_l(this.Kernel.getTime());
		cal_CapW();
		cal_horisum();
		cal_varx();
		cal_taud();
//		printinfo();
		if(list_ready.size()!=0) make_lp();


		// LLF
		if (tocompute) {
			laxityCompute();
			tocompute = false;
		}


		if (todo) {
			select();
			todo = false;
		}
	}

	// LLF
	private void laxityCompute(){
		
//		System.out.println("* Compute time : "+ this.Kernel.getTime());
		for (int i=0; i<list_ready.size(); i++){
			Task T = (Task) list_ready.get(i);
			// recuperation de la prochaine date d'echeance
			int d = T.getOwnFieldIntValue("next_deadline");
			// calcul du temps restant jusqu'a la prochaine date d'echeance
			int l = d-this.Kernel.getTime();
			// calcul de la laxite dynamique
			T.setOwnFieldIntValue("laxity", l-T.getRET());

//		System.out.println("task("+T.getId()+","+T.getOwnFieldIntValue("job_idx")+")'s laxity = "+T.getOwnFieldIntValue("laxity")+" at "+this.Kernel.getTime());

			
		}
	}

	public void select() {
//		System.out.println("*** Select : "+ this.Kernel.getTime());
		
		Collections.sort(list_ready,list_ready);
		for (int i=0; i<list_ready.size(); i++){
			Task T = (Task) list_ready.get(i);
			
//			int d = T.getOwnFieldIntValue("next_deadline");
//			System.out.println("-Id = "+T.getId()+" - deadline = "+d);
		}
		
//		ArrayList CPUS =this.Kernel.getTasksListeManager().getProcessors();
		int m = CPUS.size();
		
		for (int i=m; i<list_ready.size(); i++){
			Task T = (Task) list_ready.get(i);
			if (T.isIsrunning()) T.preempt();
		}
		
		int j = 0;
		for (int i=0; (i<m) && (i<list_ready.size()); i++){
			Task T = (Task) list_ready.get(i);
//			System.out.println("task"+i+"  Id = "+T.getId());
			if (!T.isIsrunning()) {
				Processor P = null;
				for (; j<m; j++){
					P = (Processor) CPUS.get(j);
					if (!P.isRunning()) {
						j++;
						break;
					}
				}
				T.runningOn(P);
			}
		}

/*
		System.out.println("*** Select : "+ this.Kernel.getTime());
		ArrayList TASKSTT;
		TASKSTT =this.Kernel.getTasksListeManager().getALLTask();
		for(int i=0;i<TASKS.size();i++)
		{
			Task T = (Task)TASKSTT.get(i);
//			System.out.println("Task "+T.getId()+"'s isIsrunning(): "+T.isIsrunning());
//			System.out.println("Task "+T.getId()+"'s isready: "+T.isready);
			boolean isisisisready = false;
			for(int ii=0;ii<list_ready.size();ii++)
			{
				Task T0 = (Task) list_ready.get(ii);
				if( (i+1) == T0.getId() ) isisisisready = true;
			}

			if( isisisisready ) 
				System.out.println("Task "+T.getId()+"'s isisisisready: "+isisisisready);
		}
*/
	}
}
