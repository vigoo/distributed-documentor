
package hu.distributeddocumentor.gui;

import com.google.common.base.Function;

public interface LongOperationRunner {
    
    public void run(final RunnableWithProgress action);
    public <T> T run(final Function<ProgressUI, T> action);
}
