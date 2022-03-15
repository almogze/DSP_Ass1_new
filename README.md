# Project Manual 

## Assignment 1 of the course Distributed Systems programming course given by Meni Adler, Semester 1 2022 (Starts at Oct' 21).

*all instances in our program are: ami-00e95a9222311e8ed  - micro
which is a linux EC2 that support java sdk.*

## Activation guide:

We start with a quick explanation of the roles of the different classes in the assingment:
  - Local: Starts the whole application, specifically, starts the Manager and uploads the input file to S3 memory.
  - Manager: Performs analysis of the input file (after downloading from S3), pushes the relevant messages (tasks) to the queue from which the workers take them later and starts the relevant amount of Workers, according to the ratio n, and the limit we have in amazon aws.
  - AwsBundle: The class which contains all functions that have to do with the interaction with amazon aws (i.e. starting instances, terminating them, uploading queues and so on). 
  - LocalHandler: The LocalHandler is the obejct which 'takes responsibility' for a certain local. This object holds a thread-pool (scalability) from which it generates threads to perform single tasks given from this single local.
  - TaskHandler: The TaskHandler is the object which performs a single task, i.e. a group of tasks derived from one input file (one request from a certain local). This object performs the process of instantiating the workers themselves (if needed), which in turn will perform the wanted task (Note: All workers perform all tasks, i.e. they don't care which local gave the task. So the taskHandler only makes sure there are enough workers (instances) up and running, and if not, starts the amount needed so that will be true.
  - Workers: A singletone class, which is shared among ALL threads made by the manager (all threads that have something to do with workers in this project).
            The workers class is very synchronized, since allot of instances turn to it to perform tasks in the project.
            
  - Worker: The instance (ec2) that performs each task (that the Manager pushed to the workersTasksQueue) by pulling a message from the workersTasksQueue, performing the wanted action on it, and uploading the relevant output file to S3, updating the Manager of it's actions (as depicted in the API of the assignment), and deleting the message when the it has finished performing it's functionality on it successfully.
Notice: The use of the visibility timeout used in order not to miss a message, and not perform a message twice.


![Local - Manager - Worker configuration](https://user-images.githubusercontent.com/73799544/145728739-faae4969-dce1-485a-8e7d-c0e22a141a07.jpg)

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
 
 
 
 
 
 
 
 
 
 
 
 
 
 

