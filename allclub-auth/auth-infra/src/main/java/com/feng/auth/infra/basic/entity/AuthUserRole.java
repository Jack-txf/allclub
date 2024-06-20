package com.feng.auth.infra.basic.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (AuthUserRole)实体类
 *
 * @author txf
 * @since 2023-11-03 00:18:09
 */
@Data
public class AuthUserRole implements Serializable {
    private static final long serialVersionUID = -34579360091831908L;
    
    private Long id;
    
    private Long userId;
    
    private Long roleId;
    /**
     * 创建人
     */
    private String createdBy;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private Date updateTime;
    
    private Integer isDeleted;
}

