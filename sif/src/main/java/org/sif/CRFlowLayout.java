package org.sif;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

public class CRFlowLayout extends FlowLayout {

    @Override
    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int maxwidth = target.getWidth()
                    - (insets.left + insets.right + getHgap() * 2);
            int nmembers = target.getComponentCount();
            int x = 0, y = insets.top + getVgap();
            int rowh = 0, start = 0;

            boolean ltr = target.getComponentOrientation().isLeftToRight();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {

                    if (m instanceof Container) {
                        if (((Container) m).getLayout() instanceof CRFlowLayout) {
                            layoutContainer((Container) m);
                        }
                    }

                    Dimension d = m.getPreferredSize();
                    m.setSize(d.width, d.height);

                    if (((x == 0) || ((x + d.width) <= maxwidth))
                            && !(m instanceof CarriageReturn)) {
                        if (x > 0) {
                            x += getHgap();
                        }
                        x += d.width;
                        rowh = Math.max(rowh, d.height);
                    } else {
                        moveComponents(target, insets.left + getHgap(), y,
                                maxwidth - x, rowh, start, i, ltr);
                        x = d.width;
                        y += getVgap() + rowh;
                        rowh = d.height;
                        start = i;
                    }
                }
            }
            moveComponents(target, insets.left + getHgap(), y, maxwidth - x,
                    rowh, start, nmembers, ltr);
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            Dimension dim = new Dimension(0, 0);
            int nmembers = target.getComponentCount();
            boolean firstVisibleComponent = true;
            boolean newLine = true;
            boolean firstLine = true;
            int x = 0;
            int y = 0;
            
            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    if (!(m instanceof CarriageReturn)) {
                        if (firstVisibleComponent) {
                            firstVisibleComponent = false;
                        } else {
                            x += getHgap();
                            y = Math.max(y, d.height);
                        }
                        x += d.width;
                    }
                    if (newLine) {
                        if (!firstLine){
                            y += getVgap();
                        }
                        firstLine = false;
                        y += d.height;
                        newLine = false;
                    }
                    
                    if (m instanceof CarriageReturn){
                        dim.height += y;
                        y = 0;
                        dim.width = Math.max(x, dim.width);
                        x = 0;
                        newLine = true;
                        firstVisibleComponent = true;
                    }
                }
            }
            dim.height += y;
            dim.width = Math.max(x, dim.width);
            
            
            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right + getHgap() * 2;
            dim.height += insets.top + insets.bottom + getVgap() * 2;
            return dim;
        }
    }

    /**
     * Centers the elements in the specified row, if there is any slack.
     * 
     * @param target
     *            the component which needs to be moved
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @param width
     *            the width dimensions
     * @param height
     *            the height dimensions
     * @param rowStart
     *            the beginning of the row
     * @param rowEnd
     *            the the ending of the row
     */
    private void moveComponents(Container target, int x, int y, int width,
            int height, int rowStart, int rowEnd, boolean ltr) {
        synchronized (target.getTreeLock()) {
            switch (getAlignment()) {
            case LEFT:
                x += ltr ? 0 : width;
                break;
            case CENTER:
                x += width / 2;
                break;
            case RIGHT:
                x += ltr ? width : 0;
                break;
            case LEADING:
                break;
            case TRAILING:
                x += width;
                break;
            }
            for (int i = rowStart; i < rowEnd; i++) {
                Component m = target.getComponent(i);
                if (m instanceof CarriageReturn)
                    continue;
                if (m.isVisible()) {
                    if (ltr) {
                        m.setLocation(x, y + (height - m.getHeight()) / 2);
                    } else {
                        m.setLocation(target.getWidth() - x - m.getWidth(), y
                                + (height - m.getHeight()) / 2);
                    }
                    x += m.getWidth() + getHgap();
                }
            }
        }
    }

}
