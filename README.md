# SearchablePDF
Currently just a proof of concept for textract OCR-enhance.

# Instructions

1. Setup AWS Account and AWS CLI using getting started with Amazon Textract.
2. Clone this repo
3. Install [Apache Maven](https://maven.apache.org/install.html) if it is not already installed.
4. In the project directory run `mvn package`.
5. Run: `java -cp target/searchable-pdf-1.0.jar DemoS3`` to run Java project with Demo as main class.

This will run on a sample file on a sample GHP home form (s3://sagemaker-coherehealth-1/tmp-eval-cedarfax/coverpgs/home 2.pdf) and create a new ml-ocrenhance pdf to s3://sagemaker-coherehealth-1/tmp-eval-cedarfax/coverpgs/home 2/ml-ocrenhance2.pdf.
This pdf will be searchable. The original pdf was searchable, but only the typed text. This one should have the handwriting textract identified as searchable as well. 
To not overwrite ml-ocrenhance2.pdf, change the output file name in DemoS3 so something unique. 

See https://github.com/aws-samples/amazon-textract-searchable-pdf for original repo. 
