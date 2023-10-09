package ru.netology;

import org.apache.http.NameValuePair;

import java.net.Socket;
import java.util.List;

public class Request {
    private final Socket socket;
    private String method;
    private String path;
    private List<NameValuePair> queryParams;
    private String headers;
    private String body;

    public Request(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getHeaders() {
        return headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setQueryParams(List<NameValuePair> list) { //NameValuePair
        this.queryParams = list;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        for (NameValuePair nvp : queryParams) {
            if (nvp.getName().equals(name)) {
                return nvp.getValue();
            }
        }
        return null;
    }

    public String toString() {
        return "Request: \n" +
                "headers: \n" +
                headers +
                "\nbody: \n" +
                body;
    }
}

