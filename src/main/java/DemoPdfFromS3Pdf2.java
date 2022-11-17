import com.amazon.textract.pdf.ImageType;
import com.amazon.textract.pdf.PDFDocument;
import com.amazon.textract.pdf.TextLine;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DemoPdfFromS3Pdf2 {
    public void run(String bucketName, String documentName, String outputDocumentName, String startJobId) throws IOException, InterruptedException {

        System.out.println("Generating searchable pdf from: " + bucketName + "/" + documentName);

        //Extract text using Amazon Textract
        List<ArrayList<TextLine>> linesInPages = extractText(bucketName, documentName, startJobId);

        //Get input pdf document from Amazon S3
        InputStream inputPdf = getPdfFromS3(bucketName, documentName);

        //Create new PDF document
        PDFDocument pdfDocument = new PDFDocument();

        //For each page add text layer and image in the pdf document
        PDDocument inputDocument = PDDocument.load(inputPdf);
        PDFRenderer pdfRenderer = new PDFRenderer(inputDocument);
        BufferedImage image = null;
        for (int page = 0; page < inputDocument.getNumberOfPages(); ++page) {
            image = pdfRenderer.renderImageWithDPI(page, 300, org.apache.pdfbox.rendering.ImageType.RGB);

            pdfDocument.addPage(image, ImageType.JPEG, linesInPages.get(page));

            System.out.println("Processed page index: " + page);
        }

        //Save PDF to stream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        pdfDocument.save(os);
        pdfDocument.close();
        inputDocument.close();

        //Upload PDF to S3
        UploadToS3(bucketName, outputDocumentName, "application/pdf", os.toByteArray());

        System.out.println("Generated searchable pdf: " + bucketName + "/" + outputDocumentName);
    }

    private List<ArrayList<TextLine>> extractText(String bucketName, String documentName, String startJobId) throws InterruptedException {

        AmazonTextract client = AmazonTextractClientBuilder.defaultClient();

        // StartDocumentAnalysisRequest req = new StartDocumentAnalysisRequest()
        //         .withDocumentLocation(new DocumentLocation()
        //                 .withS3Object(new S3Object()
        //                         .withBucket(bucketName)
        //                         .withName(documentName)))
        //         .withJobTag("DetectingText");

        // StartDocumentAnalysisResult startDocumentAnalysisResult = client.startDocumentAnalysis(req);
        // String startJobId = startDocumentAnalysisResult.getJobId();

        System.out.println("Text detection job started with Id: " + startJobId);

        GetDocumentAnalysisRequest DocumentAnalysisRequest = null;
        GetDocumentAnalysisResult response = null;

        String jobStatus = "IN_PROGRESS";

        while (jobStatus.equals("IN_PROGRESS")) {
            System.out.println("Waiting for job to complete...");
            TimeUnit.SECONDS.sleep(10);
            DocumentAnalysisRequest = new GetDocumentAnalysisRequest()
                    .withJobId(startJobId)
                    .withMaxResults(1);

            response = client.getDocumentAnalysis(DocumentAnalysisRequest);
            jobStatus = response.getJobStatus();
        }

        int maxResults = 1000;
        String paginationToken = null;
        Boolean finished = false;

        List<ArrayList<TextLine>> pages = new ArrayList<ArrayList<TextLine>>();
        ArrayList<TextLine> page = null;
        BoundingBox boundingBox = null;

        while (finished == false) {
            DocumentAnalysisRequest = new GetDocumentAnalysisRequest()
                    .withJobId(startJobId)
                    .withMaxResults(maxResults)
                    .withNextToken(paginationToken);
            response = client.getDocumentAnalysis(DocumentAnalysisRequest);

            //Show blocks information
            List<Block> blocks = response.getBlocks();
            for (Block block : blocks) {
                if (block.getBlockType().equals("PAGE")) {
                    page = new ArrayList<TextLine>();
                    pages.add(page);
                } else if (block.getBlockType().equals("LINE")) {
                    boundingBox = block.getGeometry().getBoundingBox();
                    page.add(new TextLine(boundingBox.getLeft(),
                            boundingBox.getTop(),
                            boundingBox.getWidth(),
                            boundingBox.getHeight(),
                            block.getText()));
                }
            }
            paginationToken = response.getNextToken();
            if (paginationToken == null)
                finished = true;
        }

        return pages;
    }

    private InputStream getPdfFromS3(String bucketName, String documentName) throws IOException {

        AmazonS3 s3client = AmazonS3ClientBuilder.defaultClient();
        com.amazonaws.services.s3.model.S3Object fullObject = s3client.getObject(new GetObjectRequest(bucketName, documentName));
        InputStream in = fullObject.getObjectContent();
        return in;
    }

    private void UploadToS3(String bucketName, String objectName, String contentType, byte[] bytes) {
        AmazonS3 s3client = AmazonS3ClientBuilder.defaultClient();
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(bytes);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(contentType);
        PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectName, baInputStream, metadata);
        s3client.putObject(putRequest);
    }
}
