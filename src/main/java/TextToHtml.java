import org.fit.pdfdom.PDFDomTree;

import java.io.*;

public class TextToHtml {

    public static void main(String[] args){
       /*
        try{
            String outPath = TextToHtmlConverter("input-sample-2.txt");
        } catch (IOException e){
            System.out.println("Problem performing the conversion");
            e.printStackTrace();
        }
        */

    }

    public static String TextToHtmlConverter(String inputPath, String output) throws IOException {
        // Can make this void, that gets the path in which is should open the file.

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
        return outputPath;
    }

}
