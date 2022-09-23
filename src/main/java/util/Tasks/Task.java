package util.Tasks;

public interface Task extends Runnable {
    float getProgress();

    public void addSuccessRunnable(Runnable runnable);

    public void addFailureRunnable(Runnable runnable);

    public String getName();

}
