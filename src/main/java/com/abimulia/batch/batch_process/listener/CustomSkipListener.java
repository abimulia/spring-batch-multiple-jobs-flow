package com.abimulia.batch.batch_process.listener;

import org.springframework.batch.core.SkipListener;

import com.abimulia.batch.batch_process.record.Order;
import com.abimulia.batch.batch_process.record.TrackedOrder;

public class CustomSkipListener implements SkipListener<Order, TrackedOrder> {

	@Override
	public void onSkipInRead(Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSkipInWrite(TrackedOrder item, Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSkipInProcess(Order item, Throwable t) {
		System.out.println("## Skipping processing of item with id: " + item.orderId());

	}

}
