package com.spring.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadFile(MultipartFile file) {
        // Validate file type
        String fileName = file.getOriginalFilename();
        if (!isValidFileType(fileName)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: jpeg, jpg, png, ppt");
        }

        File fileObj = convertMultiPartFileToFile(file);
        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
        s3Client.putObject(new PutObjectRequest(bucketName, uniqueFileName, fileObj));
        fileObj.delete();
        return "File uploaded: " + uniqueFileName;
    }

    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
        return fileName + " removed ...";
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertedFile;
    }

    private boolean isValidFileType(String fileName) {
        String[] allowedExtensions = {"jpeg", "jpg", "png", "ppt"};
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        for (String extension : allowedExtensions) {
            if (fileExtension.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}
