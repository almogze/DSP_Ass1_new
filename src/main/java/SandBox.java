import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.fit.pdfdom.PDFDomTree;
import software.amazon.awssdk.services.ec2.model.IamInstanceProfileSpecification;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Base64;


import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudsearchdomain.model.Bucket;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Base64;
import java.util.List;


public class SandBox {

    final static AwsBundle awsBundle = AwsBundle.getInstance();

    public static void main(String[] args) throws IOException {

        String inputPath = "input-sample-2.txt";
        int count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line = reader.readLine();
            while (line != null){
                perform(line);
                line = reader.readLine();
                count ++;
            }
            // Creating new workers for work
        }catch (Exception e){
            System.out.println(e);
        }

    }

    private static String perform(String line) throws IOException {
        String outputPath = "File Not processed!";

        int ind = line.indexOf("\t");
        String command = line.substring(0, ind);
        String inPath = line.substring(ind+1);

        String[] splitted = inPath.split("/");
        String pdf_name = "output/" + splitted[splitted.length-1].substring(0, splitted[splitted.length-1].length()-4);

        String downloadedFile = DownloadFile(inPath, pdf_name);

        switch (command) {
            case "ToImage":
                // (try) generating an image from the first page of a pdf.
                try {
                    generateImageFromPDF(downloadedFile, pdf_name, "jpg");
                    outputPath = pdf_name + ".jpg";
                } catch (IOException e) {
                    System.out.println("Problem during transforming to an image.");
                    e.printStackTrace();
                    throw e;
                    //System.out.println("IOError in generating image process. In the near future - Change this catch, so the line written to the final file is: \"\"");
                }
                break;
            case "ToHTML":
                // (try) generating an html page from the first page of a pdf.
                try {
                    generateHTMLFromPDF(downloadedFile, pdf_name);
                    outputPath = pdf_name + ".html";
                } catch (IOException e) {
                    System.out.println("Problem with generating html.");
                    e.printStackTrace();
                    throw e;
                }
                break;
            case "ToText":
                // (try) generating a .text file from the first page of a pdf.
                try {
                    generateTextFromPDF(downloadedFile, pdf_name);
                    outputPath = pdf_name + ".txt";
                } catch (IOException e) {
                    System.out.println("Problem generating text file. Couldn't extract text.");
                    e.printStackTrace();
                    throw e;
                }
                break;
            default:
                System.out.println("No match for the command and our API!!");
                break;
        }
        System.out.println("Successfully downloaded and converted file.");
        return outputPath;
    }

    private static String DownloadFile(String filePath, String pdfName){
        String outPath = pdfName + ".pdf";

        System.out.println(filePath);

        try {
            //String fileUrl = filePath;
            URL url = new URL(filePath);
            // create an input stream to the file InputStream
            InputStream inputStream = url.openStream();
            // create a channel with this input stream
            ReadableByteChannel channel = Channels.newChannel( url.openStream());
            // create an output stream
            FileOutputStream fos = new FileOutputStream( new File(outPath));
            // get output channel, read from input channel and write to it
            fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
            // close resources
            fos.close();
            channel.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return outPath;
    }


    private static void generateImageFromPDF(String inputPath, String outputPath, String extension) throws IOException {

        PDDocument document = PDDocument.load(new File(inputPath));
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        for (int page = 0; page < 1 /*document.getNumberOfPages() */; ++page) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(
                    page, 300, ImageType.RGB);
            ImageIOUtil.writeImage(
                    bim, String.format(outputPath + "_pdf.%s", extension), 300);
        }
        document.close();
    }


    private static void generateTextFromPDF(String inputPath, String outputPath) throws IOException {
        if (inputPath == null) {
            System.out.println("No input file given!");
            return;
        }
        try (PDDocument document = PDDocument.load(new File(inputPath))) {
            AccessPermission ap = document.getCurrentAccessPermission();
            if (!ap.canExtractContent())
                throw new IOException("You don't have permissions to extract data from this file!!");

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            for (int p = 1; p < 2 /*document.getNumberOfPages()*/; ++p){

                // Set the page interval to extract. If you don't, then all pages would be extracted.
                stripper.setStartPage(p);
                stripper.setEndPage(p);

                // let the magic happen
                String text = stripper.getText(document);

                System.out.println(text.trim());

                try {
                    PrintWriter pw = new PrintWriter(outputPath + ".txt");
                    pw.print(text);
                    pw.close();
                } catch (FileNotFoundException e) { System.out.println("File not found, and not able to create a new one of this name!!"); }
            }
        }
    }

    private static void generateHTMLFromPDF(String inputPath, String outputPath) throws IOException{
        PDDocument doc = PDDocument.load(new File(inputPath));

        AccessPermission ap = doc.getCurrentAccessPermission();
        if (!ap.canExtractContent())
            throw new IOException("You don't have permissions to extract data from this file!!");

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);

        stripper.setStartPage(1);
        stripper.setEndPage(1);

        String text = stripper.getText(doc);

        PrintWriter pw = new PrintWriter(outputPath + ".html");

        PDFDomTree p = new PDFDomTree();
        p.setEndPage(1);
        p.writeText(doc, pw);

        doc.close();
    }





        // awsBundle.createBucketIfNotExists(AwsBundle.bucketName);

        // awsBundle.putS3Object(AwsBundle.bucketName, "DSP_Manager.jar", "DSP_Manager.jar");
        // awsBundle.putS3Object(AwsBundle.bucketName, "DSP_Worker.jar", "DSP_Worker.jar");

        // awsBundle.putS3Object(AwsBundle.bucketName, "HelloWorldProj.jar", "HelloWorldProj.jar");
        // System.out.println(ret);

