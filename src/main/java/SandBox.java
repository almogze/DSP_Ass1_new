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
    static int counter = 0;

    public static void main(String[] args) throws IOException {

        // mvn exec:java -Dexec.mainClass="SandBox" -Dexec.args="input-sample-1.txt outPutHTML 200 terminate"

        //awsBundle.createEC2Instance("almog" , AwsBundle.ami, "");

        //awsBundle.putS3Object(AwsBundle.bucketName, "ocr-assignment1/JarFiles/DSP_Manager.jar", "D:\\University\\2022\\DSPS\\DSP_Ass1_new\\out\\artifacts\\DSP_Ass1_new_jar\\DSP_Manager.jar");
        awsBundle.putS3Object(AwsBundle.bucketName, "ocr-assignment1/JarFiles/DSP_Worker.jar", "D:\\University\\2022\\DSPS\\DSP_Ass1_new\\out\\artifacts\\DSP_Ass1_new_jar\\DSP_Worker.jar");

        // boolean check = awsBundle.checkIfInstanceExist("almog");
        // System.out.println(check);
        // if(!check)
        //    awsBundle.createEC2Instance("almog" , AwsBundle.ami, "");
    }

}

















