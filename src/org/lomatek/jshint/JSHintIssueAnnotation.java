/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lomatek.jshint;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
//import java.util.regex.Matcher;
import org.openide.cookies.LineCookie;
//import org.openide.text.Line;
import org.openide.loaders.DataObject;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;

/**
 *
 * @author LORD
 */
public class JSHintIssueAnnotation extends Annotation {
    //private static List<Annotation> annotations = new ArrayList<Annotation>();
    private static Map<DataObject, List<Annotation>> annotations = new HashMap<DataObject, List<Annotation>>();

    private String reason;
    private int character;

    public static JSHintIssueAnnotation create(DataObject dObj, int character, String reason) {
        JSHintIssueAnnotation annotation = new JSHintIssueAnnotation(character, reason);
        //annotations.add(annotation);
        getAnnotationList(dObj).add(annotation);
        return annotation;
    }

    private JSHintIssueAnnotation(int character, String reason) {
        this.reason = reason;
        this.character = character;
    }

    public static List<Annotation> getAnnotationList(DataObject dObj) {
        if (null == annotations.get(dObj)) {
            annotations.put(dObj, new ArrayList<Annotation>());
        }
        return annotations.get(dObj);
    }

    public static void clear(DataObject dObj) {
        for (Annotation annotation : getAnnotationList(dObj)) {
            annotation.detach();
        }
        getAnnotationList(dObj).clear();
    }

    public static void remove(DataObject dObj, JSHintIssueAnnotation annotation) {
        //annotations.remove(annotation);
        getAnnotationList(dObj).remove(annotation);
    }

    /**
     * Define the Tidy Annotation type
     *
     * @return Constant String "TidyErrorAnnotation"
     */
    @Override
    public String getAnnotationType() {
        return "org-lomatek-jshint-jshinterrorannotation";
    }

    /** Provide the Tidy error message as a description.
     * @return Annotation Reason*/
    @Override
    public String getShortDescription() {
        return reason + " (" + "Column: " + character + ")";
    }

    /** Create an annotation for a line from match string*/
    public static void createAnnotation(
        final DataObject dObj, final LineCookie cLine, final String reason, final int line, final int character, final int length
    ) throws IndexOutOfBoundsException, NumberFormatException {
        try {
            Line currentLine = cLine.getLineSet().getCurrent(line-1);
            final Line.Part currentPartLine = currentLine.createPart(character - 1, length);
            final JSHintIssueAnnotation annotation = JSHintIssueAnnotation.create(dObj, character, reason);

            annotation.attach(currentPartLine);

            currentPartLine.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent ev) {
                    String type = ev.getPropertyName();
                    if (type == null || type.equals(Annotatable.PROP_TEXT)) {
                        // User edited the line, assume error should be cleared.
                        currentPartLine.removePropertyChangeListener(this);
                        annotation.detach();
                        //annotation.
                        JSHintIssueAnnotation.remove(dObj, annotation);
                    }
                }
            });
        } catch (IndexOutOfBoundsException e) {
            // might happen, if state of file is not saved. ignore
        }
    }
}
