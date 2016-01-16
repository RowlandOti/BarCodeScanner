package it.jaschke.alexandria.camera;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ali Muzaffar on 2/01/2016.
 * <p/>
 * Modified by Oti Rowland on 1/8/2016.
 */
public class ScanThreadPool {

    private static final int KEEP_ALIVE = 10;
    private static ScanThreadPool mInstance;
    private static int MAX_POOL_SIZE;
    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    private ThreadPoolExecutor mThreadPoolExec;

    private ScanThreadPool() {
        int coreNum = Runtime.getRuntime().availableProcessors();
        MAX_POOL_SIZE = coreNum * 2;
        mThreadPoolExec = new ThreadPoolExecutor(
                coreNum,
                MAX_POOL_SIZE,
                KEEP_ALIVE,
                TimeUnit.SECONDS,
                workQueue);
    }

    public static synchronized void post(Runnable runnable) {
        if (mInstance == null) {
            mInstance = new ScanThreadPool();
        }
        mInstance.mThreadPoolExec.execute(runnable);
    }

    public static void finish() {
        mInstance.mThreadPoolExec.shutdown();
    }
}
