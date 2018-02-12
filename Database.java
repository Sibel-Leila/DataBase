import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Database implements MyDatabase {
	ArrayList<Integer> threads;
	ArrayList<Table> tables;
	ExecutorService tpe;
	
	public Database() {
		tables = new ArrayList<Table>();
	}
	
	@Override
	public void initDb(int numWorkerThreads) {
		threads = new ArrayList<Integer>(numWorkerThreads);
		tpe = Executors.newFixedThreadPool(numWorkerThreads);

		for(int i = 0; i < numWorkerThreads; i++) {
			tpe.submit(new MyRunnable(i, numWorkerThreads, tpe));
		}
	}

	@Override
	public void stopDb() {
		tpe.shutdown();	
	}

	@Override
	public void createTable(String tableName, String[] columnNames, String[] columnTypes) {
		tables.add(new Table(tableName, columnNames, columnTypes));
	}

	@Override
	public void insert(String tableName, ArrayList<Object> values) {
		for(int i = 0; i < tables.size(); i++) {
			if (tables.get(i).name.equals(tableName)) {		// adaug in tabela corespunzatoare
				tables.get(i).insert(values);
			}
		}
	}

	@Override
	public void update(String tableName, ArrayList<Object> values, String condition) {
		int i = 0;
		for(i = 0; i < tables.size(); i++) 
			if (tables.get(i).name.equals(tableName))		// verific conditia
				break;
		
		int maxim;
		if(threads.isEmpty())
			tables.get(i).update(condition, values, 0, tables.get(i).table.size());
		else {
			maxim = tables.get(i).table.size() / threads.size();
		
			//aplic update pe tabela coresounzatoare
			for(int j = 0; j < threads.size(); j++)
				tables.get(i).update(condition, values, j, maxim);		//ce fac in cazul in care sunt de la 5 la 10
		}
	}
	
	@Override
	public ArrayList<ArrayList<Object>> select(String tableName, String[] operations, String condition) {
		ArrayList<ArrayList<Object>> newTable = new ArrayList<ArrayList<Object>>();

		int i = 0;
		for(i = 0; i < tables.size(); i++)
			if(tables.get(i).name.equals(tableName))
				break;
		
		// am tabela corespunzatoare din care scot datele
		int maxim;
		if(threads.isEmpty())
			newTable = tables.get(i).getSelect(operations, condition, 0, tables.get(i).table.size());
		else {
			maxim = tables.get(i).table.size() / threads.size();
		
			//aplic update pe tabela coresounzatoare
			for(int j = 0; j < threads.size(); j++)
				newTable.addAll(tables.get(i).getSelect(operations, condition, j, maxim));		//ce fac in cazul in care sunt de la 5 la 10
		}
	
		return newTable;
	}
	
	@Override
	public void startTransaction(String tableName) {

	}

	@Override
	public void endTransaction(String tableName) {
	}
}
