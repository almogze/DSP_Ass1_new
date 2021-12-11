import software.amazon.awssdk.services.sqs.model.Message;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalHandler implements Handler {

    private final ExecutorService threads = Executors.newFixedThreadPool(5);
    private final AwsBundle awsBundle;
    private boolean gotResult;
    private int n;
    private final Workers workers;
    private final int localID;

    public LocalHandler(AwsBundle awsBundle, Workers workers, int localID) {
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
        String localQueueUrl = awsBundle.getQueueUrl(awsBundle.requestsAppsQueueName);
        while(!this.gotResult)
        {
            List<Message> messages_local = awsBundle.receiveMessages(localQueueUrl, 1);
            if (!messages_local.isEmpty())
            {
                String [] result = messages_local.get(0).body().split(AwsBundle.Delimiter);
                if(Integer.parseInt(result[0]) == this.localID){
                    this.n = Integer.parseInt(result[1]);
                    threads.execute(new TaskHandler(this.awsBundle, this.workers, messages_local.get(0).body(), this.n, this.localID));

                    awsBundle.deleteMessages(localQueueUrl, messages_local);
                }
            }
            if(workers.shouldTerminate()){this.gotResult = true;}
        }
    }
}
