package com.aluxian.codementor.models;

@SuppressWarnings("unused")
public class Request {

    private String filename;
    private long size;
    private String url;

    public Request() {}

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

        return size == request.size
                && filename.equals(request.filename)
                && url.equals(request.url);

    }

    @Override
    public int hashCode() {
        int result = filename.hashCode();
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + url.hashCode();
        return result;
    }

}
