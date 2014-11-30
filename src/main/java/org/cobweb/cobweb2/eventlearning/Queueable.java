package org.cobweb.cobweb2.eventlearning;

public interface Queueable extends Describeable {

	public void happen();

	public boolean isComplete();
}