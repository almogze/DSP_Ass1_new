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

        awsBundle.createBucketIfNotExists(AwsBundle.bucketName);
        awsBundle.putS3Object(AwsBundle.bucketName, "ocr-assignment1/JarFiles/DSP_Manager.jar", "DSP_Manager.jar");
        // awsBundle.putS3Object(AwsBundle.bucketName, "ocr-assignment1/JarFiles/DSP_Worker.jar", "DSP_Worker.jar");

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
















