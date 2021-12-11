# Project Manual 
Id's: 205789001 - 313292120

Assignment 1 of the course Distributed Systems programming course given by Meni Adler, Semester 1 2022 (Starts at Oct' 21).

*all instances in our program are: ami-00e95a9222311e8ed  - micro
which is a linux EC2 that support java sdk.*

Activation guide:
The whole application is started from the Local class. By running it with the proper arguments, the functionality of this code is performed.
The task is execute by typing:  java -cp yourjar.jar Local inputFileName outputFileName n [terminate]

This will activate the main method in Local to start operating with the following arguments inputFileName outputFileName n [terminate] (where [terminate] is optional).

There is a secound option to run the program with maven by writing: 
mvn exec:java -Dexec.mainClass="Local" -Dexec.args="input file output file ratio terminate".

This command will create a local, which will in turn create a manager (if not already exists), and the relevant queues.
 
When AWS is finished initializing the manager instance, the manager starts by checking if a new local wants to connect. 

 If a local wants to connect, the manager creates a Thread from a threadpool of the ExecutorService it possesses and this thread will 'take care' of this specific local (creating a new task, performing it and sending the result to the local).
 
 If the message recived is "terminate", the manager will not accept more locals and tasks. After the Manager finishs all tasks, the manager will:
 1. Delete all queues.
 2. Terminate gracefully all running instances.
 3. Terminate itself.
 
 An output file with the name 'output file name' will appear as an HTML file in the local diractory at the end, with content as requested in the assignment API.

 
 Our progran work for 5 minutes on 'input-sample-2.txt' (ratio: 30) and for 10 minutes for 'input-sample-1.txt' (ratio: 500).
 We worked with several ratios given several input files. We made sure not to pass 9 total instunces running at once.
 

