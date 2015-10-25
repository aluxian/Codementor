package com.aluxian.codementor.data.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Request {

    private String filename;
    private long size;
    private String url;

    private String type;
    private String id;
    private String title;

    @SuppressWarnings("unused")
    public Request() {}

    @SuppressWarnings("unused")
    public Request(String filename, long size, String url) {
        this.filename = filename;
        this.size = size;
        this.url = url;
    }

    @SuppressWarnings("unused")
    public Request(String type, String id, String title) {
        this.type = type;
        this.id = id;
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;

        Request request = (Request) o;
        return size == request.size && !(filename != null ? !filename.equals(request.filename) : request.filename !=
                null) && !(url != null ? !url.equals(request.url) : request.url != null);

    }

    @Override
    public int hashCode() {
        int result = filename != null ? filename.hashCode() : 0;
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

}
