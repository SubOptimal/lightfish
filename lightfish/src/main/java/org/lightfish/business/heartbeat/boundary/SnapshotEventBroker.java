/*
 Copyright 2012 Adam Bien, adam-bien.com

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.lightfish.business.heartbeat.boundary;

import org.lightfish.business.heartbeat.control.Serializer;
import org.lightfish.business.logging.Log;
import org.lightfish.business.monitoring.boundary.Severity;
import org.lightfish.business.monitoring.entity.Snapshot;
import org.lightfish.presentation.publication.BrowserWindow;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.inject.Instance;

/**
 *
 * @author Adam Bien, blog.adam-bien.com
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SnapshotEventBroker {

    private ConcurrentLinkedQueue<BrowserWindow> browsers = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String, Snapshot> escalations = new ConcurrentHashMap<>();
    @Inject
    Log LOG;
    @Inject
    Serializer serializer;
    @Resource
    TimerService timerService;
    private Timer timer;
    @Inject
    private Instance<Integer> interval;

    @PostConstruct
    public void startTimer() {
        ScheduleExpression expression = new ScheduleExpression();
        expression.minute("*").second("*/" + interval.get()).hour("*");
        this.timer = this.timerService.createCalendarTimer(expression);
    }

    public void onBrowserRequest(@Observes BrowserWindow browserWindow) {
        browsers.add(browserWindow);
    }

    public void onNewSnapshot(@Observes @Severity(Severity.Level.HEARTBEAT) Snapshot snapshot) {
        for (BrowserWindow browserWindow : browsers) {
            if (browserWindow.getChannel() == null) {
                try {
                    send(browserWindow, snapshot);
                } finally {
                    browsers.remove(browserWindow);
                }
            }
        }
    }

    public void onNewEscalation(@Observes @Severity(Severity.Level.ESCALATION) Snapshot escalated) {
        this.escalations.put(escalated.getEscalationChannel(), escalated);
    }

    @Timeout
    public void notifyEscalationListeners() {
        for (BrowserWindow browserWindow : browsers) {
            String channel = browserWindow.getChannel();
            if (channel != null) {
                Snapshot snapshot = this.escalations.get(channel);
                try {
                    if(snapshot != null){
                        send(browserWindow, snapshot);
                    }else{
                        browserWindow.nothingToSay();
                    }
                } finally {
                    browsers.remove(browserWindow);
                }
            }
        }
    }

    void send(BrowserWindow browserWindow, Snapshot snapshot) {
        Writer writer = browserWindow.getWriter();
        serializer.serialize(snapshot, writer);
        browserWindow.send();
    }
}
