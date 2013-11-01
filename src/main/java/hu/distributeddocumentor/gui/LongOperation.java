package hu.distributeddocumentor.gui;

import com.google.common.base.Function;
import java.awt.Frame;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to run long operations on a worker thread while displaying a
 * progress dialog
 */
public class LongOperation {

    private static final Logger log = LoggerFactory.getLogger(LongOperation.class.getName());
    private final static ExecutorService executor = Executors.newFixedThreadPool(2);
    private static Frame parent;

    public static void setFrame(Frame parent) {
        LongOperation.parent = parent;
    }

    public static LongOperationRunner get() {
        return new LongOperationRunner() {

            @Override
            public void run(final RunnableWithProgress action) {
                try {
                    LongOperation.run(new Function<ProgressUI, Object>() {

                        @Override
                        public Object apply(ProgressUI progress) {
                            action.run(progress);
                            return null;
                        }
                    });
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public <T> T run(Function<ProgressUI, T> action) {
                try {
                    return LongOperation.run(action);
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    private static <T> T run(final Function<ProgressUI, T> action) throws InterruptedException, ExecutionException {

        final ProgressDialog dialog = new ProgressDialog(parent);

        log.debug("Submitting long operation...");
        Future<T> future = executor.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {

                log.debug("Executing long operation...");
                try {
                    return action.apply(dialog);
                } finally {
                    dialog.done();
                    log.debug("Long operation finished.");
                }
            }
        });
        dialog.setVisible(true);

        return future.get();
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
