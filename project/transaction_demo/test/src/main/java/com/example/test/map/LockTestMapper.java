package com.example.test.map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.test.domain.LockTest;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

@Mapper
public interface LockTestMapper extends BaseMapper<LockTest> {
}
