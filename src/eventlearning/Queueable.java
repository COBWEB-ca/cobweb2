package eventlearning;

public interface Queueable extends Describeable {

	public void happen();

	public boolean isComplete();
}