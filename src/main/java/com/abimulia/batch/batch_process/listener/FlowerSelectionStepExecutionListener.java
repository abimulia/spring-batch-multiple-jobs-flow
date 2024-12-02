package com.abimulia.batch.batch_process.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FlowerSelectionStepExecutionListener implements  StepExecutionListener{
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.debug("## beforeStep()");
        System.out.println("== Before Flower Selection step logic");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.debug("## afterStep()");
        System.out.println("== After Flower Selection step logic");
        String flowerType = stepExecution.getJobParameters().getString("type");
        return flowerType.equalsIgnoreCase("roses") ? new ExitStatus("TRIM_REQUIRED"): new ExitStatus("NO_TRIM_REQUIRED");
    }
}
