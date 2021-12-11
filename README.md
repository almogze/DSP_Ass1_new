# Project Manual 
id's: 205789001 - 313292120

Assignment 1 of the course Distributed Systems programming course given by Meni Adler, Semester 1 2022 (Starts at Oct' 21).

*all instances in our program are: ami-00e95a9222311e8ed  - micro
which is a linux EC2 that support java sdk.*

Activation guide:
The whole application is started from the Local class. By running it with the proper arguments, the functionality of this code is performed.
The task is execute by typing:  java -cp yourjar.jar Local inputFileName outputFileName n [terminate]
This will cause the class Main Local to start operate with the following arguments inputFileName outputFileName n [terminate] (where [terminate] is optional).

There is a secound option to run the program from the code itself by writing: 
mvn exec:java -Dexec.mainClass="Local" -Dexec.args="<input file> <output file> <ratio> <terminate>"

This command will create a local, by its turn will create a manager (if not already exsist), queues between the manager and the local.
 
When AWS finish initialize the manager instance, the manager start checking if a new local want to connect or terminate. 

 If a local want to connect, the manager create a Thread from a threadpool in the ExecutorService and this thread will take care for this specific local (creating new task and sending the result to the local).
 
 If the message recived is "terminate", the manager will not accept more locals and tasks. After finish all tasks, the manager will:
 1. Delete all queues
 2. Terminate gracefully all instances
 3. Terminate itself
 
 An output file with the name <output file name> will appear as an HTML file in the local diractory at the end.

 
 Our progran work for 2 minutis on 'input-sample-2.txt' and for 10 minuits for 'input-sample-1.txt'.
 The ratio we used for 'input-sample-2.txt' was n = 25, i.e 4 instances.
 The ratio for 'input-sample-1.txt' was the max number of instances we could get using a student profile on amason web servies (9 instances at the most)
 i.e:  n = 300.
