------------------------------ Baza de date minimalista------------------------------------

Nume: Bechir Sibel-Leila
Noiembrie 2017

	Am implementat functiile de insert, update si select in Java. 
	
	Am implementat clasele (de la 1 la 4):
1. Database 
2. Table
3. MyLock
4. MyRunnable
5. Others

------------
1. Database
	Contine un vector de inturi in care se memoreaza dimensiunea threadurilor pentru fiecare tabela, o lista de tabele si un Executor Service

	In initDb initializez threadurile. Iitializez executor service-ul tpe caruia ii adaug niste threaduri.

	In stopDb opresc executorService-ul.

	In insert/update/select caut tabela corespunzatoare cu numele tableName, caruia ii fac insert/update/select.

---------
2. Table
	Contine numele tabelei, o lista de liste care contine lista, o lista care contine numele coloanelor, o lista care contine tipurile listelor, semafoare (care corespund cu paradigma cititor scriitor din curs) si 3 lockuri.
	In constructor instantiez toate variabilele.
	
2.1. Insert
- Setez semaforul ca fiind ocupat, verific daca trebuie intarziat scriitorul apoi il deblocez.
- Verific  daca valoarea coincide cu tipul coloanei. Adaug la sfarsitul tabelei valorile mele.
- Ca apoi sa setez semaforul ca ocupat si verific daca trebuie trezit un cititor sau un alt scriiitor sau deblochez tabela.
	
2.2. Update
- Setez semaforul ca fiind ocupat, verific daca trebuie intarziat scriitorul apoi il deblocez.
- Iterez prin matrice iar in cazul in care conditia este valida sterg eleentul de pe pozitia j si adaug linia corespunzatoare.
- Ca apoi sa setez semaforul ca ocupat si verific daca trebuie trezit un cititor sau un alt scriiitor sau deblochez tabela.

2.3. GetSelect
- setez tabela ca fiind ocupata, verific daca trebuie sa intarzii un cititor apoi trezesc un cititor.
- am o variabila booleana done cu care verific daca exista o functie de agregare (tratez cazul intr-o alta functie), daca nu iterez prin matrice si adaug in noua mea tabela newtable linii.
- liniile se construiesc pe baza formatului care ar trebui sa fie construita tabela.
- adica pentru fiecare nume din coloana, adaug celula corespunzatoare 
- blochez tabela apoi trezesc un cititor sau deblochez tabela
- returnez tabela noua.

  2.3.2. AggregationSelect
  - in cazul in care exista functii de agregare creez o linie 
  - despart fiecare operatie in operations in tokeni si le prelucrez in functii separate. ca apoi sa adaug elementele pe aceeasi linie.

    2.3.2.1. Count
    - numar liniile indiferent de tip

    2.3.2.2. Column
    - returnez numarul coloanei careia ii coresounde numele.

    2.3.2.3. Min
    - gasesc numarul coloanei
    - verific tipul coloanei daca este de tip intreg
    - determin mininul coloanei

    2.3.2.4. Max
    - gasesc numarul coloanei
    - verific tipul coloanei daca este de tip intreg
    - determin maximul coloanei

    2.3.2.4. Sum
    - gasesc numarul coloanei
    - verific tipul coloanei daca este de tip intreg
    - determin suma coloanei

    2.3.2.4. Avg
    - gasesc numarul coloanei
    - determin suma coloanei
    - returnez un float care reprezinta media aritmetica a coloanei 

2.4 Check
- cea mai IMPORTANTA metoda, o folosesc atat in update cat si in select
- despart conditia in tokeni
- gasesc numarul coloanei care corespunde cu numele coloanei din conditie
- in functi de tipul coloanei sortez conditiile

  2.4.1. Boolean
  - transform elementul din conditie in boolean 
  - verific daca operatia coincide cu "==" sau "!="
  - daca da, returnez adevarat
  
  2.4.2. String
  - transform elementul din conditie in string 
  - verific daca operatia coincide cu "==" sau "!="
  - daca da, returnez adevarat
  
  2.4.3. Intreg
  - transform elementul din conditie in intreg 
  - verific daca operatia coincide cu "==" sau ">" sau "<"
  - daca da, returnez adevarat
  
- altfel returnez fals.

----------
3. MyLock
    Contine o variabila booleana care este true cand se initializeaza.
- metoda lock imi seteaza variabila pe false in cazul in care este diferit de true, ceea ce blocheaza functia de select, update sau insert.
- matoda unlock seteaza variabila x pe true care deblocheaza threadurile

--------------
4. MyRunnable
- implementeaza interfata Runnable
- constructorul instantiaza numarul threadului, numarul total de threaduri si executorService corespunzator
- matoda run este doar supradefinita.

----------
5. Others
- In urma rularii comenzii "ant compile jar", va trebui sa rezulte ın radacina un fisier numit "database.jar", care
va putea fi rulat cu comanda "java -jar database.jar". Aceasta comanda va rula suita de teste.
- versiune java: openjdk version "1.8.0_151"