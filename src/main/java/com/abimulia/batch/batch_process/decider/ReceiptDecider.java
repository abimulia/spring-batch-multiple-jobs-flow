package com.abimulia.batch.batch_process.decider;

import java.util.Random;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ReceiptDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        log.debug("## decided()");
        String exitCode = new Random().nextFloat() < .70f ? "CORRECT":"INCORRECT";
        System.out.println("== The item delivered is: "+ exitCode);
        return new FlowExecutionStatus(exitCode);
    }

}
