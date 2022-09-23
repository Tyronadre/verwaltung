package gui;

import util.Tasks.Task;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

abstract public class ContentPanel extends JPanel {
    private boolean blur = true;
    public Gui gui;

    public ContentPanel(Gui gui) {
        this.gui = gui;
    }

    private final List<Task> backgroundTasks = new ArrayList<>();


    public void setBlur(boolean enabled) {
        System.out.println("WIP");

        //TODO WIP
//        blur = enabled;
//        for (Component component : getComponents()) {
//            component.setEnabled(enabled);
//            component.setFocusable(enabled);
//        }
    }

    /**
     * @return All background tasks for this contentPanel.
     */
    public abstract List<Task> getBackgroundTasks();

//    @Override
//    public void paint(Graphics g) {
//        if (false) {
//
//            BufferedImage mOffscreenImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
//
//            Graphics2D ig2 = mOffscreenImage.createGraphics();
//            ig2.setClip(g.getClip());
//            super.paint(ig2);
//            ig2.dispose();
//            Graphics2D g2 = (Graphics2D) g;
//            float ninth = 1.0f / 9.0f;
//            g2.drawImage(mOffscreenImage, new ConvolveOp(new Kernel(3, 3, new float[]{ninth, ninth, ninth, ninth, ninth, ninth,
//                    ninth, ninth, ninth}),
//                    ConvolveOp.EDGE_NO_OP, null), 0, 0);
//        } else {
//            super.paint(g);
//        }
//    }

    /**
     * Registers one new Task to executed in the background.
     * @param backgroundTask the Task
     */
    void registerNewBackgroundTask(Task backgroundTask) {//TODO Show and hide
        backgroundTask.addSuccessRunnable(() -> backgroundTasks.remove(backgroundTask));
        backgroundTasks.add(backgroundTask);
        updateControlBar();
        backgroundTask.run();
    }

    /**
     * Updates the progress in the control bar of this contentPanel.
     */
    protected abstract void updateControlBar();

    /**
     * Builds the Toolbar (at the top) for this contentPanel.
     * @return The JToolBar
     */
    public abstract JToolBar getToolbar();

    /**
     * Builds the JMenu (at the very top) for this contentPanel.
     * @return The JMenu
     */
    public abstract JMenu getMenu();

    /**
     * Builds the ControlPanel (the bar the bottom) for this contentPanel.
     * @return The JPanel
     */
    abstract public JPanel getControlPanel();

    /**
     * NAME OF THIS TAB
     * @return
     */
    abstract public String getTabName();

    /**
     * WANTS USER OR NOT
     * @return
     */
    abstract public boolean wantUser();

    /**
     * ACTION LISTENER FOR USER BUTTON
     * @return
     */
    abstract public ActionListener getUserActionListener();

}
