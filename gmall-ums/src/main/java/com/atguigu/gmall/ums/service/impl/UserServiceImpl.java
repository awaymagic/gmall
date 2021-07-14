package com.atguigu.gmall.ums.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.crypto.Digest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import org.springframework.util.CollectionUtils;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        switch (type) {
            case 1: wrapper.eq("username",data);break;
            case 2: wrapper.eq("phone",data);break;
            case 3: wrapper.eq("email",data);break;
            default:
                return null;
        }

        // 0未使用
        return this.count(wrapper) == 0;
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        // 验证码

        // 生成盐
        final String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        userEntity.setSalt(salt);

        // 对密码加盐
        userEntity.setPassword(DigestUtils.md5Hex(userEntity.getPassword() + salt));

        // 新增用户
        userEntity.setLevelId(1l);
        userEntity.setNickname(userEntity.getUsername());
        userEntity.setSourceType(1);
        userEntity.setIntegration(1000);
        userEntity.setGrowth(1000);
        userEntity.setStatus(1);
        userEntity.setCreateTime(new Date());

        this.save(userEntity);

        // 删除redis中短信验证码
    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        // 根据用户名查询数据
        List<UserEntity> userEntities = this.list(new QueryWrapper<UserEntity>().eq("username", loginName)
            .or().eq("phone", loginName)
            .or().eq("email", loginName));

        // 如果为空 返回null
        if (CollectionUtils.isEmpty(userEntities)) {
            return null;
        }

        // 获取盐 对明文加密
        for (UserEntity userEntity : userEntities) {
            // 用户输入密码后 和数据库进行比较
            if (StringUtils.equals(DigestUtils.md5Hex(password + userEntity.getSalt()), userEntity.getPassword())) {
                return userEntity;
            }
        }
        return null;
    }
}