import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.io.*;

import software.amazon.awssdk.services.sqs.model.Message;

public class Local {

    final static AwsBundle awsBundle = AwsBundle.getInstance();

    public static void main(String[] args) {


        // mvn exec:java -Dexec.mainClass="Local" -Dexec.args="input-sample-1.txt outPutHTML 200 terminate"

        //awsBundle.putS3Object(AwsBundle.bucketName, "ocr-assignment1/JarFiles/DSP_Manager.jar", "D:\\University\\2022\\DSPS\\DSP_Manager\\out\\artifacts\\DSP_Manager_jar\\DSP_Manager.jar");
        //awsBundle.putS3Object(AwsBundle.bucketName, "ocr-assignment1/JarFiles/DSP_Worker.jar", "D:\\University\\2022\\DSPS\\DSP_Worker\\out\\artifacts\\DSP_Worker_jar\\DSP_Worker.jar");


        final String uniqueLocalId = "1";
        final String uniquePathLocalApp =  AwsBundle.inputFolder + uniqueLocalId + "/";
        boolean shouldTerminate = false;

        if(args.length == 3 || args.length == 4) {
            if (args.length == 4) {
                if (args[3].equals("terminate"))
                    shouldTerminate = true;
                else {
                    System.err.println("Invalid command line argument: " + args[3]);
                    System.exit(1);
                }
            }
        }
        else {
            System.err.println("Invalid number of command line arguments");
            System.exit(1);
        }

        if (!isLegalFileSize(args[0]))
        {
            // System.out.println(args[0]);
            System.out.println("Input file is over maximal size (10MB)");
            System.exit(1);
        }

        if(!awsBundle.checkIfInstanceExist("Manager")){
                createManager();
        }

        // Creating bucket and loading file to S3
        awsBundle.createBucketIfNotExists(AwsBundle.bucketName);
        awsBundle.putS3Object(AwsBundle.bucketName,uniquePathLocalApp + args[0], args[0]);

        String localManagerQueueUrl = awsBundle.createQueue(awsBundle.localManagerConnectionQueue);
        String managerRequestQueueUrl = awsBundle.createQueue(awsBundle.requestsAppsQueueName);
        String managerResultQueueUrl = awsBundle.createQueue(awsBundle.resultsAppsQueueName);

        // Establishing new connection between the manager and the local
        awsBundle.sendMessage(localManagerQueueUrl, uniqueLocalId + AwsBundle.Delimiter + "new connection");

        // Send New Task to manager. args[2] is the ratio of tasks per worker.
        awsBundle.sendMessage(managerRequestQueueUrl, uniqueLocalId + AwsBundle.Delimiter + args[2] + AwsBundle.Delimiter + uniquePathLocalApp + args[0]);
        System.out.println("New Task Message sent to manager");

        boolean taskFinished = false;
        while(!taskFinished){
            List<Message> messages= awsBundle.receiveMessages(managerResultQueueUrl, 1);
            if (!messages.isEmpty()){
                String [] result = messages.get(0).body().split(AwsBundle.Delimiter);
                if(result[0].equals(uniqueLocalId)){
                    if(result[1].equals("end task")){
                        String S3BucketKeyName = result[2];
                        String tempOutputFile = "tempOutPutFile.txt";
                        String outputFileName = args[1];
                        awsBundle.getS3Object(AwsBundle.bucketName, S3BucketKeyName, tempOutputFile); // File from S3 will be extract into tempOutputFile
                        System.out.println("Local downloaded file successfully!");
                        try {
                            TextToHtmlConverter(tempOutputFile, outputFileName);
                        }catch (Exception e) {
                            System.out.println("Could not convert tempOutPutFile.txt to HTML file: " + e);
                        }
                    }
                    awsBundle.deleteMessages(managerResultQueueUrl, messages);
                   taskFinished = true;
                }
            }
        }

        if(shouldTerminate) {
            awsBundle.sendMessage(localManagerQueueUrl, "terminate");
        }


    }

    /**
     * Convert text file to an html file.
     * @param inputPath: Input path of the file to convert.
     * @param output: Path to save the new html file in.
     * @throws IOException if unable to open the file.
     */
    public static void TextToHtmlConverter(String inputPath, String output) throws IOException {

        File inputFile = new File(inputPath);
        BufferedReader br = new BufferedReader(new FileReader(inputFile));

        String outputPath = output + ".html";
        File outputFile = new File(outputPath);

        if (outputFile.createNewFile()) {
            System.out.println("output file created, at path: " + outputPath);
        }
        else{
            System.out.println("File already exists. Oh well.");
        }

        FileWriter osw = new FileWriter(outputFile);
        osw.write("<h1><center>This is the output file!</h1>");
        String line;
        while ((line = br.readLine()) != null) {
            osw.write(line + "<br>");
        }

        osw.close();
    }

    /**
     * Check legality of the input file.
     * @param filePath: Path to the input file in memory.
     * @return true if legal size, false else.
     */
    private static boolean isLegalFileSize(String filePath){
        try{
            System.out.println("Size: " + Files.size(Paths.get(filePath)));
            if (Files.size(Paths.get(filePath)) < 10 * (Math.pow(2, 20)))
                return true;
            else
                return false;
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Problem with file size!");
        }
        return false;
    }

    /**
     * Create the manager instance.
     */
    private static void createManager()
    {
        String managerScript = "#! /bin/bash\n" +
                "mkdir ManagerFiles\n" +
                "aws s3 cp s3://" + AwsBundle.bucketName + "/ocr-assignment1/JarFiles/DSP_Manager.jar ./ManagerFiles\n" +
                "java -cp ./ManagerFiles/DSP_Manager.jar Manager\n";

        awsBundle.createEC2Instance("Manager",AwsBundle.ami,managerScript);
    }
}

