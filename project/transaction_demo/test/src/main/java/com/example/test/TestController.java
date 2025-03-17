package com.example.test;

import com.example.test.service.LockTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("test")
@Slf4j
public class TestController {

    @Resource
    private LockTestService lockTestService;

    @PostMapping("lock")
    public void authPage(){
        Integer id = lockTestService.insert(1);
        lockTestService.updateId(id);
        return ;
    }
}
