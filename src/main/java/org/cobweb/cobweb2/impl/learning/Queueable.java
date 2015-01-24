package org.cobweb.cobweb2.impl.learning;

public interface Queueable extends Describeable {

	public void happen();

	public boolean isComplete();
}