/*
         String userData = "#! /bin/bash\n" +
                "echo \"I'm alive!\"\n" +
                "mkdir files\n" +
                // "aws s3 cp s3://" + AwsBundle.bucketName + "/HelloWorldProj.jar ./files\n" +
                "aws s3 cp s3://" + AwsBundle.bucketName + "/DSPAss1New.jar ./files\n" +
                "echo \"Downloaded files succesfully!\"\n" +
                "echo \"Attempting to run the files: \"\n" +
                "java -cp ./files/DSPAss1New.jar SandBox";
                // "java -cp ./files/HelloWorldProj.jar HelloWorld";

        awsBundle.createEC2Instance("First_of_many3", AwsBundle.ami, userData);
*/









        // awsBundle.createEC2Instance("Leader", AwsBundle.ami, "");

        // System.out.println(awsBundle.checkIfInstanceExist("Leader"));

        // System.out.println(awsBundle.getAmountOfRunningInstances());

        // awsBundle.createBucketIfNotExists(AwsBundle.bucketName);

        // awsBundle.putS3Object(AwsBundle.bucketName, "HelloWorldProj.jar", "HelloWorldProj.jar");

        // awsBundle.getS3Object(AwsBundle.bucketName, "HelloWorldProj.jar", "src/HelloWorldProj.jar");

        // String QueueUrl = awsBundle.createQueue("myNewQueue");

        // String QueueUrl = awsBundle.getQueueUrl("myNewQueue");

        // System.out.println(QueueUrl);

        // awsBundle.sendMessage(QueueUrl, "My new message!!!!$@#$!@#$#@");

        // List<Message> message = awsBundle.receiveMessages(QueueUrl, 1);

        // System.out.println(message.get(0).body());


        // Deleting and terminating:

        // awsBundle.deleteMessages(QueueUrl, message);

        // awsBundle.deleteSQSQueueByName("myNewQueue");

        // awsBundle.deleteBucketObjects(AwsBundle.bucketName, "HelloWorldProj.jar");

        // awsBundle.terminateEC2("i-039c0fd7d7488d72e");

















        //awsBundle.deleteSQSQueue("firstQueueOfMany");

/*
        // awsBundle.createBucketIfNotExists(AwsBundle.bucketName);

        String ret = awsBundle.putS3Object(AwsBundle.bucketName, "HelloWorldProj.jar", "HelloWorldProj.jar");
        //System.out.println(ret);

        String userData = "#! /bin/bash\n" +
                "echo \"I'm alive!\"\n" +
                "mkdir files\n" +
                "aws s3 cp s3://" + AwsBundle.bucketName + "/HelloWorldProj ./files\n" +
                "echo \"Downloaded file succesfully!\"\n" +
                "echo \"Attempting to run the file: \"\n" +
                "ls\n" +
                "java -jar files/HelloWorldProj.jar";

        awsBundle.createEC2Instance("First_of_many", AwsBundle.ami, userData);

        String userData2 = "#! /bin/bash\n" +
                "echo \"I'm alive!\"\n" +
                "mkdir files\n" +
                "aws s3 cp s3://" + AwsBundle.bucketName + "/HelloWorldProj ./files\n" +
                "echo \"Downloaded file succesfully!\"\n" +
                "echo \"Attempting to run the file: \"\n" +
                "cd files\n" +
                "ls\n" +
                "java -jar HelloWorldProj.jar";


        awsBundle.createEC2Instance("First_of_many2", AwsBundle.ami, userData2);
*/



        /*

        awsBundle.createBucketIfNotExists("dspass1razalmog");

        String QueueUrl = awsBundle.createQueue("firstQueueOfMany");

        awsBundle.sendBatchMessages(QueueUrl);

        List<Message> messages = awsBundle.receiveMessages(QueueUrl, 10);

        for (Message m : messages){
            System.out.println(m.body());
        }

        awsBundle.deleteMessages(QueueUrl, messages);

        */


        /*
        String ManagerScript2 = "#! /bin/bash\n" +
                "echo \"I'm alive!\"\n" +
                "mkdir files\n" +
                "aws s3 cp s3://dspass1razalmog/Assignment1.pdf ./files\n" +
                "echo \"Downloaded file succesfully!\"";
                // "mkdir workerFiles\n" +
                // "sudo yum install -y java-1.8.0-openjdk\n" +
                // "sudo yum update -y\n" +
                // "aws s3 cp s3://assignment1razalmog/ocr-assignment1/JarFiles/DSPManager.jar ./ManagerFiles\n" +
                // "cat > MANIFEST.MF Main Class: Manager\n\n" +
                // "java -jar /ManagerFiles/DSPManager.jar\n";

        // awsBundle.createEC2Instance("First_of_many", AwsBundle.ami, ManagerScript2);


         */








































        //AwsBundle.createBucketIfNotExists(AwsBundle.bucketName);
        //AwsBundle.uploadFileToS3(AwsBundle.bucketName, "Ass1", "Assignment1.pdf");
        // AwsBundle.uploadFileToS3(AwsBundle.bucketName, "ocr-assignment1/JarFiles/DSPManager.jar", "DSPManager.jar");
        // AwsBundle.uploadFileToS3(AwsBundle.bucketName, "ocr-assignment1/JarFiles/DSPWorker.jar", "DSPWorker.jar");
