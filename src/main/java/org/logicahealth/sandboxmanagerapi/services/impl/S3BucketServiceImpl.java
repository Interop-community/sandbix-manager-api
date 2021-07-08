package org.logicahealth.sandboxmanagerapi.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.time.DateUtils;
import org.logicahealth.sandboxmanagerapi.services.S3BucketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Service
public class S3BucketServiceImpl implements S3BucketService {

    private final String s3BucketName;
    private final AmazonS3 amazonS3;
    private String bucketLocation;

    private static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class.getName());

    public S3BucketServiceImpl(@Value("${aws.s3BucketName}") String s3BucketName, AmazonS3 amazonS3) {
        this.s3BucketName = s3BucketName;
        this.amazonS3 = amazonS3;
    }

    @Override
    public void putFile(String fileName, InputStream inputStream) {
        var metaData = new ObjectMetadata();
        byte[] md5Digest;
        try {
            md5Digest = DigestUtils.md5Digest(inputStream);
        } catch (IOException e) {
            LOGGER.error("Exception while trying to create MD5 digest of input stream", e);
            throw new RuntimeException(e);
        }
        var streamMD5 = new String(Base64.encodeBase64(md5Digest));
        metaData.setContentMD5(streamMD5);
        metaData.setContentType("application/zip");
        var request = new PutObjectRequest(this.s3BucketName, fileName, inputStream, metaData);
        this.amazonS3.putObject(request);
    }

    @Override
    public void notifyUser(String user) {

    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanUpOldFiles() {
        LOGGER.info("Removing sandbox exports older than 1 day.");
        var yesterday = DateUtils.addDays(new Date(), -1);
        var files = this.amazonS3.listObjects(this.s3BucketName).getObjectSummaries();
        files.stream()
             .filter(file -> file.getLastModified().before(yesterday))
             .forEach(file -> this.amazonS3.deleteObject(this.s3BucketName, file.getKey()));
    }
}
