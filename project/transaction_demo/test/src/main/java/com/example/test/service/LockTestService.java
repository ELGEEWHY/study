package com.example.test.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.test.domain.LockTest;

public interface LockTestService extends IService<LockTest> {

    Integer insert(int i);

    void updateId(Integer id);
}
