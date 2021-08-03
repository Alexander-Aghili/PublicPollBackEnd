package com.rest.publicpoll;

import java.io.IOException;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class BucketStorage {
	
	private Region region = Region.US_WEST_1;
    private String bucketName = "public-poll-bucket";
    S3Client s3;

	
	public BucketStorage() {
		//Initializing the s3 public-poll-bucket bucket
		s3 = S3Client.builder().region(region).build();
	}
	
	public String uploadObject(String key, byte[] data) {
		//uploads byte array to s3 with the key (key is the name)
		s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(key).build(), RequestBody.fromBytes(data));
		return "https://public-poll-bucket.s3.us-west-1.amazonaws.com/" + key;
	}
	
	public byte[] downloadObject(String key) throws IOException {
		ResponseInputStream<GetObjectResponse> downloadable = s3.getObject(GetObjectRequest.builder().bucket(bucketName).key(key).build());
		return downloadable.readAllBytes();
	}
	
}
