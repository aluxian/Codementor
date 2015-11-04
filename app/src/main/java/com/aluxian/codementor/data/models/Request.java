package com.aluxian.codementor.data.models;

import com.aluxian.codementor.utils.Constants;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;

import java.io.Serializable;

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
public class Request implements Serializable {

    @JsonField String filename;
    @JsonField String url;
    @JsonField long size;

    @JsonField String type;
    @JsonField String title;
    @JsonField String id;

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

    public String getQuestionPageUrl() {
        return String.format("%s/%s", Constants.CODEMENTOR_QUESTIONS_URL, id);
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
