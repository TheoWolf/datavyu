package org.openshapa.views.discrete.datavalues;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import javax.swing.text.JTextComponent;
import org.openshapa.views.discrete.EditorComponent;

/**
 * Leaf item in the Editor Component for fixed text like brackets and commas.
 * Stubs the abstract methods to nothing. FixedTexts are not "editable" so
 * EditorTracker will never set them to be the current editor, so these stubs
 * will never be called.
 */
public class FixedText extends EditorComponent {

    /**
     * Constructor.
     *
     * @param ta The Parent JTextComponent that this FixedText editor is nested
     * within.
     * @param text The inital text to use for this Fixedtext component.
     */
    public FixedText(final JTextComponent ta, final String text) {
        super(ta, text);
    }

    @Override
    public void keyPressed(final KeyEvent e) {
    }

    @Override
    public void keyTyped(final KeyEvent e) {
    }

    @Override
    public void keyReleased(final KeyEvent e) {
    }

    @Override
    public void focusGained(final FocusEvent fe) {
    }

    @Override
    public void focusLost(final FocusEvent fe) {
    }
}