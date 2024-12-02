package com.abimulia.batch.batch_process.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class SimpleItemWriter implements ItemWriter<String>{

    @Override
    public void write(Chunk<? extends String> items) throws Exception {
        System.out.println(String.format("Received list of size: %s", items.size()));
        for(String item : items){
            System.out.println("Output: " + item); //Output to console
        }
    }

}
