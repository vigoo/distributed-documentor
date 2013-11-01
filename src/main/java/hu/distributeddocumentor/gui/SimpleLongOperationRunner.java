package hu.distributeddocumentor.gui;

import com.google.common.base.Function;

public class SimpleLongOperationRunner implements LongOperationRunner {

    ProgressUI dummyProgress = new ProgressUI() {

        @Override
        public void setStatus(String status) {
        }

        @Override
        public void setProgress(double percentage) {
        }

        @Override
        public void setIndeterminate() {
        }
    };

    @Override
    public void run(RunnableWithProgress action) {
        action.run(dummyProgress);
    }

    @Override
    public <T> T run(Function<ProgressUI, T> action) {
        return action.apply(dummyProgress);
    }
}