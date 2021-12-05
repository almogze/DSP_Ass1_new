import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Manager {
    public static CountDownLatch countDownLatch;
    public static void main(String[] args) {
        AwsBundle awsBundle = AwsBundle.getInstance();
        awsBundle.createQueue(awsBundle.requestsWorkersQueueName);
        awsBundle.createQueue(awsBundle.resultsWorkersQueueName);

        //
        awsBundle.createQueue(awsBundle.debuggingQueueName);


        String localManagerConnectionQueueUrl = awsBundle.getQueueUrl(awsBundle.localManagerConnectionQueue);

        //
        awsBundle.sendMessage(awsBundle.getQueueUrl(awsBundle.debuggingQueueName), "Created Queues");


        ThreadPerClient tpcLocals = new ThreadPerClient();
        Workers workers = Workers.getInstance(awsBundle);

        //
        awsBundle.sendMessage(awsBundle.getQueueUrl(awsBundle.debuggingQueueName), "Created Workers and TPC");


        boolean shouldTerminate = false;

        while (!shouldTerminate){
            List<Message> messages = awsBundle.receiveMessages(localManagerConnectionQueueUrl, 1);
            if (!messages.isEmpty()){
                if (messages.get(0).body().equals("terminate")){

                    //
                    awsBundle.sendMessage(awsBundle.getQueueUrl(awsBundle.debuggingQueueName), "Terminating");


                    shouldTerminate = true;
                }else if(messages.get(0).body().split(AwsBundle.Delimiter)[1].equals("new connection")){

                    //
                    awsBundle.sendMessage(awsBundle.getQueueUrl(awsBundle.debuggingQueueName), "Creating Local Handler");


                    tpcLocals.execute(new LocalHandler(awsBundle, workers, Integer.parseInt(messages.get(0).body().split(AwsBundle.Delimiter)[0])));
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
