package com.example.test.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.test.service.LockTestService;
import com.example.test.domain.LockTest;
import com.example.test.map.LockTestMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LockTestServiceImpl extends ServiceImpl<LockTestMapper, LockTest> implements LockTestService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insert(int i) {
        LockTest lockTest = new LockTest();
        lockTest.setLock(i);
        this.baseMapper.insert(lockTest);
        return lockTest.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateId(Integer id) {
        LockTest lockTest = new LockTest();
        lockTest.setId(id);
        lockTest.setLock(2);
        this.baseMapper.updateById(lockTest);
    }
}
