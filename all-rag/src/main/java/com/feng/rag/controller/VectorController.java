package com.feng.rag.controller;

import com.feng.rag.vector.dto.CreateCollectionRequest;
import com.feng.rag.vector.service.VectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 向量数据库操作 Controller
 *
 * @author txf
 * @since 2026/3/26
 */
@Slf4j
@RestController
@RequestMapping("/v1/vector")
@RequiredArgsConstructor
public class VectorController {
    private final VectorService milvusService;

    // ==================== 集合管理 ====================
    /**
     * 创建集合
     */
    @PostMapping("/collections")
    public R createCollection(
            @Validated @RequestBody CreateCollectionRequest request) {
        log.info("[VectorController] 创建集合: name={}, dimension={}",
                request.getCollectionName(), request.getDimension());
        boolean success = request.getCollectionName() != null
                ? milvusService.createCollection(request.getCollectionName(), request.getDimension())
                : milvusService.createCollection();
        return R.ok().add("success", success);
    }
    /**
     * 删除集合
     */
    @DeleteMapping("/collections/{collectionName}")
    public R dropCollection(
            @PathVariable(value = "collectionName") String collectionName) {
        log.info("[VectorController] 删除集合: {}", collectionName);
        boolean success = milvusService.dropCollection(collectionName);
        return R.ok().add("success", success);
    }
    /**
     * 获取所有集合
     */
    @GetMapping("/collections")
    public R listCollections() {
        log.info("[VectorController] 获取集合列表");
        List<String> collections = milvusService.listCollections();
        return R.ok().add("collections", collections);
    }
    /**
     * 检查集合是否存在
     */
    @GetMapping("/collections/{collectionName}/exists")
    public R hasCollection(
            @PathVariable(value = "collectionName") String collectionName) {
        boolean exists = milvusService.hasCollection(collectionName);
        return R.ok().add("exists", exists);
    }

    /*
     上传文件，分块，然后向量化，存入milvus
     */
    @PostMapping("/upload")
    public R uploadFile(@RequestPart("file") MultipartFile file) {
        log.info("[VectorController] 上传文件: {}", file.getOriginalFilename());
        String org_id = "org_id123456";
        return milvusService.tackleFile(file, org_id);
    }

}
