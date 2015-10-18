package com.aluxian.codementor.data.models;

import com.aluxian.codementor.data.annotations.GsonModel;

@GsonModel
public class Request {

    private String filename;
    private long size;
    private String url;

    public Request(String filename, long size, String url) {
        this.filename = filename;
        this.size = size;
        this.url = url;
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
