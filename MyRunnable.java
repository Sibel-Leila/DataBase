import java.util.concurrent.ExecutorService;

public class MyRunnable implements Runnable {
	int threadNo, max;
	ExecutorService tpe;
	
	public MyRunnable(int threadNo, int max, ExecutorService tpe) {
		this.threadNo = threadNo;
		this.max = max;
		this.tpe = tpe;
	}
		
	@Override
	public void run() {
		if (threadNo <= max) 
			tpe.submit(new MyRunnable(threadNo + 1, max, tpe));
	}
}
