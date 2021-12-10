import java.io.*;
import java.util.List;
import software.amazon.awssdk.services.sqs.model.Message;

public class TaskHandler implements Handler{

    private final AwsBundle awsBundle;
    private final String S3Details;
    private final int n;
    private final Workers workers;
    private int numOfTasks;
    private final int serialNum;
    private final int localID;

    public TaskHandler(AwsBundle awsBundle, Workers workers, String S3Details, int n, int localID) {
        this.awsBundle = awsBundle;
        this.workers = workers;
        this.S3Details = S3Details;
        this.n = n;
        this.numOfTasks = 0;
        this.serialNum = workers.getTaskNum();
        this.localID = localID;
    }

    /**
     * - first, initiate the number of workers needed to work in order to do the task
     * - create an output file to sent to Local via S3
     * - send a message to local with type 'end task' and the keyname to the output file
     */
    public void run() {
        this.numOfTasks = TaskMessage(this.S3Details, this.n);
        String workerResultQueueUrl = awsBundle.getQueueUrl(awsBundle.resultsWorkersQueueName);
        String localResultQueueUrl = awsBundle.getQueueUrl(awsBundle.resultsAppsQueueName);
        String outputPath = "output" + this.serialNum + ".txt";
        try {
            PrintWriter outputFile = new PrintWriter(outputPath);
            while(this.numOfTasks > 0){
                List<Message> messages_workers_result = awsBundle.receiveMessages(workerResultQueueUrl, 1);
                if (!messages_workers_result.isEmpty())
                {
                    String [] result = messages_workers_result.get(0).body().split(AwsBundle.Delimiter);
                    if(Integer.parseInt(result[0]) == this.localID && Integer.parseInt(result[1]) == this.serialNum){
                        outputFile.write(messages_workers_result.get(0).body().split(AwsBundle.Delimiter)[2] + "\n");
                        numOfTasks--;
                        awsBundle.deleteMessages(workerResultQueueUrl, messages_workers_result);
                    }
                }
            }
            outputFile.close();

            awsBundle.putS3Object(AwsBundle.bucketName, outputPath, outputPath);
            String message = this.localID + AwsBundle.Delimiter + "end task" + AwsBundle.Delimiter + outputPath + AwsBundle.Delimiter + outputPath;
            awsBundle.sendMessage(localResultQueueUrl, message);
            workers.decreaseNumberOfRunningTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param s3Details - bucket name and key name
     * @param n - ratio of # workers per # URLs
     * @return number of tasks need to be execute
     */
    private int TaskMessage(String s3Details, int n){


        String keyName = s3Details.split(AwsBundle.Delimiter)[2];
        String inputPath = "input" + this.serialNum + ".txt";
        int count = 0;      // Counter of the amount of urls in the file

        awsBundle.getS3Object(AwsBundle.bucketName, keyName, inputPath);  // File from S3 will be extract in inputPath
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String workerRequestQueueUrl = awsBundle.getQueueUrl(awsBundle.requestsWorkersQueueName);
            String line = reader.readLine();
            while (line != null){
                String message = this.localID + AwsBundle.Delimiter + this.serialNum + AwsBundle.Delimiter + line;
                awsBundle.sendMessage(workerRequestQueueUrl, message);
                line = reader.readLine();
                count ++;
            }
            // Creating new workers for work
            workers.createNewWorkersForTask(count, n);
        }catch (Exception e){
            System.out.println(e);
        }

        workers.increaseNumberOfRunningTasks();
        return count;
    }
}
