package com.quashbugs.quash.dto.miscellaneous;


import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


public class InMemoryMultipartDTO implements MultipartFile {
    private final byte[] fileContent;
    private final String fileName;
    private final String contentType;

    public InMemoryMultipartDTO(byte[] fileContent, String fileName, String contentType) {
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return this.fileName;
    }

    @Override
    public String getOriginalFilename() {
        return this.fileName;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return this.fileContent == null || this.fileContent.length == 0;
    }

    @Override
    public long getSize() {
        return this.fileContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return this.fileContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.fileContent);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        // Writes the file to the given destination
        try (InputStream in = new ByteArrayInputStream(this.fileContent)) {
            org.apache.commons.io.FileUtils.copyInputStreamToFile(in, dest);
        }
    }
}