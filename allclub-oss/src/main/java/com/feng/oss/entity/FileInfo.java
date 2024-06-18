package com.feng.oss.entity;

import lombok.Data;

/**
 * @author Williams_Tian
 * @CreateDate 2024/6/18
 */
@Data
public class FileInfo {
    private String fileName;

    private Boolean directoryFlag;

    private String etag;
}
