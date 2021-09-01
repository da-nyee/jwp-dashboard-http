package nextstep.jwp.http.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import nextstep.jwp.controller.Controller;
import nextstep.jwp.exception.http.request.InvalidHttpRequestException;

public class HttpRequest {

    private static final String SPLIT_DELIMITER = " ";

    private final RequestHandlerMapping handlerMapping;
    private final RequestLine line;
    private final RequestHeader header;

    private RequestBody body;

    public HttpRequest(BufferedReader reader) {
        this(
            new RequestHandlerMapping(),
            new RequestLine(readStartLine(reader).split(SPLIT_DELIMITER)),
            new RequestHeader()
        );
        setHeaderAndBody(reader);
    }

    private static String readStartLine(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (Exception e) {
            throw new InvalidHttpRequestException();
        }
    }

    private void setHeaderAndBody(BufferedReader reader) {
        try {
            setHeader(reader);
            this.body = setBody(reader);
        } catch (Exception e) {
            throw new InvalidHttpRequestException();
        }
    }

    private void setHeader(BufferedReader reader) throws IOException {
        while (reader.ready()) {
            String readLine = reader.readLine();

            if ("".equals(readLine)) {
                break;
            }
            header.setHeader(readLine);
        }
    }

    private RequestBody setBody(BufferedReader reader) throws IOException {
        if (header.isContentLength()) {
            int contentLength = Integer.parseInt(header.getValue("Content-Length"));
            char[] buffer = new char[contentLength];
            reader.read(buffer, 0, contentLength);

            return new RequestBody(new String(buffer));
        }
        return new RequestBody("");
    }

    public HttpRequest(
        RequestHandlerMapping handlerMapping,
        RequestLine line,
        RequestHeader header
    ) {
        this.handlerMapping = handlerMapping;
        this.line = line;
        this.header = header;
    }

    public Controller getHandler() {
        return handlerMapping.getHandler(line.getPath());
    }

    public boolean isGet() {
        return getMethod().isGet();
    }

    public boolean isPost() {
        return getMethod().isPost();
    }

    private HttpMethod getMethod() {
        return line.getMethod();
    }

    public String getUri() {
        return line.getUri();
    }

    public String getPath() {
        return line.getPath();
    }

    public Map<String, String> getQuery() {
        return body.getQuery();
    }

    public RequestLine getLine() {
        return line;
    }

    public RequestHeader getHeader() {
        return header;
    }

    public RequestBody getBody() {
        return body;
    }
}
