package com.multicloud.vms.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeRegionsResponse;
import software.amazon.awssdk.services.ec2.model.Region;

import java.util.List;

@RestController
@RequestMapping("/vm/aws")
public class AwsRegionsController {

    private static final Logger logger = LoggerFactory.getLogger(AwsRegionsController.class);

    @GetMapping("/regions")
    public List<String> listAwsRegions() {
        try (Ec2Client ec2Client = Ec2Client.create()) {
            DescribeRegionsResponse response = ec2Client.describeRegions();
            return response.regions().stream()
                    .map(Region::regionName)
                    .toList();
        } catch (Exception e) {
            logger.error("Error retrieving AWS regions", e);
            return List.of("Error retrieving regions");
        }
    }
}