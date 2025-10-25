package com.elevate.consultingplatform.service.lms.impl;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.catalog.Stage;
import com.elevate.consultingplatform.service.lms.LmsGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LmsGatewayStub implements LmsGateway {
    private static final Logger log = LoggerFactory.getLogger(LmsGatewayStub.class);

    @Override
    public void enroll(User user, Stage stage) {
        // No-op for MVP; just log
        log.info("LMS enroll stub: user={} stage={} courseId={} url={}",
                user.getEmail(), stage.getName(), stage.getLmsCourseId(), stage.getContentUrl());
    }

    @Override
    public String launchUrl(User user, Stage stage) {
        // Return configured content URL if present
        return stage.getContentUrl();
    }
}
