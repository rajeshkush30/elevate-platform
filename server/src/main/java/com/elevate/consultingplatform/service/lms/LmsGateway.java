package com.elevate.consultingplatform.service.lms;

import com.elevate.consultingplatform.entity.User;
import com.elevate.consultingplatform.entity.catalog.Stage;

public interface LmsGateway {
    void enroll(User user, Stage stage);
    String launchUrl(User user, Stage stage);
}
