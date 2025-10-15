package com.feng.tim.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feng.tim.auth.api.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description:
 * @Author: txf
 * @Date: 2025/10/9
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
