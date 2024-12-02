package com.abimulia.batch.batch_process.decider;



import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Value;

public class DeliveryDecider implements JobExecutionDecider {
    // Get Environment Variable to simulate customer present or not
    @Value("${IS_PRESENT:false}")
    private String IS_PRESENT;

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        
        String result = IS_PRESENT.equalsIgnoreCase("true") ? "PRESENT" : "NOT_PRESENT";
        System.out.println("== Decider result is: " + result);
        return new FlowExecutionStatus(result);

    }

}
