package com.aluxian.codementor.models;

public class Request {

    private String filename;
    private String key;
    private String mimetype;
    private long size;
    private String url;

    public Request() {}

    public String getFilename() {
        return filename;
    }

    public String getKey() {
        return key;
    }

    public String getMimetype() {
        return mimetype;
    }

    public long getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }

}
