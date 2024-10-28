package com.multicloud.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Service
public class IpGeolocationService {

    @Value("${ipinfo.api.token}")
    private String ipinfoApiToken;

    private final Logger logger = LoggerFactory.getLogger(IpGeolocationService.class);
    public String[] getGeolocation(String ipAddress) {
        String url = "https://ipinfo.io/" + ipAddress + "/json?token=" + ipinfoApiToken;
        RestTemplate restTemplate = new RestTemplate();
        final String UNKNOWN="Unknown";
        try {
            // Make the API call to IPinfo and get the result as a String
            String response = restTemplate.getForObject(url, String.class);

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response);
            String city = jsonResponse.optString("city", UNKNOWN);
            String region = jsonResponse.optString("region", UNKNOWN);
            String country = jsonResponse.optString("country", UNKNOWN);
            String loc = jsonResponse.optString("loc", UNKNOWN);  // Latitude,Longitude
            logger.info("City: {}, Region: {}, Country: {}, Location: {}", city, region, country, loc);
            return new String[]{city, region, country, loc};
        } catch (Exception e) {
            logger.error("Error Occurred {}", e.getMessage());
            return new String[]{UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN};
        }
    }


}

