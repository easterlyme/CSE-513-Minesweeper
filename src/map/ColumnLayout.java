package map;

import java.awt.*;

/**
 * The ColumnLayout class lays out items vertically and centered
 * so that the left hand side of each component lines up.
 *
 * @version March 1997
 * @author John D. Ramsdell
 */
public class ColumnLayout implements LayoutManager, java.io.Serializable {

  int hgap;			// Horizontal gap
  int vgap;			// Vertical gap

  /**
   * Constructs a new ColumnLayout.
   * @param hgap         horizontal gap
   * @param vgap         vertical gap
   */
  public ColumnLayout(int hgap, int vgap) {
    this.hgap = hgap;
    this.vgap = vgap;
  }

  /**
   * Constructs a new ColumnLayout
   * with default values for the
   * horizontal and vertical gap.
   */
  public ColumnLayout() {
    this(5, 5);
  }

  /**
   * Adds the specified component to the layout. Not used by this class.
   * @param name the name of the component
   * @param comp the the component to be added
   */
  public void addLayoutComponent(String name, Component comp) {
  }

  /**
   * Removes the specified component from the layout. Not used by
   * this class.
   * @param comp the component to remove
   */
  public void removeLayoutComponent(Component comp) {
  }

  /**
   * Returns the preferred dimensions for this layout given the components
   * in the specified target container.
   * @param target the component which needs to be laid out
   * @see Container
   * @see #minimumLayoutSize
   */

  public Dimension preferredLayoutSize(Container target) {
    Dimension dim = new Dimension(0, 0);
    int nmembers = target.getComponentCount();

    for (int i = 0 ; i < nmembers ; i++) {
      Component m = target.getComponent(i);
      if (m.isVisible()) {
	Dimension d = m.getPreferredSize();
	dim.width = Math.max(dim.width, d.width);
	if (i > 0) {
	  dim.height += vgap;
	}
	dim.height += d.height;
      }
    }
    return addEdges(target, dim);
  }

  private Dimension addEdges(Container target, Dimension dim) {
    Insets insets = target.getInsets();
    dim.width += insets.left + insets.right + hgap*2;
    dim.height += insets.top + insets.bottom + vgap*2;
    return dim;
  }

  /**
   * Returns the minimum dimensions needed to layout the components
   * contained in the specified target container.
   * @param target the component which needs to be laid out
   * @see #preferredLayoutSize
   */
  public Dimension minimumLayoutSize(Container target) {
    Dimension dim = new Dimension(0, 0);
    int nmembers = target.getComponentCount();

    for (int i = 0 ; i < nmembers ; i++) {
      Component m = target.getComponent(i);
      if (m.isVisible()) {
	Dimension d = m.getMinimumSize();
	dim.width = Math.max(dim.width, d.width);
	if (i > 0) {
	  dim.height += vgap;
	}
	dim.height += d.height;
      }
    }
    return addEdges(target, dim);
  }

  /**
   * Lays out the container.
   * @param target the specified component being laid out.
   * @see Container
   */
  public void layoutContainer(Container target) {
    Insets insets = target.getInsets();
    int maxwidth = target.getSize().width
      - (insets.left + insets.right + hgap*2);
    int x = insets.left + hgap;
    int y = insets.top;
    int nmembers = target.getComponentCount();

    int preferred_width = 0;
    for (int i = 0; i < nmembers; i++) {
      Component m = target.getComponent(i);
      if (m.isVisible()) {
	Dimension d = m.getPreferredSize();
	preferred_width = Math.max(preferred_width, d.width);
      }
    }

    if (preferred_width < maxwidth)
      x += (maxwidth - preferred_width) / 2;

    for (int i = 0; i < nmembers; i++) {
      Component m = target.getComponent(i);
      y += vgap;
      if (m.isVisible()) {
	Dimension d = m.getPreferredSize();
	m.setSize(d.width, d.height);
	m.setLocation(x, y);
	y += d.height;
      }
    }
  }

  /**
   * Returns the String representation of this ColumnLayout's values.
   */
  public String toString() {
    return getClass().getName() + "[ hgap=" + hgap + ",vgap=" + vgap + "]";
  }
}
