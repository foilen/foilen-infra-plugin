/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.core.system.fake.service;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.TimerEventContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.TimerEventHandler;
import com.foilen.infra.plugin.v1.core.service.TimerService;
import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class TimerServiceImpl extends AbstractBasics implements TimerService {

    @Autowired
    private CommonServicesContext commonServicesContext;
    @Autowired
    private InternalServicesContext internalServicesContext;

    private ExecutorService waitingExecutorService;
    private ExecutorService executingExecutorService;

    @Override
    public void executeLater(TimerEventHandler eventHandler) {
        timerAdd(new TimerEventContext(eventHandler, "executeLater", Calendar.SECOND, 5, true, true));
    }

    @PostConstruct
    public void init() {
        BasicThreadFactory demonThreadFactory = new BasicThreadFactory.Builder() //
                .daemon(true) //
                .build();
        waitingExecutorService = Executors.newCachedThreadPool(demonThreadFactory);
        executingExecutorService = Executors.newSingleThreadExecutor(demonThreadFactory);
    }

    @Override
    public void timerAdd(TimerEventContext timer) {
        waitingExecutorService.submit(new TimerWaitingRunnable(commonServicesContext, internalServicesContext, executingExecutorService, timer));
    }

}
