package com.csye6225.springbootapi.storageservice.s3;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.csye6225.springbootapi.pojo.Attachment;

@Service
public class AmazonClient {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private AmazonS3 s3client;
	// Get the value from application properties
	@Value("${amazonProperties.endpointUrl}")
	private String endpointUrl;

	@Value("${amazonProperties.bucketName}")
	private String bucketName;

	// @Value("${amazonProperties.accessKey}")
	// private String accessKey;

	// @Value("${amazonProperties.secretKey}")
	// private String secretKey;

	// Initiate the s3client
	@PostConstruct
	private void initializeAmazon() {
		// AWSCredentials credentials = new BasicAWSCredentials(this.accessKey,
		// this.secretKey);
		// ClientConfiguration clientConfig = new ClientConfiguration();
		// clientConfig.setProtocol(Protocol.HTTP);
		// this.s3client = new AmazonS3Client(credentials, clientConfig);
		// System.out.println(this.accessKey);
		// System.out.println(this.secretKey);
		// System.out.println(this.bucketName);

		// this.s3client = AmazonS3ClientBuilder.standard().withCredentials(new
		// AWSStaticCredentialsProvider(credentials)).withRegion(Regions.US_EAST_1).build();
		// DefaultAWSCredentialsProviderChain credential =
		// DefaultAWSCredentialsProviderChain.getInstance();

		this.s3client = AmazonS3ClientBuilder.standard()
				.withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
				.withEndpointConfiguration(new EndpointConfiguration(this.endpointUrl, "us-east-1")).build();
		// s3client.setEndpoint(this.endpointUrl);
		// System.out.println(s3client.doesBucketExistV2(bucketName));
	}

	public String uploadFileTos3bucket(String fileName, File file) {
		// System.out.println(fileName);
		// System.out.println(file.getName());
		s3client.putObject(new PutObjectRequest(bucketName, fileName, file));

		return s3client.getUrl(bucketName, fileName).toString();
	}

	// Turn the multipartfile to normal file
	private File convertMultiPartToFile(String name, String ext, MultipartFile file) {
		String tmpPath = System.getenv("CATALINA_HOME") + "/temp";
		File dir = new File(tmpPath);
		if (!dir.exists())
			dir.mkdirs();
		File convFile = new File(tmpPath + "/" + "tmp_" + name + ext);
		try {

			FileOutputStream fos = new FileOutputStream(convFile);
			fos.write(file.getBytes());
			fos.close();

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return convFile;
	}

	public String uploadFile(String name, String ext, MultipartFile multipartFile) {

		String fileUrl = "";
		try {
			// Convert the multipartFile first
			File file = convertMultiPartToFile(name, ext, multipartFile);
			// Put the file into bucket
			fileUrl = uploadFileTos3bucket(name + ext, file);
			// Delete the multipartFile
			file.delete();
		} catch (Exception e) {
			LOGGER.error("S3 Service Error: " + e.getMessage());
		}
		return fileUrl;
	}

	public void deleteFileFromS3Bucket(String url) {

		// s3client.deleteObject(new DeleteObjectRequest(bucketName + "/", name));
		AmazonS3URI as3uri = new AmazonS3URI(url);
		s3client.deleteObject(as3uri.getBucket(), as3uri.getKey());
	}

	public String updateFile(String url, String id, String extension, MultipartFile file) {

		deleteFileFromS3Bucket(url);
		return uploadFile(id, extension, file);

	}

	public void clear(List<Attachment> attachments) {
		for (Attachment a : attachments) {
			deleteFileFromS3Bucket(a.getUrl());
		}
	}
}
