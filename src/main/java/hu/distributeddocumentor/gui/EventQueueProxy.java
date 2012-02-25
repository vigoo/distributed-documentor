package hu.distributeddocumentor.gui;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import javax.swing.JOptionPane;

public class EventQueueProxy extends EventQueue {
     
    @Override
    protected void dispatchEvent(AWTEvent newEvent) {
        try {
            super.dispatchEvent(newEvent);
        } catch (Throwable t) {
            t.printStackTrace();

            String message = t.getMessage();

            if (message == null || message.length() == 0) {
                message = "Fatal: " + t.getClass();
            }

            JOptionPane.showMessageDialog(null, message, "General Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

