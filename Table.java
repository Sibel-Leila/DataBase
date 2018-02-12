import java.util.ArrayList;
import java.util.StringTokenizer;

public class Table {
	String name;
	ArrayList<ArrayList<Object>> table;
	ArrayList<String> columnNames, columnTypes;

	int semaphoreMain, semaphoreRead, semaphoreWrite;
	int delayReader, useReader;
	int delayWriter, useWriter;
	
	public Table(String name, String[] columnNames, String[] columnTypes) {
		this.name = name;
		this.table = new ArrayList<ArrayList<Object>>();
		this.columnNames = new ArrayList<String>();
		this.columnTypes = new ArrayList<String>();
		
		for(int i = 0; i < columnNames.length; i++) {
			this.columnNames.add(columnNames[i]);
			this.columnTypes.add(columnTypes[i]);
		}
		
		semaphoreMain = 0;
		semaphoreRead = 0;
		semaphoreWrite = 0;
		
		delayReader = 0;
		useReader = 0;
		
		delayWriter = 0;
		useWriter = 0;
	}
	
	void insert(ArrayList<Object> values) {
		if (semaphoreMain > 0)
			semaphoreMain --;

		if (useReader > 0 || useWriter > 0){
			delayWriter ++;
			
			semaphoreMain ++;
			
			if (semaphoreWrite > 0)
				semaphoreWrite --;
		}
		
		useWriter ++;
		
		semaphoreMain ++;
	
		/* sectiune critica */
		int ok = 0;
		for(int i = 0; i < values.size(); i++) {
			switch (columnTypes.get(i)) {		// verific daca tipul coincide
				case "bool": {
					if (!(values.get(i) instanceof Boolean))
						ok = 1;
					break;
				}
				case "int": {
					if (!(values.get(i) instanceof Integer))
						ok = 1;
					break;
				}
				case "string": {
					if (!(values.get(i) instanceof String))
						ok = 1;
					break;
				}
			};
		}	
		if (ok == 0) {		// verific daca tipul valorii corespunde coloanei
			table.add(values);		// adaug pe ultima linie din tabel valorile din values
			//System.out.println("Am adaugat in tabela valorile: " + values);
		}
		/* sfarsitul sectiunii critice */
		
		if (semaphoreMain > 0)
			semaphoreMain --;

		useWriter --;
		
		if (delayReader > 0 && delayWriter == 0){
			delayReader --;
			semaphoreRead ++;
		} else if (delayWriter > 0){
			delayWriter --;
			semaphoreWrite ++;
		} else if (delayReader == 0 && delayWriter == 0)
			semaphoreMain ++;
	}

	void update(String condition, ArrayList<Object> values, int size, int maxim) {
		if (semaphoreMain > 0)
			semaphoreMain = semaphoreMain - 1;

		if (useReader > 0 || useWriter > 0){
			delayWriter = delayWriter + 1;
			
			semaphoreMain = semaphoreMain + 1;
			
			if (semaphoreWrite > 0)
				semaphoreWrite = semaphoreWrite - 1;
		}
		
		useWriter = useWriter + 1;
		
		semaphoreMain = semaphoreMain + 1;
	
		/* sectiune critica */
		// asta trebuie impartita pe threaduri.... dar cum?
		
		// un thread face de la 0 la 4 deci ii da un submit in care max este 4, iar al doilea va avea max + 1, adica 5 pana la 10
		
		//int maxim = table.size() / threads;
		//System.out.println(maxim);
		//for(int i = 0; i < threads; i++) {
		
		for(int j = size * maxim; j < (size + 1) * maxim; j++) {
			if(check(condition, j)) {
				table.remove(j);
				table.add(j, values);
			}
		}
		//}
		/* sfarsitul sectiunii critice */
		
		if (semaphoreMain > 0)
			semaphoreMain = semaphoreMain - 1;

		useWriter = useWriter - 1;
		
		if (delayReader > 0 && delayWriter == 0){
			delayReader = delayReader - 1;
			semaphoreRead = semaphoreRead + 1;
		} else if (delayWriter > 0){
			delayWriter = delayWriter - 1;
			semaphoreWrite = semaphoreWrite + 1;
		} else if (delayReader == 0 && delayWriter == 0)
			semaphoreMain = semaphoreMain + 1;
	}
	
