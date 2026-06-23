package com.vtmer.microteachingquality.common.component;

import com.hung.microoauth2commons.commonutils.api.Result;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.model.dto.file.FileServiceFileObjectDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

@Component
public class FileObjectReferenceService {

    private static final String FILE_OBJECT_PREFIX = "file-object:";

    @Value("${app.file-service.base-url:http://127.0.0.1:9701}")
    private String fileServiceBaseUrl;

    @Resource
    private RestTemplate restTemplate;

    public boolean isFileObjectReference(String path) {
        return path != null && path.startsWith(FILE_OBJECT_PREFIX);
    }

    public String buildReference(Long fileObjectId) {
        return FILE_OBJECT_PREFIX + fileObjectId;
    }

    public Long parseFileObjectId(String path) {
        if (!isFileObjectReference(path)) {
            throw new CustomException("文件引用格式非法");
        }
        try {
            return Long.valueOf(path.substring(FILE_OBJECT_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new CustomException("文件引用格式非法");
        }
    }

    public FileServiceFileObjectDTO getFileObjectByReference(String path) {
        return getFileObject(parseFileObjectId(path));
    }

    public FileServiceFileObjectDTO getFileObject(Long fileObjectId) {
        String url = fileServiceBaseUrl + "/file/objects/" + fileObjectId;
        ResponseEntity<Result<FileServiceFileObjectDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Result<FileServiceFileObjectDTO>>() {
                }
        );
        Result<FileServiceFileObjectDTO> result = response.getBody();
        if (result == null || result.getCode() != 200 || result.getData() == null) {
            throw new CustomException(result == null ? "文件服务响应为空" : result.getMessage());
        }
        return result.getData();
    }

    public void deleteReferencedFile(String path, Integer operatorUserId) {
        Long fileObjectId = parseFileObjectId(path);
        URI uri = UriComponentsBuilder.fromHttpUrl(fileServiceBaseUrl)
                .path("/file/objects/{fileObjectId}")
                .queryParam("operatorUserId", operatorUserId)
                .buildAndExpand(fileObjectId)
                .toUri();
        ResponseEntity<Result<String>> response = restTemplate.exchange(
                uri,
                HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<Result<String>>() {
                }
        );
        Result<String> result = response.getBody();
        if (result == null || result.getCode() != 200) {
            throw new CustomException(result == null ? "文件服务删除失败" : result.getMessage());
        }
    }

    public void writeReferencedFileToResponse(String path, HttpServletResponse response) throws IOException {
        Long fileObjectId = parseFileObjectId(path);
        String url = fileServiceBaseUrl + "/file/objects/" + fileObjectId + "/download";
        ResponseEntity<byte[]> remoteResponse = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);
        byte[] body = remoteResponse.getBody();
        if (body == null) {
            throw new CustomException("文件下载失败");
        }

        HttpHeaders headers = remoteResponse.getHeaders();
        List<String> contentDispositions = headers.get(HttpHeaders.CONTENT_DISPOSITION);
        if (contentDispositions != null && !contentDispositions.isEmpty()) {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDispositions.get(0));
        }
        if (headers.getContentType() != null) {
            response.setContentType(headers.getContentType().toString());
        } else {
            response.setContentType("application/octet-stream");
        }
        if (headers.getContentLength() >= 0) {
            response.setContentLengthLong(headers.getContentLength());
        }

        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(body);
            outputStream.flush();
        }
    }
}
