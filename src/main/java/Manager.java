import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Manager {
    public static CountDownLatch countDownLatch;
    public static void main(String[] args) {
        AwsBundle awsBundle = AwsBundle.getInstance();
        awsBundle.createQueue(awsBundle.requestsWorkersQueueName);
        awsBundle.createQueue(awsBundle.resultsWorkersQueueName);

        String localManagerConnectionQueueUrl = awsBundle.getQueueUrl(awsBundle.localManagerConnectionQueue);

        // ThreadPerClient tpcLocals = new ThreadPerClient();
        ExecutorService threads = Executors.newFixedThreadPool(5);
        Workers workers = Workers.getInstance(awsBundle);

        boolean shouldTerminate = false;

        while (!shouldTerminate){
            List<Message> messages = awsBundle.receiveMessages(localManagerConnectionQueueUrl, 1);
            if (!messages.isEmpty()){
                if (messages.get(0).body().equals("terminate")){
                    shouldTerminate = true;
                }else if(messages.get(0).body().split(AwsBundle.Delimiter)[1].equals("new connection")){
                    threads.execute(new LocalHandler(awsBundle, workers, Integer.parseInt(messages.get(0).body().split(AwsBundle.Delimiter)[0])));
                }
                awsBundle.deleteMessages(localManagerConnectionQueueUrl, messages);
            }
        }

        terminateGracefully(workers, awsBundle);
    }

    public static void terminateGracefully(Workers workers, AwsBundle awsBundle){
        synchronized (workers){
            countDownLatch = new CountDownLatch(workers.getNumberOfRunningTasks());
            workers.setShouldTerminate();
        }
        try{
            countDownLatch.await();		// Wait for all WorkersHandlers to finish initialize method.
        } catch (Exception e) {System.out.println("problem accrued with countDownLatch await()."); }
        awsBundle.terminateAllIntancesButManager();
        deleteAll(awsBundle);
        awsBundle.terminateCurrentInstance();
    }

    private static void deleteAll(AwsBundle awsBundle){
        awsBundle.deleteSQSQueueByName(awsBundle.requestsWorkersQueueName);
        awsBundle.deleteSQSQueueByName(awsBundle.resultsWorkersQueueName);
        awsBundle.deleteSQSQueueByName(awsBundle.requestsAppsQueueName);
        awsBundle.deleteSQSQueueByName(awsBundle.resultsAppsQueueName);
        awsBundle.deleteSQSQueueByName(awsBundle.localManagerConnectionQueue);
    }
}