	ArrayList<ArrayList<Object>> getSelect(String[] operations, String condition, int size, int maxim) {
		if(semaphoreMain > 0)
			semaphoreMain = semaphoreMain - 1;

		if (useWriter > 0 || delayWriter > 0) {
			delayReader = delayReader + 1;
			
			semaphoreMain = semaphoreMain + 1;
			
			if(semaphoreRead > 0)
				semaphoreRead = semaphoreRead - 1;
		}

		useReader = useReader + 1;

		if (delayReader > 0) {
			delayReader = delayReader - 1;
			semaphoreRead = semaphoreRead + 1;
		} 
		else 
			if (delayReader == 0)
				semaphoreMain = semaphoreMain + 1;
		
		/* partea critica */
		ArrayList<ArrayList<Object>> newTable = new ArrayList<ArrayList<Object>>();
		boolean done = false;
		
		for(int i = 0; i < operations.length; i++) {	// verific daca contine functii de agregare
			if(done)
				break;
			if((operations[i].startsWith("min(") ||
			   operations[i].startsWith("max(") ||
			   operations[i].startsWith("sum(") ||
			   operations[i].startsWith("avg(") || 
			   operations[i].startsWith("count(")) && operations[i].endsWith(")")) {
				newTable.add(aggregationSelect(operations, condition));
				done = true;
			}
		}
		
		if(!done) {
			for(int i = size * maxim; i < (size + 1) * maxim; i++) {
				// verific pentru fiecare linie conditia
				if(check(condition, i)) {
					ArrayList<Object> line = new ArrayList<Object>();
					
					for(int j = 0; j < operations.length; j++) {
						//daca are numele coloanei
						for(int k = 0; k < columnNames.size(); k++)
							if(operations[j].equals(columnNames.get(k))) 
								line.add(table.get(i).get(k));
					}
					newTable.add(line);
				}
			}
		}
		/* sfarsitul partii critice */
		
		if(semaphoreMain > 0) 
			semaphoreMain = semaphoreMain - 1;

		useReader = useReader - 1;

		if (useReader == 0 && delayWriter > 0) {
			delayWriter = delayWriter - 1;
			
			semaphoreWrite = semaphoreWrite + 1;
		}
		else 
			if (useReader > 0 || delayWriter == 0)
				semaphoreMain = semaphoreMain + 1;
		
		/* parte noncritica */
		return newTable;
	}

	ArrayList<Object> aggregationSelect(String[] operations, String condition) {
		ArrayList<Object> line = new ArrayList<Object>();		// linia in care se adauga 
		
		for(int i = 0; i < operations.length; i++) {
			StringTokenizer st = new StringTokenizer(operations[i], "()");
			String operation = st.nextToken();
			String column = st.nextToken();
			
			switch(operation) {
			case "count": {				
				line.add(count(condition));
				break;
			}
			case "min": {
				line.add(min(column));
				break;
			}
			case "max": {
				line.add(max(column));
				break;
			}
			case "sum": {
				line.add(sum(column));
				break;
			}
			case "avg": {
				line.add(avg(column));
				break;
			}
			};
			
		}
		return line;
	}
	
	int count(String condition) {
		int count = 0;
		for(int j = 0; j < table.size(); j++)
			if(check(condition, j))
				count ++;
		return count;
	}
	
	int column(String column) {		//return numarul coloanei care are aceeasi denumire ca si stringul column
		int j;
		for(j = 0; j < columnNames.size(); j++)
			if(columnNames.get(j).equals(column))
				break;
		return j;
	}
	
	int min(String column) {
		int min = Integer.MAX_VALUE;
		int j = column(column);
		if (columnTypes.get(j).equals("int")) {
			for(int k = 0; k < table.size(); k++) {
				if(min > (Integer) table.get(k).get(j)) {
					min = (Integer) table.get(k).get(j);
				}							
			}
		}
		return min;
	}
	
	int max(String column) {
		int max = Integer.MIN_VALUE;
		int j = column(column);
		if (columnTypes.get(j).equals("int")) {
			for(int k = 0; k < table.size(); k++) {
				if(max < (Integer) table.get(k).get(j)) {
					max = (Integer) table.get(k).get(j);
				}
			}
		}
		return max;
	}
	
	int sum(String column) {
		int j = column(column), sum = 0;
		if (columnTypes.get(j).equals("int")) {
			for(int k = 0; k < table.size(); k++) {
				sum += (Integer) table.get(k).get(j);						
			}
		}
		return sum;
	}
	
	float avg(String column) {
		int sum = sum(column);
		
		return (float) sum / table.size();
	}
	
	boolean check(String condition, int line) {
		StringTokenizer st = new StringTokenizer(condition);
		String columnName = st.nextToken();
		String compare = st.nextToken();
		String value = st.nextToken();
		
		int i;
		for(i = 0; i < columnNames.size(); i++)
			if (columnNames.get(i).equals(columnName))		//gasesc coloana
				break;
		
		switch(columnTypes.get(i)) { 		// gasesc tipul coloanei si verific daca este string, bool sau int
			case "bool": {
				boolean newValue = Boolean.parseBoolean(value);

				if(compare.equals("==")) {
					boolean elem = (Boolean) table.get(line).get(i);
							
					if(elem == newValue)
						return true;
				}

				if(compare.equals("!=")) {
					boolean elem = (Boolean) table.get(line).get(i);
						
					if(elem != newValue)
						return true;
				}
				break;
			}
			case "string": {
				if(compare.equals("==")) {
					String elem = (String) table.get(line).get(i);
						
					if(elem.equals(value))
						return true;
				}
	
				if(compare.equals("!=")) {
					String elem = (String) table.get(line).get(i);
						
					if(!elem.equals(value))
						return true;
				}
				break;
			}
			case "int": {
				int newValue = Integer.parseInt(value);		// transform valoarea in int
				
				if(compare.equals("==")) {
					int elem = (Integer) table.get(line).get(i);
					
					if(elem == newValue) {
						return true;
					}
				}

				if(compare.equals("<")) {
					int elem = (Integer) table.get(line).get(i);
					
					if(elem < newValue) {	
						return true;
					}
				}
				
				if(compare.equals(">")) {
					int elem = (Integer) table.get(line).get(i);
					
					if(elem > newValue) {
						return true;
					}
				}
							
				break;
			}
		};
		return false;
	}
}