/*
        String workerScript = "#! /bin/bash\n" +
                "mkdir workerFiles\n" +
                // "sudo yum install -y java-1.8.0-openjdk\n" +
                // "sudo yum update -y\n" +
                "aws s3 cp s3://assignment1razalmog/ocr-assignment1/JarFiles/DSPWorker.jar ./workerFiles\n" +
                //         "cat > MANIFEST.MF Main Class: Worker\n\n" +
                "java -jar /workerFiles/DSPWorker.jar\n";

         awsBundle.createInstance("Worker1", AwsBundle.ami, workerScript);


        String workerScript2 = "#! /bin/bash\n" +
                // "echo I'm alive!\n" +
                "mkdir workerFiles\n" +
                // "sudo yum install -y java-1.8.0-openjdk\n" +
                // "sudo yum update -y\n" +
                "aws s3 cp s3://assignment1razalmog/ocr-assignment1/JarFiles/DSPWorker.jar ./workerFiles\n" +
                "cat > MANIFEST.MF Main Class: Worker\n\n" +
                "java -jar /workerFiles/DSPWorker.jar\n";

        awsBundle.createInstance("Worker2", AwsBundle.ami, workerScript2);

        String ManagerScript1 = "#! /bin/bash\n" +
                // "echo I'm alive!\n" +
                "mkdir workerFiles\n" +
                // "sudo yum install -y java-1.8.0-openjdk\n" +
                // "sudo yum update -y\n" +
                "aws s3 cp s3://assignment1razalmog/ocr-assignment1/JarFiles/DSPManager.jar ./ManagerFiles\n" +
                "cat > MANIFEST.MF Main Class: Manager\n\n" +
                "java -jar /ManagerFiles/DSPManager.jar\n";

        awsBundle.createInstance("Manager1", AwsBundle.ami, ManagerScript1);
        String ManagerScript2 = "#! /bin/bash\n" +
                "echo \"I'm alive!\"\n" +
                "mkdir workerFiles\n" +
                // "sudo yum install -y java-1.8.0-openjdk\n" +
                // "sudo yum update -y\n" +
                "aws s3 cp s3://assignment1razalmog/ocr-assignment1/JarFiles/DSPManager.jar ./ManagerFiles\n" +
                // "cat > MANIFEST.MF Main Class: Manager\n\n" +
                "java -jar /ManagerFiles/DSPManager.jar\n";

        awsBundle.createInstance("Manager2", AwsBundle.ami, ManagerScript2);

*/
        // (awsBundle.getEc2()).shutdown();

        //String queueURL = awsBundle.createMsgQueue(awsBundle.requestsWorkersQueueName);
        //awsBundle.sendMessage(queueURL, "https://assignment1razalmog.s3.us-west-2.amazonaws.com/ocr-assignment1/JarFiles/DSPWorker.jar");






/*
        String workerScript = "#! /bin/bash\n" +
                "mkdir workerFiles\n" +
                "sudo yum install -y java-1.8.0-openjdk\n" +
                "sudo yum update -y\n" +
                "aws s3 cp s3://assignment1razalmog/ocr-assignment1/JarFiles/DSPWorker.jar ./workerFiles\n" +
                "cat > MANIFEST.MF Main Class: Worker\n\n" +
                "aws s3 rm s3://assignment1razalmog/ocr-assignment1/JarFiles/DSPWorker.jar\n";
               // "java -jar /workerFiles/DSPWorker.jar\n";
*/



/*
        String workerScript = "#! /bin/bash\n" +
                "mkdir workerFiles\n" +
                "sudo yum install -y java-1.8.0-openjdk\n" +
                "sudo yum update -y\n" +
                "aws s3 rm s3://assignment1razalmog/Ass1";
*/
/*
        String workerScript2 = "#! /bin/bash\n" +
                "mkdir workerFiles\n" +
                "sudo yum install -y java-1.8.0-openjdk\n" +
                "sudo yum update -y\n" +
                "aws s3 cp s3://ocr-assignment1/JarFiles/HelloWorld.jar ./workerFiles\n" +
                "java -jar /ManagerFiles/HelloWorld.jar\n";

        awsBundle.createInstance("Worker2", AwsBundle.ami, workerScript2);
*/

}
















