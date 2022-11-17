public class DemoS3 {
    public static void main(String args[]) {
        try {
//            //Generate searchable PDF from pdf in Amazon S3 bucket
           DemoPdfFromS3Pdf2 s3Pdf = new DemoPdfFromS3Pdf2();
           s3Pdf.run("sagemaker-coherehealth-1", 
           "tmp-eval-cedarfax/coverpgs/home 2.pdf",
           "tmp-eval-cedarfax/coverpgs/home 2/ml-ocrenhance2.pdf",
           "69aa3ce6e6749b4d3983b8986c59ea3c2cfb57e6ba0b5ad7a02834d7db48d9e5"
        //    "024aa3449c24c8c073003e49a7c1b8f263acf1edd3637bdebbc014225549b83f"
           );
        //    "69aa3ce6e6749b4d3983b8986c59ea3c2cfb57e6ba0b5ad7a02834d7db48d9e5"
        //    );
        //    024aa3449c24c8c073003e49a7c1b8f263acf1edd3637bdebbc014225549b83f
//
//            //Generate searchable PDF from pdf in Amazon S3 bucket
//            //(by adding text to the input pdf document)
//            DemoPdfFromS3PdfAppend s3PdfAppend = new DemoPdfFromS3PdfAppend();
//            s3PdfAppend.run("ki-textract-demo-docs", "SampleInput.pdf", "SampleOutput.pdf");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}