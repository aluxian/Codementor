package com.aluxian.codementor.data.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;

import java.io.Serializable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Request implements Serializable {

    private String filename;
    private String url;
    private long size;

    private String type;
    private String title;
    private String id;

    public Request() {}

    public String getFilename() {
        return filename;
    }

    public String getUrl() {
        return url;
    }

    public long getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;
        Request request = (Request) o;
        return Objects.equal(size, request.size) &&
                Objects.equal(filename, request.filename) &&
                Objects.equal(url, request.url) &&
                Objects.equal(type, request.type) &&
                Objects.equal(id, request.id) &&
                Objects.equal(title, request.title);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(filename, size, url, type, id, title);
    }

}
