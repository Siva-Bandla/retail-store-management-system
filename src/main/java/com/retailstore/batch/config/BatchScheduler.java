package com.retailstore.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.util.Map;

@Configuration
@EnableScheduling
@Slf4j
public class BatchScheduler {

    @Autowired private JobLauncher jobLauncher;

    @Autowired private Map<String, Job> jobs;

    private final Map<String, Map<String, String>> jobParameterOverrides = Map.of(
            "productUploadJob", Map.of("filePath", "uploads/products-upload.csv"),
            "stockReconciliationJob", Map.of("filePath", "uploads/stocks-reconciliation.csv")
            // Add more jobs with custom parameters here if needed
    );

    @Scheduled(cron = "0 */5 *  * * *")
//    @Scheduled(cron = "0 0 2  * * *")
    public void runScheduledJobs() {

        jobs.forEach((jobName, job) -> {
            try {
                if (jobParameterOverrides.containsKey(jobName)) {
                    Map<String, String> params = jobParameterOverrides.get(jobName);
                    if (params.containsKey("filePath")) {
                        File file = new File(params.get("filePath"));
                        if (!file.exists()) {
                            log.warn("Skipping {} - file not found: {}", jobName, file.getAbsolutePath());
                            return;
                        }
                    }
                }
                JobParametersBuilder builder = new JobParametersBuilder()
                        .addLong("runId_" + jobName, System.currentTimeMillis());

                // Add extra parameters for specific jobs
                if (jobParameterOverrides.containsKey(jobName)) {
                    jobParameterOverrides.get(jobName).forEach(builder::addString);
                }

                JobParameters params = builder.toJobParameters();

                jobLauncher.run(job, params);
                log.info("{} executed successfully!", jobName);

            } catch (Exception exception) {
                log.error("Error executing {}: {}", jobName, exception.getMessage(), exception);
                exception.printStackTrace();
            }
        });
    }
}
