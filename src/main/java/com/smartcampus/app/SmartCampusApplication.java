package com.smartcampus.app;

import javax.ws.rs.ApplicationPath;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.smartcampus.app.api.DiscoveryResource;
import com.smartcampus.app.api.SensorResource;
import com.smartcampus.app.api.SensorRoomResource;
import com.smartcampus.app.error.GlobalExceptionMapper;
import com.smartcampus.app.error.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.app.error.NotFoundExceptionMapper;
import com.smartcampus.app.error.RoomNotEmptyExceptionMapper;
import com.smartcampus.app.error.SensorUnavailableExceptionMapper;
import com.smartcampus.app.filter.RequestResponseLoggingFilter;
import com.smartcampus.app.store.CampusData;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        final CampusData data = new CampusData();

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(data).to(CampusData.class);
            }
        });

        register(JacksonFeature.class);
        register(DiscoveryResource.class);
        register(SensorRoomResource.class);
        register(SensorResource.class);
        register(RoomNotEmptyExceptionMapper.class);
        register(LinkedResourceNotFoundExceptionMapper.class);
        register(NotFoundExceptionMapper.class);
        register(SensorUnavailableExceptionMapper.class);
        register(GlobalExceptionMapper.class);
        register(RequestResponseLoggingFilter.class);
    }
}
