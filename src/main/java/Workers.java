import java.util.concurrent.atomic.AtomicInteger;

public class Workers {

    private static Workers workers = null;
    private final AwsBundle awsBundle;
    private int workerCount;
    private int taskNum;
    private AtomicInteger numOfRunningTasks;
    private boolean shouldTerminate;

    public static Workers getInstance(AwsBundle awsBundle){
        if (workers == null){
            workers = new Workers(awsBundle);
        }
        return workers;
    }

    private Workers(AwsBundle awsBundle){
        this.awsBundle = awsBundle;
        this.workerCount = 0;
        this.taskNum = -1;
        this.numOfRunningTasks = new AtomicInteger(0);
        this.shouldTerminate = false;
    }

    private synchronized void createWorker()
    {
        String workerScript = "#! /bin/bash\n" +
                // "sudo yum install -y java-1.8.0-openjdk\n" +
                "mkdir WorkerFiles\n" +
                "aws s3 cp s3://" + AwsBundle.bucketName + "/ocr-assignment1/JarFiles/DSP_Worker.jar ./WorkerFiles\n" +
                "java -cp ./WorkerFiles/DSP_Worker.jar Worker\n";

        awsBundle.createEC2Instance("Worker" + this.workerCount++, AwsBundle.ami,workerScript);
    }

    // Creating new Workers for work
    public synchronized void createNewWorkersForTask(int count, int n){
        int k = awsBundle.getAmountOfRunningInstances() - 1;    //Number of active workers
        int m = count / n;                              // Number of required workers for new job
        int need_to_create = m - k;
        if (need_to_create + k + 1 < 19){
            System.out.println("Creating " + need_to_create + " new instances");
            for(int i = 0; i < need_to_create; i++){
                createWorker();
            }
        }
        else if (need_to_create > 0){
            int need_to_create2 = 18 - k;
            System.out.println("Creating " + need_to_create2 + " new instances");
            for(int i = 0; i < need_to_create2; i++){
                createWorker();
            }
        }
    }


    public synchronized int getTaskNum(){
        this.taskNum++;
        return this.taskNum;
    }

    public void increaseNumberOfRunningTasks(){
        this.numOfRunningTasks.incrementAndGet();
    }

    public void decreaseNumberOfRunningTasks(){
        this.numOfRunningTasks.decrementAndGet();
        if(shouldTerminate()){
            Manager.countDownLatch.countDown();
        }
    }

    public int getNumberOfRunningTasks(){
        return this.numOfRunningTasks.intValue();
    }

    public synchronized void setShouldTerminate(){
        this.shouldTerminate = true;
    }

    public synchronized boolean shouldTerminate(){
        return this.shouldTerminate;
    }
}