import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.fit.pdfdom.PDFDomTree;
import java.awt.image.BufferedImage;
import java.net.URL;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import java.io.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import software.amazon.awssdk.services.sqs.model.Message;


public class Worker {

    final static AwsBundle awsBundle = AwsBundle.getInstance();

    public static void main(String[] args){
        String RequestManagerQueueUrl = awsBundle.getQueueUrl(awsBundle.requestsWorkersQueueName);
        String ResultsManagerQueueUel = awsBundle.getQueueUrl(awsBundle.resultsWorkersQueueName);

        boolean gotMessage = false;

        while(!gotMessage){
            List<Message> messages= awsBundle.receiveMessages(RequestManagerQueueUrl, 1);
            if (!messages.isEmpty())
            {
                String [] result = messages.get(0).body().split(AwsBundle.Delimiter);
                int localID = Integer.parseInt(result[0]);
                int serialNum = Integer.parseInt(result[1]);
                String line = result[2];

                int ind = line.indexOf("\t");
                String command = line.substring(0, ind);
                String inPath = line.substring(ind + 1);

                String outputFilePath = null;
                try {
                    outputFilePath = perform(line);
                    String outPath = AwsBundle.outputFolder + localID + "/" + serialNum + "/" + outputFilePath;
                    awsBundle.putS3Object(AwsBundle.bucketName, outPath, outputFilePath);
                    String outPathInS3 = AwsBundle.bucketName + "\\" + outPath + "/" + outputFilePath;

                    // Put a message in an SQS queue indicating the original URL of the PDF, the S3 url of the new
                    //image file, and the operation that was performed.

                    String outMessage =  localID + AwsBundle.Delimiter + serialNum + AwsBundle.Delimiter + command + ": " + outPathInS3 + " " + inPath;
                    awsBundle.sendMessage(ResultsManagerQueueUel, outMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                    String outMessage =  localID + AwsBundle.Delimiter + serialNum + AwsBundle.Delimiter + inPath + " " + e;
                    awsBundle.sendMessage(ResultsManagerQueueUel, outMessage);
                }
                awsBundle.deleteMessages(RequestManagerQueueUrl, messages);
            }
        }
    }

    private static String perform(String line) throws IOException {
        String outputPath = "File Not processed!";

        int ind = line.indexOf("\t");
        String command = line.substring(0, ind);
        String inPath = line.substring(ind+1);

        String[] splitted = inPath.split("/");
        String pdf_name = splitted[splitted.length-1].substring(0, splitted[splitted.length-1].length()-4);

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
                System.out.println("No match for the command in our API!!");
                break;
        }
        System.out.println("Successfully downloaded and converted file.");
        return outputPath;
    }

    private static String DownloadFile(String filePath, String pdfName) throws IOException {
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
            System.out.println("problem downloading file");
            e.printStackTrace();
            throw e;
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
                    bim, outputPath + String.format("_pdf.%s", extension), 300);
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
                } catch (FileNotFoundException e) {
                    System.out.println("File not found, and not able to create a new one of this name!!");
                    throw e;
                }
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

}