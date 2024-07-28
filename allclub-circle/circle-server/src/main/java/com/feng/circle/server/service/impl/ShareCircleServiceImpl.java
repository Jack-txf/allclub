package com.feng.circle.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.feng.circle.api.enums.IsDeletedFlagEnum;
import com.feng.circle.api.req.RemoveShareCircleReq;
import com.feng.circle.api.req.SaveShareCircleReq;
import com.feng.circle.api.req.UpdateShareCircleReq;
import com.feng.circle.api.vo.ShareCircleVO;
import com.feng.circle.server.dao.ShareCircleMapper;
import com.feng.circle.server.entity.po.ShareCircle;
import com.feng.circle.server.service.ShareCircleService;
import com.feng.circle.server.util.LoginUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 圈子信息 服务实现类
 * </p>
 *
 * @author txf
 * @since 2024/05/16
 */
@Service
public class ShareCircleServiceImpl extends ServiceImpl<ShareCircleMapper, ShareCircle> implements ShareCircleService {

    private static final Cache<Integer, List<ShareCircleVO>> CACHE = Caffeine.newBuilder().initialCapacity(1)
            .maximumSize(1).expireAfterWrite(Duration.ofSeconds(30)).build();

    @Override
    public List<ShareCircleVO> listResult() {
        List<ShareCircleVO> res = CACHE.getIfPresent(1);
        // Optional
        return Optional.ofNullable(res)
                .orElseGet(() -> {
                    List<ShareCircle> list = super.list(Wrappers.<ShareCircle>lambdaQuery()
                            .eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())); // 查询所有未删除的圈子
                    // 得到所有父级
                    List<ShareCircle> parentList = list.stream().filter(item -> item.getParentId() == -1L).collect(Collectors.toList());
                    Map<Long, List<ShareCircle>> map = list.stream().collect(Collectors.groupingBy(ShareCircle::getParentId)); // 分组
                    /* 处理成这样的格式。
                    [
                        {"热门圈子": ["Java", "C++"]}, {"前端圈子": ["Vue圈子", "React交流"]}
                    ]
                     */
                    List<ShareCircleVO> collect = parentList.stream().map(item -> {
                        ShareCircleVO vo = new ShareCircleVO();
                        vo.setId(item.getId());
                        vo.setCircleName(item.getCircleName());
                        vo.setIcon(item.getIcon());
                        List<ShareCircle> shareCircles = map.get(item.getId());
                        if (CollectionUtils.isEmpty(shareCircles)) {
                            vo.setChildren(Collections.emptyList());
                        } else {
                            List<ShareCircleVO> children = shareCircles.stream().map(cItem -> {
                                ShareCircleVO cVo = new ShareCircleVO();
                                cVo.setId(cItem.getId());
                                cVo.setCircleName(cItem.getCircleName());
                                cVo.setIcon(cItem.getIcon());
                                cVo.setChildren(Collections.emptyList());
                                return cVo;
                            }).collect(Collectors.toList());
                            vo.setChildren(children);
                        }
                        return vo;
                    }).collect(Collectors.toList());
                    CACHE.put(1, collect);
                    return collect;
                });
    }

    @Override
    public Boolean saveCircle(SaveShareCircleReq req) {
        ShareCircle circle = new ShareCircle();
        circle.setCircleName(req.getCircleName());
        circle.setIcon(req.getIcon());
        circle.setParentId(req.getParentId());
        circle.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        circle.setCreatedTime(new Date());
        circle.setCreatedBy(LoginUtil.getLoginId());
        CACHE.invalidateAll();
        return save(circle);
    }

    @Override
    public Boolean updateCircle(UpdateShareCircleReq req) {
        LambdaUpdateWrapper<ShareCircle> update = Wrappers.<ShareCircle>lambdaUpdate()
                .eq(ShareCircle::getId, req.getId())
                .eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
                .set(Objects.nonNull(req.getParentId()), ShareCircle::getParentId, req.getParentId())
                .set(Objects.nonNull(req.getIcon()), ShareCircle::getIcon, req.getIcon())
                .set(Objects.nonNull(req.getCircleName()), ShareCircle::getCircleName, req.getCircleName())
                .set(ShareCircle::getUpdateBy, LoginUtil.getLoginId())
                .set(ShareCircle::getUpdateTime, new Date());
        boolean res = super.update(update);
        CACHE.invalidateAll();// 清除缓存
        return res;
    }

    @Override
    public Boolean removeCircle(RemoveShareCircleReq req) {
        boolean res = super.update(Wrappers.<ShareCircle>lambdaUpdate()
                .eq(ShareCircle::getId, req.getId())
                .eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
                .set(ShareCircle::getIsDeleted, IsDeletedFlagEnum.DELETED.getCode()));
        // 这是什么鬼？
        super.update(Wrappers.<ShareCircle>lambdaUpdate()
                .eq(ShareCircle::getParentId, req.getId())
                .eq(ShareCircle::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode())
                .set(ShareCircle::getIsDeleted, IsDeletedFlagEnum.DELETED.getCode()));
        CACHE.invalidateAll();
        return res;
    }
}
