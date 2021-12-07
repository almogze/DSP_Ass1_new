import software.amazon.awssdk.services.sqs.model.Message;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalHandler implements Handler {

    // private final ThreadPerClient tpcWorkers;
    private final ExecutorService threads = Executors.newFixedThreadPool(5);
    private final AwsBundle awsBundle;
    private boolean gotResult;
    private int n;
    private final Workers workers;
    private final int localID;

    public LocalHandler(AwsBundle awsBundle, Workers workers, int localID) {
        // this.tpcWorkers = new ThreadPerClient();
        this.awsBundle = awsBundle;
        this.gotResult = false;
        this.workers = workers;
        this.localID = localID;
        this.n = 0;
    }

    /**
     * - monitor the local queue for new messages
     * - preform the desirable task related to the type of the message
     * - only finished when get termination message
     */
    public void run() {

        //
        awsBundle.sendMessage(awsBundle.getQueueUrl(awsBundle.debuggingQueueName), "Entered Local Handler");


        String localQueueUrl = awsBundle.getQueueUrl(awsBundle.requestsAppsQueueName);
        while(!this.gotResult)
        {

            //
            awsBundle.sendMessage(awsBundle.getQueueUrl(awsBundle.debuggingQueueName), "Entered Local Handler Loop");


            List<Message> messages_local = awsBundle.receiveMessages(localQueueUrl, 1);
            if (!messages_local.isEmpty())
            {

                //
                awsBundle.sendMessage(awsBundle.getQueueUrl(awsBundle.debuggingQueueName), "Local got new Message");


                String [] result = messages_local.get(0).body().split(AwsBundle.Delimiter);
                if(Integer.parseInt(result[0]) == this.localID){

                    //
                    awsBundle.sendMessage(awsBundle.getQueueUrl(awsBundle.debuggingQueueName), "Have the current local ID");


                    this.n = Integer.parseInt(result[1]);
                    // tpcWorkers.execute(new TaskHandler(this.awsBundle, this.workers, messages_local.get(0).body(), this.n, this.localID));
                    threads.execute(new TaskHandler(this.awsBundle, this.workers, messages_local.get(0).body(), this.n, this.localID));

                    awsBundle.deleteMessages(localQueueUrl, messages_local);
                }
            }
            if(workers.shouldTerminate()){this.gotResult = true;}
        }
    }
}
