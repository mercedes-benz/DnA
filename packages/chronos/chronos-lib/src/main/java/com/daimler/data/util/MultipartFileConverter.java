package com.daimler.data.util;

import java.io.*;

import org.springframework.web.multipart.MultipartFile;

public class MultipartFileConverter implements MultipartFile {
    //previous methods


    private final byte[] imgContent;
    private final String originalFilename;
    private final String name;
    private final String contentType;


    public MultipartFileConverter(byte[] imgContent, String originalFilename, String name, String contentType){
        this.imgContent = imgContent;
        this.originalFilename = originalFilename;
        this.name = name;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        // TODO - implementation depends on your requirements
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        // TODO - implementation depends on your requirements
        return this.originalFilename;
    }

    @Override
    public String getContentType() {
        // TODO - implementation depends on your requirements
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return imgContent == null || imgContent.length == 0;
    }

    @Override
    public long getSize() {
        return imgContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return imgContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(imgContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        new FileOutputStream(dest).write(imgContent);

    }
}