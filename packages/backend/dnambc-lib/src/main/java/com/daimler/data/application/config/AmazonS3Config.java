/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.application.config;

import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

@Configuration
public class AmazonS3Config {

    @Value("${storage.bucketname}")
    private String bucketname;
    
    @Value("${storage.accesskey}")
    private String accessKey;
    
    @Value("${storage.secretkey}")
    private String secretKey;
    
    @Value("${storage.endpointurl}")
    private String endpointUrl;
    
    @Value("${storage.maxParallelUploads}")
    private int maxUploadThreads;
    
    @Value("${storage.minFileSize}")
    private int minFileSize;
    
    @Value("${storage.maxFileSize}")
    private int maxFileSize;
    
    @Bean
    public AmazonS3 awsS3Client() {
    	System.setProperty(SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY,"true");
    	
        AWSCredentials credentials = new BasicAWSCredentials(accessKey,secretKey);
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        AmazonS3 client = AmazonS3ClientBuilder.standard().withPathStyleAccessEnabled(true).withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointUrl, null)).withCredentials(credentialsProvider).build();
        return client;	
    }
    
    @Bean
    public TransferManager transferManager(){
      TransferManager tm = TransferManagerBuilder.standard()
                    .withS3Client(awsS3Client())
                    .withDisableParallelDownloads(false)
                    .withMinimumUploadPartSize(Long.valueOf(minFileSize))
                    .withMultipartUploadThreshold(Long.valueOf(maxFileSize))
                    .withMultipartCopyPartSize(Long.valueOf(minFileSize))
                    .withMultipartCopyThreshold(Long.valueOf(maxFileSize))
                    .withExecutorFactory(()-> Executors.newFixedThreadPool(maxUploadThreads))
                    .build();
      return tm;
    }
	
}
