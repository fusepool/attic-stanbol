package org.apache.stanbol.reasoners.jobs.api;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
/**
 * This interface defines the executor of asynch processes.
 * 
 * @author enridaga
 *
 */
public interface JobManager {
    /**
     * Adds and runs an asynch process. Returns the String id to use for pinging its execution.
     * 
     * @param task
     * @return
     */
    public String execute(Callable<?> task);

    /**
     * Get the Future object to monitor the state of a job
     */
    public Future<?> ping(String id);

    /**
     * If the executor is managing the given job
     * 
     * @param id
     * @return
     */
    public boolean hasJob(String id);

    /**
     * The currently managed jobs, in any state (running, complete, interrupted)
     * 
     * @return
     */
    public int size();

    /**
     * Interrupt the asynch process and remove it from the job list.
     * To interrupt the process and keeping it, use the Future object from the ping() method.
     * 
     * @param id
     */
    public void remove(String id);
}