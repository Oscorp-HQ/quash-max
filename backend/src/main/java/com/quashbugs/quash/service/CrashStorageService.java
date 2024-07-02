package com.quashbugs.quash.service;

import com.quashbugs.quash.model.CrashLog;
import com.quashbugs.quash.repo.CrashLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CrashStorageService {

    @Autowired
    private CrashLogRepository crashLogRepository;

    public CrashLog saveCrashlog(CrashLog crashLog) {
        return crashLogRepository.save(crashLog);
    }
}
